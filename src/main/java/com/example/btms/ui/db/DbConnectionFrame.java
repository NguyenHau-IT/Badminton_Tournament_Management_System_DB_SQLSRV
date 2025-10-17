package com.example.btms.ui.db;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.example.btms.config.Prefs;
import com.example.btms.ui.db.DbConnectionSelection.DbType;
import com.example.btms.ui.db.DbConnectionSelection.Mode;
import com.example.btms.util.h2db.H2ScriptUtil;

public class DbConnectionFrame extends JFrame {

    // ==== UI controls ====
    private final JRadioButton rbLocalLan = new JRadioButton("Sử dụng local / mạng LAN");
    private final JRadioButton rbInitNew = new JRadioButton("Sử dụng tích hợp");
    private final JRadioButton rbOnline = new JRadioButton("Sử dụng Online");
    private final JRadioButton rbImport = new JRadioButton("Nhập từ bản sao lưu");

    private final JComboBox<DbType> cbDbType = new JComboBox<>();
    private final JTextField txtDbName = new JTextField(22);
    private final JComboBox<String> cbExisting = new JComboBox<>();
    private final JTextField txtServer = new JTextField(22);
    private final JTextField txtPort = new JTextField(8);
    private final JTextField txtUser = new JTextField(22);
    private final JPasswordField txtPass = new JPasswordField(22);
    private final JCheckBox chkRemember = new JCheckBox("Ghi nhớ thông tin kết nối");

    private DbConnectionSelection result;
    private Consumer<DbConnectionSelection> onOk;
    private Runnable onCancel;

    // ==== H2 URL options (THÊM) ====
    private static final String H2_BASE_OPTS = ";DATABASE_TO_UPPER=FALSE;SCHEMA_SEARCH_PATH=PUBLIC;INIT=SET SCHEMA PUBLIC";
    private static final String H2_REQUIRE_EXISTS = ";IFEXISTS=TRUE";

