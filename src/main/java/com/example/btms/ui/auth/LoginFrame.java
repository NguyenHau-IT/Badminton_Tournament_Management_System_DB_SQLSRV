package com.example.btms.ui.auth;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class LoginFrame extends JFrame {

    // ---- UI
    private final JTextField tfUser = new JTextField(20);
    private final JPasswordField pfPass = new JPasswordField(20);
    private final JComboBox<String> cboMode = new JComboBox<>(new String[] { "Client", "Admin" });
    private final JLabel lblStatus = new JLabel(" ");

    private final JButton btnLogin = new JButton("Đăng nhập");
    private final JButton btnCancel = new JButton("Hủy");

    public LoginFrame() {
        super("Đăng nhập");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();

        // mặc định
        tfUser.setText("admin");
        pfPass.setText("set");
        cboMode.setSelectedIndex(1); // Admin
        setInputsEnabled(false); // chờ controller bật khi DB ready

        pack();
        setSize(520, 360);
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(btnLogin);
    }

    public void open() {
        setVisible(true);
        toFront();
        requestFocus();
    }

    public void closeView() {
        dispose();
    }

    // -------- expose cho Controller ----------
    public String getUsername() {
        return tfUser.getText().trim();
    }

    public char[] getPassword() {
        return pfPass.getPassword();
    }

    public void clearPassword() {
        pfPass.setText("");
    }

    public boolean isAdminSelected() {
        return cboMode.getSelectedIndex() == 1;
    }

    public JButton getLoginButton() {
        return btnLogin;
    }

    public JButton getCancelButton() {
        return btnCancel;
    }

    public void setInputsEnabled(boolean enabled) {
        tfUser.setEnabled(enabled);
        pfPass.setEnabled(enabled);
        cboMode.setEnabled(enabled);
        btnLogin.setEnabled(enabled);
    }

    public void setDbReady(boolean ready) {
        lblStatus.setText(ready ? "Kết nối DB sẵn sàng." : "Chưa có kết nối DB.");
        if (ready)
            SwingUtilities.invokeLater(() -> pfPass.requestFocusInWindow());
    }

    public void showOk(String msg) {
        lblStatus.setForeground(new Color(60, 120, 60));
        lblStatus.setText(msg);
    }

    public void showErr(String msg) {
        lblStatus.setForeground(new Color(170, 60, 60));
        lblStatus.setText(msg);
    }

    // ----- UI layout -----
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Đăng nhập");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        int r = 0;
        addRow(form, c, r++, "Tài khoản", tfUser);
        addRow(form, c, r++, "Mật khẩu", pfPass);
        addRow(form, c, r++, "Chế độ", cboMode);
        root.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnLogin);
        buttons.add(btnCancel);
        south.add(buttons, BorderLayout.NORTH);

        lblStatus.setForeground(new Color(120, 120, 120));
        south.add(lblStatus, BorderLayout.SOUTH);

        root.add(south, BorderLayout.SOUTH);
        setLayout(new BorderLayout());
        add(root, BorderLayout.CENTER);
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
}
