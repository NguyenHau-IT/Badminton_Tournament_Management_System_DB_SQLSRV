package com.example.btms.ui.category;

import com.example.btms.model.category.NoiDung;
import com.example.btms.service.category.NoiDungService;

import javax.swing.*;
import java.awt.*;

public class NoiDungDialog extends JDialog {
    private final NoiDungService noiDungService;
    private final NoiDung originalNoiDung;
    private final boolean isEditMode;

    private JTextField tenNoiDungField, tuoiDuoiField, tuoiTrenField, gioiTinhField;
    private JCheckBox teamCheckBox;
    private JButton btnSave, btnCancel;

    // Lưu trạng thái sau khi bấm lưu để caller có thể xử lý tiếp
    private boolean saved = false;
    private NoiDung resultNoiDung = null;

    public NoiDungDialog(Window parent, String title, NoiDung noiDung, NoiDungService noiDungService) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.noiDungService = noiDungService;
        this.originalNoiDung = noiDung;
        this.isEditMode = noiDung != null;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        if (isEditMode) {
            loadData();
        }
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        tenNoiDungField = new JTextField(30);
        tuoiDuoiField = new JTextField(5);
        tuoiTrenField = new JTextField(5);
        gioiTinhField = new JTextField(2);
        teamCheckBox = new JCheckBox("Đồng đội");
        btnSave = new JButton(isEditMode ? "Cập nhật" : "Thêm mới");
        btnCancel = new JButton("Hủy");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tên nội dung *:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(tenNoiDungField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Tuổi dưới:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(tuoiDuoiField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Tuổi trên:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(tuoiTrenField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Giới tính (M/F):"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(gioiTinhField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(teamCheckBox, gbc);

        add(formPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void setupEventHandlers() {
        btnSave.addActionListener(e -> saveNoiDung());
        btnCancel.addActionListener(e -> dispose());
    }

    private void loadData() {
        tenNoiDungField.setText(originalNoiDung.getTenNoiDung());
        tuoiDuoiField.setText(String.valueOf(originalNoiDung.getTuoiDuoi()));
        tuoiTrenField.setText(String.valueOf(originalNoiDung.getTuoiTren()));
        gioiTinhField.setText(originalNoiDung.getGioiTinh());
        teamCheckBox.setSelected(Boolean.TRUE.equals(originalNoiDung.getTeam()));
    }

    private void saveNoiDung() {
        try {
            String tenNoiDung = tenNoiDungField.getText().trim();
            if (tenNoiDung.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên nội dung không được để trống!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            int tuoiDuoi = Integer.parseInt(tuoiDuoiField.getText().trim());
            int tuoiTren = Integer.parseInt(tuoiTrenField.getText().trim());
            String gioiTinh = gioiTinhField.getText().trim();
            boolean team = teamCheckBox.isSelected();
            if (isEditMode) {
                originalNoiDung.setTenNoiDung(tenNoiDung);
                originalNoiDung.setTuoiDuoi(tuoiDuoi);
                originalNoiDung.setTuoiTren(tuoiTren);
                originalNoiDung.setGioiTinh(gioiTinh);
                originalNoiDung.setTeam(team);
                boolean updated = noiDungService.updateNoiDung(originalNoiDung);
                if (updated) {
                    saved = true;
                    resultNoiDung = originalNoiDung;
                    JOptionPane.showMessageDialog(this, "Cập nhật nội dung thành công!", "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể cập nhật nội dung!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                NoiDung newNoiDung = new NoiDung(null, tenNoiDung, tuoiDuoi, tuoiTren, gioiTinh, team);
                NoiDung created = noiDungService.createNoiDung(newNoiDung);
                if (created != null) {
                    saved = true;
                    resultNoiDung = created;
                }
                JOptionPane.showMessageDialog(this, "Thêm nội dung thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public NoiDung getResultNoiDung() {
        return resultNoiDung;
    }
}
