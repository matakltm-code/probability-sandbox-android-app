with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'r') as f:
    content = f.read()

# Fix the missing properties initialization
replacement = '''class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val siteRuleRepository: SiteRuleRepository
    private val toolProfileRepository: ToolProfileRepository
    private var bookmarkRepository: BookmarkRepository
    val siteRules: StateFlow<List<SiteRule>>
    var bookmarks: StateFlow<List<Bookmark>>
'''
import re
content = re.sub(
    r'class HomeScreenViewModel\(application: Application\) : AndroidViewModel\(application\) \{.*?\n    val bookmarks: StateFlow<List<Bookmark>>',
    replacement,
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'w') as f:
    f.write(content)
