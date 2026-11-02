package com.apksafe.cardgame.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_history")
data class MatchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val player1Name: String,
    val player2Name: String,
    val winnerName: String,
    val roundsPlayed: Int,
    val afkSwitchesCount: Int,
    val endedReason: String, // "KO", "AFK_FORFEIT", "SURRENDER"
    val timestamp: Long = System.currentTimeMillis()
)
