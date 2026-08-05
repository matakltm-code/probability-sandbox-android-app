package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameHistory(history: GameHistory)

    @Query("SELECT * FROM game_history WHERE gameType = :gameType AND timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getHistorySince(gameType: String, sinceTimestamp: Long): Flow<List<GameHistory>>

    @Query("DELETE FROM game_history WHERE timestamp < :olderThanTimestamp")
    suspend fun deleteOldHistory(olderThanTimestamp: Long)
}
