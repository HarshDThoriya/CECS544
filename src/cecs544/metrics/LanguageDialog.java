package cecs544.metrics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LanguageDialog extends JDialog {

    private String selectedLanguage = null;

    // Languages shown in your screenshot (same order)
    private static final String[] LANGS = {
            "Assembler",
            "Ada 95",
            "C",
            "C++",
            "C#",
            "COBOL",
            "FORTRAN",
            "HTML",
            "Java",
            "JavaScript",
            "VBScript",
            "Visual Basic"
    };

    // Model item representing one checkbox row
    private static class CheckItem {
        final String label;
        boolean selected;
        CheckItem(String label, boolean selected) {
            this.label = label;
            this.selected = selected;
        }
        @Override public String toString() { return label; }
    }

    public LanguageDialog(JFrame owner, String current) {
        super(owner, "Select one language", true);
        setSize(260, 360);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel("Select one language");
        title.setBorder(BorderFactory.createEmptyBorder(8, 10, 0, 10));
        add(title, BorderLayout.NORTH);

        DefaultListModel<CheckItem> model = new DefaultListModel<>();
        for (String lang : LANGS) {
            boolean isSelected = current != null && current.trim().equalsIgnoreCase(lang);
            model.addElement(new CheckItem(lang, isSelected));
            if (isSelected) selectedLanguage = lang;
        }

        JList<CheckItem> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(10);

        // Render each row as a JCheckBox
        list.setCellRenderer((lst, value, index, isSelected, cellHasFocus) -> {
            JCheckBox cb = new JCheckBox(value.label, value.selected);
            cb.setBackground(isSelected ? lst.getSelectionBackground() : lst.getBackground());
            cb.setForeground(isSelected ? lst.getSelectionForeground() : lst.getForeground());
            cb.setOpaque(true);
            cb.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            return cb;
        });

        // Toggle check on click, but enforce "only one selected"
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index < 0) return;

                CheckItem clicked = model.getElementAt(index);
                boolean newState = !clicked.selected;

                // Uncheck all first (single-select)
                for (int i = 0; i < model.size(); i++) {
                    model.get(i).selected = false;
                }

                // If user clicked to check it on, select it; if clicked to uncheck, none selected
                clicked.selected = newState;

                selectedLanguage = clicked.selected ? clicked.label : null;
                list.repaint();
            }
        });

        add(new JScrollPane(list), BorderLayout.CENTER);

        JButton done = new JButton("Done");
        done.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bottom.add(done);
        add(bottom, BorderLayout.SOUTH);
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }
}