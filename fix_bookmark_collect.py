with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    content = f.read()
    
# Remove the initialValue to avoid collectAsStateWithLifecycle error
content = content.replace('collectAsStateWithLifecycle(initialValue = false)', 'collectAsStateWithLifecycle(false)')

with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.write(content)
