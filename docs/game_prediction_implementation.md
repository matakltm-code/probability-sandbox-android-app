
## 3. Element Mapping for Overlays (Like Aviator Round History)
As shown in the attached image, Aviator and similar games often display their round history in a popup modal or overlay dialog.
To map these elements effectively:
1.  **Popup Navigation:** The user first clicks the history icon in the game to open the "Round History" overlay.
2.  **Element Selection:** The user enables "Map Page Elements" from the control panel.
3.  **Selector Targeting:** The JavaScript mapping logic (`MapperJS.kt`) captures the specific CSS path of the overlay container holding the historical data rows (e.g., the grid of pink, blue, and green multiplier bubbles).
4.  **Data Extraction:** The app uses `querySelectorAll` on the mapped container to extract the text content of all child elements (the multipliers) into an array.
5.  **Processing:** This array of historical multipliers is then fed into the prediction algorithm to calculate the next expected value.

This step-by-step flow ensures that even data rendered dynamically inside in-game overlays can be accurately targeted, extracted, and utilized for continuous prediction.

## 4. Implementation Status
The overlay mapping and continuous background extraction have been **fully implemented**.
- **Keno Data Mapping**: Dynamically extracts drawn numbers from mapped history logs and predicts 5 grouped tickets consisting of 10 sorted numbers each.
- **Aviator Data Mapping**: Maps multiplier bubbles to calculate and predict a single upcoming multiplier.
- **Continuous Live Sync**: Automated loop parses all dynamically loaded DOM sub-elements to ensure that as new history rows load in the overlay, the algorithm continuously receives fresh data.
- **Bookmarks**: Seamlessly navigate directly to your favorite prediction games.
