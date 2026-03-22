package cecs544.metrics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.function.Consumer;

public class FunctionPointsPanel extends JPanel {

    public static final String[] FP_ITEMS = {
            "External Inputs",
            "External Outputs",
            "External Inquiries",
            "Internal Logical Files",
            "External Interface Files"
    };

    // Weights shown in your screenshot (IFPUG standard)
    // Simple / Average / Complex
    private static final int[][] WEIGHTS = {
            {3, 4, 6},    // External Inputs
            {4, 5, 7},    // External Outputs
            {3, 4, 6},    // External Inquiries
            {7, 10, 15},  // Internal Logical Files
            {5, 7, 10}    // External Interface Files
    };

    private final JTextField[] countFields = new JTextField[5];
    private final JTextField[] weightedFields = new JTextField[5];
    private final ButtonGroup[] groups = new ButtonGroup[5];

    private final JTextField totalCountField = roField(8);
    private final JTextField fpField = roField(12);
    private final JTextField vafSumField = roField(4);
    private final JTextField currentLanguageField = roField(10);
    private final JTextField codeSizeField = roField(14);

    private int[] vafValues = new int[14];

    private final DecimalFormat fpFmt = new DecimalFormat("#,##0.0");
    private final DecimalFormat locFmt = new DecimalFormat("#,##0");

    private final JFrame owner;
    private final Consumer<ProjectModel.FPState> onStateChanged;

    public FunctionPointsPanel(JFrame owner, String currentLanguage, Consumer<ProjectModel.FPState> onStateChanged) {
        this.owner = owner;
        this.onStateChanged = onStateChanged;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(14, 18, 14, 18));

        currentLanguageField.setText((currentLanguage == null || currentLanguage.isBlank()) ? "" : currentLanguage);

        add(buildCenterUI(), BorderLayout.CENTER);

