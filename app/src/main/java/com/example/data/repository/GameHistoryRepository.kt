package com.example.data.repository

import com.example.data.local.GameHistory
import com.example.data.local.GameHistoryDao
import kotlinx.coroutines.flow.Flow

class GameHistoryRepository(private val gameHistoryDao: GameHistoryDao) {
    fun getGameHistory(gameType: String): Flow<List<GameHistory>> {
        val twoWeeksAgo = System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000L
        return gameHistoryDao.getHistorySince(gameType, twoWeeksAgo)
    }

    suspend fun insertGameHistory(history: GameHistory) {
        gameHistoryDao.insertGameHistory(history)
    }

    suspend fun cleanupOldData() {
        val twoWeeksAgo = System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000L
        gameHistoryDao.deleteOldHistory(twoWeeksAgo)
    }
}
