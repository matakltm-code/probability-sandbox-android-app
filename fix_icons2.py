with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'import androidx.compose.material.icons.filled.Code',
    'import androidx.compose.material.icons.filled.Code\nimport androidx.compose.material.icons.filled.Favorite\nimport androidx.compose.material.icons.filled.FavoriteBorder'
)

with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.write(content)
