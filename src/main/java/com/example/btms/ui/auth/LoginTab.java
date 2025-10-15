package com.example.btms.ui.auth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.example.btms.model.auth.AuthResult;
import com.example.btms.service.auth.AuthService;
import com.example.btms.util.security.HashUtil;

public class LoginTab extends JPanel {
    public enum Role {
        CLIENT, ADMIN
    }

    public interface Listener {
        void onLoggedIn(String username, Role role);
    }

    private AuthService authService; // <-- thay vì Connection
    private Listener listener;

    private final JTextField tfUser = new JTextField(20);
    private final JPasswordField pfPass = new JPasswordField(20);
    private final JComboBox<String> cboMode = new JComboBox<>(new String[] { "Client", "Admin" });
    private final JLabel lblStatus = new JLabel(" ");

    public LoginTab() {
        super(new BorderLayout());
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Đăng nhập");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        int r = 0;
        addRow(form, c, r++, "Tài khoản", tfUser);
        addRow(form, c, r++, "Mật khẩu", pfPass);
        addRow(form, c, r++, "Chế độ", cboMode);

        JButton btnLogin = new JButton("Đăng nhập");
        btnLogin.addActionListener(e -> doLogin());
        pfPass.addActionListener(e -> btnLogin.doClick());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.add(btnLogin);

        lblStatus.setForeground(new Color(120, 120, 120));

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.add(title, BorderLayout.NORTH);
        center.add(form, BorderLayout.CENTER);
        center.add(buttons, BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.add(center, new GridBagConstraints());

        add(wrap, BorderLayout.CENTER);
        add(lblStatus, BorderLayout.SOUTH);

        // mặc định
        tfUser.setText("admin");
        pfPass.setText("set");
        cboMode.setSelectedIndex(1);

        setInputsEnabled(false); // chờ setAuthService(...)
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        p.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        p.add(comp, c);
    }

    private void setInputsEnabled(boolean enabled) {
        tfUser.setEnabled(enabled);
        pfPass.setEnabled(enabled);
        cboMode.setEnabled(enabled);
    }

    /** Gọi từ MainFrame sau khi đã có Connection */
    public void setAuthService(AuthService service) {
        this.authService = service;
        boolean ready = (service != null);
        setInputsEnabled(ready);
        lblStatus.setText(ready ? "Kết nối DB sẵn sàng." : "Chưa có kết nối DB.");
        if (ready)
            SwingUtilities.invokeLater(() -> pfPass.requestFocusInWindow());
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

    /* ------------ Login flow -------------- */
    private void doLogin() {
        if (authService == null) {
            toastErr("Chưa có kết nối DB.");
            return;
        }

        String user = tfUser.getText().trim();
        char[] pass = pfPass.getPassword();
        if (user.isEmpty() || pass.length == 0) {
            toastErr("Nhập tài khoản và mật khẩu.");
            return;
        }

        try {
            String md5 = HashUtil.md5Hex(pass);
            AuthResult ar = authService.authenticate(user, md5);

            if (!ar.found()) {
                toastErr("Sai tài khoản hoặc mật khẩu.");
                return;
            }
            if (ar.locked()) {
                toastErr("Tài khoản đã bị khoá (GESPERRT).");
                return;
            }

            Role role = (cboMode.getSelectedIndex() == 1) ? Role.ADMIN : Role.CLIENT;
            toastOk("Đăng nhập thành công: " + user + " (" + role + ")");
            if (listener != null)
                listener.onLoggedIn(user, role);

        } catch (Exception ex) {
            toastErr("Lỗi DB: " + ex.getMessage());
        } finally {
            java.util.Arrays.fill(pfPass.getPassword(), '\0');
            pfPass.setText("");
        }
    }

    /* -------------- helpers -------------- */
    private void toastOk(String msg) {
        lblStatus.setForeground(new Color(60, 120, 60));
        lblStatus.setText(msg);
    }

    private void toastErr(String msg) {
        lblStatus.setForeground(new Color(170, 60, 60));
        lblStatus.setText(msg);
    }
}
