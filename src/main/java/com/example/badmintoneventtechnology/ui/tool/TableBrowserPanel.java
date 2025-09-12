package com.example.badmintoneventtechnology.ui.tool;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class TableBrowserPanel extends JPanel {
    private final JLabel lblConnStatus = new JLabel("Not connected");
    private final JComboBox<String> cboTables = new JComboBox<>();
    private final JTextField txtLimit = new JTextField("500");
    private final JTextField txtFilter = new JTextField("");
    private final JTable tblData = new JTable();
    private final DefaultListModel<String> listColumnsModel = new DefaultListModel<>();
    private final JList<String> listColumns = new JList<>(listColumnsModel);

    private Connection conn;

    public TableBrowserPanel() {
        super(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Toolbar
        JPanel bar = new JPanel(new BorderLayout(6, 6));

        // Hàng 1: Status
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(new JLabel("Status:"));
        row1.add(lblConnStatus);

        // Hàng 2: các control còn lại
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(new JLabel("Table (PUBLIC):"));
        cboTables.setPreferredSize(new Dimension(280, 26)); // cho combo rộng dễ nhìn
        row2.add(cboTables);

        row2.add(new JLabel("Limit:"));
        txtLimit.setColumns(6);
        row2.add(txtLimit);

        row2.add(new JLabel("WHERE:"));
        txtFilter.setPreferredSize(new Dimension(260, 26));
        row2.add(txtFilter);

        JButton btnLoad = new JButton("Load");
        btnLoad.addActionListener(e -> loadTable());
        row2.add(btnLoad);

        // ghép 2 hàng vào thanh bar
        bar.add(row1, BorderLayout.NORTH);
        bar.add(row2, BorderLayout.CENTER);

        add(bar, BorderLayout.NORTH);

        // Sidebar cột
        listColumns.setVisibleRowCount(12);
        JScrollPane left = new JScrollPane(listColumns);
        left.setPreferredSize(new Dimension(220, 400));
        left.setBorder(BorderFactory.createTitledBorder("Columns"));

        // Bảng dữ liệu
        tblData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane center = new JScrollPane(tblData);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, center);
        split.setResizeWeight(0.0);
        add(split, BorderLayout.CENTER);

        cboTables.addActionListener(e -> refreshColumnsList());
    }

    public void setConnection(Connection connection, String host, String port) {
        this.conn = connection;
        lblConnStatus.setText(conn != null ? ("Connected to " + host + ":" + port) : "Not connected");
        fillTablesCombo();
    }

    /** Liệt kê TẤT CẢ TABLE/VIEW thuộc schema PUBLIC */
    private void fillTablesCombo() {
        cboTables.removeAllItems();
        if (conn == null)
            return;

        try {
            DatabaseMetaData md = conn.getMetaData();
            java.util.List<String> names = new ArrayList<>();

            // 1) Ưu tiên meta-data với schemaPattern = "PUBLIC"
            try (ResultSet rs = md.getTables(null, "PUBLIC", "%", new String[] { "TABLE", "VIEW" })) {
                while (rs.next()) {
                    String schema = rs.getString("TABLE_SCHEM"); // "PUBLIC"
                    String name = rs.getString("TABLE_NAME");
                    if (name != null)
                        names.add(schema + "." + name);
                }
            }

            // 2) Fallback: đọc INFORMATION_SCHEMA.TABLES nếu danh sách rỗng
            if (names.isEmpty()) {
                String sql = "SELECT TABLE_SCHEMA, TABLE_NAME " +
                        "FROM INFORMATION_SCHEMA.TABLES " +
                        "WHERE UPPER(TABLE_SCHEMA)='PUBLIC' " +
                        "ORDER BY TABLE_NAME";
                try (Statement st = conn.createStatement();
                        ResultSet rs = st.executeQuery(sql)) {
                    while (rs.next()) {
                        names.add(rs.getString(1) + "." + rs.getString(2));
                    }
                }
            }

            names.sort(String::compareToIgnoreCase);
            for (String n : names)
                cboTables.addItem(n);

            if (cboTables.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy bảng nào trong schema PUBLIC.",
                        "No tables", JOptionPane.WARNING_MESSAGE);
            } else {
                cboTables.setSelectedIndex(0);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "List tables failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        refreshColumnsList();
    }

    /** Hiển thị danh sách cột của bảng đã chọn (PUBLIC.<name>) */
    private void refreshColumnsList() {
        listColumnsModel.clear();
        String table = (String) cboTables.getSelectedItem();
        if (table == null || conn == null)
            return;

        String[] parts = table.split("\\.", 2);
        String schema = (parts.length == 2) ? parts[0] : "PUBLIC";
        String name = (parts.length == 2) ? parts[1] : table;

        try {
            DatabaseMetaData md = conn.getMetaData();
            try (ResultSet rs = md.getColumns(null, schema, name, "%")) {
                while (rs.next()) {
                    String col = rs.getString("COLUMN_NAME");
                    String type = rs.getString("TYPE_NAME");
                    String badge = (type != null && type.matches("(?i).*(INT|DEC|NUM|REAL|DOUBLE|FLOAT).*"))
                            ? "123"
                            : "A-Z";
                    listColumnsModel.addElement(badge + "  " + col);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "List columns failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Load dữ liệu với WHERE optional và LIMIT */
    private void loadTable() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Please connect first.", "Not connected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String table = (String) cboTables.getSelectedItem();
        if (table == null) {
            JOptionPane.showMessageDialog(this, "No table selected.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int limit = 500;
        try {
            limit = Integer.parseInt(txtLimit.getText().trim());
        } catch (NumberFormatException ignored) {
        }

        String where = txtFilter.getText().trim();
        String sql = "SELECT * FROM " + table + (where.isEmpty() ? "" : " WHERE " + where) + " LIMIT " + limit;

        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            tblData.setModel(toTableModel(rs));
            autoResizeColumns(tblData);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static DefaultTableModel toTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        String[] names = new String[cols];
        for (int i = 0; i < cols; i++)
            names[i] = md.getColumnLabel(i + 1);
        java.util.List<Object[]> data = new java.util.ArrayList<>();
        while (rs.next()) {
            Object[] row = new Object[cols];
            for (int i = 0; i < cols; i++)
                row[i] = rs.getObject(i + 1);
            data.add(row);
        }
        DefaultTableModel model = new DefaultTableModel(names, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (Object[] r : data)
            model.addRow(r);
        return model;
    }

    private static void autoResizeColumns(JTable table) {
        for (int col = 0; col < table.getColumnCount(); col++) {
            int width = 80;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, col);
                Component comp = table.prepareRenderer(renderer, row, col);
                width = Math.max(width, comp.getPreferredSize().width + 12);
            }
            table.getColumnModel().getColumn(col).setPreferredWidth(Math.min(width, 400));
        }
    }
}
