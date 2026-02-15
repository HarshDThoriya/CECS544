package cecs544.metrics;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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

    // Standard IFPUG weights (common defaults)
    // You can adjust if your class uses different weights.
    private static final int[][] WEIGHTS = {
            // Simple, Average, Complex
            {3, 4, 6},   // External Inputs
            {4, 5, 7},   // External Outputs
            {3, 4, 6},   // External Inquiries
            {7, 10, 15}, // Internal Logical Files
            {5, 7, 10}   // External Interface Files
    };

    private final JTextField[] countFields = new JTextField[5];
    private final ButtonGroup[] complexityGroups = new ButtonGroup[5];
    private final JLabel[] weightedLabels = new JLabel[5];

    private final JLabel totalCountLabel = new JLabel("0");
    private final JLabel vafSumLabel = new JLabel("0");
    private final JTextField fpOutput = new JTextField("0.0");

    private final JLabel currentLanguageLabel = new JLabel("None");
    private final JTextField codeSizeOutput = new JTextField("");

    private int[] vafValues = new int[14]; // 14 GSCs (0..5), typical FP VAF scheme
    private final DecimalFormat fpFmt = new DecimalFormat("#,##0.0");

    private final JFrame owner;
    private final Consumer<ProjectModel.FPState> onStateChanged;

    public FunctionPointsPanel(JFrame owner, String currentLanguage, Consumer<ProjectModel.FPState> onStateChanged) {
        this.owner = owner;
        this.onStateChanged = onStateChanged;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top info strip
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.add(new JLabel("Current Language:"));
        currentLanguageLabel.setText(currentLanguage == null ? "None" : currentLanguage);
        top.add(currentLanguageLabel);

        add(top, BorderLayout.NORTH);

        // Center: FP input grid
        add(buildFpGrid(), BorderLayout.CENTER);

        // Right: actions panel
        add(buildActionsPanel(), BorderLayout.EAST);

        fpOutput.setEditable(false);
        codeSizeOutput.setEditable(false);

        // compute once
        recalcAndUpdate();
    }

    private JComponent buildFpGrid() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new TitledBorder("Function Point Inputs"));

        JPanel grid = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        // header row
        g.gridy = 0;
        g.gridx = 0; grid.add(new JLabel("Item"), g);
        g.gridx = 1; grid.add(new JLabel("Count"), g);
        g.gridx = 2; grid.add(new JLabel("Complexity"), g);
        g.gridx = 3; grid.add(new JLabel("Weighted"), g);

        for (int i = 0; i < FP_ITEMS.length; i++) {
            int row = i + 1;
            g.gridy = row;

            g.gridx = 0;
            grid.add(new JLabel(FP_ITEMS[i]), g);

            g.gridx = 1;
            JTextField tf = new JTextField("0", 6);
            countFields[i] = tf;
            grid.add(tf, g);

            g.gridx = 2;
            JPanel radios = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            JRadioButton simple = new JRadioButton("Simple");
            JRadioButton avg = new JRadioButton("Average");
            JRadioButton complex = new JRadioButton("Complex");
            ButtonGroup bg = new ButtonGroup();
            bg.add(simple); bg.add(avg); bg.add(complex);
            complexityGroups[i] = bg;
            avg.setSelected(true); // default Average
            radios.add(simple); radios.add(avg); radios.add(complex);
            grid.add(radios, g);

            g.gridx = 3;
            JLabel w = new JLabel("0");
            weightedLabels[i] = w;
            grid.add(w, g);

            // listeners
            tf.getDocument().addDocumentListener((SimpleDocListener) e -> recalcAndUpdate());
            simple.addActionListener(e -> recalcAndUpdate());
            avg.addActionListener(e -> recalcAndUpdate());
            complex.addActionListener(e -> recalcAndUpdate());
        }

        // totals row
        JPanel totals = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        totals.add(new JLabel("Total Count:"));
        totals.add(totalCountLabel);
        totals.add(Box.createHorizontalStrut(16));
        totals.add(new JLabel("VAF Sum:"));
        totals.add(vafSumLabel);
        totals.add(Box.createHorizontalStrut(16));
        totals.add(new JLabel("Function Points:"));
        fpOutput.setColumns(10);
        totals.add(fpOutput);

        panel.add(grid, BorderLayout.CENTER);
        panel.add(totals, BorderLayout.SOUTH);

        return panel;
    }

    private JComponent buildActionsPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new TitledBorder("Actions"));

        JButton vafBtn = new JButton("VAF...");
        JButton langBtn = new JButton("Change Language...");
        JButton computeFpBtn = new JButton("Compute FP");
        JButton computeCodeBtn = new JButton("Compute Code Size");

        p.add(vafBtn);
        p.add(Box.createVerticalStrut(8));
        p.add(langBtn);
        p.add(Box.createVerticalStrut(8));
        p.add(computeFpBtn);
        p.add(Box.createVerticalStrut(8));
        p.add(computeCodeBtn);
        p.add(Box.createVerticalStrut(12));

        JPanel codePanel = new JPanel(new BorderLayout(4, 4));
        codePanel.setBorder(new TitledBorder("Code Size"));
        codePanel.add(codeSizeOutput, BorderLayout.CENTER);

        p.add(codePanel);

        vafBtn.addActionListener(e -> openVafDialog());
        langBtn.addActionListener(e -> openLanguageDialog());
        computeFpBtn.addActionListener(e -> recalcAndUpdate());
        computeCodeBtn.addActionListener(e -> computeCodeSize());

        return p;
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
        LanguageDialog dlg = new LanguageDialog(owner, currentLanguageLabel.getText());
        dlg.setVisible(true);
        if (dlg.getSelectedLanguage() != null) {
            setCurrentLanguage(dlg.getSelectedLanguage());
            recalcAndUpdate();
        }
    }

    public void setCurrentLanguage(String lang) {
        currentLanguageLabel.setText(lang == null ? "None" : lang);
    }

    private int getSelectedComplexityIndex(ButtonGroup bg) {
        // order: Simple, Average, Complex
        // We find which radio is selected by iterating buttons in insertion order.
        int idx = 0;
        for (var e = bg.getElements(); e.hasMoreElements(); ) {
            AbstractButton b = e.nextElement();
            if (b.isSelected()) return idx;
            idx++;
        }
        return 1; // fallback average
    }

    private int parseNonNegativeInt(String s) {
        s = s.trim();
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
            int cx = getSelectedComplexityIndex(complexityGroups[i]); // 0/1/2
            int weighted = count * WEIGHTS[i][cx];
            weightedLabels[i].setText(String.valueOf(weighted));
            totalWeighted += weighted;
        }

        int vafSum = 0;
        for (int v : vafValues) vafSum += v;

        totalCountLabel.setText(String.valueOf(totalWeighted));
        vafSumLabel.setText(String.valueOf(vafSum));

        // Standard FP adjustment formula: FP = UFP * (0.65 + 0.01*VAFsum)
        double fp = totalWeighted * (0.65 + 0.01 * vafSum);

        fpOutput.setText(fpFmt.format(fp));

        // notify model
        if (onStateChanged != null) {
            onStateChanged.accept(exportState());
        }
    }

    private void computeCodeSize() {
        double fp;
        try {
            fp = fpFmt.parse(fpOutput.getText()).doubleValue();
        } catch (Exception ex) {
            fp = 0;
        }

        String lang = currentLanguageLabel.getText();
        if (lang == null || lang.equals("None") || lang.isBlank()) {
            JOptionPane.showMessageDialog(owner, "Please select a language first.", "Language Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double locPerFp = locPerFp(lang);
        double loc = fp * locPerFp;
        codeSizeOutput.setText(String.format("%,.0f LOC (%.0f LOC/FP)", loc, locPerFp));
    }

    private double locPerFp(String lang) {
        // Simple common table. Adjust to match your class if needed.
        return switch (lang) {
            case "Java" -> 53;
            case "C++" -> 55;
            case "C#" -> 58;
            case "Python" -> 21;
            case "Ruby" -> 21;
            case "Objective-C" -> 30;
            default -> 50;
        };
    }

    // ----- Save/load state -----
    public ProjectModel.FPState exportState() {
        ProjectModel.FPState s = new ProjectModel.FPState();
        s.language = currentLanguageLabel.getText();

        s.counts = new int[5];
        s.complexities = new int[5];
        for (int i = 0; i < 5; i++) {
            try {
                s.counts[i] = parseNonNegativeInt(countFields[i].getText());
            } catch (Exception ex) {
                s.counts[i] = 0;
            }
            s.complexities[i] = getSelectedComplexityIndex(complexityGroups[i]);
        }

        s.vafValues = vafValues.clone();
        s.totalWeighted = Integer.parseInt(totalCountLabel.getText());
        s.vafSum = Integer.parseInt(vafSumLabel.getText());
        s.fpFormatted = fpOutput.getText();
        return s;
    }

    public void loadFromState(ProjectModel.FPState s) {
        if (s == null) return;
        setCurrentLanguage(s.language);

        for (int i = 0; i < 5; i++) {
            countFields[i].setText(String.valueOf(s.counts[i]));
            setComplexitySelection(complexityGroups[i], s.complexities[i]);
        }

        if (s.vafValues != null && s.vafValues.length == 14) {
            vafValues = s.vafValues.clone();
        }

        recalcAndUpdate();
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

    // small helper interface for doc listener
    @FunctionalInterface
    interface SimpleDocListener extends javax.swing.event.DocumentListener {
        void update(javax.swing.event.DocumentEvent e);
        @Override default void insertUpdate(javax.swing.event.DocumentEvent e) { update(e); }
        @Override default void removeUpdate(javax.swing.event.DocumentEvent e) { update(e); }
        @Override default void changedUpdate(javax.swing.event.DocumentEvent e) { update(e); }
    }
}
