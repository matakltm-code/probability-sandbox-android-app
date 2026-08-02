package com.example.data.repository

import com.example.data.local.ActivityLog
import com.example.data.local.ActivityLogDao
import kotlinx.coroutines.flow.Flow

class ActivityLogRepository(private val dao: ActivityLogDao) {
    val allLogs: Flow<List<ActivityLog>> = dao.getAllLogs()

    suspend fun insert(description: String) {
        dao.insert(ActivityLog(description = description))
    }

    suspend fun deleteLogs(timeFrameStr: String) {
        if (timeFrameStr == "all_time") {
            dao.deleteAllLogs()
        } else {
            val timeOffset = when (timeFrameStr) {
                "24_hours" -> 24 * 60 * 60 * 1000L
                "week" -> 7 * 24 * 60 * 60 * 1000L
                "month" -> 30L * 24 * 60 * 60 * 1000L
                "year" -> 365L * 24 * 60 * 60 * 1000L
                else -> 0L
            }
            if (timeOffset > 0) {
                val cutoff = System.currentTimeMillis() - timeOffset
                dao.deleteLogsOlderThan(cutoff)
            }
        }
    }
}
