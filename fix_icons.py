with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'import androidx.compose.material.icons.filled.Warning',
    'import androidx.compose.material.icons.filled.Warning\nimport androidx.compose.material.icons.filled.Favorite\nimport androidx.compose.material.icons.filled.FavoriteBorder\nimport androidx.compose.material.icons.filled.Delete'
)

# Remove the previously incorrectly inserted imports
content = content.replace('import androidx.compose.material.icons.filled.Warning\nimport androidx.compose.material.icons.filled.Favorite\nimport androidx.compose.material.icons.filled.FavoriteBorder\nimport androidx.compose.material.icons.filled.Favorite\nimport androidx.compose.material.icons.filled.FavoriteBorder', 'import androidx.compose.material.icons.filled.Warning\nimport androidx.compose.material.icons.filled.Favorite\nimport androidx.compose.material.icons.filled.FavoriteBorder')

with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.write(content)
