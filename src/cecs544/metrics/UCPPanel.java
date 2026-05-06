package cecs544.metrics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.function.Consumer;

public class UCPPanel extends JPanel {

    private static final String[] ACTOR_TYPES = {
            "Simple Actor",
            "Average Actor",
            "Complex Actor"
    };

    private static final int[] ACTOR_WEIGHTS = {1, 2, 3};

    private static final String[] USE_CASE_TYPES = {
            "Simple Use Case",
            "Average Use Case",
            "Complex Use Case"
    };

    private static final int[] USE_CASE_WEIGHTS = {5, 10, 15};

    private final JFrame owner;
    private final Consumer<ProjectModel.UCPState> onStateChanged;

    private final JTextField[] actorCountFields = new JTextField[3];
    private final JTextField[] actorWeightFields = new JTextField[3];
    private final JTextField[] actorWeightedFields = new JTextField[3];

    private final JTextField[] useCaseCountFields = new JTextField[3];
    private final JTextField[] useCaseWeightFields = new JTextField[3];
    private final JTextField[] useCaseWeightedFields = new JTextField[3];

    private final JTextField uawField = roField(10);
    private final JTextField uucwField = roField(10);
    private final JTextField totalCountField = roField(10);
    private final JTextField tcfField = roField(10);
    private final JTextField ecfField = roField(10);
    private final JTextField totalUcpField = roField(12);
    private final JTextField estimatedHoursField = roField(12);
    private final JTextField estimatedLocField = roField(12);
    private final JTextField estimatedPmField = roField(12);

    private final JTextField productivityField = new JTextField("20", 10);
    private final JTextField locPerPmField = new JTextField("700", 10);
    private final JTextField locPerUcpField = new JTextField("120", 10);

    private int[] technicalRatings = new int[13];
    private int[] environmentalRatings = new int[8];
    private double tcf = 0.60;
    private double ecf = 1.40;

    private final DecimalFormat twoDec = new DecimalFormat("#,##0.00");
    private final DecimalFormat oneDec = new DecimalFormat("#,##0.0");
    private final DecimalFormat oneOrTwoDec = new DecimalFormat("#,##0.0#");

    public UCPPanel(JFrame owner, Consumer<ProjectModel.UCPState> onStateChanged) {
        this.owner = owner;
        this.onStateChanged = onStateChanged;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(14, 18, 14, 18));
        add(buildUi(), BorderLayout.CENTER);
        refreshAdjustmentFields();

        // mark dirty on any direct edits too
        attachDirtyListener(productivityField);
        attachDirtyListener(locPerPmField);
        attachDirtyListener(locPerUcpField);

