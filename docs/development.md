# Development Guide

Welcome to the Probability Sandbox development documentation. This document outlines the architecture, technologies, and educational goals behind the application.

## Educational Goals

This application serves as a practical, interactive tool for understanding probability and statistics. The core objective is to demystify mathematical algorithms by providing real-time, visual outputs based on user interaction. 

### Core Concepts Explored:
*   **Law of Large Numbers:** Observing how sample means converge to expected values as sample size increases.
*   **Probability Distributions:** Visualizing Normal, Binomial, and Uniform distributions.
*   **Statistical Analysis:** Calculating and understanding variance, standard deviation, and expected values dynamically.

## Architecture & Tech Stack

The application is built using modern Android development practices to ensure a robust, maintainable, and highly responsive user experience.

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Modern declarative UI toolkit) for any native UI elements.
*   **Asynchronous Programming:** Kotlin Coroutines & Flow for background processing.
*   **Architecture Pattern:** MVVM (Model-View-ViewModel) for separation of concerns and clear data flows.
*   **Hybrid Components:** Utilizes Android `WebView` and custom components for rich interactivity.

## Project Structure

*   `app/src/main/java/com/example/`: Contains the core Kotlin source files.
    *   `MainActivity.kt`: The main entry point of the application, handling core UI composition.
    *   `ToolProfile.kt`, `ActivityLogDB.kt`, `MapperJS.kt`: Data models, local persistence components, and JS interfaces.
*   `docs/`: Contains all project documentation (like this file).
*   `.github/workflows/`: Contains CI/CD configuration for automated builds, linting, and APK releases via GitHub Actions.

## Adding New Algorithms

To contribute new statistical algorithms or simulations to the sandbox:

1.  **Define the Model:** Create a new Kotlin class to encapsulate the mathematical logic for the algorithm.
2.  **Create the ViewModel:** Manage the state of your simulation using a `ViewModel` and expose it via `StateFlow`.
3.  **Build the UI/Integration:** Use Jetpack Compose (and `Canvas` for custom visualizations) or extend the web interface to build the interactive UI.
4.  **Integrate:** Add the new simulation to the main navigation or dashboard in the app.

## CI/CD Pipeline

We use GitHub Actions to automate our testing and release process. The workflow (`android-ci.yml`) is triggered on every push or merge to the `main` branch. It performs the following steps:
1.  **Linting:** Runs Android lint checks to ensure code quality.
2.  **Type Checking & Testing:** Compiles the project and runs unit tests.
3.  **Release Build:** Generates an APK for distribution, which is uploaded as a GitHub artifact.
