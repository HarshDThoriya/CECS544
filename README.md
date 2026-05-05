# CECS 544 Metrics Suite — Iteration 2 (Function Points + Use Case Points) ✅

Java Swing desktop app for CECS 544.  
The project is built as a desktop GUI for software metrics estimation and as a codebase for software testing activities such as black-box testing, glass-box testing, coverage, and stress testing.

Iteration 1 implemented **Function Points (FP)**.  
Iteration 2 adds **Use Case Points (UCP)** as a second estimation method.

---

## What’s Implemented

## Main Application
Desktop GUI (Swing) — not a website

Menu bar:
- **File**: New / Open / Save / Exit
- **Preferences**: Language
- **Metrics**:
    - Function Points → Enter FP Data
    - Use Case Points → Open UCP Panel
- **Edit/Help** placeholders

Title bar updates to:
- `CECS 544 Metrics Suite - <Project Name>`

---

## Iteration 1 — Function Points (FP)

### Function Points (FP) Tab
5 FP input categories:
- External Inputs
- External Outputs
- External Inquiries
- Internal Logical Files
- External Interface Files

For each category:
- Count input (non-negative integer)
- Complexity selection: Simple / Average / Complex
- Weighted result shown per row

Displays:
- Total Weighted Count (UFP)
- VAF Sum
- FP result (formatted with commas and 1 decimal)

### VAF Dialog (Value Adjustment Factors)
- 14 factors
- Each factor uses a 0–5 dropdown
- Remembers previously selected values when reopened
- Selected dropdown value remains visible when changed

### Language + Code Size
Language selection dialog:
- Java
- C++
- C#
- Python
- Ruby
- Objective-C

Code size estimate:
- `LOC = FP × LOC/FP`

### Save/Open `.ms` Project Files
Saves and restores:
- project name
- creator name
- language
- FP counts
- FP complexity selections
- VAF values
- computed FP totals / formatted output

File format:
- JSON
- no external libraries used

---

## Iteration 2 — Use Case Points (UCP)

### Use Case Points (UCP) Support
A second estimation method has been added using **Use Case Points**.

Access from menu:
- **Metrics → Use Case Points**

### UCP Windows / Tabs
- UCP opens in the main tabbed area
- Multiple UCP tabs are supported
- User can create more than one UCP window
- Each UCP window can be named, such as:
    - `Test1`
    - `Test2`
- Saved projects restore multiple UCP tabs and their values

### UCP Panel Fields
The UCP panel includes fields for:
- Simple / Average / Complex actor counts
- Simple / Average / Complex use case counts
- UAW
- UUCW
- UUCP
- TCF
- ECF
- Total UCP
- Estimated Hours
- Estimated LOC
- Estimated PM

### UCP Defaults
Editable defaults are provided for:
- Productivity Factor = `20`
- LOC / PM = `700`
- LOC per UCP = `120`

### UCP Buttons
- **Compute Count**
- **Technical Factors...**
- **Environmental Factors...**
- **Calculate UCP**

### Technical Complexity Factor (TCF)
TCF is computed from a dialog opened from the UCP panel.

- 13 technical factors
- Each factor uses a dropdown rating
- Selected dropdown value remains visible when changed
- Dialog remembers values when reopened

Formula used:
- `TCF = 0.6 + (0.01 × TechnicalFactorSum)`

### Environmental Complexity Factor (ECF)
ECF is computed from a dialog opened from the UCP panel.

- 8 environmental factors
- Each factor uses a dropdown rating
- Selected dropdown value remains visible when changed
- Dialog remembers values when reopened

Formula used:
- `ECF = 1.4 - (0.03 × EnvironmentalFactorSum)`

### UCP Calculation Behavior
The UCP panel behaves as follows:
- **TCF** updates when the Technical Factors dialog is completed
- **ECF** updates when the Environmental Factors dialog is completed
- **Total UCP**
- **Estimated Hours**
- **Estimated LOC**
- **Estimated PM**

are updated **only when the user clicks `Calculate UCP`**

### UCP Formulas
This implementation uses:

- `UUCP = UAW + UUCW`
- `Total UCP = UUCP × TCF × ECF`
- `Estimated Hours = Total UCP × Productivity Factor`
- `Estimated LOC = Total UCP × LOC per UCP`
- `Estimated PM = Estimated LOC / LOC per PM`

---

## Requirements
- Java 17+
- IntelliJ IDEA Community (recommended)

---

## Run in IntelliJ (Windows)

1. Open IntelliJ
2. **File → Open**
3. Select this project folder
4. Set JDK:
    - **File → Project Structure → Project**
    - **Project SDK** → choose JDK 17+
5. Run:
    - Open `src/cscs544/metrics/App.java`
    - Click the green ▶ next to `main`
    - Select **Run 'App.main()'**

---

## How to Use

### Create a New Project
1. **File → New**
2. Enter Project Name and Creator Name

### Function Points
1. **Metrics → Function Points → Enter FP Data**
2. Enter counts and choose complexity
3. Click **VAF...**
4. Choose factor values and press **OK**
5. Choose language under **Preferences → Language**
6. Click **Compute FP**
7. Click **Compute Code Size**

### Use Case Points
1. **Metrics → Use Case Points**
2. Enter a tab/window name
3. Enter actor counts and use case counts
4. Click **Compute Count**
5. Click **Technical Factors...** and enter factor ratings
6. Click **Environmental Factors...** and enter factor ratings
7. Click **Calculate UCP**
8. Review:
    - UUCP
    - Total UCP
    - Estimated Hours
    - Estimated LOC
    - Estimated PM

### Save / Open
- **File → Save** to create a `.ms` project file
- **File → Open** to restore a saved `.ms` file

Saved projects restore:
- project information
- FP panel values
- VAF values
- UCP tabs
- UCP values
- TCF / ECF dialog values
- computed UCP results

---

## FP Calculation
This implementation uses the standard adjustment:

`FP = UFP × (0.65 + 0.01 × VAF_SUM)`

Where:
- `UFP = sum of weighted counts`
- `VAF_SUM = sum of 14 factor values`

If your instructor provides a different weight table or formula, update it in `FunctionPointsPanel.java`.

---

## UCP Calculation
This implementation uses:

- `UUCP = UAW + UUCW`
- `TCF = 0.6 + (0.01 × TechnicalFactorSum)`
- `ECF = 1.4 - (0.03 × EnvironmentalFactorSum)`
- `UCP = UUCP × TCF × ECF`
- `Estimated Hours = UCP × Productivity Factor`
- `Estimated LOC = UCP × LOC per UCP`
- `Estimated PM = Estimated LOC / LOC per PM`

---

## Project Structure

```text
CECS544MetricsSuite/
└── src/
    └── cscs544/
        └── metrics/
            ├── App.java
            ├── MainFrame.java
            ├── FunctionPointsPanel.java
            ├── LanguageDialog.java
            ├── VafDialog.java
            ├── UCPPanel.java
            ├── TechnicalFactorsDialog.java
            ├── EnvironmentalFactorsDialog.java
            ├── ProjectModel.java
            └── JsonMini.java