        for (JTextField f : actorCountFields) attachDirtyListener(f);
        for (JTextField f : useCaseCountFields) attachDirtyListener(f);
    }

    private void attachDirtyListener(JTextField tf) {
        tf.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { fireStateChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { fireStateChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { fireStateChanged(); }
        });
    }

    private void fireStateChanged() {
        if (onStateChanged != null) onStateChanged.accept(exportState());
    }

    private JComponent buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));

        JPanel tables = new JPanel(new GridLayout(2, 1, 0, 12));
        tables.add(buildActorPanel());
        tables.add(buildUseCasePanel());
        root.add(tables, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(12, 12));
        bottom.add(buildFactorsAndSettingsPanel(), BorderLayout.WEST);
        bottom.add(buildResultsPanel(), BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        return new JScrollPane(root);
    }

    private JPanel buildActorPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Actors"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; p.add(new JLabel("Actor Type"), g);
        g.gridx = 1; p.add(new JLabel("Count"), g);
        g.gridx = 2; p.add(new JLabel("Weight"), g);
        g.gridx = 3; p.add(new JLabel("Weighted"), g);

        for (int i = 0; i < ACTOR_TYPES.length; i++) {
            g.gridy = i + 1;
            g.gridx = 0;
            p.add(new JLabel(ACTOR_TYPES[i]), g);

            g.gridx = 1;
            actorCountFields[i] = new JTextField(8);
            p.add(actorCountFields[i], g);

            g.gridx = 2;
            actorWeightFields[i] = roField(8);
            actorWeightFields[i].setText(String.valueOf(ACTOR_WEIGHTS[i]));
            p.add(actorWeightFields[i], g);

            g.gridx = 3;
            actorWeightedFields[i] = roField(8);
            p.add(actorWeightedFields[i], g);
        }

        g.gridy = 4;
        g.gridx = 2;
        p.add(new JLabel("UAW"), g);
        g.gridx = 3;
        p.add(uawField, g);

        return p;
    }

    private JPanel buildUseCasePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Use Cases"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; p.add(new JLabel("Use Case Type"), g);
        g.gridx = 1; p.add(new JLabel("Count"), g);
        g.gridx = 2; p.add(new JLabel("Weight"), g);
        g.gridx = 3; p.add(new JLabel("Weighted"), g);

        for (int i = 0; i < USE_CASE_TYPES.length; i++) {
            g.gridy = i + 1;
            g.gridx = 0;
            p.add(new JLabel(USE_CASE_TYPES[i]), g);

            g.gridx = 1;
            useCaseCountFields[i] = new JTextField(8);
            p.add(useCaseCountFields[i], g);

            g.gridx = 2;
            useCaseWeightFields[i] = roField(8);
            useCaseWeightFields[i].setText(String.valueOf(USE_CASE_WEIGHTS[i]));
            p.add(useCaseWeightFields[i], g);

            g.gridx = 3;
            useCaseWeightedFields[i] = roField(8);
            p.add(useCaseWeightedFields[i], g);
        }

        g.gridy = 4;
        g.gridx = 2;
        p.add(new JLabel("UUCW"), g);
        g.gridx = 3;
        p.add(uucwField, g);

        g.gridy = 5;
        g.gridx = 2;
        p.add(new JLabel("Total Count"), g);
        g.gridx = 3;
        p.add(totalCountField, g);

        return p;
    }

    private JPanel buildFactorsAndSettingsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Adjustments and Productivity"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        JButton computeCountButton = new JButton("Compute Count");
        computeCountButton.addActionListener(e -> {
            updateCountFields();
            fireStateChanged();
        });

        JButton technicalButton = new JButton("Technical Factors...");
        technicalButton.addActionListener(e -> {
            openTechnicalDialog();
            fireStateChanged();
        });

        JButton environmentalButton = new JButton("Environmental Factors...");
        environmentalButton.addActionListener(e -> {
            openEnvironmentalDialog();
            fireStateChanged();
        });

        JButton calculateButton = new JButton("Calculate UCP");
        calculateButton.addActionListener(e -> {
            calculateResults();
            fireStateChanged();
        });

        int row = 0;
        g.gridx = 0; g.gridy = row; g.gridwidth = 2; p.add(computeCountButton, g);

        row++;
        g.gridx = 0; g.gridy = row; g.gridwidth = 1; p.add(technicalButton, g);
        g.gridx = 1; p.add(tcfField, g);

        row++;
        g.gridx = 0; g.gridy = row; p.add(environmentalButton, g);
        g.gridx = 1; p.add(ecfField, g);

        row++;
        g.gridx = 0; g.gridy = row; p.add(new JLabel("Productivity Factor"), g);
        g.gridx = 1; p.add(productivityField, g);

        row++;
        g.gridx = 0; g.gridy = row; p.add(new JLabel("LOC / PM"), g);
        g.gridx = 1; p.add(locPerPmField, g);

        row++;
        g.gridx = 0; g.gridy = row; p.add(new JLabel("LOC per UCP"), g);
        g.gridx = 1; p.add(locPerUcpField, g);

        row++;
        g.gridx = 0; g.gridy = row; g.gridwidth = 2; p.add(calculateButton, g);

        return p;
    }

    private JPanel buildResultsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("UCP Results"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        int row = 0;
        addRow(p, g, row++, "Total UCP", totalUcpField);
        addRow(p, g, row++, "Estimated Hours", estimatedHoursField);
        addRow(p, g, row++, "Estimated LOC", estimatedLocField);
        addRow(p, g, row++, "Estimated PM", estimatedPmField);

        return p;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JTextField field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0.0;
        p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1.0;
        p.add(field, g);
    }

    private void openTechnicalDialog() {
        TechnicalFactorsDialog dlg = new TechnicalFactorsDialog(owner, technicalRatings);
        dlg.setVisible(true);
        if (dlg.isOk()) {
            technicalRatings = dlg.getRatings();
            tcf = dlg.getTcf();
            tcfField.setText(twoDec.format(tcf));
        }
    }

    private void openEnvironmentalDialog() {
        EnvironmentalFactorsDialog dlg = new EnvironmentalFactorsDialog(owner, environmentalRatings);
        dlg.setVisible(true);
        if (dlg.isOk()) {
            environmentalRatings = dlg.getRatings();
            ecf = dlg.getEcf();
            ecfField.setText(twoDec.format(ecf));
        }
    }

    private void refreshAdjustmentFields() {
        tcfField.setText(oneOrTwoDec.format(tcf));
        ecfField.setText(oneOrTwoDec.format(ecf));
    }

    private void updateCountFields() {
        double uaw = computeWeighted(actorCountFields, ACTOR_WEIGHTS, actorWeightedFields);
        double uucw = computeWeighted(useCaseCountFields, USE_CASE_WEIGHTS, useCaseWeightedFields);
        double totalCount = uaw + uucw;

        uawField.setText(formatWholeOrTwoDec(uaw));
        uucwField.setText(formatWholeOrTwoDec(uucw));
        totalCountField.setText(formatWholeOrTwoDec(totalCount));
    }

    private void calculateResults() {
        updateCountFields();

        double totalCount = parseDouble(totalCountField.getText().replace(",", ""), 0.0);
        double productivity = parseDouble(productivityField.getText(), 20.0);
        double locPerPm = parseDouble(locPerPmField.getText(), 700.0);
        double locPerUcp = parseDouble(locPerUcpField.getText(), 120.0);

        double totalUcp = totalCount * tcf * ecf;
        double roundedTotalUcp = parseDouble(oneOrTwoDec.format(totalUcp), totalUcp);
        double estimatedHours = roundedTotalUcp * productivity;
        double estimatedLoc = roundedTotalUcp * locPerUcp;
        double estimatedPm = locPerPm == 0.0 ? 0.0 : estimatedLoc / locPerPm;

        totalUcpField.setText(oneOrTwoDec.format(roundedTotalUcp));
        estimatedHoursField.setText(oneDec.format(estimatedHours));
        estimatedLocField.setText(oneDec.format(estimatedLoc));
        estimatedPmField.setText(twoDec.format(estimatedPm));
    }

    private double computeWeighted(JTextField[] countFields, int[] weights, JTextField[] weightedFields) {
        double total = 0.0;
        for (int i = 0; i < countFields.length; i++) {
            int count = parseInt(countFields[i].getText(), 0);
            int weighted = count * weights[i];
            weightedFields[i].setText(String.valueOf(weighted));
            total += weighted;
        }
        return total;
    }

    private String formatWholeOrTwoDec(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0000001) {
            return String.format("%,.0f", value);
        }
        return twoDec.format(value);
    }

    private int parseInt(String s, int fallback) {
        try {
            if (s == null || s.trim().isEmpty()) return fallback;
            return Math.max(0, Integer.parseInt(s.trim().replace(",", "")));
        } catch (Exception ex) {
            return fallback;
        }
    }

    private double parseDouble(String s, double fallback) {
        try {
            if (s == null || s.trim().isEmpty()) return fallback;
            return Double.parseDouble(s.trim().replace(",", ""));
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static JTextField roField(int cols) {
        JTextField tf = new JTextField("", cols);
        tf.setEditable(false);
        tf.setEnabled(false);
        tf.setDisabledTextColor(Color.DARK_GRAY);
        return tf;
    }

    public ProjectModel.UCPState exportState() {
        ProjectModel.UCPState s = new ProjectModel.UCPState();
        s.actorCounts = readCounts(actorCountFields);
        s.useCaseCounts = readCounts(useCaseCountFields);
        s.technicalRatings = technicalRatings.clone();
        s.environmentalRatings = environmentalRatings.clone();
        s.productivityFactor = productivityField.getText();
        s.locPerPm = locPerPmField.getText();
        s.locPerUcp = locPerUcpField.getText();
        s.uaw = uawField.getText();
        s.uucw = uucwField.getText();
        s.totalCount = totalCountField.getText();
        s.tcf = tcfField.getText();
        s.ecf = ecfField.getText();
        s.totalUcp = totalUcpField.getText();
        s.estimatedHours = estimatedHoursField.getText();
        s.estimatedLoc = estimatedLocField.getText();
        s.estimatedPm = estimatedPmField.getText();
        return s;
    }

    public void loadFromState(ProjectModel.UCPState s) {
        if (s == null) return;
        writeCounts(actorCountFields, s.actorCounts);
        writeCounts(useCaseCountFields, s.useCaseCounts);
        if (s.technicalRatings != null && s.technicalRatings.length == 13) technicalRatings = s.technicalRatings.clone();
        if (s.environmentalRatings != null && s.environmentalRatings.length == 8) environmentalRatings = s.environmentalRatings.clone();
        productivityField.setText(blankIfNull(s.productivityFactor, "20"));
        locPerPmField.setText(blankIfNull(s.locPerPm, "700"));
        locPerUcpField.setText(blankIfNull(s.locPerUcp, "120"));
        tcf = parseDouble(blankIfNull(s.tcf, "0.60"), 0.60);
        ecf = parseDouble(blankIfNull(s.ecf, "1.40"), 1.40);
        refreshAdjustmentFields();
        updateCountFields();
        uawField.setText(blankIfNull(s.uaw, uawField.getText()));
        uucwField.setText(blankIfNull(s.uucw, uucwField.getText()));
        totalCountField.setText(blankIfNull(s.totalCount, totalCountField.getText()));
        totalUcpField.setText(blankIfNull(s.totalUcp, ""));
        estimatedHoursField.setText(blankIfNull(s.estimatedHours, ""));
        estimatedLocField.setText(blankIfNull(s.estimatedLoc, ""));
        estimatedPmField.setText(blankIfNull(s.estimatedPm, ""));
    }

    private String blankIfNull(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private int[] readCounts(JTextField[] fields) {
        int[] out = new int[fields.length];
        for (int i = 0; i < fields.length; i++) {
            out[i] = parseInt(fields[i].getText(), 0);
        }
        return out;
    }

    private void writeCounts(JTextField[] fields, int[] values) {
        if (values == null) return;
        for (int i = 0; i < Math.min(fields.length, values.length); i++) {
            fields[i].setText(values[i] == 0 ? "" : String.valueOf(values[i]));
        }
    }
}