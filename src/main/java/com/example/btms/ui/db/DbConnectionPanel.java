package com.example.btms.ui.db;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.example.btms.config.Prefs;
import com.example.btms.ui.db.DbConnectionSelection.DbType;
import com.example.btms.ui.db.DbConnectionSelection.Mode;
import com.example.btms.util.h2db.H2ScriptUtil;

public class DbConnectionPanel extends JPanel {

    private final JRadioButton rbLocalLan = new JRadioButton("Sử dụng DB cục bộ / cùng mạng");
    private final JRadioButton rbInitNew = new JRadioButton("Khởi tạo mới");
    private final JRadioButton rbOnline = new JRadioButton("Sử dụng online");
    private final JRadioButton rbImport = new JRadioButton("Nhập từ file");

    private final JComboBox<DbType> cbDbType = new JComboBox<>();
    private final JTextField txtDbName = new JTextField(18);
    private final JComboBox<String> cbExisting = new JComboBox<>();
    private final JTextField txtServer = new JTextField(18);
    private final JTextField txtPort = new JTextField(6);
    private final JTextField txtUser = new JTextField(16);
    private final JPasswordField txtPass = new JPasswordField(16);
    private final JCheckBox chkRemember = new JCheckBox("Ghi nhớ đăng nhập");

    private DbConnectionSelection result;
    private Consumer<DbConnectionSelection> onOk;
    private Runnable onCancel;

