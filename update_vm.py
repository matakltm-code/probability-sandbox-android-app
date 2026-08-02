with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'import com.example.data.repository.ToolProfileRepository',
    'import com.example.data.repository.ToolProfileRepository\nimport com.example.data.local.Bookmark\nimport com.example.data.repository.BookmarkRepository\nimport kotlinx.coroutines.flow.flatMapLatest\nimport kotlinx.coroutines.flow.flowOf\nimport kotlinx.coroutines.flow.map'
)

content = content.replace(
    '    private val toolProfileRepository: ToolProfileRepository\n    val siteRules: StateFlow<List<SiteRule>>\n',
    '    private val toolProfileRepository: ToolProfileRepository\n    private val bookmarkRepository: BookmarkRepository\n    val siteRules: StateFlow<List<SiteRule>>\n    val bookmarks: StateFlow<List<Bookmark>>\n'
)

content = content.replace(
    '        siteRuleRepository = SiteRuleRepository(database.siteRuleDao())\n        toolProfileRepository = ToolProfileRepository(database.toolProfileDao())',
    '        siteRuleRepository = SiteRuleRepository(database.siteRuleDao())\n        toolProfileRepository = ToolProfileRepository(database.toolProfileDao())\n        bookmarkRepository = BookmarkRepository(database.bookmarkDao())'
)

content = content.replace(
    '            initialValue = emptyList()\n        )',
    '            initialValue = emptyList()\n        )\n        bookmarks = bookmarkRepository.allBookmarks.stateIn(\n            scope = viewModelScope,\n            started = SharingStarted.WhileSubscribed(5000),\n            initialValue = emptyList()\n        )'
)

repo_methods = '''
    fun isBookmarked(url: String): StateFlow<Boolean> {
        return bookmarkRepository.isBookmarked(url).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    }

    fun toggleBookmark(url: String, title: String) {
        viewModelScope.launch {
            val current = isBookmarked(url).value
            if (current) {
                bookmarkRepository.deleteBookmark(Bookmark(url, title))
            } else {
                bookmarkRepository.insertBookmark(Bookmark(url, title))
            }
        }
    }
'''

content = content.replace('    suspend fun getToolProfile(toolName: String): ToolProfile? {\n        return toolProfileRepository.getProfile(toolName)\n    }', '    suspend fun getToolProfile(toolName: String): ToolProfile? {\n        return toolProfileRepository.getProfile(toolName)\n    }\n' + repo_methods)

with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'w') as f:
    f.write(content)
