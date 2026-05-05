package cecs544.metrics;

import javax.swing.*;
import java.awt.*;

public class EnvironmentalFactorsDialog extends JDialog {

    private static final String[] FACTORS = {
            "E1 Familiarity with process",
            "E2 Application experience",
            "E3 OO experience",
            "E4 Lead analyst capability",
            "E5 Team motivation",
            "E6 Stable requirements",
            "E7 Part-time staff",
            "E8 Difficult programming language"
    };

    private static final double[] WEIGHTS = {
            0.85, 0.35, 0.45, 0.30, 0.45, 0.90, 0.65, 0.583333333
    };

    private boolean ok = false;
    private final JComboBox<Integer>[] combos = new JComboBox[FACTORS.length];

    public EnvironmentalFactorsDialog(JFrame owner, int[] currentRatings) {
        super(owner, "Environmental Complexity Factor", true);
        setSize(760, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        content.add(new JLabel("Rate each environmental factor from 0 to 5."), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        Integer[] values = {0, 1, 2, 3, 4, 5};

        g.gridx = 0; g.gridy = 0; g.weightx = 1.0; grid.add(new JLabel("Factor"), g);
        g.gridx = 1; g.weightx = 0.0; grid.add(new JLabel("Weight"), g);
        g.gridx = 2; g.weightx = 0.0; grid.add(new JLabel("Rating"), g);

        for (int i = 0; i < FACTORS.length; i++) {
            g.gridy = i + 1;
            g.gridx = 0; g.weightx = 1.0;
            grid.add(new JLabel(FACTORS[i]), g);
            g.gridx = 1; g.weightx = 0.0;
            grid.add(new JLabel(String.format("%.2f", WEIGHTS[i])), g);
            g.gridx = 2; g.weightx = 0.0;
            JComboBox<Integer> cb = new JComboBox<>(values);
            cb.setPreferredSize(new Dimension(70, 26));
            cb.setMinimumSize(new Dimension(70, 26));
            cb.setMaximumSize(new Dimension(70, 26));
            int initial = 0;
            if (currentRatings != null && currentRatings.length == FACTORS.length) {
                initial = Math.max(0, Math.min(5, currentRatings[i]));
            }
            cb.setSelectedItem(initial);
            combos[i] = cb;
            grid.add(cb, g);
        }

        content.add(new JScrollPane(grid), BorderLayout.CENTER);

        JButton doneBtn = new JButton("Done");
        JButton cancelBtn = new JButton("Cancel");
        doneBtn.addActionListener(e -> {
            ok = true;
            dispose();
        });
        cancelBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttons.add(doneBtn);
        buttons.add(cancelBtn);

        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    public boolean isOk() {
        return ok;
    }

    public int[] getRatings() {
        int[] out = new int[FACTORS.length];
        for (int i = 0; i < FACTORS.length; i++) {
            out[i] = (Integer) combos[i].getSelectedItem();
        }
        return out;
    }

    public double getEcf() {
        double sum = 0.0;
        int[] ratings = getRatings();
        for (int i = 0; i < FACTORS.length; i++) {
            sum += WEIGHTS[i] * ratings[i];
        }
        return 1.4 - (0.03 * sum);
    }
}
