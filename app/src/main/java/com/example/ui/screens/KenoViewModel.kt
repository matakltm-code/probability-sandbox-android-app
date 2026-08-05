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
import kotlin.math.sqrt

class KenoViewModel(application: Application) : AndroidViewModel(application) {
    private val gameHistoryRepository: GameHistoryRepository

    init {
        val database = AppDatabase.getDatabase(application)
        gameHistoryRepository = GameHistoryRepository(database.gameHistoryDao())
    }

private val _isProcessingData = MutableStateFlow(false)
    val isProcessingData: StateFlow<Boolean> = _isProcessingData.asStateFlow()
    private val _kenoData = MutableStateFlow("Ready to extract...")
    val kenoData: StateFlow<String> = _kenoData.asStateFlow()
    
    private val _kenoHistoryList = MutableStateFlow<List<Int>>(emptyList())
    val kenoHistoryList: StateFlow<List<Int>> = _kenoHistoryList.asStateFlow()

    val kenoDbHistory: StateFlow<List<GameHistory>> = gameHistoryRepository.getGameHistory("Keno").stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setKenoData(data: String) { _kenoData.value = data }

    fun updateKenoHistory(newItems: List<Int>) {
        val currentList = _kenoHistoryList.value
        if (currentList.isEmpty()) {
            _kenoHistoryList.value = newItems
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
            _kenoHistoryList.value = currentList + newItems.drop(endOverlap)
        } else if (startOverlap > endOverlap && startOverlap > 0) {
            _kenoHistoryList.value = newItems.dropLast(startOverlap) + currentList
        } else {
            // Check if there is any sublist match in newItems from currentList to avoid duplicate explosion
            _kenoHistoryList.value = currentList + newItems
        }
    }

    fun saveGameHistory(url: String, data: String) {
        viewModelScope.launch {
            val recentList = kenoDbHistory.value
            val lastSavedData = recentList.lastOrNull()?.data
            if (lastSavedData != data) {
                val history = GameHistory(
                    gameType = "Keno",
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
        val newHistory = extractedData.split(",").mapNotNull { it.trim().toIntOrNull() }
        updateKenoHistory(newHistory)
        saveGameHistory(currentUrl, extractedData)
        
        val dbHistoryList = kenoDbHistory.value.flatMap { it.data.split(",").mapNotNull { s -> s.trim().toIntOrNull() } }
        val currentHistory = kenoHistoryList.value
        val combined = dbHistoryList + currentHistory
        
        val prediction = synthesizeKenoTickets(combined)
        setKenoData(prediction)
        kotlinx.coroutines.delay(500)
        _isProcessingData.value = false
        }
    }
    private fun synthesizeKenoTickets(history: List<Int>): String {
        if (history.isEmpty()) return "Not enough data to predict."
        
        // 1. Hot/Cold Frequency Analysis
        val frequencies = mutableMapOf<Int, Int>()
        for (num in history) {
            frequencies[num] = frequencies.getOrDefault(num, 0) + 1
        }
        
        // 2. Probability Convergence & Mean Reversion (Z-Score Modeling)
        val n = history.size
        // Expected frequency E(x) for each number in a standard 80-ball keno where 20 are drawn:
        // Probability of a specific number being drawn in one round is 20/80 = 0.25
        val p = 0.25
        val expected = n * p
        val stdDev = sqrt(n * p * (1 - p))
        
        val zScores = mutableMapOf<Int, Double>()
        for (i in 1..80) {
            val freq = frequencies.getOrDefault(i, 0)
            val z = if (stdDev > 0) (freq - expected) / stdDev else 0.0
            zScores[i] = z
        }
        
        // Strategy 1: Hot Streak Way (Trend Following) - top 10 highest frequency
        val ticket1 = (1..80).sortedByDescending { frequencies.getOrDefault(it, 0) }.take(10).sorted()
        
        // Strategy 2: Mean Reversion Way (Cold Overdue) - top 10 lowest Z-Scores
        val ticket2 = (1..80).sortedBy { zScores.getOrDefault(it, 0.0) }.take(10).sorted()
        
        // Strategy 3: Stochastic Balanced Way - 4 hot, 4 cold, 2 neutral
        val sortedByFreq = (1..80).sortedByDescending { frequencies.getOrDefault(it, 0) }
        val hot = sortedByFreq.take(4)
        val cold = sortedByFreq.takeLast(4)
        val neutral = sortedByFreq.drop(4).dropLast(4).shuffled().take(2)
        val ticket3 = (hot + cold + neutral).sorted()
        
        // Strategy 4: Sector/Neighbor Correlation Way
        val lastNumber = history.last()
        val decade = (lastNumber - 1) / 10
        val ticket4 = ((decade * 10 + 1)..(decade * 10 + 10)).toList()
        
        // Strategy 5: Markov Transition Way (Simplified)
        val transitionCounts = mutableMapOf<Int, Int>()
        for (i in 0 until history.size - 1) {
            if (history[i] == lastNumber) {
                val nextNum = history[i+1]
                transitionCounts[nextNum] = transitionCounts.getOrDefault(nextNum, 0) + 1
            }
        }
        val ticket5 = if (transitionCounts.isNotEmpty()) {
            val topTransitions = transitionCounts.keys.sortedByDescending { transitionCounts[it] }.take(10)
            val fill = (1..80).shuffled().filter { it !in topTransitions }.take(10 - topTransitions.size)
            (topTransitions + fill).sorted()
        } else {
            (1..80).shuffled().take(10).sorted()
        }

        val oddCount = history.count { it % 2 != 0 }
        val oddPercent = if (history.isNotEmpty()) (oddCount.toDouble() / history.size * 100).toInt() else 0
        val highCount = history.count { it > 40 }
        val highPercent = if (history.isNotEmpty()) (highCount.toDouble() / history.size * 100).toInt() else 0

        return buildString {
            append("--- Keno Matrix Engine (Local DB) ---\n")
            append("Analysis: Last 14 days of historical data (${history.size} records)\n")
            append("Recent Drawn Number: ${history.last()}\n")
            append("Odd/Even Bias: ${oddPercent}% Odd\n")
            append("High/Low Bias: ${highPercent}% High (>40)\n\n")
            append("[T1 - Hot Streak]: ${ticket1.joinToString("-")}\n")
            append("[T2 - Cold Overdue]: ${ticket2.joinToString("-")}\n")
            append("[T3 - Balanced]: ${ticket3.joinToString("-")}\n")
            append("[T4 - Sector Prox]: ${ticket4.joinToString("-")}\n")
            append("[T5 - Markov Chain]: ${ticket5.joinToString("-")}\n")
        }
    }
}

