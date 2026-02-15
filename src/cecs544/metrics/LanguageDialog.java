package cecs544.metrics;

import javax.swing.*;
import java.awt.*;

public class LanguageDialog extends JDialog {

    private String selectedLanguage = null;

    public LanguageDialog(JFrame owner, String current) {
        super(owner, "Select Language", true);
        setSize(360, 260);
        setLocationRelativeTo(owner);

        String[] langs = {"Java", "C++", "C#", "Python", "Ruby", "Objective-C"};

        JList<String> list = new JList<>(langs);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if (current != null) {
            for (int i = 0; i < langs.length; i++) {
                if (langs[i].equalsIgnoreCase(current.trim())) {
                    list.setSelectedIndex(i);
                    break;
                }
            }
        }

        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");

        ok.addActionListener(e -> {
            selectedLanguage = list.getSelectedValue();
            dispose();
        });
        cancel.addActionListener(e -> dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(cancel);
        btns.add(ok);

        setLayout(new BorderLayout(10, 10));
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }
}
