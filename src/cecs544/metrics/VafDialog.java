package cecs544.metrics;

import javax.swing.*;
import java.awt.*;

public class VafDialog extends JDialog {

    private boolean ok = false;
    private final JComboBox<Integer>[] combos = new JComboBox[14];

    // Generic labels (you can replace with the exact GSC names if your doc lists them)
    private static final String[] FACTORS = {
            "Data communications",
            "Distributed data processing",
            "Performance",
            "Heavily used configuration",
            "Transaction rate",
            "Online data entry",
            "End-user efficiency",
            "Online update",
            "Complex processing",
            "Reusability",
            "Installation ease",
            "Operational ease",
            "Multiple sites",
            "Facilitate change"
    };

    public VafDialog(JFrame owner, int[] currentValues) {
        super(owner, "Value Adjustment Factors (0-5)", true);
        setSize(520, 420);
        setLocationRelativeTo(owner);

        JPanel grid = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4,4,4,4);
        g.fill = GridBagConstraints.HORIZONTAL;

        Integer[] values = {0,1,2,3,4,5};

        for (int i = 0; i < 14; i++) {
            g.gridy = i;
            g.gridx = 0;
            g.weightx = 1.0;
            grid.add(new JLabel((i+1) + ". " + FACTORS[i]), g);

            g.gridx = 1;
            g.weightx = 0.0;
            JComboBox<Integer> cb = new JComboBox<>(values);
            cb.setSelectedItem((currentValues != null && currentValues.length == 14) ? currentValues[i] : 0);
            combos[i] = cb;
            grid.add(cb, g);
        }

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");
        okBtn.addActionListener(e -> { ok = true; dispose(); });
        cancelBtn.addActionListener(e -> dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(cancelBtn);
        btns.add(okBtn);

        setLayout(new BorderLayout(10,10));
        add(new JScrollPane(grid), BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
    }

    public boolean isOk() {
        return ok;
    }

    public int[] getValues() {
        int[] v = new int[14];
        for (int i = 0; i < 14; i++) {
            v[i] = (Integer) combos[i].getSelectedItem();
        }
        return v;
    }
}
