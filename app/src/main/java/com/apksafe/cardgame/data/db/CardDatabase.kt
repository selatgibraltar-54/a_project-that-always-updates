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
    version = 3,
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
                        // Seed only the initial real starter local account on this device
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

            // Local user on this device (No fake accounts from other devices are seeded!)
            val salt1 = SecurityUtils.generateSalt()
            val hash1 = SecurityUtils.hashPassword("1234", salt1)
            userDao.insertUser(
                UserEntity(
                    username = "PlayerOne",
                    passwordHash = hash1,
                    salt = salt1,
                    avatar = "dragon",
                    coins = 250,
                    level = 1,
                    xp = 0,
                    wins = 0,
                    losses = 0,
                    matchesPlayed = 0,
                    originDeviceId = thisDeviceId
                )
            )
        }
    }
}