    public DbConnectionPanel() {
        super(new BorderLayout(0, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Thiết lập kết nối CSDL");
        title.putClientProperty("FlatLaf.style", "font: bold +2");
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.add(title);
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        add(form, BorderLayout.CENTER);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;

        ButtonGroup grp = new ButtonGroup();
        grp.add(rbLocalLan);
        grp.add(rbInitNew);
        grp.add(rbOnline);
        grp.add(rbImport);
        rbLocalLan.setSelected(true);

        form.add(new JLabel("Chế độ:"), gc);
        gc.gridx = 1;
        form.add(rbLocalLan, gc);
        gc.gridx = 2;
        form.add(rbInitNew, gc);
        gc.gridx = 3;
        form.add(rbOnline, gc);
        gc.gridx = 4;
        form.add(rbImport, gc);

        gc.gridx = 0;
        gc.gridy++;
        form.add(new JLabel("Kiểu DB:"), gc);
        cbDbType.setModel(new DefaultComboBoxModel<>(DbType.values()));
        cbDbType.setSelectedItem(DbType.SQLSRV);
        gc.gridx = 1;
        gc.gridwidth = 2;
        form.add(cbDbType, gc);
        gc.gridwidth = 1;

        gc.gridx = 0;
        gc.gridy++;
        form.add(new JLabel("Tên DB:"), gc);
        gc.gridx = 1;
        gc.gridwidth = 2;
        form.add(txtDbName, gc);
        gc.gridwidth = 1;

        gc.gridx = 0;
        gc.gridy++;
        form.add(new JLabel("Có sẵn:"), gc);
        gc.gridx = 1;
        gc.gridwidth = 3;
        form.add(cbExisting, gc);
        gc.gridwidth = 1;

        gc.gridx = 0;
        gc.gridy++;
        form.add(new JLabel("Server:"), gc);
        gc.gridx = 1;
        gc.gridwidth = 2;
        form.add(txtServer, gc);
        gc.gridwidth = 1;

        gc.gridx = 3;
        form.add(new JLabel("Port:"), gc);
        gc.gridx = 4;
        form.add(txtPort, gc);

        gc.gridx = 0;
        gc.gridy++;
        form.add(new JLabel("Username:"), gc);
        gc.gridx = 1;
        form.add(txtUser, gc);

        gc.gridx = 2;
        form.add(new JLabel("Password:"), gc);
        gc.gridx = 3;
        gc.gridwidth = 2;
        form.add(txtPass, gc);
        gc.gridwidth = 1;

        gc.gridx = 1;
        gc.gridy++;
        form.add(chkRemember, gc);

        JButton btnTest = new JButton("Test kết nối");
        JButton btnOk = new JButton("Kết nối");
        JButton btnCancel = new JButton("Hủy");
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(btnTest);
        footer.add(btnOk);
        footer.add(btnCancel);
        add(footer, BorderLayout.SOUTH);

        loadExistingDbOptions();
        loadPrefs();

        cbDbType.addActionListener(e -> applyDbTypeBehavior());
        applyDbTypeBehavior();

        btnTest.addActionListener(e -> onTestConnection(btnTest, btnOk, btnCancel));
        btnOk.addActionListener(e -> onOk());
        btnCancel.addActionListener(e -> onCancel());

        SwingUtilities.invokeLater(() -> txtDbName.requestFocusInWindow());
        applyUiHints();
    }

    public void setOnOk(Consumer<DbConnectionSelection> onOk) {
        this.onOk = onOk;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

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

                if (type == DbType.SQLSRV) {
                    if (server.isBlank()) {
                        showErrorLater("Vui lòng nhập Server.");
                        return;
                    }

                    StringBuilder url = new StringBuilder("jdbc:sqlserver://").append(server);
                    if (!port.isBlank())
                        url.append(":").append(port);
                    if (!dbName.isBlank())
                        url.append(";databaseName=").append(dbName);

                    url.append(";encrypt=true;trustServerCertificate=true");
                    url.append(";loginTimeout=5");

                    boolean useIntegrated = user.isBlank();
                    if (useIntegrated) {
                        url.append(";integratedSecurity=true");
                        DriverManager.getConnection(url.toString()).close();
                    } else {
                        DriverManager.getConnection(url.toString(), user, new String(pw)).close();
                    }
                    showInfoLater("Kết nối SQL Server thành công.");
                } else if (type == DbType.H2) {
                    if (dbName.isBlank()) {
                        showErrorLater("Vui lòng nhập Tên DB (H2).");
                        return;
                    }
                    boolean isLocal = serverInput.isBlank() || serverInput.equalsIgnoreCase("localhost")
                            || "127.0.0.1".equals(serverInput);
                    String url;
                    if (isLocal) {
                        url = "jdbc:h2:file:./database/" + dbName
                                + ";MODE=MSSQLServer;DATABASE_TO_UPPER=FALSE";
                    } else {
                        url = "jdbc:h2:tcp://" + server + ":9092/./database/" + dbName;
                    }
                    String userH2 = "sa";
                    String passH2 = "";
                    DriverManager.getConnection(url, userH2, passH2).close();
                    showInfoLater("Kết nối H2 thành công.");
                } else {
                    showErrorLater("Kiểu DB không hợp lệ.");
                }
            } catch (Exception ex) {
                showErrorLater("Kết nối thất bại: " + ex.getMessage());
            } finally {
                setBusy(false, toDisable);
            }
        }, "db-test-conn").start();
    }

    private void setBusy(boolean busy, JButton... buttons) {
        try {
            setCursor(busy ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR)
                    : java.awt.Cursor.getDefaultCursor());
        } catch (Exception ignore) {
        }
        if (buttons != null)
            for (JButton b : buttons)
                if (b != null)
                    b.setEnabled(!busy);
    }

    private void showInfoLater(String msg) {
        SwingUtilities.invokeLater(() -> javax.swing.JOptionPane.showMessageDialog(this, msg, "Thông báo",
                javax.swing.JOptionPane.INFORMATION_MESSAGE));
    }

    private void showErrorLater(String msg) {
        SwingUtilities.invokeLater(() -> javax.swing.JOptionPane.showMessageDialog(this, msg, "Lỗi",
                javax.swing.JOptionPane.ERROR_MESSAGE));
    }

    private void onOk() {
        DbConnectionSelection sel = new DbConnectionSelection();
        sel.setMode(rbLocalLan.isSelected() ? Mode.LOCAL_OR_LAN
                : rbInitNew.isSelected() ? Mode.INIT_NEW
                        : rbOnline.isSelected() ? Mode.ONLINE : Mode.IMPORT_FILE);
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
        if (sel.getDbType() == DbType.H2) {
            String serverInput = safe(txtServer.getText());
            boolean isLocal = serverInput.isBlank() || serverInput.equalsIgnoreCase("localhost")
                    || "127.0.0.1".equals(serverInput);
            if (isLocal) {
                sel.setJdbcUrl("jdbc:h2:file:./database/" + sel.getDbName()
                        + ";MODE=MSSQLServer;DATABASE_TO_UPPER=FALSE;INIT=SET SCHEMA dbo");
            } else {
                sel.setJdbcUrl(
                        "jdbc:h2:tcp://" + sel.getServer() + ":" + sel.getPort() + "/./database/" + sel.getDbName()
                                + ";MODE=MSSQLServer;DATABASE_TO_UPPER=FALSE;INIT=SET SCHEMA dbo");
            }
        } else if (sel.getDbType() == DbType.SQLSRV) {
            StringBuilder u = new StringBuilder("jdbc:sqlserver://").append(sel.getServer());
            if (sel.getPort() != null && !sel.getPort().isBlank())
                u.append(':').append(sel.getPort());
            if (sel.getDbName() != null && !sel.getDbName().isBlank())
                u.append(";databaseName=").append(sel.getDbName());
            sel.setJdbcUrl(u.toString());
        }

        if (sel.isRemember())
            savePrefs(sel);

        this.result = sel;
        if (onOk != null)
            onOk.accept(sel);
        java.awt.Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof JDialog d)
            d.dispose();
    }

    private void onCancel() {
        this.result = null;
        if (onCancel != null)
            onCancel.run();
        java.awt.Window w = SwingUtilities.getWindowAncestor(this);
        if (w instanceof JDialog d)
            d.dispose();
    }

    private void loadExistingDbOptions() {
        List<String> items = new ArrayList<>();
        items.add("");
        try {
            File dir = new File("database");
            if (dir.isDirectory()) {
                File[] list = dir.listFiles();
                if (list != null) {
                    for (File f : list) {
                        if (f.isFile())
                            items.add(f.getName());
                    }
                }
            }
        } catch (Exception ignore) {
        }
        cbExisting.setModel(new DefaultComboBoxModel<>(items.toArray(String[]::new)));
    }

    private void loadPrefs() {
        Prefs p = new Prefs();
        String type = p.get("db.type", DbType.SQLSRV.name());
        try {
            cbDbType.setSelectedItem(DbType.valueOf(type));
        } catch (Exception ignore) {
            cbDbType.setSelectedItem(DbType.SQLSRV);
        }
        txtDbName.setText(p.get("db.name", ""));
        txtServer.setText(p.get("db.server", ""));
        txtPort.setText(p.get("db.port", "1433"));
        txtUser.setText(p.get("db.user", ""));
        chkRemember.setSelected(p.getBool("db.remember", false));
    }

    private void savePrefs(DbConnectionSelection sel) {
        Prefs p = new Prefs();
        if (sel.getDbType() != null)
            p.put("db.type", sel.getDbType().name());

        if (sel.getMode() == Mode.INIT_NEW && sel.getDbType() == DbType.H2) {
            String dbName = sel.getDbName();
            if (dbName == null || dbName.isBlank()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên DB (H2).", "Thiếu thông tin",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (dbName.contains("/") || dbName.contains("\\")) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Tên DB không được chứa dấu / hoặc \\. Vui lòng nhập tên hợp lệ.", "Tên DB không hợp lệ",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                File dir = new File("database");
                if (!dir.exists())
                    dir.mkdirs();
                String fileUrl = "jdbc:h2:file:./database/" + dbName
                        + ";MODE=MSSQLServer;DATABASE_TO_UPPER=FALSE;INIT=SET SCHEMA dbo";
                DriverManager.getConnection(fileUrl, "sa", "").close();
                try {
                    initH2DatabaseFromScript(dbName);
                } catch (Exception initEx) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Đã tạo DB nhưng khởi tạo schema từ script thất bại: " + initEx.getMessage(),
                            "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Không thể khởi tạo DB H2: " + ex.getMessage(),
                        "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
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

    private static String readTextAuto(File file) throws IOException {
        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            return new String(bytes, 3, bytes.length - 3, java.nio.charset.StandardCharsets.UTF_8);
        }
        if (bytes.length >= 2) {
            int b0 = bytes[0] & 0xFF, b1 = bytes[1] & 0xFF;
            if (b0 == 0xFF && b1 == 0xFE) {
                return new String(bytes, 2, bytes.length - 2, java.nio.charset.StandardCharsets.UTF_16LE);
            }
            if (b0 == 0xFE && b1 == 0xFF) {
                return new String(bytes, 2, bytes.length - 2, java.nio.charset.StandardCharsets.UTF_16BE);
            }
        }
        java.nio.charset.CharsetDecoder dec = java.nio.charset.StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT);
        try {
            return dec.decode(java.nio.ByteBuffer.wrap(bytes)).toString();
        } catch (java.nio.charset.CharacterCodingException e) {
            try {
                return java.nio.charset.StandardCharsets.UTF_16LE.newDecoder()
                        .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                        .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                        .decode(java.nio.ByteBuffer.wrap(bytes)).toString();
            } catch (java.nio.charset.CharacterCodingException e2) {
                try {
                    return java.nio.charset.StandardCharsets.UTF_16BE.newDecoder()
                            .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                            .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                            .decode(java.nio.ByteBuffer.wrap(bytes)).toString();
                } catch (java.nio.charset.CharacterCodingException e3) {
                    return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        }
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
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);
    }

    private void applyDbTypeBehavior() {
        DbType type = (DbType) cbDbType.getSelectedItem();
        boolean isH2 = type == DbType.H2;
        txtPort.setEnabled(!isH2);
        txtUser.setEnabled(!isH2);
        txtPass.setEnabled(!isH2);
        if (isH2) {
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

            NetworkInterface ni = NetworkInterface.getByName(ifName);
            if (ni == null)
                return null;
            for (Enumeration<InetAddress> e = ni.getInetAddresses(); e.hasMoreElements();) {
                InetAddress addr = e.nextElement();
                if (addr instanceof Inet4Address ipv4 && !addr.isLoopbackAddress()) {
                    return ipv4.getHostAddress();
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    public DbConnectionSelection getSelection() {
        return result;
    }
}
