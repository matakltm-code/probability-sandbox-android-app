package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.GameHistory
import com.example.data.repository.GameHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AviatorViewModel(application: Application) : AndroidViewModel(application) {
    private val gameHistoryRepository: GameHistoryRepository

    init {
        val database = AppDatabase.getDatabase(application)
        gameHistoryRepository = GameHistoryRepository(database.gameHistoryDao())
    }

private val _isProcessingData = MutableStateFlow(false)
    val isProcessingData: StateFlow<Boolean> = _isProcessingData.asStateFlow()
    private val _aviatorData = MutableStateFlow("Ready to extract...")
    val aviatorData: StateFlow<String> = _aviatorData.asStateFlow()

    private val _aviatorHistoryList = MutableStateFlow<List<Double>>(emptyList())
    val aviatorHistoryList: StateFlow<List<Double>> = _aviatorHistoryList.asStateFlow()

    val aviatorDbHistory: StateFlow<List<GameHistory>> = gameHistoryRepository.getGameHistory("Aviator").stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setAviatorData(data: String) { _aviatorData.value = data }

    fun updateAviatorHistory(newItems: List<Double>) {
        val currentList = _aviatorHistoryList.value
        if (currentList.isEmpty()) {
            _aviatorHistoryList.value = newItems
            return
        }

        if (java.util.Collections.indexOfSubList(currentList, newItems) != -1) {
            return // newItems is already fully contained in currentList
        }

        var endOverlap = 0
        val maxPossibleOverlap = minOf(currentList.size, newItems.size)
        
        for (size in maxPossibleOverlap downTo 1) {
            val suffix = currentList.takeLast(size)
            val prefix = newItems.take(size)
            if (suffix == prefix) {
                endOverlap = size
                break
            }
        }

        var startOverlap = 0
        for (size in maxPossibleOverlap downTo 1) {
            val suffix = newItems.takeLast(size)
            val prefix = currentList.take(size)
            if (suffix == prefix) {
                startOverlap = size
                break
            }
        }

        if (endOverlap >= startOverlap && endOverlap > 0) {
            _aviatorHistoryList.value = currentList + newItems.drop(endOverlap)
        } else if (startOverlap > endOverlap && startOverlap > 0) {
            _aviatorHistoryList.value = newItems.dropLast(startOverlap) + currentList
        } else {
            // Check if there is any sublist match in newItems from currentList to avoid duplicate explosion
            _aviatorHistoryList.value = currentList + newItems
        }
    }

    fun saveGameHistory(url: String, data: String) {
        viewModelScope.launch {
            val recentList = aviatorDbHistory.value
            val lastSavedData = recentList.lastOrNull()?.data
            if (lastSavedData != data) {
                val history = GameHistory(
                    gameType = "Aviator",
                    siteUrl = url,
                    timestamp = System.currentTimeMillis(),
                    data = data
                )
                gameHistoryRepository.insertGameHistory(history)
                gameHistoryRepository.cleanupOldData()
            }
        }
    }

    fun synthesizeAndSetPrediction(extractedData: String, currentUrl: String) {
        viewModelScope.launch {
            _isProcessingData.value = true
        val newHistory = extractedData.split(",").mapNotNull { it.trim().replace("x", "").toDoubleOrNull() }
        updateAviatorHistory(newHistory)
        saveGameHistory(currentUrl, extractedData)
        
        val dbHistoryList = aviatorDbHistory.value.flatMap { it.data.split(",").mapNotNull { s -> s.trim().replace("x", "").toDoubleOrNull() } }
        val currentHistory = aviatorHistoryList.value
        
        // Simple combination, avoiding immediate duplicates
        val combined = mutableListOf<Double>()
        combined.addAll(dbHistoryList)
        for (item in currentHistory) {
            if (combined.isEmpty() || combined.last() != item) {
                combined.add(item)
            }
        }
        
        val prediction = synthesizeAviatorPrediction(combined)
        setAviatorData(prediction)
        kotlinx.coroutines.delay(500)
        _isProcessingData.value = false
        }
    }
    private fun synthesizeAviatorPrediction(history: List<Double>): String {
        if (history.isEmpty()) return "Not enough data to predict."
        
        // Aviator Brackets: Blue (< 2.0x), Purple (2.0x - 10.0x), Pink (>= 10.0x)
        fun getState(m: Double) = when {
            m < 2.0 -> "Blue (Low)"
            m < 10.0 -> "Purple (Medium)"
            else -> "Pink (High)"
        }
        
        val lastState = getState(history.last())
        var lowCount = 0
        var medCount = 0
        var highCount = 0
        var totalTransitions = 0
        
        for (i in 0 until history.size - 1) {
            if (getState(history[i]) == lastState) {
                totalTransitions++
                when (getState(history[i+1])) {
                    "Blue (Low)" -> lowCount++
                    "Purple (Medium)" -> medCount++
                    "Pink (High)" -> highCount++
                }
            }
        }
        
        val probLow = if (totalTransitions > 0) lowCount.toDouble() / totalTransitions else 0.50
        val probMed = if (totalTransitions > 0) medCount.toDouble() / totalTransitions else 0.40
        val probHigh = if (totalTransitions > 0) highCount.toDouble() / totalTransitions else 0.10
        
        val conservativeTarget = 1.35
        val trendTarget = if (probMed > 0.4) 2.5 else 1.8
        val sniperTarget = if (probHigh > 0.15) 12.0 else 5.5
        
        return buildString {
            append("--- Aviator Trend Engine (Local DB) ---\n")
            append("Analysis: Last 14 days of historical data (${history.size} records)\n")
            append("Recent Multiplier: ${history.last()}x\n")
            append("Current Trend: $lastState\n")
            append("Risk of Early Crash (< 2.0x): ${(probLow * 100).toInt()}%\n")
            append("[Conservative]: Predicted ${conservativeTarget}x\n")
            append("[Trend-Rider]: Predicted ${trendTarget}x\n")
            if (probHigh > 0.1) {
                append("[Sniper Hunt]: Potential ${sniperTarget}x\n")
            } else {
                append("[Sniper Hunt]: Wait (Low Probability)\n")
            }
        }
    }
}

