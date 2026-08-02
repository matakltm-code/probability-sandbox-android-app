with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    content = f.read()

target = '''    LaunchedEffect(isLivePollingActive, activeTool) {
        if (isLivePollingActive && activeTool != "Inactive") {
            while (true) {
                kotlinx.coroutines.delay(2000)
                val profile = viewModel.getToolProfile(activeTool)
                if (profile != null && profile.cssSelector.isNotEmpty()) {
                    val jsExtractor = "(function() { " +
                        "var elements = document.querySelectorAll('${profile.cssSelector}'); " +
                        "var texts = []; " +
                        "for (var i = 0; i < elements.length; i++) { texts.push(elements[i].innerText); } " +
                        "return texts.join(', '); " +
                    "})();"
                    webViewRef?.evaluateJavascript(jsExtractor) { result ->
                        val cleanResult = result?.removeSurrounding("\"") ?: "none"
                        if (activeTool == "Keno") {
                            val newNumbers = (1..80).shuffled().take(5).joinToString("-")
                            viewModel.setKenoData("Mapped Data: $cleanResult\\nPattern: $newNumbers")
                        } else if (activeTool == "Aviator") {
                            val multiplier = String.format(java.util.Locale.US, "%.2fx", kotlin.random.Random.nextDouble(1.0, 10.0))
                            viewModel.setAviatorData("Mapped Data: $cleanResult\\nTrend: $multiplier")
                        }
                    }
                }
            }
        }
    }'''

replacement = '''    LaunchedEffect(isLivePollingActive, activeTool) {
        if (isLivePollingActive && activeTool != "Inactive") {
            while (true) {
                kotlinx.coroutines.delay(2000)
                val profile = viewModel.getToolProfile(activeTool)
                if (profile != null && profile.cssSelector.isNotEmpty()) {
                    val safeSelector = profile.cssSelector.replace("'", "\\\\'")
                    // Extract all child text elements, split by spaces or newlines to get individual numbers
                    val jsExtractor = "(function() { " +
                        "try { " +
                        "  var elements = document.querySelectorAll('" + safeSelector + "'); " +
                        "  var allText = ''; " +
                        "  for (var i = 0; i < elements.length; i++) { " +
                        "    allText += elements[i].innerText + ' '; " +
                        "  } " +
                        "  var items = allText.split(/\\\\s+/).filter(Boolean); " +
                        "  return items.join(', '); " +
                        "} catch(e) { return 'error'; } " +
                    "})();"
                    
                    webViewRef?.evaluateJavascript(jsExtractor) { result ->
                        val cleanResult = result?.removeSurrounding("\"")?.replace("\\\\u003C", "<")?.trim() ?: "none"
                        
                        // Limit displayed history
                        val historyItems = cleanResult.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val displayHistory = historyItems.takeLast(10).joinToString(", ")
                        
                        if (activeTool == "Keno") {
                            // Generate 5 predicted tickets of 10 numbers each
                            val predictedTickets = StringBuilder()
                            predictedTickets.append("Target Data: $displayHistory\\n\\nPredicted Tickets:\\n")
                            for (i in 1..5) {
                                val ticketNums = (1..80).shuffled().take(10).sorted().joinToString(" ")
                                predictedTickets.append("T$i: $ticketNums\\n")
                            }
                            viewModel.setKenoData(predictedTickets.toString().trim())
                        } else if (activeTool == "Aviator") {
                            // Predict the next multiplier based on pseudo-analysis
                            val multiplier = String.format(java.util.Locale.US, "%.2fx", kotlin.random.Random.nextDouble(1.01, 5.50))
                            viewModel.setAviatorData("Target Data: $displayHistory\\n\\nNext Expected Multiplier:\\n$multiplier")
                        }
                    }
                }
            }
        }
    }'''

content = content.replace(target, replacement)
with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.write(content)
