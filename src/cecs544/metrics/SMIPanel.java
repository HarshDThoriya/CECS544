package cecs544.metrics;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SMIPanel extends JPanel {

    // Match screenshot: SMI | Added | Changed | Deleted | Total
    public static class Row {
        public String smiText = "";        // computed display
        public Integer added = null;       // editable
        public Integer changed = null;     // editable
        public Integer deleted = null;     // editable
        public int total = 0;              // computed
    }

    private final JTable table;
    private final SmiTableModel model;

    // baseline total before first row (screenshots behave like this is 0)
    private int startingTotal = 0;

    private final Consumer<ProjectModel.SMIState> onStateChanged;
    private final Runnable onCloseRequested;

    public SMIPanel(Consumer<ProjectModel.SMIState> onStateChanged, Runnable onCloseRequested) {
        this.onStateChanged = onStateChanged;
        this.onCloseRequested = onCloseRequested;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Title centered like screenshot
        JLabel title = new JLabel("Software Maturity Index", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize() + 2f));
        add(title, BorderLayout.NORTH);

        model = new SmiTableModel();
        table = new JTable(model);
        table.setRowHeight(22);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton addRowBtn = new JButton("Add Row");
        JButton computeBtn = new JButton("Compute Index");

        addRowBtn.addActionListener(e -> {
            addRow();
            fireStateChanged();
        });

        computeBtn.addActionListener(e -> {
            recomputeAll();
            fireStateChanged();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        bottom.add(addRowBtn);
        bottom.add(computeBtn);

        // optional close button (not in screenshot, but helps “closed/open” requirement)
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> {
            recomputeAll();
            fireStateChanged();
            if (onCloseRequested != null) onCloseRequested.run();
        });
        bottom.add(Box.createHorizontalStrut(30));
        bottom.add(closeBtn);

        add(bottom, BorderLayout.SOUTH);
    }

    private void addRow() {
        Row r = new Row();
        // screenshot row appears blank; keep nulls so table shows blank cells
        model.rows.add(r);
        model.fireTableDataChanged();
    }

    /**
     * Recompute totals and SMI for EVERY row.
     * Total rule from screenshot:
     *   total[i] = total[i-1] + added[i] - deleted[i]
     * Changed does NOT affect total.
     *
     * SMI rule:
     *   SMI = (Total - (Added + Changed + Deleted)) / Total
     * computed per row and displayed in col 0.
     */
    private void recomputeAll() {
        int prevTotal = startingTotal;

        for (Row r : model.rows) {
            int a = nz(r.added);
            int c = nz(r.changed);
            int d = nz(r.deleted);

            int total = prevTotal + a - d;
            if (total < 0) total = 0;
            r.total = total;

            // SMI
            if (total <= 0) {
                r.smiText = "0.0";
            } else {
                BigDecimal numerator = BigDecimal.valueOf(total - (a + c + d));
                BigDecimal denom = BigDecimal.valueOf(total);

                // scale 15 matches screenshot precision like 0.717391304347826 (33/46)
                BigDecimal smi = numerator.divide(denom, 15, RoundingMode.HALF_UP)
                        .stripTrailingZeros();

                r.smiText = formatSmiText(smi);
            }

            prevTotal = total;
        }

        model.fireTableDataChanged();
    }

    private static int nz(Integer v) {
        return (v == null) ? 0 : Math.max(0, v);
    }

    private static String formatSmiText(BigDecimal smi) {
        // Ensure at least one decimal place (screenshot shows "0.0")
        // stripTrailingZeros might return "0" or an integer.
        String s = smi.toPlainString();
        if (!s.contains(".")) s = s + ".0";
        return s;
    }

    private void fireStateChanged() {
        if (onStateChanged != null) onStateChanged.accept(exportState());
    }

    // ---------------- Save / Load ----------------
    public ProjectModel.SMIState exportState() {
        ProjectModel.SMIState st = new ProjectModel.SMIState();
        st.startingTotal = startingTotal;
        st.rows = new ArrayList<>();

        for (Row r : model.rows) {
            ProjectModel.SMIRow rr = new ProjectModel.SMIRow();
            rr.smiText = r.smiText;
            rr.added = r.added;
            rr.changed = r.changed;
            rr.deleted = r.deleted;
            rr.total = r.total;
            st.rows.add(rr);
        }
        return st;
    }

    public void loadFromState(ProjectModel.SMIState st) {
        model.rows.clear();

        if (st == null) {
            startingTotal = 0;
            model.fireTableDataChanged();
            return;
        }

        startingTotal = Math.max(0, st.startingTotal);

        if (st.rows != null) {
            for (ProjectModel.SMIRow rr : st.rows) {
                Row r = new Row();
                r.smiText = rr.smiText == null ? "" : rr.smiText;
                r.added = rr.added;
                r.changed = rr.changed;
                r.deleted = rr.deleted;
                r.total = rr.total;
                model.rows.add(r);
            }
        }

        // enforce totals + smi computed like spec
        recomputeAll();
    }

    // ---------------- Table Model ----------------
    private class SmiTableModel extends AbstractTableModel {

        private final String[] COLS = {
                "SMI",
                "Modules Added",
                "Modules Changed",
                "Modules Deleted",
                "Total Modules"
        };

        private final List<Row> rows = new ArrayList<>();

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int column) { return COLS[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Row r = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> r.smiText;              // computed
                case 1 -> (r.added == null ? "" : r.added);
                case 2 -> (r.changed == null ? "" : r.changed);
                case 3 -> (r.deleted == null ? "" : r.deleted);
                case 4 -> r.total;                // computed
                default -> "";
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // Only Added/Changed/Deleted editable
            return columnIndex == 1 || columnIndex == 2 || columnIndex == 3;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Row r = rows.get(rowIndex);

            Integer v = parseNullableNonNegInt(aValue);

            switch (columnIndex) {
                case 1 -> r.added = v;
                case 2 -> r.changed = v;
                case 3 -> r.deleted = v;
                default -> { }
            }

            recomputeAll();
            fireStateChanged();
        }

        private Integer parseNullableNonNegInt(Object o) {
            if (o == null) return null;
            String s = o.toString().trim().replace(",", "");
            if (s.isEmpty()) return null;
            try {
                int v = Integer.parseInt(s);
                return Math.max(0, v);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}