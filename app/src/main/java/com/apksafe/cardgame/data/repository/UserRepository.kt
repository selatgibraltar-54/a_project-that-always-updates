package com.apksafe.cardgame.data.repository

import com.apksafe.cardgame.data.db.CardDatabase
import com.apksafe.cardgame.data.db.MatchHistoryEntity
import com.apksafe.cardgame.data.db.SecurityUtils
import com.apksafe.cardgame.data.db.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRepository(private val database: CardDatabase) {
    private val userDao = database.userDao()
    private val matchHistoryDao = database.matchHistoryDao()

    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsersFlow()
    val allHistory: Flow<List<MatchHistoryEntity>> = matchHistoryDao.getAllHistoryFlow()

    fun getUserByIdFlow(id: Long): Flow<UserEntity?> = userDao.getUserByIdFlow(id)

    fun getExternalUsersFlow(thisDeviceId: String): Flow<List<UserEntity>> = userDao.getExternalUsersFlow(thisDeviceId)
    fun getLocalDeviceUsersFlow(thisDeviceId: String): Flow<List<UserEntity>> = userDao.getLocalDeviceUsersFlow(thisDeviceId)

    suspend fun registerUser(
        username: String,
        pinOrPass: String,
        avatar: String,
        originDeviceId: String = ""
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val trimmed = username.trim()
        if (trimmed.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("Username must be at least 3 characters"))
        }
        if (pinOrPass.length < 4) {
            return@withContext Result.failure(IllegalArgumentException("PIN/Password must be at least 4 characters"))
        }

        val existing = userDao.getUserByUsername(trimmed)
        if (existing != null) {
            return@withContext Result.failure(IllegalArgumentException("User '$trimmed' already exists"))
        }

        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(pinOrPass, salt)
        val newUser = UserEntity(
            username = trimmed,
            passwordHash = hash,
            salt = salt,
            avatar = avatar,
            coins = 150,
            level = 1,
            xp = 0,
            wins = 0,
            losses = 0,
            matchesPlayed = 0,
            originDeviceId = originDeviceId,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )
        val id = userDao.insertUser(newUser)
        val created = userDao.getUserById(id) ?: newUser.copy(id = id)
        Result.success(created)
    }

    suspend fun loginUser(username: String, pinOrPass: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByUsername(username.trim())
            ?: return@withContext Result.failure(IllegalArgumentException("User '$username' not found"))

        val isValid = SecurityUtils.verifyPassword(pinOrPass, user.salt, user.passwordHash)
        if (!isValid) {
            return@withContext Result.failure(IllegalArgumentException("Invalid PIN/Password"))
        }

        userDao.updateLastLogin(user.id, System.currentTimeMillis())
        Result.success(user)
    }

    suspend fun recordMatch(
        winnerUsername: String,
        loserUsername: String?,
        rounds: Int,
        afkSwitchesCount: Int,
        reason: String
    ) = withContext(Dispatchers.IO) {
        // Record history
        matchHistoryDao.insertHistory(
            MatchHistoryEntity(
                player1Name = winnerUsername,
                player2Name = loserUsername ?: "Bot",
                winnerName = winnerUsername,
                roundsPlayed = rounds,
                afkSwitchesCount = afkSwitchesCount,
                endedReason = reason
            )
        )

        // Award winner: 50 coins, 40 XP, +1 win
        userDao.recordMatchResult(
            username = winnerUsername,
            earnedCoins = 50,
            earnedXp = 40,
            winDelta = 1,
            lossDelta = 0
        )

        // Loser consolation: 15 coins, 10 XP, +1 loss
        if (loserUsername != null && loserUsername != "Bot") {
            userDao.recordMatchResult(
                username = loserUsername,
                earnedCoins = 15,
                earnedXp = 10,
                winDelta = 0,
                lossDelta = 1
            )
        }
    }

    suspend fun deleteUser(userId: Long) = withContext(Dispatchers.IO) {
        userDao.deleteUser(userId)
    }
}
