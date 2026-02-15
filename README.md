# CECS 544 Metrics Suite — Iteration 1 (Function Points) ✅

Java Swing desktop app for CECS 544.  
Iteration 1 focuses on **Function Points (FP)** and exists mainly as a **codebase for software testing** (black-box, glass-box, coverage, stress).

---

## What’s Implemented (Iteration 1)

### Main Application
- Desktop **GUI (Swing)** — **not a website**
- Menu bar:
  - **File**: New / Open / Save / Exit
  - **Preferences**: Language
  - **Metrics**: Function Points → Enter FP Data
  - (Edit/Help placeholders)
- Title bar updates to:
  - `CECS 544 Metrics Suite - <Project Name>`

### Function Points (FP) Tab
- 5 FP input categories:
  1. External Inputs  
  2. External Outputs  
  3. External Inquiries  
  4. Internal Logical Files  
  5. External Interface Files
- For each category:
  - Count input (non-negative integer)
  - Complexity selection: **Simple / Average / Complex** (default **Average**)
  - Weighted result shown per row
- Displays:
  - Total Weighted Count (UFP)
  - VAF Sum
  - FP result (formatted with commas and 1 decimal)

### VAF Dialog (Value Adjustment Factors)
- 14 factors (0–5 dropdown each)
- Remembers previously selected values when reopened

### Language + Code Size
- Language selection dialog (Java, C++, C#, Python, Ruby, Objective-C)
- Code size estimate:
  - `LOC = FP × LOC/FP` (simple table-based estimate)

### Save/Open `.ms` Project Files
- Saves and restores:
  - project name + creator name
  - language
  - FP counts + complexity selections
  - VAF values
  - computed totals / formatted FP output
- File format: JSON (no external libraries)

---

## Requirements
- **Java 17+**
- IntelliJ IDEA Community (recommended)

---

## Run in IntelliJ (Windows)

1. Open IntelliJ
2. **File → Open** → select this project folder
3. Set JDK:
   - **File → Project Structure → Project**
   - **Project SDK** → choose **JDK 17+**
4. Run:
   - Open `src/cscs544/metrics/App.java`
   - Click the green ▶ next to `main`
   - Select **Run 'App.main()'**

---

## How to Use

1. **File → New**  
   Enter *Project Name* and *Creator Name*
2. **Metrics → Function Points → Enter FP Data**  
   Opens the FP tab
3. Enter counts + select complexity
4. Click **VAF…**  
   Choose 0–5 for each factor, press OK
5. **Preferences → Language**  
   Select a language
6. Click **Compute FP**
7. Click **Compute Code Size**
8. **File → Save** to create a `.ms` file
9. **File → Open** to restore a saved `.ms` file

---

## FP Calculation

This implementation uses the standard adjustment:

- `FP = UFP × (0.65 + 0.01 × VAF_SUM)`

Where:
- `UFP` = sum of weighted counts
- `VAF_SUM` = sum of 14 factor values (each 0–5)

> If your instructor provides a different weight table or formula, update it in `FunctionPointsPanel.java`.

---

## Project Structure

CECS544MetricsSuite/
└── src/
└── cscs544/
└── metrics/
├── App.java
├── MainFrame.java
├── FunctionPointsPanel.java
├── LanguageDialog.java
├── VafDialog.java
├── ProjectModel.java
└── JsonMini.java



---

## Known Limitations (Iteration 1 Scope)
- Only the FP metric is implemented (other metrics come in later iterations)
- Save/Open currently stores one FP state (can be extended to multiple panes if required)