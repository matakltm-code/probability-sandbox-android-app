with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {\n    private val siteRuleRepository: SiteRuleRepository\n    private val toolProfileRepository: ToolProfileRepository\n    val siteRules: StateFlow<List<SiteRule>>',
    'class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {\n    private val siteRuleRepository: SiteRuleRepository\n    private val toolProfileRepository: ToolProfileRepository\n    private val bookmarkRepository: BookmarkRepository\n    val siteRules: StateFlow<List<SiteRule>>\n    val bookmarks: StateFlow<List<Bookmark>>'
)

with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'w') as f:
    f.write(content)
