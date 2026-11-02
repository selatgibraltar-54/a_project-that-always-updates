package com.apksafe.cardgame.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val avatar: String = "dragon",
    val coins: Int = 100,
    val level: Int = 1,
    val xp: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val matchesPlayed: Int = 0,
    val originDeviceId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)
