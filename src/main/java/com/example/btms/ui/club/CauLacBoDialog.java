package com.example.btms.ui.club;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.example.btms.model.club.CauLacBo;
import java.awt.Cursor;
import javax.swing.JComponent;

public class CauLacBoDialog extends JDialog {

    private final JTextField tenField = new JTextField(28);
    private final JTextField tenNganField = new JTextField(18);
    private final JButton btnSave = new JButton("Lưu");
    private final JButton btnCancel = new JButton("Hủy");

    public CauLacBoDialog(Window parent, String title, boolean editMode) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(BorderLayout.NORTH.equals(BorderLayout.NORTH)
                ? BorderFactory.createEmptyBorder(10, 12, 6, 12)
                : BorderFactory.createEmptyBorder(10, 12, 6, 12));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Thông tin Câu lạc bộ"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.anchor = GridBagConstraints.WEST;

        // Tên CLB
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Tên CLB:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        tenField.setToolTipText("Tên đầy đủ của câu lạc bộ (bắt buộc)");
        form.add(tenField, gc);

        // Tên ngắn
        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Tên ngắn:"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        tenNganField.setToolTipText("Tên rút gọn/viết tắt (tối đa 100 ký tự, có thể để trống)");
        form.add(tenNganField, gc);

        wrap.add(form, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.add(btnSave);
        buttons.add(btnCancel);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        bottom.add(buttons, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // default/esc
        getRootPane().setDefaultButton(btnSave);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(parent);
    }

    // ----- Expose cho controller -----
    public JTextField getTenField() {
        return tenField;
    }

    public JTextField getTenNganField() {
        return tenNganField;
    }

    public JButton getBtnSave() {
        return btnSave;
    }

    public JButton getBtnCancel() {
        return btnCancel;
    }

    public void fillFrom(CauLacBo clb) {
        if (clb == null)
            return;
        tenField.setText(clb.getTenClb());
        tenNganField.setText(clb.getTenNgan());
    }

    public void setBusy(boolean busy) {
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        getRootPane().setEnabled(!busy);
    }

    public void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    public void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}