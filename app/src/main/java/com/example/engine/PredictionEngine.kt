package com.example.engine

import kotlin.math.sqrt

object PredictionEngine {
    
    /**
     * Synthesizes Keno predictions using multiple strategies based on the last drawn numbers.
     * Generates 5 tickets of 10 sorted numbers each.
     */
    fun synthesizeKenoTickets(history: List<Int>): String {
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
        // Grouping by physical proximity (e.g. decades)
        val lastNumber = history.last()
        val decade = (lastNumber - 1) / 10
        val ticket4 = ((decade * 10 + 1)..(decade * 10 + 10)).toList()
        
        // Strategy 5: Markov Transition Way (Simplified)
        // Selects numbers that frequently followed the most recently drawn number in history
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

        return buildString {
            append("--- Keno Matrix Engine ---\n")
            append("History length: ${history.size}\n")
            append("[T1 - Hot]: ${ticket1.joinToString("-")}\n")
            append("[T2 - Cold]: ${ticket2.joinToString("-")}\n")
            append("[T3 - Bal]: ${ticket3.joinToString("-")}\n")
            append("[T4 - Sect]: ${ticket4.joinToString("-")}\n")
            append("[T5 - Mrkv]: ${ticket5.joinToString("-")}")
        }
    }

    /**
     * Synthesizes Aviator predictions based on a sequence of historical multipliers.
     */
    fun synthesizeAviatorPrediction(history: List<Double>): String {
        if (history.isEmpty()) return "Not enough data to predict."
        
        // Simplified Markov Chain State Transitions
        // States: Low < 1.5x, Medium 1.5x - 4.9x, High >= 5x
        fun getState(m: Double) = when {
            m < 1.5 -> "Low"
            m < 5.0 -> "Medium"
            else -> "High"
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
                    "Low" -> lowCount++
                    "Medium" -> medCount++
                    "High" -> highCount++
                }
            }
        }
        
        val probLow = if (totalTransitions > 0) lowCount.toDouble() / totalTransitions else 0.33
        
        val conservativeTarget = 1.35
        val trendTarget = 2.0
        val sniperTarget = 5.5
        
        return buildString {
            append("--- Aviator Trend Engine ---\n")
            append("Recent Multiplier: ${history.last()}x\n")
            append("Low Crash Prob: ${(probLow * 100).toInt()}%\n")
            append("[Conservative]: Predicted ${conservativeTarget}x\n")
            append("[Trend-Rider]: Predicted ${trendTarget}x\n")
            if (probLow < 0.4) {
                append("[Sniper Hunt]: Potential ${sniperTarget}x")
            } else {
                append("[Sniper Hunt]: Low Chance")
            }
        }
    }
}
