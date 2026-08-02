# App Improvements Plan

## Step 1: Code Architecture (Modularization) (Completed)
- Break down the massive `MainActivity.kt` file into smaller, focused and modular files.
- Move `KetayPredictorApp` logic to its own screen/file.
- Extract `ActivityLogScreen` and `DeveloperScreen` into separate files inside a `ui/screens` directory.
- Separate UI components (like bottom sheets, dialogs, WebView wrappers) into reusable components within a `ui/components` directory.
- Ensure state hoisting and clear separations of concerns across the UI layer.

## Step 2: Proper Navigation (Completed)
- Implement Jetpack Navigation Compose (`NavHost`) instead of managing screen state manually via boolean variables (`isActivityScreenVisible`, `isDeveloperScreenVisible`).
- Define strongly-typed routes.

## Step 3: ViewModels & State Management (Completed)
- Create a dedicated ViewModel for the main Predictor app logic.
- Replace massive in-composable state variables with `StateFlow` handled by ViewModels.

## Step 4: Data Layer (Completed)
- Transition to using Room for local data persistence properly, adhering to repository pattern. 
- Improve `ActivityLogDB` logic by structuring it with standard Room best practices.

## Step 5: Visual and UX Polish (Completed)
- Standardize the design system using Material 3 `Theme.kt`, `Color.kt`, and `Typography.kt`.
- Clean up magic colors or hardcoded dimensions.
- Refine transitions and user interactions.
