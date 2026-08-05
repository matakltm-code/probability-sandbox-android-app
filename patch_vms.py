import re
import os

# --- Patch KenoViewModel ---
with open("app/src/main/java/com/example/ui/screens/KenoViewModel.kt", "r") as f:
    keno_text = f.read()

# Add imports for math
if "import kotlin.math.sqrt" not in keno_text:
    keno_text = keno_text.replace("import kotlinx.coroutines.launch\n", "import kotlinx.coroutines.launch\nimport kotlin.math.sqrt\n")

# Add synthesizeKenoTickets
keno_engine_code = """
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
"""
keno_text = keno_text.replace("        val prediction = PredictionEngine.synthesizeKenoTickets(combined)\n", "        val prediction = synthesizeKenoTickets(combined)\n")
keno_text = re.sub(r'import com\.example\.engine\.PredictionEngine\n', '', keno_text)
keno_text = re.sub(r'\n}$', keno_engine_code, keno_text)

with open("app/src/main/java/com/example/ui/screens/KenoViewModel.kt", "w") as f:
    f.write(keno_text)


# --- Patch AviatorViewModel ---
with open("app/src/main/java/com/example/ui/screens/AviatorViewModel.kt", "r") as f:
    aviator_text = f.read()

aviator_engine_code = """
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
"""

aviator_text = aviator_text.replace("        val prediction = PredictionEngine.synthesizeAviatorPrediction(combined)\n", "        val prediction = synthesizeAviatorPrediction(combined)\n")
aviator_text = re.sub(r'import com\.example\.engine\.PredictionEngine\n', '', aviator_text)
aviator_text = re.sub(r'\n}$', aviator_engine_code, aviator_text)

with open("app/src/main/java/com/example/ui/screens/AviatorViewModel.kt", "w") as f:
    f.write(aviator_text)

