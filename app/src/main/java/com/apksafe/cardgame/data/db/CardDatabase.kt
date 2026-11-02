package com.apksafe.cardgame.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.apksafe.cardgame.data.network.DeviceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserEntity::class, MatchHistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class CardDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun matchHistoryDao(): MatchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: CardDatabase? = null

        fun getInstance(context: Context): CardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CardDatabase::class.java,
                    "card_battle_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default offline accounts in background
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            seedDefaultUsers(database.userDao(), context)
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDefaultUsers(userDao: UserDao, context: Context) {
            val thisDeviceId = DeviceUtils.getThisDeviceId(context)

            // Local user on this device
            val salt1 = SecurityUtils.generateSalt()
            val hash1 = SecurityUtils.hashPassword("1234", salt1)
            userDao.insertUser(
                UserEntity(
                    username = "PlayerOne",
                    passwordHash = hash1,
                    salt = salt1,
                    avatar = "dragon",
                    coins = 250,
                    level = 3,
                    xp = 120,
                    wins = 5,
                    losses = 2,
                    matchesPlayed = 7,
                    originDeviceId = thisDeviceId
                )
            )

            // External players from other devices (WiFi / Mobile Data pool)
            val externalPlayers = listOf(
                Triple("CyberKnight_99", "dev_ext_pixel8_pro", "knight"),
                Triple("MysticSorceress", "dev_ext_galaxy_s24", "wizard"),
                Triple("StormChaser_ID", "dev_ext_iphone15_sg", "phoenix"),
                Triple("NatureDruid_Bali", "dev_ext_xiaomi14", "guardian")
            )

            for ((username, extDeviceId, avatar) in externalPlayers) {
                val salt = SecurityUtils.generateSalt()
                val hash = SecurityUtils.hashPassword("1234", salt)
                userDao.insertUser(
                    UserEntity(
                        username = username,
                        passwordHash = hash,
                        salt = salt,
                        avatar = avatar,
                        coins = (100..400).random(),
                        level = (2..6).random(),
                        xp = (30..90).random(),
                        wins = (4..15).random(),
                        losses = (1..6).random(),
                        matchesPlayed = (5..20).random(),
                        originDeviceId = extDeviceId
                    )
                )
            }
        }
    }
}
