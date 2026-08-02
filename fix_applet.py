with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    content = f.read()

# Fix the collectAsStateWithLifecycle on bookmarks
content = content.replace(
    'val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()',
    'val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle(initialValue = emptyList())'
)

with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.write(content)
