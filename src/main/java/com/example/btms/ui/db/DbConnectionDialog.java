package com.example.btms.ui.db;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

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
import com.example.btms.util.ui.IconUtil;

public class DbConnectionDialog extends JDialog {

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

    public DbConnectionDialog(Window owner) {
        super(owner, "Kết nối cơ sở dữ liệu", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        JLabel title = new JLabel("Thiết lập kết nối CSDL");
        title.putClientProperty("FlatLaf.style", "font: bold +2");
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.add(title);
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        root.add(form, BorderLayout.CENTER);
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
        root.add(footer, BorderLayout.SOUTH);

        loadExistingDbOptions();
        loadPrefs();

        btnTest.addActionListener(e -> onTestConnection(btnTest, btnOk, btnCancel));
        btnOk.addActionListener(e -> onOk());
        btnCancel.addActionListener(e -> {
            result = null;
            dispose();
        });

        SwingUtilities.invokeLater(() -> IconUtil.applyTo(this));
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
        SwingUtilities.invokeLater(() -> txtDbName.requestFocusInWindow());
        applyUiHints();
    }

    private void onTestConnection(JButton... toDisable) {
        setBusy(true, toDisable);
        new Thread(() -> {
            try {
                DbType type = (DbType) cbDbType.getSelectedItem();
                String dbName = safe(txtDbName.getText());
                String server = safe(txtServer.getText());
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

                    // ✅ Mặc định chống lỗi SSL (dứt điểm PKIX)
                    url.append(";encrypt=true;trustServerCertificate=true");
                    url.append(";loginTimeout=5");

                    boolean useIntegrated = user.isBlank();
                    if (useIntegrated) {
                        url.append(";integratedSecurity=true");
                        try (Connection c = DriverManager.getConnection(url.toString())) {
                            /* ok */ }
                    } else {
                        try (Connection c = DriverManager.getConnection(url.toString(), user, new String(pw))) {
                            /* ok */ }
                    }
                    showInfoLater("Kết nối SQL Server thành công.");
                } else if (type == DbType.H2) {
                    if (dbName.isBlank()) {
                        showErrorLater("Vui lòng nhập Tên DB (H2).");
                        return;
                    }
                    String url = "jdbc:h2:file:./database/" + dbName + ";MODE=MSSQLServer;AUTO_SERVER=TRUE";
                    String userH2 = user.isBlank() ? "sa" : user;
                    String passH2 = new String(pw);
                    try (Connection c = DriverManager.getConnection(url, userH2, passH2)) {
                        /* ok */ }
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
        sel.setServer(safe(txtServer.getText()));
        sel.setPort(safe(txtPort.getText()));
        sel.setUsername(safe(txtUser.getText()));
        sel.setPassword(txtPass.getPassword());
        sel.setRemember(chkRemember.isSelected());

        if (sel.isRemember())
            savePrefs(sel);

        this.result = sel;
        dispose();
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
        p.put("db.name", nz(sel.getDbName()));
        p.put("db.server", nz(sel.getServer()));
        p.put("db.port", nz(sel.getPort()));
        p.put("db.user", nz(sel.getUsername()));
        p.putBool("db.remember", sel.isRemember());
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

    public DbConnectionSelection getSelection() {
        return result;
    }
}
