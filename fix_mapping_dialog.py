with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    content = f.read()

target = '''    if (showMappingDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowMappingDialog(false) },
            title = { Text("Map this element?", color = Color.White) },
            text = { 
                Column {
                    Text("Target Tool: $activeTool", color = com.example.ui.theme.TextHighlight)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Path: $selectedCssSelector", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sample text: $selectedText", color = MaterialTheme.colorScheme.onBackground)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.insertToolProfile(
                                ToolProfile(
                                    toolName = activeTool,
                                    cssSelector = selectedCssSelector,
                                    label = "version_0000001"
                                )
                            )
                            activityLogViewModel.logActivity("Mapped $activeTool CSS: $selectedCssSelector")
                        }
                        viewModel.setShowMappingDialog(false)
                        viewModel.setSelectionModeActive(false)
                    }
                ) {
                    Text("Save Mapping", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowMappingDialog(false) }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }'''

replacement = '''    if (showMappingDialog) {
        val extractedData = selectedText.replace("\\n", ", ").take(100)
        
        AlertDialog(
            onDismissRequest = { viewModel.setShowMappingDialog(false) },
            title = { 
                Text(
                    text = if (activeTool == "Keno") "Confirm Keno Withdrawn Numbers?" 
                           else if (activeTool == "Aviator") "Confirm Aviator Multipliers?" 
                           else "Map this element?", 
                    color = Color.White
                ) 
            },
            text = { 
                Column {
                    if (activeTool == "Keno" || activeTool == "Aviator") {
                        Text(
                            text = if (activeTool == "Keno") "Are these the correct withdrawn numbers?" 
                                   else "Did you mean to track these multipliers?",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Extracted: $extractedData...", color = com.example.ui.theme.TextHighlight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Path: $selectedCssSelector", color = Color.Gray, fontSize = 10.sp)
                    } else {
                        Text("Target Tool: $activeTool", color = com.example.ui.theme.TextHighlight)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Path: $selectedCssSelector", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sample text: $selectedText", color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.insertToolProfile(
                                ToolProfile(
                                    toolName = activeTool,
                                    cssSelector = selectedCssSelector,
                                    label = "version_0000001"
                                )
                            )
                            activityLogViewModel.logActivity("Mapped $activeTool CSS: $selectedCssSelector")
                            
                            // Initialize prediction
                            if (activeTool == "Keno") {
                                val newNumbers = (1..80).shuffled().take(5).joinToString("-")
                                viewModel.setKenoData("Processing history...\nPredicted Tickets:\n1. $newNumbers")
                            } else if (activeTool == "Aviator") {
                                viewModel.setAviatorData("Processing history...\nPredicting next...")
                            }
                        }
                        viewModel.setShowMappingDialog(false)
                        viewModel.setSelectionModeActive(false)
                        viewModel.setLivePollingActive(true) // Auto-start live polling after confirm
                    }
                ) {
                    Text("Confirm", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowMappingDialog(false) }) {
                    Text("Cancel/Reselect", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }'''

content = content.replace(target, replacement)
with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.write(content)
