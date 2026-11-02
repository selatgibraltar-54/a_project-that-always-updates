package com.apksafe.cardgame.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchHistoryDao {
    @Query("SELECT * FROM match_history ORDER BY timestamp DESC")
    fun getAllHistoryFlow(): Flow<List<MatchHistoryEntity>>

    @Query("SELECT * FROM match_history WHERE player1Name = :username OR player2Name = :username ORDER BY timestamp DESC")
    fun getHistoryForUserFlow(username: String): Flow<List<MatchHistoryEntity>>

    @Insert
    suspend fun insertHistory(match: MatchHistoryEntity): Long
}
