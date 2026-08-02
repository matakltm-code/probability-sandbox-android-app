import re

with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'r') as f:
    content = f.read()

replacement = '''class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val siteRuleRepository: SiteRuleRepository
    private val toolProfileRepository: ToolProfileRepository
    private val bookmarkRepository: BookmarkRepository
    val siteRules: StateFlow<List<SiteRule>>
    val bookmarks: StateFlow<List<Bookmark>>
'''

content = re.sub(
    r'class HomeScreenViewModel\(application: Application\) : AndroidViewModel\(application\) \{.*?init \{',
    replacement + '    init {',
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'w') as f:
    f.write(content)
