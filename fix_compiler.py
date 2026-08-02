import re

with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'r') as f:
    content = f.read()
    
# Fix missing initialization in ViewModel
content = content.replace(
    '    private val toolProfileRepository: ToolProfileRepository\n    private val bookmarkRepository: BookmarkRepository\n    val siteRules: StateFlow<List<SiteRule>>\n    val bookmarks: StateFlow<List<Bookmark>>',
    '    private val toolProfileRepository: ToolProfileRepository\n    private lateinit var bookmarkRepository: BookmarkRepository\n    val siteRules: StateFlow<List<SiteRule>>\n    lateinit var bookmarks: StateFlow<List<Bookmark>>'
)

with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'w') as f:
    f.write(content)
    
with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    content = f.read()

# Fix clickable import
content = content.replace('import androidx.compose.ui.Alignment', 'import androidx.compose.ui.Alignment\nimport androidx.compose.foundation.clickable')

with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.write(content)
