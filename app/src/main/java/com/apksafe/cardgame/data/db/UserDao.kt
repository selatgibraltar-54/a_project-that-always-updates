package com.apksafe.cardgame.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY lastLoginAt DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE originDeviceId != :thisDeviceId ORDER BY wins DESC")
    fun getExternalUsersFlow(thisDeviceId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE originDeviceId = :thisDeviceId ORDER BY lastLoginAt DESC")
    fun getLocalDeviceUsersFlow(thisDeviceId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Long): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET lastLoginAt = :loginTime WHERE id = :userId")
    suspend fun updateLastLogin(userId: Long, loginTime: Long)

    @Query("UPDATE users SET coins = coins + :earnedCoins, xp = xp + :earnedXp, matchesPlayed = matchesPlayed + 1, wins = wins + :winDelta, losses = losses + :lossDelta WHERE username = :username")
    suspend fun recordMatchResult(username: String, earnedCoins: Int, earnedXp: Int, winDelta: Int, lossDelta: Int)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Long)
}
