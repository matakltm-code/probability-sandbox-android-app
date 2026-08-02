with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '    fun isBookmarked(url: String): StateFlow<Boolean> {\n        return bookmarkRepository.isBookmarked(url).stateIn(\n            scope = viewModelScope,\n            started = SharingStarted.WhileSubscribed(5000),\n            initialValue = false\n        )\n    }',
    '    fun isBookmarked(url: String): Flow<Boolean> {\n        return bookmarkRepository.isBookmarked(url)\n    }'
)

content = content.replace('import kotlinx.coroutines.flow.Flow\n', '')
content = content.replace('import kotlinx.coroutines.flow.flatMapLatest', 'import kotlinx.coroutines.flow.Flow\nimport kotlinx.coroutines.flow.flatMapLatest')

with open('app/src/main/java/com/example/ui/screens/HomeScreenViewModel.kt', 'w') as f:
    f.write(content)
