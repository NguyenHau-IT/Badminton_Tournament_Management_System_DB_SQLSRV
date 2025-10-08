package com.example.btms.ui.category;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;

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
import com.example.btms.service.category.NoiDungService;

public class NoiDungDialog extends JDialog {
    private final NoiDungService noiDungService;
    private final NoiDung originalNoiDung;
    private final boolean isEditMode;

    private JTextField tenNoiDungField, tuoiDuoiField, tuoiTrenField;
    private JComboBox<String> gioiTinhCombo;
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
        gioiTinhCombo = new JComboBox<>(new String[] { "", "m", "f" });
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
        formPanel.add(new JLabel("Giới tính:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(gioiTinhCombo, gbc);

        // Ghi chú nhỏ dưới combobox
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel note = new JLabel("Ghi chú: m = Nam, f = Nữ, để trống = cả Nam và Nữ");
        note.setFont(note.getFont().deriveFont(note.getFont().getSize2D() - 1f));
        note.setForeground(java.awt.Color.DARK_GRAY);
        formPanel.add(note, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
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
        String gt = originalNoiDung.getGioiTinh();
        if (gt == null || gt.isBlank()) {
            gioiTinhCombo.setSelectedItem("");
        } else if (gt.equalsIgnoreCase("m")) {
            gioiTinhCombo.setSelectedItem("m");
        } else if (gt.equalsIgnoreCase("f")) {
            gioiTinhCombo.setSelectedItem("f");
        } else {
            // nếu dữ liệu cũ khác m/f, để trống để tránh gây hiểu nhầm
            gioiTinhCombo.setSelectedItem("");
        }
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
            String gioiTinh = ((String) gioiTinhCombo.getSelectedItem());
            if (gioiTinh != null)
                gioiTinh = gioiTinh.trim();
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
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Tuổi dưới/trên phải là số nguyên hợp lệ!", "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException se) {
            String msg = se.getMessage();
            if (msg == null && se.getCause() != null)
                msg = se.getCause().getMessage();
            JOptionPane.showMessageDialog(this,
                    "Lỗi cơ sở dữ liệu: " + (msg != null ? msg : se.getClass().getSimpleName()),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(this, iae.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException re) {
            String msg = re.getMessage();
            if (msg == null && re.getCause() != null)
                msg = re.getCause().getMessage();
            JOptionPane.showMessageDialog(this, "Lỗi: " + (msg != null ? msg : re.getClass().getSimpleName()), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public NoiDung getResultNoiDung() {
        return resultNoiDung;
    }
}
