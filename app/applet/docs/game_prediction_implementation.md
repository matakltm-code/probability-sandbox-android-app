# Game Prediction Implementation Plan

This document outlines the planned implementation for extracting historical game data and generating predictions for Keno and Aviator games using the WebView mapping system.

## 1. Keno Game Prediction Flow

### Step 1: Navigation
The user opens the target website (e.g., Melbet) within the secured WebView sandbox and manually navigates to the Keno game's "History" or "Results" page.

### Step 2: Element Selection & Mapping
The user activates the "Element Mapping" tool and taps on the specific card or container that holds the most recent **20 withdrawn numbers**. The DOM selector for this element is captured.

### Step 3: Confirmation Dialog
Before proceeding, the app extracts the numbers from the selected element and presents a confirmation popup to the user:
*   **Prompt:** "Are these the correct withdrawn numbers? [List of 20 numbers]"
*   **Actions:** User can "Confirm" or "Cancel/Reselect".

### Step 4: Deep Extraction
Once confirmed, the app uses the mapped CSS selector pattern to traverse the DOM and extract additional historical data (e.g., the last 5-10 previous rounds of 20 numbers each) to feed into the prediction engine.

### Step 5: Prediction & Injection
The extracted historical dataset is processed by the prediction algorithm. The app then generates the predicted next withdrawn numbers (or grouped as 5 expected tickets) and displays them in the Control Panel overlay, updating continuously as new data is polled.

---

## 2. Aviator Game Prediction Flow

### Step 1: Navigation
The user opens the target website and navigates to the Aviator game screen where the live game is running.

### Step 2: Element Selection & Mapping
In Aviator, historical multipliers are typically displayed in a horizontal row at the top of the game screen. The user activates the "Element Mapping" tool and selects the container holding this row of past multipliers.

### Step 3: Confirmation Dialog
The app extracts the visible multipliers (e.g., `1.24x, 2.50x, 1.05x`) and shows a popup:
*   **Prompt:** "Did you mean to track these multipliers? [List of extracted multipliers]"
*   **Actions:** User can "Confirm" or "Cancel/Reselect".

### Step 4: Injection & Continuous Prediction
Upon confirmation, the app saves the selector profile and begins a polling loop to extract new multipliers as they appear in the row.
The algorithm analyzes the sequence and calculates the expected trend, injecting the next predicted multiplier (e.g., `Predicted: > 2.00x`) into the active Control Panel.
