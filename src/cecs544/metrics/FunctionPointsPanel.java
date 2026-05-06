package cecs544.metrics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private static final int[][] WEIGHTS = {
            {3, 4, 6},
            {4, 5, 7},
            {3, 4, 6},
            {7, 10, 15},
            {5, 7, 10}
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

        vafSumField.setText("0");
        totalCountField.setText("");
        fpField.setText("");
        codeSizeField.setText("");
    }

    public void resetToBlankDefaults(String language) {
        setCurrentLanguage(language);
        for (int i = 0; i < 5; i++) {
            countFields[i].setText("");
            setComplexitySelection(groups[i], 1);
            weightedFields[i].setText("");
        }
        vafValues = new int[14];
        vafSumField.setText("0");
        totalCountField.setText("");
        fpField.setText("");
        codeSizeField.setText("");
        fireStateChanged();
    }

    private void fireStateChanged() {
        if (onStateChanged != null) onStateChanged.accept(exportState());
    }

    private JComponent buildCenterUI() {
        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        JLabel header = new JLabel("Weighting Factors");
        header.setFont(header.getFont().deriveFont(Font.BOLD, header.getFont().getSize() + 2f));

        g.gridx = 0; g.gridy = 0; g.gridwidth = 6; g.anchor = GridBagConstraints.CENTER;
        root.add(header, g);

        g.gridy = 1; g.gridwidth = 1; g.anchor = GridBagConstraints.CENTER;
        g.gridx = 2; root.add(new JLabel("Simple"), g);
        g.gridx = 3; root.add(new JLabel("Average"), g);
        g.gridx = 4; root.add(new JLabel("Complex"), g);

        g.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < FP_ITEMS.length; i++) {
            int row = i + 2;

            g.gridx = 0; g.gridy = row;
            root.add(new JLabel(FP_ITEMS[i]), g);

            g.gridx = 1;
            JTextField count = new JTextField("", 6);
            countFields[i] = count;
            root.add(count, g);

            groups[i] = new ButtonGroup();
            JRadioButton simple = new JRadioButton(String.valueOf(WEIGHTS[i][0]));
            JRadioButton avg = new JRadioButton(String.valueOf(WEIGHTS[i][1]));
            JRadioButton complex = new JRadioButton(String.valueOf(WEIGHTS[i][2]));
            groups[i].add(simple); groups[i].add(avg); groups[i].add(complex);
            avg.setSelected(true);

            g.anchor = GridBagConstraints.CENTER;
            g.gridx = 2; root.add(simple, g);
            g.gridx = 3; root.add(avg, g);
            g.gridx = 4; root.add(complex, g);
            g.anchor = GridBagConstraints.WEST;

            g.gridx = 5;
            JTextField weighted = roField(8);
            weightedFields[i] = weighted;
            weighted.setText("");
            root.add(weighted, g);
        }

        int totalRow = FP_ITEMS.length + 2;
        g.gridx = 0; g.gridy = totalRow;
        root.add(new JLabel("Total Count"), g);

        g.gridx = 5;
        root.add(totalCountField, g);

        int buttonStartRow = totalRow + 1;

        JButton computeFpBtn = new JButton("Compute FP");
        JButton vafBtn = new JButton("Value Adjustments");
        JButton computeCodeBtn = new JButton("Compute Code Size");
        JButton changeLangBtn = new JButton("Change Language");

        computeFpBtn.addActionListener(e -> {
            computeFpIfPossible();
            fireStateChanged();
        });

        vafBtn.addActionListener(e -> {
            openVafDialog();
            fireStateChanged();
        });

        computeCodeBtn.addActionListener(e -> {
            computeCodeSize();
            fireStateChanged();
        });

        changeLangBtn.addActionListener(e -> {
            openLanguageDialog();
            fireStateChanged();
        });

        g.gridx = 0; g.gridy = buttonStartRow; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        root.add(computeFpBtn, g);
        g.gridy = buttonStartRow + 1; root.add(vafBtn, g);
        g.gridy = buttonStartRow + 2; root.add(computeCodeBtn, g);
        g.gridy = buttonStartRow + 3; root.add(changeLangBtn, g);

        g.gridwidth = 1; g.fill = GridBagConstraints.NONE;

        g.gridx = 5; g.gridy = buttonStartRow; root.add(fpField, g);
        g.gridy = buttonStartRow + 1; root.add(vafSumField, g);
        g.gridy = buttonStartRow + 2; root.add(codeSizeField, g);

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
        tf.setEnabled(false);
        tf.setDisabledTextColor(Color.DARK_GRAY);
        return tf;
    }

    private void openVafDialog() {
        VafDialog dlg = new VafDialog(owner, vafValues);
        dlg.setVisible(true);

        if (dlg.isOk()) {
            vafValues = dlg.getValues();
            int sum = 0;
            for (int v : vafValues) sum += v;
            vafSumField.setText(String.valueOf(sum));
        }
    }

    private void openLanguageDialog() {
        LanguageDialog dlg = new LanguageDialog(owner, currentLanguageField.getText());
        dlg.setVisible(true);

        if (dlg.getSelectedLanguage() != null) {
            setCurrentLanguage(dlg.getSelectedLanguage());
        }
    }

    public void setCurrentLanguage(String lang) {
        currentLanguageField.setText((lang == null) ? "" : lang);
    }

    private int getSelectedComplexityIndex(ButtonGroup bg) {
        int idx = 0;
        for (var e = bg.getElements(); e.hasMoreElements(); ) {
            AbstractButton b = e.nextElement();
            if (b.isSelected()) return idx;
            idx++;
        }
        return 1;
    }

    private Integer parseNonNegativeIntOrNullIfBlank(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        int v = Integer.parseInt(s);
        if (v < 0) v = 0;
        return v;
    }

    private boolean allInputsBlank() {
        for (JTextField f : countFields) {
            if (f.getText() != null && !f.getText().trim().isEmpty()) return false;
        }
        return true;
    }

    private void computeFpIfPossible() {
        if (allInputsBlank()) return;

        int totalWeighted = 0;

        for (int i = 0; i < 5; i++) {
            Integer count;
            try {
                count = parseNonNegativeIntOrNullIfBlank(countFields[i].getText());
            } catch (Exception ex) {
                count = null;
            }
            if (count == null) count = 0;

            int cx = getSelectedComplexityIndex(groups[i]);
            int weighted = count * WEIGHTS[i][cx];
            weightedFields[i].setText(String.valueOf(weighted));
            totalWeighted += weighted;
        }

        int vafSum = 0;
        for (int v : vafValues) vafSum += v;

        totalCountField.setText(String.valueOf(totalWeighted));
        vafSumField.setText(String.valueOf(vafSum));

        double fp = totalWeighted * (0.65 + 0.01 * vafSum);
        fpField.setText(fpFmt.format(fp));
    }

    private void computeCodeSize() {
        String lang = currentLanguageField.getText();
        if (lang == null || lang.isBlank()) {
            JOptionPane.showMessageDialog(owner, "Please select a language first.", "Language Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (fpField.getText() == null || fpField.getText().isBlank()) {
            return;
        }

        double fp;
        try {
            fp = fpFmt.parse(fpField.getText()).doubleValue();
        } catch (Exception ex) {
            return;
        }

        int ufp = 0;
        try { ufp = Integer.parseInt(totalCountField.getText().trim()); } catch (Exception ignored) {}
        int vafSum = 0;
        try { vafSum = Integer.parseInt(vafSumField.getText().trim()); } catch (Exception ignored) {}

        double loc = fp * locPerFp(lang, ufp, vafSum);

        BigDecimal rounded = BigDecimal.valueOf(loc).setScale(0, RoundingMode.HALF_UP);
        codeSizeField.setText(locFmt.format(rounded));
    }

    private double locPerFp(String lang, int ufp, int vafSum) {
        return switch (lang) {
            case "Java" -> 55.0;
            case "COBOL" -> 1520.0 / 19.5;
            case "Ada 95" -> (148.509 + 0.05136 * ufp + 0.0359 * vafSum);
            case "Assembler" -> 320.0;
            case "C" -> 128.0;
            case "C++" -> 55.0;
            case "C#" -> 58.0;
            case "FORTRAN" -> 105.0;
            case "HTML" -> 15.0;
            case "JavaScript" -> 47.0;
            case "VBScript" -> 38.0;
            case "Visual Basic" -> 50.0;
            default -> 50.0;
        };
    }

    public ProjectModel.FPState exportState() {
        ProjectModel.FPState s = new ProjectModel.FPState();
        s.language = currentLanguageField.getText();

        s.counts = new int[5];
        s.complexities = new int[5];

        for (int i = 0; i < 5; i++) {
            Integer v;
            try {
                v = parseNonNegativeIntOrNullIfBlank(countFields[i].getText());
            } catch (Exception ex) {
                v = null;
            }
            s.counts[i] = (v == null) ? 0 : v;
            s.complexities[i] = getSelectedComplexityIndex(groups[i]);
        }

        s.vafValues = vafValues.clone();

        try { s.totalWeighted = Integer.parseInt(totalCountField.getText().trim()); } catch (Exception ex) { s.totalWeighted = 0; }
        try { s.vafSum = Integer.parseInt(vafSumField.getText().trim()); } catch (Exception ex) { s.vafSum = 0; }
        s.fpFormatted = fpField.getText() == null ? "" : fpField.getText();
        s.codeSizeFormatted = codeSizeField.getText() == null ? "" : codeSizeField.getText();

        return s;
    }

    public void loadFromState(ProjectModel.FPState s) {
        if (s == null) return;

        setCurrentLanguage(s.language);

        for (int i = 0; i < 5; i++) {
            if (s.counts[i] == 0) countFields[i].setText("");
            else countFields[i].setText(String.valueOf(s.counts[i]));
            setComplexitySelection(groups[i], s.complexities[i]);
        }

        if (s.vafValues != null && s.vafValues.length == 14) {
            vafValues = s.vafValues.clone();
        }

        vafSumField.setText(String.valueOf(s.vafSum));
        totalCountField.setText(s.totalWeighted == 0 ? "" : String.valueOf(s.totalWeighted));
        fpField.setText(s.fpFormatted == null ? "" : s.fpFormatted);
        codeSizeField.setText(s.codeSizeFormatted == null ? "" : s.codeSizeFormatted);

        if (totalCountField.getText() != null && !totalCountField.getText().isBlank()) {
            for (int i = 0; i < 5; i++) {
                int cx = getSelectedComplexityIndex(groups[i]);
                int w = s.counts[i] * WEIGHTS[i][cx];
                weightedFields[i].setText(String.valueOf(w));
            }
        } else {
            for (int i = 0; i < 5; i++) weightedFields[i].setText("");
        }
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
}