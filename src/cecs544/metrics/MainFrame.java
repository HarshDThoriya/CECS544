package cecs544.metrics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

public class MainFrame extends JFrame {

    private final JTabbedPane tabs = new JTabbedPane();
    private ProjectModel project = ProjectModel.newEmpty("Untitled", "", "", "");
    private File currentFile = null;

    private boolean projectCreated = false;
    private boolean dirty = false;

    private SMIPanel smiPanel = null;

    private JMenuItem fpEnterItem;
    private JMenuItem ucpEnterItem;
    private JMenuItem smiEnterItem;

    public MainFrame() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setSize(980, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);

        setJMenuBar(buildMenuBar());
        refreshTitle();
        updateMenuEnabledState();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptExit();
            }
        });
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("File");
        JMenuItem mNew = new JMenuItem("New");
        JMenuItem mOpen = new JMenuItem("Open");
        JMenuItem mSave = new JMenuItem("Save");
        JMenuItem mExit = new JMenuItem("Exit");

        mNew.addActionListener(e -> attemptNewProject());
        mOpen.addActionListener(e -> attemptOpenProject());
        mSave.addActionListener(e -> {
            if (!projectCreated) return;
            boolean ok = saveProject();
            if (ok) dirty = false;
        });
        mExit.addActionListener(e -> attemptExit());

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
        fpEnterItem = new JMenuItem("Enter FP Data");
        fpEnterItem.addActionListener(e -> addFunctionPointsTabAskName());
        fp.add(fpEnterItem);

        JMenu ucp = new JMenu("Use Case Points");
        ucpEnterItem = new JMenuItem("Open UCP Panel");
        ucpEnterItem.addActionListener(e -> addUseCasePointsTabAskName());
        ucp.add(ucpEnterItem);

        JMenu smi = new JMenu("Software Maturity Index");
        smiEnterItem = new JMenuItem("Open SMI Panel");
        smiEnterItem.addActionListener(e -> openSmiPanel());
        smi.add(smiEnterItem);

        metrics.add(fp);
        metrics.add(ucp);
        metrics.add(smi);

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

    private void updateMenuEnabledState() {
        boolean enable = projectCreated;
        if (fpEnterItem != null) fpEnterItem.setEnabled(enable);
        if (ucpEnterItem != null) ucpEnterItem.setEnabled(enable);
        if (smiEnterItem != null) smiEnterItem.setEnabled(enable);
    }

    // ---------- Save/Discard guards ----------
    private void attemptExit() {
        if (!projectCreated || !dirty) {
            dispose();
            return;
        }

        int choice = JOptionPane.showOptionDialog(
                this,
                "You have unsaved changes.\n\nSave changes before exiting?",
                "Unsaved Changes",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new Object[]{"Save", "Discard Changes", "Cancel"},
                "Save"
        );

        if (choice == 0) {
            boolean ok = saveProject();
            if (ok) {
                dirty = false;
                dispose();
            }
        } else if (choice == 1) {
            dispose();
        }
    }

    private void attemptNewProject() {
        if (!projectCreated || !dirty) {
            newProject();
            return;
        }

        int choice = JOptionPane.showOptionDialog(
                this,
                "You have unsaved changes.\n\nSave changes before creating a new project?",
                "Unsaved Changes",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new Object[]{"Save", "Discard Changes", "Cancel"},
                "Save"
        );

        if (choice == 0) {
            boolean ok = saveProject();
            if (ok) {
                dirty = false;
                newProject();
            }
        } else if (choice == 1) {
            newProject();
        }
    }

    private void attemptOpenProject() {
        if (!projectCreated || !dirty) {
            openProject();
            return;
        }

        int choice = JOptionPane.showOptionDialog(
                this,
                "You have unsaved changes.\n\nSave changes before opening another project?",
                "Unsaved Changes",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new Object[]{"Save", "Discard Changes", "Cancel"},
                "Save"
        );

        if (choice == 0) {
            boolean ok = saveProject();
            if (ok) {
                dirty = false;
                openProject();
            }
        } else if (choice == 1) {
            openProject();
        }
    }

    // ---------- Project ----------
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
            project.fpPanes = new ArrayList<>();
            project.ucpPanes = new ArrayList<>();
            project.smiOpen = false;
            project.smiState = null;

            currentFile = null;

            tabs.removeAll();
            smiPanel = null;

            projectCreated = true;
            dirty = false;
            refreshTitle();
            updateMenuEnabledState();
            return;
        }
    }

    private void chooseLanguage() {
        if (!projectCreated) return;

        LanguageDialog dlg = new LanguageDialog(this, project.language);
        dlg.setVisible(true);

        if (dlg.getSelectedLanguage() != null) {
            project.language = dlg.getSelectedLanguage();
            dirty = true;

            for (int i = 0; i < tabs.getTabCount(); i++) {
                Component c = tabs.getComponentAt(i);
                if (c instanceof FunctionPointsPanel fpp) {
                    fpp.setCurrentLanguage(project.language);
                }
            }
        }
    }

    // ---------- FP ----------
    private void addFunctionPointsTabAskName() {
        if (!projectCreated) return;

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
        dirty = true;
    }

    private void addFunctionPointsTab(String tabName, ProjectModel.FPState stateOrNull) {
        FunctionPointsPanel panel = new FunctionPointsPanel(
                this,
                project.language,
                (st) -> dirty = true
        );

        if (stateOrNull != null) {
            panel.loadFromState(stateOrNull);
        } else {
            panel.resetToBlankDefaults(project.language);
        }

        tabs.addTab(tabName, panel);
        tabs.setSelectedComponent(panel);
    }

    // ---------- UCP ----------
    private void addUseCasePointsTabAskName() {
        if (!projectCreated) return;

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
        dirty = true;
    }

    private void addUseCasePointsTab(String tabName, ProjectModel.UCPState stateOrNull) {
        UCPPanel panel = new UCPPanel(this, (st) -> dirty = true);
        if (stateOrNull != null) panel.loadFromState(stateOrNull);

        tabs.addTab(tabName, panel);
        tabs.setSelectedComponent(panel);
    }

    // ---------- SMI (matches screenshot: one tab named SMI) ----------
    private void openSmiPanel() {
        if (!projectCreated) return;

        if (smiPanel != null) {
            tabs.setSelectedComponent(smiPanel);
            return;
        }

        smiPanel = new SMIPanel(
                (st) -> {
                    project.smiState = st;
                    dirty = true;
                },
                this::closeSmiPanel
        );

        if (project.smiState != null) {
            smiPanel.loadFromState(project.smiState);
        }

        tabs.addTab("SMI", smiPanel);
        tabs.setSelectedComponent(smiPanel);

        project.smiOpen = true;
        dirty = true;
    }

    private void closeSmiPanel() {
        if (smiPanel == null) return;

        project.smiState = smiPanel.exportState();
        project.smiOpen = false;
        dirty = true;

        tabs.remove(smiPanel);
        smiPanel = null;
    }

    // ---------- Save/Open ----------
    private boolean saveProject() {
        if (!projectCreated) return false;

        project.fpPanes = new ArrayList<>();
        project.ucpPanes = new ArrayList<>();

        // collect open tabs
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
            } else if (c instanceof SMIPanel sp) {
                project.smiState = sp.exportState();
                project.smiOpen = true;
            }
        }

        // if SMI is closed, still save its last known state
        if (smiPanel == null) {
            project.smiOpen = false;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Project");
        chooser.setSelectedFile(currentFile != null ? currentFile : new File(project.projectName + ".ms"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return false;

        File f = chooser.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".ms")) {
            f = new File(f.getParentFile(), f.getName() + ".ms");
        }

        try {
            String json = project.toJson();
            java.nio.file.Files.writeString(f.toPath(), json);
            currentFile = f;

            JOptionPane.showMessageDialog(this, "Saved: " + f.getAbsolutePath());
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
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
            smiPanel = null;

            projectCreated = true;
            dirty = false;

            refreshTitle();
            updateMenuEnabledState();

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

            // Only reopen if it was open at save time
            if (project.smiOpen) {
                openSmiPanel();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Open failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}