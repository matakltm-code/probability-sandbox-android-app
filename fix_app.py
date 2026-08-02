with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '    val isPageLoading by viewModel.isPageLoading.collectAsStateWithLifecycle()',
    '    val isPageLoading by viewModel.isPageLoading.collectAsStateWithLifecycle()\n    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()'
)

bookmark_ui = '''                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Bookmarks", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (bookmarks.isEmpty()) {
                        Text("No bookmarks yet.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(bookmarks) { bookmark ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            viewModel.setTargetUrl(bookmark.url)
                                            viewModel.setWebViewVisible(true)
                                        }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(bookmark.title, color = Color.White, fontSize = 14.sp)
                                        Text(bookmark.url, color = Color.Gray, fontSize = 12.sp)
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleBookmark(bookmark.url, bookmark.title) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove Bookmark", tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }'''

content = content.replace('                            Spacer(modifier = Modifier.height(12.dp))\n                            Button(\n                                onClick = { \n                                    viewModel.setTargetUrl("https://melbet-et.com")', bookmark_ui + '\n                            Spacer(modifier = Modifier.height(12.dp))\n                            Button(\n                                onClick = { \n                                    viewModel.setTargetUrl("https://melbet-et.com")')


bookmark_icon_ui = '''
                        val currentUrl = webViewRef?.url ?: targetUrl
                        val currentTitle = webViewRef?.title ?: "Web Page"
                        val isBookmarked by viewModel.isBookmarked(currentUrl).collectAsStateWithLifecycle(initialValue = false)
                        IconButton(
                            onClick = { viewModel.toggleBookmark(currentUrl, currentTitle) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) Color.Red else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
'''

content = content.replace(
    '                            IconButton(\n                                onClick = {\n                                    viewModel.setShowExitConfirmation(true)',
    bookmark_icon_ui + '                            IconButton(\n                                onClick = {\n                                    viewModel.setShowExitConfirmation(true)'
)

content = content.replace('import androidx.compose.material.icons.filled.Warning', 'import androidx.compose.material.icons.filled.Warning\nimport androidx.compose.material.icons.filled.Favorite\nimport androidx.compose.material.icons.filled.FavoriteBorder')

with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.write(content)
