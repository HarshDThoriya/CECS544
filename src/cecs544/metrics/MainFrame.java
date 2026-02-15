package cecs544.metrics;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainFrame extends JFrame {

    private final JTabbedPane tabs = new JTabbedPane();
    private ProjectModel project = ProjectModel.newEmpty("Untitled", "Unknown");
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

        // File
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

        // Edit (placeholder)
        JMenu edit = new JMenu("Edit");
        edit.add(new JMenuItem("(not used in Iteration 1)"));

        // Preferences
        JMenu prefs = new JMenu("Preferences");
        JMenuItem mLang = new JMenuItem("Language");
        mLang.addActionListener(e -> chooseLanguage());
        prefs.add(mLang);

        // Metrics
        JMenu metrics = new JMenu("Metrics");
        JMenu fp = new JMenu("Function Points");
        JMenuItem enterFp = new JMenuItem("Enter FP Data");
        enterFp.addActionListener(e -> addFunctionPointsTab());
        fp.add(enterFp);
        metrics.add(fp);

        // Help (placeholder)
        JMenu help = new JMenu("Help");
        help.add(new JMenuItem("(not used in Iteration 1)"));

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
        JTextField projectName = new JTextField("Untitled");
        JTextField creator = new JTextField("Unknown");

        JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
        p.add(new JLabel("Project Name:"));
        p.add(projectName);
        p.add(new JLabel("Creator Name:"));
        p.add(creator);

        int ok = JOptionPane.showConfirmDialog(
                this, p, "New Project", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (ok != JOptionPane.OK_OPTION) return;

        project = ProjectModel.newEmpty(projectName.getText().trim(), creator.getText().trim());
        currentFile = null;

        tabs.removeAll();
        refreshTitle();
    }

    private void chooseLanguage() {
        LanguageDialog dlg = new LanguageDialog(this, project.language);
        dlg.setVisible(true);

        if (dlg.getSelectedLanguage() != null) {
            project.language = dlg.getSelectedLanguage();
            // update any open FP panels so labels refresh
            for (int i = 0; i < tabs.getTabCount(); i++) {
                Component c = tabs.getComponentAt(i);
                if (c instanceof FunctionPointsPanel fpp) {
                    fpp.setCurrentLanguage(project.language);
                }
            }
        }
    }

    private void addFunctionPointsTab() {
        FunctionPointsPanel panel = new FunctionPointsPanel(
                this,
                project.language,
                (fpState) -> project.fpState = fpState // callback to keep model updated
        );

        // if project already has fpState (from open), push it into the new panel
        if (project.fpState != null) {
            panel.loadFromState(project.fpState);
        }

        tabs.addTab("Function Points", panel);
        tabs.setSelectedComponent(panel);
    }

    private void saveProject() {
        // capture fp state from selected fp panel if any
        FunctionPointsPanel fpp = getAnyFpPanel();
        if (fpp != null) {
            project.fpState = fpp.exportState();
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

            // On open, create an FP tab and populate it (as spec says)
            addFunctionPointsTab();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Open failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private FunctionPointsPanel getAnyFpPanel() {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            Component c = tabs.getComponentAt(i);
            if (c instanceof FunctionPointsPanel fpp) return fpp;
        }
        return null;
    }
}
