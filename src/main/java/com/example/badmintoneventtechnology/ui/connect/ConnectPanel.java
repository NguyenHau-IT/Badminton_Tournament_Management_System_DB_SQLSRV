package com.example.badmintoneventtechnology.ui.connect;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.example.badmintoneventtechnology.config.Prefs;
import com.example.badmintoneventtechnology.model.db.ConnectionConfig;
import com.example.badmintoneventtechnology.model.system.ServerInfo;
import com.example.badmintoneventtechnology.service.db.DatabaseService;
import com.example.badmintoneventtechnology.ui.net.NetworkConfig;
import com.example.badmintoneventtechnology.util.ui.IconUtil;
import com.example.badmintoneventtechnology.util.ui.Ui;

public class ConnectPanel extends JPanel {
    private final DatabaseService service;
    private final Prefs prefs;

    // --- Form fields ---
    private final JTextField txtDb = new JTextField("bet_db");
    private final JComboBox<String> dbMode = new JComboBox<>(new String[] {
            "Database name"
    });

    private final JTextField txtServer = new JTextField("127.0.0.1");
    private final JTextField txtPort = new JTextField("3306");
    private final JTextField txtUser = new JTextField("root");
    private final JPasswordField txtPass = new JPasswordField("");
    private final JCheckBox chkRemember = new JCheckBox("Remember password on next login", true);

    private final JTextArea log = new JTextArea(10, 70);
    private final JLabel lblBaseDir = new JLabel("-");
    private final JLabel lblDbPath = new JLabel("-");
    private final JLabel lblDbName = new JLabel("-");

    private JButton btnConnect, btnDisconnect, btnProbe, btnShowUrl;
    private JLabel statusLabel;

    // Hiển thị interface đã chọn
    private final JLabel lblBoundIf = new JLabel("Interface: (none)");

    // Lưu interface đã chọn để kiểm tra subnet
    private NetworkConfig selectedIf;

    public interface ConnectionConsumer {
        void onConnected(Connection c, String host, String port);

        void onDisconnected();
    }

    private final ConnectionConsumer consumer;

    public ConnectPanel(DatabaseService service, Prefs prefs, ConnectionConsumer consumer) {
        super(new BorderLayout(12, 12));
        this.service = service;
        this.prefs = prefs;
        this.consumer = consumer;
        buildUi();
        loadPrefs();
        updateButtons();
    }

    /* ---------------- UI ---------------- */

