package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ActivityLog
import com.example.data.local.AppDatabase
import com.example.data.repository.ActivityLogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
