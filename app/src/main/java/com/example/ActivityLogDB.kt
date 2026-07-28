package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Insert
    suspend fun insert(log: ActivityLog)

    @Query("DELETE FROM activity_logs WHERE timestamp < :timestamp")
    suspend fun deleteLogsOlderThan(timestamp: Long)

    @Query("DELETE FROM activity_logs")
    suspend fun deleteAllLogs()
}

@Database(entities = [ActivityLog::class, ToolProfile::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun toolProfileDao(): ToolProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "activity_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

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

class ActivityLogViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ActivityLogRepository
    val logs: StateFlow<List<ActivityLog>>

    private var lastLoggedDescription: String? = null
    private var lastLoggedTime: Long = 0

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ActivityLogRepository(database.activityLogDao())
        logs = repository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun logActivity(description: String) {
        val currentTime = System.currentTimeMillis()
        // Prevent duplicate logs if same description and within 5 seconds, OR just same description consecutively.
        // Let's just prevent consecutive exact duplicate descriptions.
        if (description == lastLoggedDescription) {
            return
        }
        lastLoggedDescription = description
        lastLoggedTime = currentTime
        
        viewModelScope.launch {
            repository.insert(description)
        }
    }

    fun deleteLogs(timeFrameStr: String) {
        viewModelScope.launch {
            repository.deleteLogs(timeFrameStr)
        }
    }
}
