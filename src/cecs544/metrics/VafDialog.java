package cecs544.metrics;

import javax.swing.*;
import java.awt.*;

public class VafDialog extends JDialog {

    private boolean ok = false;
    private final JComboBox<Integer>[] combos = new JComboBox[14];

    private static final String[] QUESTIONS = {
            "Does the system require reliable backup and recovery processes?",
            "Are specialized data communications required to transfer information to or from the application?",
            "Are there distributed processing functions?",
            "Is performance critical?",
            "Will the system run in an existing, heavily utilized operational environment?",
            "Does the system require online data entry?",
            "Does the online data entry require the input transaction to be built over multiple screens or operations?",
            "Are the internal logical files updated online?",
            "Are the input, output, files or inquiries complex?",
            "Is the internal processing complex?",
            "Is the code designed to be reusable?",
            "Are conversion and installation included in the design?",
            "Is the system designed for multiple installations in different organizations?",
            "Is the application designed to facilitate change and for ease of use by the user?"
    };

    public VafDialog(JFrame owner, int[] currentValues) {
        super(owner, "Value Adjustment Factors", true);
        setSize(980, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel header = new JLabel(
                "Assign a value from 0 to 5 for each of the following Value Adjustment Factors:"
        );

        content.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        Integer[] values = {0, 1, 2, 3, 4, 5};

        for (int i = 0; i < QUESTIONS.length; i++) {
            g.gridy = i;

            g.gridx = 0;
            g.weightx = 1.0;
            JLabel q = new JLabel(QUESTIONS[i]);
            grid.add(q, g);

            g.gridx = 1;
            g.weightx = 0.0;
            JComboBox<Integer> cb = new JComboBox<>(values);
            cb.setPreferredSize(new Dimension(70, 26));
            cb.setMinimumSize(new Dimension(70, 26));
            cb.setMaximumSize(new Dimension(70, 26));
            int initial = 0;
            if (currentValues != null && currentValues.length == 14) {
                initial = Math.max(0, Math.min(5, currentValues[i]));
            }
            cb.setSelectedItem(initial);
            combos[i] = cb;
            grid.add(cb, g);
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        content.add(scroll, BorderLayout.CENTER);

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

    public int[] getValues() {
        int[] v = new int[14];
        for (int i = 0; i < 14; i++) {
            v[i] = (Integer) combos[i].getSelectedItem();
        }
        return v;
    }
}