        recalcAndUpdate();
    }

    private JComponent buildCenterUI() {
        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        // --- Header: "Weighting Factors"
        JLabel header = new JLabel("Weighting Factors");
        header.setFont(header.getFont().deriveFont(Font.BOLD, header.getFont().getSize() + 2f));

        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 6;
        g.anchor = GridBagConstraints.CENTER;
        root.add(header, g);

        // --- Column headers: Simple Average Complex
        g.gridy = 1;
        g.gridwidth = 1;
        g.anchor = GridBagConstraints.CENTER;

        g.gridx = 2; root.add(new JLabel("Simple"), g);
        g.gridx = 3; root.add(new JLabel("Average"), g);
        g.gridx = 4; root.add(new JLabel("Complex"), g);

        // --- Rows
        g.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < FP_ITEMS.length; i++) {
            int row = i + 2;

            // Label
            g.gridx = 0; g.gridy = row;
            root.add(new JLabel(FP_ITEMS[i]), g);

            // Count field
            g.gridx = 1;
            JTextField count = new JTextField("0", 6);
            countFields[i] = count;
            root.add(count, g);

            // Radio buttons with weight numbers under Simple/Average/Complex
            groups[i] = new ButtonGroup();

            JRadioButton simple = new JRadioButton(String.valueOf(WEIGHTS[i][0]));
            JRadioButton avg = new JRadioButton(String.valueOf(WEIGHTS[i][1]));
            JRadioButton complex = new JRadioButton(String.valueOf(WEIGHTS[i][2]));

            groups[i].add(simple);
            groups[i].add(avg);
            groups[i].add(complex);

            avg.setSelected(true); // default Average

            g.anchor = GridBagConstraints.CENTER;
            g.gridx = 2; root.add(simple, g);
            g.gridx = 3; root.add(avg, g);
            g.gridx = 4; root.add(complex, g);
            g.anchor = GridBagConstraints.WEST;

            // Weighted output field (right side)
            g.gridx = 5;
            JTextField weighted = roField(8);
            weightedFields[i] = weighted;
            root.add(weighted, g);

            // Listeners
            count.getDocument().addDocumentListener((SimpleDocListener) e -> recalcAndUpdate());
            simple.addActionListener(e -> recalcAndUpdate());
            avg.addActionListener(e -> recalcAndUpdate());
            complex.addActionListener(e -> recalcAndUpdate());
        }

        // --- Total Count row
        int totalRow = FP_ITEMS.length + 2;
        g.gridx = 0; g.gridy = totalRow;
        root.add(new JLabel("Total Count"), g);

        g.gridx = 5;
        root.add(totalCountField, g);

        // --- Buttons (left column) + outputs (right column) like screenshot
        int buttonStartRow = totalRow + 1;

        JButton computeFpBtn = new JButton("Compute FP");
        JButton vafBtn = new JButton("Value Adjustments");
        JButton computeCodeBtn = new JButton("Compute Code Size");
        JButton changeLangBtn = new JButton("Change Language");

        computeFpBtn.addActionListener(e -> recalcAndUpdate());
        vafBtn.addActionListener(e -> openVafDialog());
        computeCodeBtn.addActionListener(e -> computeCodeSize());
        changeLangBtn.addActionListener(e -> openLanguageDialog());

        // Buttons stacked on left
        g.gridx = 0; g.gridy = buttonStartRow;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        root.add(computeFpBtn, g);

        g.gridy = buttonStartRow + 1;
        root.add(vafBtn, g);

        g.gridy = buttonStartRow + 2;
        root.add(computeCodeBtn, g);

        g.gridy = buttonStartRow + 3;
        root.add(changeLangBtn, g);

        g.gridwidth = 1;
        g.fill = GridBagConstraints.NONE;

        // Right-side result fields (stacked-ish)
        // FP (top right)
        g.gridx = 5; g.gridy = buttonStartRow;
        root.add(fpField, g);

        // VAF sum (below FP)
        g.gridy = buttonStartRow + 1;
        root.add(vafSumField, g);

        // Code size (below VAF)
        g.gridy = buttonStartRow + 2;
        root.add(codeSizeField, g);

        // Current Language centered bottom-ish
        g.anchor = GridBagConstraints.EAST;
        g.gridx = 3; g.gridy = buttonStartRow + 2;
        root.add(new JLabel("Current Language"), g);

        g.anchor = GridBagConstraints.WEST;
        g.gridx = 4;
        root.add(currentLanguageField, g);

        return root;
    }

    private static JTextField roField(int cols) {
        JTextField tf = new JTextField("", cols);
        tf.setEditable(false);
        tf.setEnabled(false); // gives the greyed look like the screenshot
        tf.setDisabledTextColor(Color.DARK_GRAY);
        return tf;
    }

    private void openVafDialog() {
        VafDialog dlg = new VafDialog(owner, vafValues);
        dlg.setVisible(true);

        if (dlg.isOk()) {
            vafValues = dlg.getValues();
            recalcAndUpdate();
        }
    }

    private void openLanguageDialog() {
        LanguageDialog dlg = new LanguageDialog(owner, currentLanguageField.getText());
        dlg.setVisible(true);

        if (dlg.getSelectedLanguage() != null) {
            setCurrentLanguage(dlg.getSelectedLanguage());
            recalcAndUpdate();
        }
    }

    public void setCurrentLanguage(String lang) {
        currentLanguageField.setText((lang == null) ? "" : lang);
    }

    private int getSelectedComplexityIndex(ButtonGroup bg) {
        // 0=Simple,1=Average,2=Complex (buttons were added in that order)
        int idx = 0;
        for (var e = bg.getElements(); e.hasMoreElements(); ) {
            AbstractButton b = e.nextElement();
            if (b.isSelected()) return idx;
            idx++;
        }
        return 1;
    }

    private int parseNonNegativeInt(String s) {
        s = (s == null) ? "" : s.trim();
        if (s.isEmpty()) return 0;
        int v = Integer.parseInt(s);
        return Math.max(v, 0);
    }

    private void recalcAndUpdate() {
        int totalWeighted = 0;

        for (int i = 0; i < 5; i++) {
            int count;
            try {
                count = parseNonNegativeInt(countFields[i].getText());
            } catch (Exception ex) {
                count = 0;
            }
            int cx = getSelectedComplexityIndex(groups[i]); // 0/1/2
            int weighted = count * WEIGHTS[i][cx];
            weightedFields[i].setText(String.valueOf(weighted));
            totalWeighted += weighted;
        }

        int vafSum = 0;
        for (int v : vafValues) vafSum += v;

        totalCountField.setText(String.valueOf(totalWeighted));
        vafSumField.setText(String.valueOf(vafSum));

        // FP = UFP * (0.65 + 0.01*VAFsum)
        double fp = totalWeighted * (0.65 + 0.01 * vafSum);
        fpField.setText(fpFmt.format(fp));

        // clear code size display until user clicks compute code size (like typical UI)
        // but keep it if it was already computed
        if (codeSizeField.getText() == null || codeSizeField.getText().isBlank()) {
            // leave blank
        }

        if (onStateChanged != null) onStateChanged.accept(exportState());
    }

    private void computeCodeSize() {
        String lang = currentLanguageField.getText();
        if (lang == null || lang.isBlank()) {
            JOptionPane.showMessageDialog(owner, "Please select a language first.", "Language Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double fp;
        try {
            fp = fpFmt.parse(fpField.getText()).doubleValue();
        } catch (Exception ex) {
            fp = 0;
        }

        double locPerFp = locPerFp(lang);
        double loc = fp * locPerFp;

        codeSizeField.setText(locFmt.format(loc));
        if (onStateChanged != null) onStateChanged.accept(exportState());
    }

    private double locPerFp(String lang) {
        // Basic approximations (adjust if your class provides an official table)
        return switch (lang) {
            case "Assembler" -> 320;
            case "Ada 95" -> 71;
            case "C" -> 128;
            case "C++" -> 55;
            case "C#" -> 58;
            case "COBOL" -> 80;
            case "FORTRAN" -> 105;
            case "HTML" -> 15;
            case "Java" -> 53;
            case "JavaScript" -> 47;
            case "VBScript" -> 38;
            case "Visual Basic" -> 50;
            default -> 50;
        };
    }

    // ----- Save/load state -----
    public ProjectModel.FPState exportState() {
        ProjectModel.FPState s = new ProjectModel.FPState();
        s.language = currentLanguageField.getText();

        s.counts = new int[5];
        s.complexities = new int[5];
        for (int i = 0; i < 5; i++) {
            try {
                s.counts[i] = parseNonNegativeInt(countFields[i].getText());
            } catch (Exception ex) {
                s.counts[i] = 0;
            }
            s.complexities[i] = getSelectedComplexityIndex(groups[i]);
        }

        s.vafValues = vafValues.clone();

        // computed displays
        try { s.totalWeighted = Integer.parseInt(totalCountField.getText().trim()); } catch (Exception ex) { s.totalWeighted = 0; }
        try { s.vafSum = Integer.parseInt(vafSumField.getText().trim()); } catch (Exception ex) { s.vafSum = 0; }
        s.fpFormatted = fpField.getText();

        return s;
    }

    public void loadFromState(ProjectModel.FPState s) {
        if (s == null) return;

        setCurrentLanguage(s.language);

        for (int i = 0; i < 5; i++) {
            countFields[i].setText(String.valueOf(s.counts[i]));
            setComplexitySelection(groups[i], s.complexities[i]);
        }

        if (s.vafValues != null && s.vafValues.length == 14) {
            vafValues = s.vafValues.clone();
        }

        // Recalc to refresh UI fields
        recalcAndUpdate();

        // Restore code size if it was previously computed (optional)
        // If you want it to persist, compute it on load when language exists:
        // computeCodeSize();
    }

    private void setComplexitySelection(ButtonGroup bg, int idx) {
        int i = 0;
        for (var e = bg.getElements(); e.hasMoreElements(); ) {
            AbstractButton b = e.nextElement();
            if (i == idx) {
                b.setSelected(true);
                return;
            }
            i++;
        }
    }

    // Document listener helper
    @FunctionalInterface
    interface SimpleDocListener extends javax.swing.event.DocumentListener {
        void update(javax.swing.event.DocumentEvent e);
        @Override default void insertUpdate(javax.swing.event.DocumentEvent e) { update(e); }
        @Override default void removeUpdate(javax.swing.event.DocumentEvent e) { update(e); }
        @Override default void changedUpdate(javax.swing.event.DocumentEvent e) { update(e); }
    }
}