import re

with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '    private val toolProfileRepository: ToolProfileRepository\n    val siteRules: StateFlow<List<SiteRule>>',
    '    private val toolProfileRepository: ToolProfileRepository\n    private lateinit var bookmarkRepository: BookmarkRepository\n    val siteRules: StateFlow<List<SiteRule>>\n    lateinit var bookmarks: StateFlow<List<Bookmark>>'
)

with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'w') as f:
    f.write(content)

