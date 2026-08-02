with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    content = f.read()

content = content.replace('    BackHandler(enabled = isWebViewVisible) {\n        if (isWebViewVisible) {\n        } else if (webViewRef?.canGoBack() == true) {', '    BackHandler(enabled = isWebViewVisible) {\n        if (webViewRef?.canGoBack() == true) {')

with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.write(content)
