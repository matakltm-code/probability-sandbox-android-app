import re
with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'r') as f:
    content = f.read()

# Fix the toggleBookmark function which is using value incorrectly
content = re.sub(
    r'            val current = isBookmarked\(url\)\.value.*?bookmarkRepository\.insertBookmark\(Bookmark\(url, title\)\)\n            \}',
    '''            val isCurrentlyBookmarked = bookmarks.value.any { it.url == url }
            if (isCurrentlyBookmarked) {
                bookmarkRepository.deleteBookmark(Bookmark(url, title))
            } else {
                bookmarkRepository.insertBookmark(Bookmark(url, title))
            }''',
    content, flags=re.DOTALL
)

with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'w') as f:
    f.write(content)
