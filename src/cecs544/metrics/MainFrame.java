package cecs544.metrics;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class MainFrame extends JFrame {

    private final JTabbedPane tabs = new JTabbedPane();
    private ProjectModel project = ProjectModel.newEmpty("Untitled", "", "", "");
    private File currentFile = null;

    public MainFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(980, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);

        setJMenuBar(buildMenuBar());
        refreshTitle();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("File");
        JMenuItem mNew = new JMenuItem("New");
        JMenuItem mOpen = new JMenuItem("Open");
        JMenuItem mSave = new JMenuItem("Save");
        JMenuItem mExit = new JMenuItem("Exit");

        mNew.addActionListener(e -> newProject());
        mOpen.addActionListener(e -> openProject());
        mSave.addActionListener(e -> saveProject());
        mExit.addActionListener(e -> dispose());

        file.add(mNew);
        file.add(mOpen);
        file.add(mSave);
        file.addSeparator();
        file.add(mExit);

        JMenu edit = new JMenu("Edit");
        edit.add(new JMenuItem("NOSE"));

        JMenu prefs = new JMenu("Preferences");
        JMenuItem mLang = new JMenuItem("Languages");
        mLang.addActionListener(e -> chooseLanguage());
        prefs.add(mLang);

        JMenu metrics = new JMenu("Metrics");
        JMenu fp = new JMenu("Function Points");
        JMenuItem enterFp = new JMenuItem("Enter FP Data");
        enterFp.addActionListener(e -> addFunctionPointsTabAskName());
        fp.add(enterFp);

        JMenu ucp = new JMenu("Use Case Points");
        JMenuItem enterUcp = new JMenuItem("Open UCP Panel");
        enterUcp.addActionListener(e -> addUseCasePointsTabAskName());
        ucp.add(enterUcp);

        metrics.add(fp);
        metrics.add(ucp);

        JMenu help = new JMenu("Help");
        help.add(new JMenuItem("NOSE"));

        bar.add(file);
        bar.add(edit);
        bar.add(prefs);
        bar.add(metrics);
        bar.add(help);

        return bar;
    }

    private void refreshTitle() {
        setTitle("CECS 544 Metrics Suite - " + project.projectName);
    }

    private void newProject() {
        JTextField projectNameField = new JTextField(project.projectName == null ? "" : project.projectName);
        JTextField productNameField = new JTextField(project.productName == null ? "" : project.productName);
        JTextField creatorField = new JTextField(project.creatorName == null ? "" : project.creatorName);

        JTextArea commentsArea = new JTextArea(4, 28);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        commentsArea.setText(project.comments == null ? "" : project.comments);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Project Name:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;
        form.add(projectNameField, g);

        g.gridx = 0; g.gridy = 1; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        form.add(new JLabel("Product Name:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;
        form.add(productNameField, g);

        g.gridx = 0; g.gridy = 2; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        form.add(new JLabel("Creator:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;
        form.add(creatorField, g);

        g.gridx = 0; g.gridy = 3; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        g.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Comments:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.BOTH; g.weightx = 1.0; g.weighty = 1.0;
        form.add(new JScrollPane(commentsArea), g);

        while (true) {
            int ok = JOptionPane.showConfirmDialog(
                    this, form, "New Project",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (ok != JOptionPane.OK_OPTION) return;

            String pn = projectNameField.getText().trim();
            String pr = productNameField.getText().trim();
            String cr = creatorField.getText().trim();

            if (pn.isBlank() || pr.isBlank() || cr.isBlank()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Missing required input.\n\nRequired: Project Name, Product Name, Creator.",
                        "Required Fields",
                        JOptionPane.ERROR_MESSAGE
                );
                continue;
            }

            project = ProjectModel.newEmpty(pn, cr, pr, commentsArea.getText());
            project.language = project.language;
            project.fpPanes = new ArrayList<>();
            project.ucpPanes = new ArrayList<>();

            currentFile = null;
            tabs.removeAll();
            refreshTitle();
            return;
        }
    }

    private void chooseLanguage() {
        LanguageDialog dlg = new LanguageDialog(this, project.language);
        dlg.setVisible(true);

        if (dlg.getSelectedLanguage() != null) {
            project.language = dlg.getSelectedLanguage();

            for (int i = 0; i < tabs.getTabCount(); i++) {
                Component c = tabs.getComponentAt(i);
                if (c instanceof FunctionPointsPanel fpp) {
                    fpp.setCurrentLanguage(project.language);
                }
            }
        }
    }

    private void addFunctionPointsTabAskName() {
        String name = JOptionPane.showInputDialog(
                this,
                "Name the panel:",
                "Enter FP Data",
                JOptionPane.PLAIN_MESSAGE
        );

        if (name == null) return;
        name = name.trim();
        if (name.isBlank()) {
            JOptionPane.showMessageDialog(this, "Panel name is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        addFunctionPointsTab(name, null);
    }

    private void addFunctionPointsTab(String tabName, ProjectModel.FPState stateOrNull) {
        FunctionPointsPanel panel = new FunctionPointsPanel(
                this,
                project.language,
                (ignored) -> { }
        );

        if (stateOrNull != null) {
            panel.loadFromState(stateOrNull);
        } else {
            panel.resetToBlankDefaults(project.language);
        }

        tabs.addTab(tabName, panel);
        tabs.setSelectedComponent(panel);
    }

    private void addUseCasePointsTabAskName() {
        String name = JOptionPane.showInputDialog(
                this,
                "Name the window:",
                "Use Case Points",
                JOptionPane.PLAIN_MESSAGE
        );

        if (name == null) return;
        name = name.trim();
        if (name.isBlank()) {
            JOptionPane.showMessageDialog(this, "Window name is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        addUseCasePointsTab(name, null);
    }

    private void addUseCasePointsTab(String tabName, ProjectModel.UCPState stateOrNull) {
        UCPPanel panel = new UCPPanel(this);
        if (stateOrNull != null) {
            panel.loadFromState(stateOrNull);
        }
        tabs.addTab(tabName, panel);
        tabs.setSelectedComponent(panel);
    }

    private void saveProject() {
        project.fpPanes = new ArrayList<>();
        project.ucpPanes = new ArrayList<>();

        for (int i = 0; i < tabs.getTabCount(); i++) {
            Component c = tabs.getComponentAt(i);
            if (c instanceof FunctionPointsPanel fpp) {
                ProjectModel.FPPaneEntry entry = new ProjectModel.FPPaneEntry();
                entry.tabName = tabs.getTitleAt(i);
                entry.state = fpp.exportState();
                project.fpPanes.add(entry);
            } else if (c instanceof UCPPanel ucpPanel) {
                ProjectModel.UCPPaneEntry entry = new ProjectModel.UCPPaneEntry();
                entry.tabName = tabs.getTitleAt(i);
                entry.state = ucpPanel.exportState();
                project.ucpPanes.add(entry);
            }
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Project");
        chooser.setSelectedFile(currentFile != null ? currentFile : new File(project.projectName + ".ms"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File f = chooser.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".ms")) {
            f = new File(f.getParentFile(), f.getName() + ".ms");
        }

        try {
            String json = project.toJson();
            java.nio.file.Files.writeString(f.toPath(), json);
            currentFile = f;
            JOptionPane.showMessageDialog(this, "Saved: " + f.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Project (.ms)");

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File f = chooser.getSelectedFile();
        try {
            String json = java.nio.file.Files.readString(f.toPath());
            project = ProjectModel.fromJson(json);
            currentFile = f;

            tabs.removeAll();
            refreshTitle();

            if (project.fpPanes != null) {
                for (ProjectModel.FPPaneEntry e : project.fpPanes) {
                    addFunctionPointsTab(e.tabName, e.state);
                }
            }

            if (project.ucpPanes != null) {
                for (ProjectModel.UCPPaneEntry e : project.ucpPanes) {
                    addUseCasePointsTab(e.tabName, e.state);
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Open failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
