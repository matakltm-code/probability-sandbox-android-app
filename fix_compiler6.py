with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {\n    private val siteRuleRepository: SiteRuleRepository\n    private val toolProfileRepository: ToolProfileRepository\n    private val bookmarkRepository: BookmarkRepository\n    val siteRules: StateFlow<List<SiteRule>>\n    val bookmarks: StateFlow<List<Bookmark>>',
    'class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {\n    private val siteRuleRepository: SiteRuleRepository\n    private val toolProfileRepository: ToolProfileRepository\n    private val bookmarkRepository: BookmarkRepository\n    val siteRules: StateFlow<List<SiteRule>>\n    val bookmarks: StateFlow<List<Bookmark>>'
)

# Replace the incorrect constructor
content = content.replace(
    '        siteRuleRepository = SiteRuleRepository(database.siteRuleDao())\n        toolProfileRepository = ToolProfileRepository(database.toolProfileDao())\n        bookmarkRepository = BookmarkRepository(database.bookmarkDao())',
    '        siteRuleRepository = SiteRuleRepository(database.siteRuleDao())\n        toolProfileRepository = ToolProfileRepository(database.toolProfileDao())\n        bookmarkRepository = BookmarkRepository(database.bookmarkDao())'
)

with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'w') as f:
    f.write(content)