    public DbConnectionFrame() {
        super("Database Connection Setup");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 8));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 12, 12, 12));

        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel();
        headerLabel.setHorizontalAlignment(JLabel.CENTER);
        headerLabel.setVerticalAlignment(JLabel.CENTER);
        var icon = loadBtmsIcon(110);
        if (icon != null) {
            headerLabel.setIcon(icon);
        } else {
            headerLabel.setText("Kết nối cơ sở dữ liệu");
            headerLabel.putClientProperty("FlatLaf.style", "font: bold +1");
        }
        header.add(headerLabel, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // ===== FORM =====
        JPanel form = new JPanel(new GridBagLayout());
        add(form, BorderLayout.CENTER);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 8, 0);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        // --- Group: Connection ---
        JPanel pnlConn = groupPanel("Connection");

        // 1) Mode
        {
            ButtonGroup grp = new ButtonGroup();
            JRadioButton[] radios = { rbLocalLan, rbInitNew, rbOnline, rbImport };
            for (JRadioButton r : radios)
                grp.add(r);
            rbLocalLan.setSelected(true);

            JPanel grid = new JPanel(new GridBagLayout());
            GridBagConstraints r = new GridBagConstraints();
            r.insets = new Insets(2, 6, 2, 6);
            r.anchor = GridBagConstraints.WEST;
            r.fill = GridBagConstraints.HORIZONTAL;
            r.weightx = 0.5;
            int cols = 2;
            for (int i = 0; i < radios.length; i++) {
                r.gridx = i % cols;
                r.gridy = i / cols;
                grid.add(radios[i], r);
            }
            pnlConn.add(grid);
        }

        // 2) Database Type
        pnlConn.add(rowOne(caption("Loại cơ sở dữ liệu"), comboDbType()));

        // 3) Database (name + list)
        pnlConn.add(rowTwo(caption("Cơ sở dữ liệu"), txtDbName, cbExisting, true));

        // 4) Server | Port
        pnlConn.add(rowTwo(caption("Máy chủ / Cổng"), txtServer, txtPort, false));

        // 5) Username | Password
        pnlConn.add(rowTwo(caption("Tài khoản / Mật khẩu"), txtUser, txtPass, false));

        // 6) Remember
        JPanel rememberRow = new JPanel(new GridBagLayout());
        GridBagConstraints rr = new GridBagConstraints();
        rr.gridx = 1;
        rr.gridy = 0;
        rr.weightx = 1;
        rr.anchor = GridBagConstraints.WEST;
        rr.insets = new Insets(2, 0, 0, 0);
        rememberRow.add(chkRemember, rr);
        pnlConn.add(rememberRow);

        form.add(pnlConn, gc);

        // separator
        gc.gridy++;
        form.add(new JSeparator(), gc);

        // ===== Footer =====
        JButton btnTest = new JButton("Kiểm tra kết nối");
        JButton btnOk = new JButton("Kết nối");
        JButton btnCancel = new JButton("Hủy");
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        footer.add(btnTest);
        footer.add(btnOk);
        footer.add(btnCancel);
        add(footer, BorderLayout.SOUTH);
        this.getRootPane().setDefaultButton(btnOk);

        // ===== polish nhỏ =====
        polishFields();

        // ===== Logic giữ nguyên =====
        loadExistingDbOptions();
        loadPrefs();

        cbDbType.addActionListener(e -> applyDbTypeBehavior());
        applyDbTypeBehavior();

        btnTest.addActionListener(e -> onTestConnection(btnTest, btnOk, btnCancel));
        btnOk.addActionListener(e -> onOk());
        btnCancel.addActionListener(e -> onCancel());

        SwingUtilities.invokeLater(() -> txtDbName.requestFocusInWindow());
        applyUiHints();

        pack();
        setSize(760, 560);
        setLocationRelativeTo(null); // JFrame: không có owner, căn giữa màn hình
    }

    /** Thuận tiện như .show()/.open() */
    public void open() {
        setVisible(true);
        toFront();
        requestFocus();
    }

    // ---------- Helpers bố cục ----------
    private JPanel rowOne(JLabel label, JComponent comp) {
        JPanel row = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 10);
        c.gridx = 0;
        c.gridy = 0;
        row.add(label, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        row.add(comp, c);
        return row;
    }

    private JPanel rowTwo(JLabel label, JComponent left, JComponent right, boolean rightCompact) {
        JPanel row = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 10);
        row.add(label, c);

        c.gridx = 1;
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 6);
        row.add(left, c);

        c.gridx = 2;
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 6, 0, 0);
        if (rightCompact && right instanceof JComboBox<?> comboAny) {
            @SuppressWarnings("unchecked")
            JComboBox<String> comboStr = (JComboBox<String>) comboAny;
            comboStr.setPrototypeDisplayValue("Local H2 Databases...");
        }
        row.add(right, c);

        return row;
    }

    private JComboBox<DbType> comboDbType() {
        cbDbType.setModel(new DefaultComboBoxModel<>(DbType.values()));
        return cbDbType;
    }

    private static JLabel caption(String text) {
        JLabel l = new JLabel(text);
        l.putClientProperty("FlatLaf.style", "font: small");
        return l;
    }

    private static JPanel groupPanel(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        Border inner = new EmptyBorder(8, 10, 8, 10);
        Border line = new LineBorder(UIManager.getColor("Component.borderColor"), 1, true);
        Border titled = (title == null) ? line
                : new TitledBorder(line, title, TitledBorder.LEFT, TitledBorder.TOP);
        p.setBorder(new CompoundBorder(titled, inner));
        return new SectionPanel(p);
    }

    private static class SectionPanel extends JPanel {
        private int nextRow = 0;

        SectionPanel(JPanel base) {
            super(new GridBagLayout());
            setBorder(base.getBorder());
            setOpaque(base.isOpaque());
        }

        @Override
        public Component add(Component comp) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = nextRow++;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(4, 0, 4, 0);
            super.add(comp, c);
            return comp;
        }
    }

    private void polishFields() {
        setPlaceholder(txtDbName, "badminton_tournament");
        setPlaceholder(txtServer, "localhost or 192.168.x.x");
        setPlaceholder(txtPort, "1433 / 9092");
        setPlaceholder(txtUser, "username");
        setPlaceholder(txtPass, "password");
        txtPort.setColumns(8);
    }

    private static void setPlaceholder(JComponent c, String text) {
        try {
            c.putClientProperty("JComponent.roundRect", Boolean.TRUE);
            c.putClientProperty("JTextComponent.placeholderText", text);
        } catch (Throwable ignore) {
        }
    }

    private static javax.swing.ImageIcon loadBtmsIcon(int maxHeight) {
        try {
            java.awt.image.BufferedImage img = null;
            java.net.URL res = DbConnectionFrame.class.getResource("/icons/btms.png");
            if (res != null)
                img = javax.imageio.ImageIO.read(res);
            if (img == null) {
                java.io.File f = new java.io.File("icons/btms.png");
                if (f.isFile())
                    img = javax.imageio.ImageIO.read(f);
            }
            if (img == null)
                return null;
            int h = Math.min(maxHeight, img.getHeight());
            double scale = h / (double) img.getHeight();
            int w = (int) Math.round(img.getWidth() * scale);
            java.awt.Image scaled = img.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
            return new javax.swing.ImageIcon(scaled);
        } catch (java.io.IOException ignore) {
            return null;
        }
    }

    // ==== API ====
    public void setOnOk(Consumer<DbConnectionSelection> onOk) {
        this.onOk = onOk;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public DbConnectionSelection getSelection() {
        return result;
    }

    // ==== Test connection: H2 KHÔNG tự tạo mới ====
    private void onTestConnection(JButton... toDisable) {
        setBusy(true, toDisable);
        new Thread(() -> {
            try {
                DbType type = (DbType) cbDbType.getSelectedItem();
                String dbName = safe(txtDbName.getText());
                String serverInput = safe(txtServer.getText());
                String server = resolveServer(serverInput);
                String port = safe(txtPort.getText());
                String user = safe(txtUser.getText());
                char[] pw = txtPass.getPassword();
                switch (type) {
                    case SQLSRV -> {
                        if (server.isBlank()) {
                            showErrorLater("Please enter Server.");
                            return;
                        }
                        StringBuilder url = new StringBuilder("jdbc:sqlserver://").append(server);
                        if (!port.isBlank())
                            url.append(":").append(port);
                        if (!dbName.isBlank())
                            url.append(";databaseName=").append(dbName);
                        url.append(";encrypt=true;trustServerCertificate=true;loginTimeout=5");
                        boolean useIntegrated = user.isBlank();
                        if (useIntegrated) {
                            url.append(";integratedSecurity=true");
                            DriverManager.getConnection(url.toString()).close();
                        } else {
                            DriverManager.getConnection(url.toString(), user, new String(pw)).close();
                        }
                        showInfoLater("SQL Server connection OK.");
                    }
                    case H2 -> {
                        if (dbName.isBlank()) {
                            showErrorLater("Please enter Database name (H2).");
                            return;
                        }
                        boolean isLocal = serverInput.isBlank()
                                || serverInput.equalsIgnoreCase("localhost")
                                || "127.0.0.1".equals(serverInput);
                        String url = isLocal
                                ? "jdbc:h2:file:./database/" + dbName + H2_BASE_OPTS + H2_REQUIRE_EXISTS
                                : "jdbc:h2:tcp://" + server + ":9092/./database/" + dbName + H2_BASE_OPTS
                                        + H2_REQUIRE_EXISTS;
                        DriverManager.getConnection(url, "sa", "").close();
                        showInfoLater("H2 connection OK.");
                    }
                    default -> showErrorLater("Unsupported DB type.");
                }
            } catch (java.sql.SQLException ex) {
                String m = ex.getMessage();
                if (m != null && m.toLowerCase().contains("not found")) {
                    showErrorLater("H2 database not found. Please create it via 'Use integrated' first.");
                } else {
                    showErrorLater("Connection failed: " + m);
                }
            } finally {
                setBusy(false, toDisable);
            }
        }, "db-test-conn").start();
    }

    private void setBusy(boolean busy, JButton... buttons) {
        try {
            setCursor(busy
                    ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR)
                    : java.awt.Cursor.getDefaultCursor());
        } catch (Exception ignore) {
        }
        if (buttons != null)
            for (JButton b : buttons)
                if (b != null)
                    b.setEnabled(!busy);
    }

    private void showInfoLater(String msg) {
        SwingUtilities
                .invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE));
    }

    private void showErrorLater(String msg) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE));
    }

    private void onOk() {
        if (rbInitNew.isSelected() && cbDbType.getSelectedItem() == DbType.H2) {
            String dbName = safe(txtDbName.getText());
            if (dbName.isBlank()) {
                JOptionPane.showMessageDialog(this, "Please enter Database name (H2).",
                        "Missing info", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (dbName.contains("/") || dbName.contains("\\")) {
                JOptionPane.showMessageDialog(this, "Database name must not contain / or \\.",
                        "Invalid name", JOptionPane.WARNING_MESSAGE);
                return;
            }

            setBusy(true);
            new Thread(() -> {
                try {
                    File dir = new File("database");
                    if (!dir.exists())
                        dir.mkdirs();
                    String fileUrl = "jdbc:h2:file:./database/" + dbName + H2_BASE_OPTS; // KHÔNG IFEXISTS ở chế độ tạo
                    DriverManager.getConnection(fileUrl, "sa", "").close();

                    try {
                        initH2DatabaseFromScript(dbName);
                    } catch (Exception initEx) {
                        showErrorLater("Init schema failed: " + initEx.getMessage());
                    }

                    SwingUtilities.invokeLater(() -> {
                        loadExistingDbOptions();
                        rbLocalLan.setSelected(true);
                        cbDbType.setSelectedItem(DbType.H2);
                        txtServer.setText("localhost");
                        JOptionPane.showMessageDialog(this,
                                "H2 database created. Switch to 'Use local / network' and press Connect.",
                                "Done", JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (java.sql.SQLException ex) {
                    showErrorLater("Cannot create H2 DB: " + ex.getMessage());
                } finally {
                    setBusy(false);
                }
            }, "h2-init-new").start();
            return;
        }

        DbConnectionSelection sel = new DbConnectionSelection();
        sel.setMode(rbLocalLan.isSelected() ? Mode.LOCAL_OR_LAN
                : rbInitNew.isSelected() ? Mode.INIT_NEW
                        : rbOnline.isSelected() ? Mode.ONLINE
                                : Mode.IMPORT_FILE);
        sel.setDbType((DbType) cbDbType.getSelectedItem());
        sel.setDbName(safe(txtDbName.getText()));
        sel.setExistingEntry((String) cbExisting.getSelectedItem());
        sel.setServer(resolveServer(safe(txtServer.getText())));
        if (sel.getDbType() == DbType.H2) {
            sel.setPort("9092");
            sel.setUsername("sa");
            sel.setPassword(new char[0]);
        } else {
            sel.setPort(safe(txtPort.getText()));
            sel.setUsername(safe(txtUser.getText()));
            sel.setPassword(txtPass.getPassword());
        }
        sel.setRemember(chkRemember.isSelected());

        switch (sel.getDbType()) {
            case H2 -> {
                String serverInput = safe(txtServer.getText());
                boolean isLocal = serverInput.isBlank() || serverInput.equalsIgnoreCase("localhost")
                        || "127.0.0.1".equals(serverInput);
                boolean requireExists = sel.getMode() != Mode.INIT_NEW;
                String suffix = H2_BASE_OPTS + (requireExists ? H2_REQUIRE_EXISTS : "");
                if (isLocal)
                    sel.setJdbcUrl("jdbc:h2:file:./database/" + sel.getDbName() + suffix);
                else
                    sel.setJdbcUrl("jdbc:h2:tcp://" + sel.getServer() + ":" + sel.getPort()
                            + "/./database/" + sel.getDbName() + suffix);
            }
            case SQLSRV -> {
                StringBuilder u = new StringBuilder("jdbc:sqlserver://").append(sel.getServer());
                if (sel.getPort() != null && !sel.getPort().isBlank())
                    u.append(':').append(sel.getPort());
                if (sel.getDbName() != null && !sel.getDbName().isBlank())
                    u.append(";databaseName=").append(sel.getDbName());
                sel.setJdbcUrl(u.toString());
            }
            default -> {
            }
        }

        setBusy(true);
        new Thread(() -> {
            try {
                switch (sel.getDbType()) {
                    case H2 -> DriverManager.getConnection(sel.getJdbcUrl(), "sa", "").close();
                    case SQLSRV -> {
                        StringBuilder testUrl = new StringBuilder(sel.getJdbcUrl())
                                .append(";encrypt=true;trustServerCertificate=true;loginTimeout=5");
                        String user = nz(sel.getUsername());
                        boolean useIntegrated = user.isBlank();
                        if (useIntegrated) {
                            testUrl.append(";integratedSecurity=true");
                            DriverManager.getConnection(testUrl.toString()).close();
                        } else {
                            DriverManager.getConnection(testUrl.toString(), user,
                                    sel.getPassword() == null ? "" : new String(sel.getPassword())).close();
                        }
                    }
                    default -> throw new IllegalStateException("Unsupported DB type.");
                }
                SwingUtilities.invokeLater(() -> completeSelectionAndClose(sel));
            } catch (java.sql.SQLException ex) {
                String m = ex.getMessage();
                if (m != null && m.toLowerCase().contains("not found") && sel.getDbType() == DbType.H2) {
                    showErrorLater("H2 database not found. Please create it via 'Use integrated' first.");
                } else {
                    showErrorLater("Connection failed: " + m);
                }
            } finally {
                setBusy(false);
            }
        }, "db-preflight-connect").start();
    }

    private void onCancel() {
        this.result = null;
        if (onCancel != null)
            onCancel.run();
        dispose();
    }

    private void completeSelectionAndClose(DbConnectionSelection sel) {
        if (sel.isRemember())
            savePrefs(sel);
        this.result = sel;
        if (onOk != null)
            onOk.accept(sel);
        dispose();
    }

    private void loadExistingDbOptions() {
        List<String> items = new ArrayList<>();
        items.add("Local H2 Databases...");
        try {
            File dir = new File("database");
            if (dir.isDirectory()) {
                File[] list = dir.listFiles();
                if (list != null)
                    for (File f : list)
                        if (f.isFile())
                            items.add(f.getName());
            }
        } catch (java.lang.SecurityException ignore) {
        }
        cbExisting.setModel(new DefaultComboBoxModel<>(items.toArray(String[]::new)));
    }

    private void loadPrefs() {
        Prefs p = new Prefs();
        String type = p.get("db.type", DbType.SQLSRV.name());
        try {
            cbDbType.setSelectedItem(DbType.valueOf(type));
        } catch (IllegalArgumentException ignore) {
            cbDbType.setSelectedItem(DbType.SQLSRV);
        }
        txtDbName.setText(p.get("db.name", ""));
        txtServer.setText(p.get("db.server", "localhost"));
        txtPort.setText(p.get("db.port", cbDbType.getSelectedItem() == DbType.H2 ? "9092" : "1433"));
        txtUser.setText(p.get("db.user", ""));
        chkRemember.setSelected(p.getBool("db.remember", false));
    }

    private void savePrefs(DbConnectionSelection sel) {
        Prefs p = new Prefs();
        if (sel.getDbType() != null)
            p.put("db.type", sel.getDbType().name());
        p.put("db.name", nz(sel.getDbName()));
        p.put("db.server", nz(sel.getServer()));
        p.put("db.port", nz(sel.getPort()));
        p.put("db.user", nz(sel.getUsername()));
        p.putBool("db.remember", sel.isRemember());
    }

    private void initH2DatabaseFromScript(String dbName) throws Exception {
        File script = new File("database", "script.sql");
        H2ScriptUtil.runSqlServerScriptOnH2FileDb(dbName, script);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private void applyUiHints() {
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 10);
    }

    private void applyDbTypeBehavior() {
        DbType type = (DbType) cbDbType.getSelectedItem();
        boolean isH2 = type == DbType.H2;
        txtPort.setEnabled(!isH2);
        txtUser.setEnabled(!isH2);
        txtPass.setEnabled(!isH2);
        if (isH2) {
            txtServer.setText("localhost");
            txtPort.setText("9092");
            txtUser.setText("sa");
            txtPass.setText("");
        }
    }

    private String resolveServer(String server) {
        if (server == null)
            return "";
        if (!server.equalsIgnoreCase("localhost"))
            return server;
        String ip = getPreferredInterfaceIp();
        return (ip != null && !ip.isBlank()) ? ip : server;
    }

    private String getPreferredInterfaceIp() {
        try {
            Prefs p = new Prefs();
            String ifName = p.get("net.ifName", "");
            if (ifName == null || ifName.isBlank())
                ifName = p.get("ui.network.ifName", "");
            if (ifName == null || ifName.isBlank())
                return null;

            NetworkInterface ni;
            try {
                ni = NetworkInterface.getByName(ifName);
            } catch (java.net.SocketException e) {
                return null;
            }
            if (ni == null)
                return null;
            for (Enumeration<InetAddress> e = ni.getInetAddresses(); e.hasMoreElements();) {
                InetAddress addr = e.nextElement();
                if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                    return addr.getHostAddress();
                }
            }
        } catch (java.lang.SecurityException ignore) {
        }
        return null;
    }
}
