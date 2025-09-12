package com.example.badmintoneventtechnology.ui.tool;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

public class ConnectionsManagerPanel extends JPanel {
    private Connection conn;

    // --- UI: Sessions ---
    private final JTable tblSessions = new JTable();
    private final JLabel lblHint = new JLabel(" ");
    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnKill = new JButton("Kill Session…");
    private final JCheckBox chkAuto = new JCheckBox("Auto-refresh (2s)");
    private final Timer autoTimer;

    // --- UI: Whitelist ---
    private final DefaultTableModel wlModel = new DefaultTableModel(new Object[] { "IP", "Note" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return true;
        }
    };
    private final JTable wlTable = new JTable(wlModel);
    private final JButton btnWLAdd = new JButton("Add");
    private final JButton btnWLDel = new JButton("Remove");
    private final JButton btnWLReload = new JButton("Reload from DB");
    private final JButton btnWLSave = new JButton("Save to DB");

    public ConnectionsManagerPanel() {
        super(new BorderLayout(8, 8));

        // --- Sessions panel ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.add(btnRefresh);
        topBar.add(btnKill);
        topBar.add(chkAuto);

        JScrollPane spSessions = new JScrollPane(tblSessions);
        JPanel sesPanel = new JPanel(new BorderLayout(6, 6));
        sesPanel.add(topBar, BorderLayout.NORTH);
        sesPanel.add(spSessions, BorderLayout.CENTER);
        sesPanel.add(lblHint, BorderLayout.SOUTH);

        // --- Whitelist panel ---
        JPanel wlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        wlBar.add(btnWLAdd);
        wlBar.add(btnWLDel);
        wlBar.add(btnWLReload);
        wlBar.add(btnWLSave);

        JScrollPane spWL = new JScrollPane(wlTable);
        JPanel wlPanel = new JPanel(new BorderLayout(6, 6));
        wlPanel.add(wlBar, BorderLayout.NORTH);
        wlPanel.add(spWL, BorderLayout.CENTER);

        // --- Tabs ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Sessions", sesPanel);
        tabs.addTab("Whitelist", wlPanel);
        add(tabs, BorderLayout.CENTER);

        // --- Actions ---
        btnRefresh.addActionListener(e -> loadSessions());
        btnKill.addActionListener(e -> killSessionDialog());

        btnWLAdd.addActionListener(e -> wlModel.addRow(new Object[] { "", "" }));
        btnWLDel.addActionListener(e -> {
            int r = wlTable.getSelectedRow();
            if (r >= 0)
                wlModel.removeRow(r);
        });
        btnWLReload.addActionListener(e -> loadWhitelist());
        btnWLSave.addActionListener(e -> saveWhitelist());

        autoTimer = new Timer(2000, e -> loadSessions());
        chkAuto.addActionListener(e -> {
            if (chkAuto.isSelected())
                autoTimer.start();
            else
                autoTimer.stop();
        });
    }

    /** H2Client gọi sau khi connect */
    public void setConnection(Connection connection) {
        this.conn = connection;
        loadSessions();
        loadWhitelist();
    }

    // -------------------- SESSIONS --------------------

    private void loadSessions() {
        if (conn == null) {
            setTableEmpty(tblSessions, "Not connected");
            return;
        }
        // MySQL: Sử dụng SHOW PROCESSLIST để xem các kết nối
        String sql = "SHOW PROCESSLIST";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            tblSessions.setModel(toModel(rs));
            lblHint.setText("MySQL Process List - Shows active connections");
        } catch (SQLException ex) {
            setTableEmpty(tblSessions, "Cannot read MySQL processlist: " + ex.getMessage());
        }
    }

    private void killSessionDialog() {
        if (conn == null)
            return;
        String id = JOptionPane.showInputDialog(this,
                "Enter CONNECTION ID to kill:",
                "Kill Connection", JOptionPane.QUESTION_MESSAGE);
        if (id == null || id.isBlank())
            return;

        // MySQL: KILL CONNECTION <id>
        try (Statement st = conn.createStatement()) {
            st.execute("KILL CONNECTION " + id.trim());
            JOptionPane.showMessageDialog(this, "Requested to kill connection " + id);
            loadSessions();
            return;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Kill connection failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------- WHITELIST (sổ tay IP) --------------------

    private void ensureWhitelistTable() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                        CREATE TABLE IF NOT EXISTS ALLOWED_CLIENTS(
                            IP   VARCHAR(100) PRIMARY KEY,
                            NOTE VARCHAR(255)
                        )
                    """);
        }
    }

    private void loadWhitelist() {
        wlModel.setRowCount(0);
        if (conn == null)
            return;
        try {
            ensureWhitelistTable();
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("SELECT IP, NOTE FROM ALLOWED_CLIENTS ORDER BY IP")) {
                while (rs.next()) {
                    wlModel.addRow(new Object[] { rs.getString(1), rs.getString(2) });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Load whitelist failed: " + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveWhitelist() {
        if (conn == null)
            return;
        try {
            ensureWhitelistTable();
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.execute("TRUNCATE TABLE ALLOWED_CLIENTS");
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO ALLOWED_CLIENTS(IP, NOTE) VALUES(?, ?)")) {
                for (int r = 0; r < wlModel.getRowCount(); r++) {
                    String ip = val(wlModel.getValueAt(r, 0));
                    String note = val(wlModel.getValueAt(r, 1));
                    if (!ip.isBlank()) {
                        ps.setString(1, ip);
                        ps.setString(2, note);
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
            conn.commit();
            JOptionPane.showMessageDialog(this, "Saved.");
        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (Exception ignore) {
            }
            JOptionPane.showMessageDialog(this,
                    "Save whitelist failed: " + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (Exception ignore) {
            }
        }
    }

    // -------------------- helpers --------------------

    private static String val(Object o) {
        return o == null ? "" : o.toString().trim();
    }

    private static void setTableEmpty(JTable t, String msg) {
        DefaultTableModel m = new DefaultTableModel(new Object[] { "Info" }, 0);
        m.addRow(new Object[] { msg });
        t.setModel(m);
    }

    private static DefaultTableModel toModel(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        Vector<String> colNames = new Vector<>();
        for (int i = 1; i <= cols; i++)
            colNames.add(md.getColumnLabel(i));

        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>(cols);
            for (int i = 1; i <= cols; i++)
                row.add(rs.getObject(i));
            data.add(row);
        }
        return new DefaultTableModel(data, colNames) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    private static String hintFromColumns(JTable t) {
        List<String> cols = new ArrayList<>();
        for (int i = 0; i < t.getColumnCount(); i++)
            cols.add(t.getColumnName(i).toUpperCase());
        boolean hasAddr = cols.contains("HOST") || cols.contains("USER") || cols.contains("DB");
        StringBuilder sb = new StringBuilder("Columns: ").append(String.join(", ", cols));
        if (hasAddr)
            sb.append("  |  MySQL connection info detected.");
        else
            sb.append("  |  MySQL processlist columns.");
        return sb.toString();
    }
}