    private void buildUi() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(6, 8, 6, 8));
        JLabel title = new JLabel("Kết nối đến MySQL Server");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        JLabel subtitle = new JLabel("Nhập thông số kết nối, chọn chế độ đường dẫn và quản lý phiên làm việc.");
        subtitle.setForeground(new Color(120, 120, 120));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(6, 6, 6, 6));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        Ui.placeholder(txtDb, "Tên database (schema)");
        Ui.placeholder(txtServer, "IP/Host của MySQL Server");
        Ui.placeholder(txtPort, "Cổng (mặc định 3306)");
        Ui.placeholder(txtUser, "Tên đăng nhập DB (ví dụ: root)");
        txtPass.putClientProperty("JPasswordField.placeholderText", "Mật khẩu DB (có thể để trống)");

        int r = 0;
        addRow(form, c, r++, "Database", txtDb);
        addRow(form, c, r++, "Database mode", dbMode);
        addRow(form, c, r++, "Server (IP / Host)", txtServer);
        addRow(form, c, r++, "Port", txtPort);
        addRow(form, c, r++, "DB-Username", txtUser);
        addRow(form, c, r++, "DB-Password", txtPass);

        // dòng hiển thị interface
        c.gridx = 0;
        c.gridy = r++;
        c.gridwidth = 2;
        c.weightx = 1;
        lblBoundIf.setForeground(new Color(90, 90, 90));
        form.add(lblBoundIf, c);

        c.gridx = 0;
        c.gridy = r++;
        c.gridwidth = 2;
        form.add(chkRemember, c);

        JPanel right = new JPanel(new GridLayout(0, 1, 8, 8));
        btnShowUrl = new JButton("Show URL");
        btnShowUrl.addActionListener(e -> append("URL -> " + buildUrlFromFields()));
        btnConnect = new JButton("Connect");
        btnConnect.addActionListener(e -> doConnect());
        btnProbe = new JButton("Probe");
        btnProbe.addActionListener(e -> doProbe());
        btnDisconnect = new JButton("Disconnect");
        btnDisconnect.addActionListener(e -> doDisconnect());
        right.add(btnShowUrl);
        right.add(btnConnect);
        right.add(btnProbe);
        right.add(btnDisconnect);

        JPanel detected = new JPanel(new GridBagLayout());
        detected.setBorder(new EmptyBorder(6, 6, 6, 6));
        GridBagConstraints d = new GridBagConstraints();
        d.insets = new Insets(4, 8, 4, 8);
        d.fill = GridBagConstraints.HORIZONTAL;
        int rd = 0;
        addRow(detected, d, rd++, "Detected BASE_DIR", lblBaseDir);
        addRow(detected, d, rd++, "Detected DB Path", lblDbPath);
        addRow(detected, d, rd++, "Detected DB Name", lblDbName);

        JPanel mid = new JPanel(new BorderLayout(8, 8));
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        log.setEditable(false);
        mid.add(new JScrollPane(log), BorderLayout.CENTER);
        mid.add(detected, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout(12, 12));
        top.add(header, BorderLayout.NORTH);
        top.add(form, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        // add(mid, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        IconUtil.applyTo(this);
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;
        JLabel lab = new JLabel(label);
        lab.setBorder(new EmptyBorder(0, 0, 0, 4));
        p.add(lab, c);
        c.gridx = 1;
        c.weightx = 1;
        p.add(field, c);
    }

    private JComponent buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
        statusLabel = new JLabel();
        statusLabel.setBorder(new EmptyBorder(6, 10, 6, 10));
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    /* ---------------- Prefs & config ---------------- */

    private void loadPrefs() {
        txtDb.setText(prefs.get("db_input", txtDb.getText()));
        txtServer.setText(prefs.get("server", txtServer.getText()));
        txtPort.setText(prefs.get("port", txtPort.getText()));
        txtUser.setText(prefs.get("user", txtUser.getText()));
        if (prefs.getBool("remember", false)) {
            txtPass.setText(prefs.get("pass", ""));
            chkRemember.setSelected(true);
        }
        dbMode.setSelectedIndex(prefs.getInt("db_mode", 0));
        updateStatus();
    }

    private void persistPrefs() {
        prefs.put("db_input", txtDb.getText().trim());
        prefs.put("server", txtServer.getText().trim());
        prefs.put("port", txtPort.getText().trim());
        prefs.put("user", txtUser.getText().trim());
        prefs.putInt("db_mode", dbMode.getSelectedIndex());
        prefs.putBool("remember", chkRemember.isSelected());
        if (chkRemember.isSelected())
            prefs.put("pass", new String(txtPass.getPassword()));
        else
            prefs.remove("pass");
    }

    private ConnectionConfig configFromFields() {
        ConnectionConfig.Mode mode = switch (dbMode.getSelectedIndex()) {
            case 0 -> ConnectionConfig.Mode.NAME;
            case 1 -> ConnectionConfig.Mode.HOME;
            default -> ConnectionConfig.Mode.ABSOLUTE;
        };
        return new ConnectionConfig()
                .databaseInput(txtDb.getText().trim())
                .host(txtServer.getText().trim())
                .port(txtPort.getText().trim())
                .user(txtUser.getText().trim())
                .password(new String(txtPass.getPassword()))
                .mode(mode);
    }

    private String buildUrlFromFields() {
        var cfg = configFromFields();
        String raw = cfg.databaseInput();
        String dbPath = switch (cfg.mode()) {
            case NAME -> raw;
            case HOME -> raw.startsWith("~/") ? raw : ("~/" + raw);
            case ABSOLUTE -> raw;
        };
        String port = (cfg.port() == null || cfg.port().isBlank()) ? "3306" : cfg.port();
        return "jdbc:mysql://" + cfg.host() + ":" + port + "/" + cfg.databaseInput() + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    /* --------- Nhận interface đã chọn (để kiểm tra subnet) --------- */
    public void setOutboundBind(NetworkConfig cfg) {
        this.selectedIf = cfg;
        if (cfg != null && cfg.ifName() != null && !cfg.ifName().isBlank())
            lblBoundIf.setText("Interface đã chọn: " + cfg.ifName());
        else
            lblBoundIf.setText("Interface: (none)");
    }

    /* ---------------- RÀNG BUỘC: phải cùng subnet ---------------- */

    private boolean enforceSameSubnet() {
        if (selectedIf == null || selectedIf.ifName() == null || selectedIf.ifName().isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Bạn chưa chọn interface mạng. Hãy chọn interface trước khi kết nối.",
                    "Thiếu interface", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            // Tìm NetworkInterface theo name hoặc displayName
            NetworkInterface ni = NetworkInterface.getByName(selectedIf.ifName());
            if (ni == null) {
                // fall back: so khớp theo displayName
                for (NetworkInterface x : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
                    if (selectedIf.ifName().equalsIgnoreCase(x.getDisplayName())) {
                        ni = x;
                        break;
                    }
                }
            }
            if (ni == null) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy interface: " + selectedIf.ifName(),
                        "Interface không tồn tại", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Lấy IPv4 + prefix đầu tiên của interface
            Inet4Address local4 = null;
            short prefix = -1;
            for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                if (ia.getAddress() instanceof Inet4Address a4) {
                    local4 = a4;
                    prefix = ia.getNetworkPrefixLength();
                    break;
                }
            }
            if (local4 == null || prefix < 0 || prefix > 32) {
                JOptionPane.showMessageDialog(this,
                        "Interface này không có IPv4 hợp lệ để kiểm tra subnet.",
                        "Không có IPv4", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            // Lấy IPv4 của host đích
            Inet4Address remote4 = resolveIPv4(txtServer.getText().trim());
            if (remote4 == null) {
                JOptionPane.showMessageDialog(this,
                        "Server nhập vào không phải IPv4 hợp lệ.",
                        "Sai địa chỉ", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            int mask = prefix == 0 ? 0 : (int) (0xFFFFFFFFL << (32 - prefix));
            int a = ipv4ToInt(local4);
            int b = ipv4ToInt(remote4);

            boolean same = (a & mask) == (b & mask);
            if (!same) {
                JOptionPane.showMessageDialog(this,
                        "KHÔNG cùng mạng với interface đã chọn.\n" +
                                "Interface: " + selectedIf.ifName() + " → " + local4.getHostAddress() + "/" + prefix
                                + "\n" +
                                "Server:    " + remote4.getHostAddress() + "\n\n" +
                                "Vui lòng chọn đúng interface hoặc nhập Server thuộc cùng subnet.",
                        "Chặn kết nối khác mạng", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không kiểm tra được subnet: " + ex.getMessage(),
                    "Lỗi kiểm tra mạng", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private static Inet4Address resolveIPv4(String host) throws UnknownHostException {
        // Nếu là IPv4 literal
        if (host.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) {
            return (Inet4Address) InetAddress.getByName(host);
        }
        // DNS -> lấy bản ghi IPv4 đầu tiên
        for (InetAddress a : InetAddress.getAllByName(host)) {
            if (a instanceof Inet4Address a4)
                return a4;
        }
        return null;
    }

    private static int ipv4ToInt(Inet4Address ip) {
        byte[] b = ip.getAddress();
        return ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | (b[3] & 0xFF);
    }

    /* ---------------- Actions ---------------- */

    private void doConnect() {
        if (!enforceSameSubnet())
            return; // << khóa khác mạng

        var cfg = configFromFields();
        service.manager().setConfig(cfg);
        append("Connecting to: " + service.builtUrl());
        try {
            Connection c = service.connect();
            onConnected(c);
        } catch (SQLException ex) {
            append("✗ Failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Connect error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doProbe() {
        if (!enforceSameSubnet())
            return; // << khóa khác mạng

        JOptionPane.showMessageDialog(this,
                "Probe chỉ áp dụng cho H2. Với MySQL, hãy dùng Connect.",
                "Probe không khả dụng",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void doDisconnect() {
        if (!service.isConnected()) {
            updateButtons();
            updateStatus();
            return;
        }
        append("Disconnecting...");
        service.disconnect();
        lblBaseDir.setText("-");
        lblDbPath.setText("-");
        lblDbName.setText("-");
        consumer.onDisconnected();
        append("✓ Disconnected.");
        updateButtons();
        updateStatus();
    }

    private void onConnected(Connection c) {
        persistPrefs();
        ServerInfo info = service.detectInfo(c);
        lblBaseDir.setText(info.baseDir());
        lblDbPath.setText(info.databasePath());
        lblDbName.setText(info.databaseName());
        consumer.onConnected(c, txtServer.getText().trim(), txtPort.getText().trim());
        append("✓ Connected!\nDetected BASE_DIR: " + info.baseDir()
                + "\nDB Path: " + info.databasePath()
                + "\nDB Name: " + info.databaseName());
        updateButtons();
        updateStatus();
    }

    private void updateButtons() {
        boolean connected = service.isConnected();
        if (btnConnect != null)
            btnConnect.setEnabled(!connected);
        if (btnDisconnect != null)
            btnDisconnect.setEnabled(connected);
        if (btnProbe != null)
            btnProbe.setEnabled(!connected);
        if (btnShowUrl != null)
            btnShowUrl.setEnabled(true);
    }

    private void updateStatus() {
        if (statusLabel == null)
            return;
        if (service.isConnected()) {
            statusLabel.setText("  \u25CF  Đã kết nối");
            statusLabel.setForeground(new Color(46, 204, 113));
        } else {
            statusLabel.setText("  \u25CB  Chưa kết nối");
            statusLabel.setForeground(new Color(140, 140, 140));
        }
    }

    private void append(String s) {
        log.append(s + "\n");
    }
}
