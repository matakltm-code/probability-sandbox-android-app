# Probability Sandbox Improvement Plan

## 1. Executive Summary
This document outlines a strategic improvement plan based on an analysis of recent external contributions (4 commits from a forked repository). After a thorough architectural review, the proposed improvements in CI/CD signing, WebView modernization, local persistence (Bookmarks), statistical modeling, and JavaScript DOM stability are highly beneficial for the Ketay Predictor app.

This plan details how we will incorporate and build upon these concepts to solidify the application's stability and predictive capabilities.

---

## 2. Analysis of Proposed Improvements

### 2.1 WebView Capability Updates & CI Pipeline
**Evaluation: Highly Recommended**
*   **Current State:** Our WebView handles basic JavaScript extraction, but can be susceptible to state loss or CORS/blocking issues on modern dynamic SPAs (Single Page Applications) if not configured perfectly.
*   **Proposed Benefit:** Implementing explicit third-party cookie acceptance, DOM storage, and user-agent masking will drastically reduce the chances of target sites blocking the embedded crawler. Moving signing configs to GitHub Actions (`pipeline.yml`) is a best practice for secure CI/CD.
*   **Action Plan:** Adopt the WebView configuration enhancements fully to ensure long-term scraping stability.

### 2.2 Bookmarking System (Room Database)
**Evaluation: Highly Recommended (Already Partially Aligned)**
*   **Current State:** We currently utilize Room for Activity Logs and basic Tool Profiles. 
*   **Proposed Benefit:** A robust Bookmark entity with a dedicated repository and ViewModel flow allows users to instantly return to specific game URLs. It leverages our existing Clean Architecture perfectly.
*   **Action Plan:** Ensure our `AppDatabase` migration paths and `BookmarkDao` implementations fully reflect a scalable MVVM pattern, allowing seamless additions to the UI bottom bar.

### 2.3 Mathematical Prediction Models (Z-Scores & Markov Chains)
**Evaluation: Crucial for App Utility**
*   **Current State:** Our prediction logic uses randomized or pseudo-random approximations based on extracted strings.
*   **Proposed Benefit:** Transitioning from pseudo-random to genuine statistical models (Hot/Cold frequency, Mean Reversion via Z-Scores, and Markov Chain transitions) is what elevates the app from a visual prototype to a functional mathematical sandbox.
*   **Action Plan:** Adopt the multi-ticket synthesis for Keno and the risk-hedging strategies for Aviator into our Kotlin `ViewModel` layer. 

### 2.4 JavaScript Crawler Optimization (`MapperJS.kt`)
**Evaluation: Critical Fix**
*   **Current State:** Rapid DOM updates can cause race conditions or UI freezing if standard polling evaluates too heavily on the main thread.
*   **Proposed Benefit:** Migrating to a `MutationObserver` architecture (event-driven) or optimizing the polling interval state sync prevents memory leaks and ensures Kotlin Flow doesn't get flooded with duplicate emissions.
*   **Action Plan:** Audit the current `evaluateJavascript` polling loop and integrate the more robust mutation-aware script described in the commit report.

---

## 3. Implementation Roadmap

### Phase 1: Core Stability & Configuration
*   Update `KetayPredictorApp.kt` WebView settings (Cookies, DOM storage, mixed content).
*   Finalize CI/CD YAML configurations for automated APK signing.

### Phase 2: Refined Data Persistence
*   Perform a database migration audit to ensure the `Bookmark` subsystem operates flawlessly alongside the `ActivityLog` system.
*   Implement the UI toggles and visual list for the bookmarking system.

### Phase 3: Mathematical Engine Overhaul
*   Implement the statistical formulas outlined in the documentation (Z-Scores, Frequencies).
*   Create a dedicated `PredictionEngine` class to separate this heavy mathematical logic from the `HomeScreenViewModel`.

### Phase 4: Crawler Reliability 
*   Replace standard interval polling with a robust DOM `MutationObserver` bridge, passing real-time updates directly into the `PredictionEngine` via JavascriptInterface.
