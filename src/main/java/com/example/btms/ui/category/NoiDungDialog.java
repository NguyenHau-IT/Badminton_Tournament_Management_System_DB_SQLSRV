package com.example.btms.ui.category;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.example.btms.model.category.NoiDung;
import java.awt.Color;
import java.awt.Cursor;

public class NoiDungDialog extends JDialog {
    // --- UI fields
    private final JTextField tenNoiDungField = new JTextField(30);
    private final JTextField tuoiDuoiField = new JTextField(5);
    private final JTextField tuoiTrenField = new JTextField(5);
    private final JComboBox<String> gioiTinhCombo = new JComboBox<>(new String[] { "", "m", "f" });
    private final JCheckBox teamCheckBox = new JCheckBox("Đồng đội");
    private final JButton btnSave;
    private final JButton btnCancel = new JButton("Hủy");

    public NoiDungDialog(Window parent, String title, boolean isEditMode) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.btnSave = new JButton(isEditMode ? "Cập nhật" : "Thêm mới");
        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Tên nội dung *:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(tenNoiDungField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Tuổi dưới:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(tuoiDuoiField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Tuổi trên:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(tuoiTrenField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Giới tính:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(gioiTinhCombo, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel note = new JLabel("Ghi chú: m = Nam, f = Nữ, để trống = cả Nam và Nữ");
        note.setFont(note.getFont().deriveFont(note.getFont().getSize2D() - 1f));
        note.setForeground(Color.DARK_GRAY);
        form.add(note, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        form.add(teamCheckBox, gbc);

        JPanel buttons = new JPanel();
        buttons.add(btnSave);
        buttons.add(btnCancel);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    // --- Getters cho Controller
    public JTextField getTenNoiDungField() {
        return tenNoiDungField;
    }

    public JTextField getTuoiDuoiField() {
        return tuoiDuoiField;
    }

    public JTextField getTuoiTrenField() {
        return tuoiTrenField;
    }

    public JComboBox<String> getGioiTinhCombo() {
        return gioiTinhCombo;
    }

    public JCheckBox getTeamCheckBox() {
        return teamCheckBox;
    }

    public JButton getBtnSave() {
        return btnSave;
    }

    public JButton getBtnCancel() {
        return btnCancel;
    }

    // --- Helpers cho Controller
    public void fillFrom(NoiDung nd) {
        if (nd == null)
            return;
        tenNoiDungField.setText(nd.getTenNoiDung());
        tuoiDuoiField.setText(String.valueOf(nd.getTuoiDuoi()));
        tuoiTrenField.setText(String.valueOf(nd.getTuoiTren()));
        String gt = nd.getGioiTinh();
        if (gt == null || gt.isBlank())
            gioiTinhCombo.setSelectedItem("");
        else if ("m".equalsIgnoreCase(gt))
            gioiTinhCombo.setSelectedItem("m");
        else if ("f".equalsIgnoreCase(gt))
            gioiTinhCombo.setSelectedItem("f");
        else
            gioiTinhCombo.setSelectedItem("");
        teamCheckBox.setSelected(Boolean.TRUE.equals(nd.getTeam()));
    }

    public void setBusy(boolean busy) {
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        getRootPane().setEnabled(!busy);
    }

    public void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}