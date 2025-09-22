package com.example.badmintoneventtechnology.ui.tournament;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.example.badmintoneventtechnology.model.tournament.GiaiDau;
import com.example.badmintoneventtechnology.service.tournament.GiaiDauService;

/**
 * Dialog để thêm/sửa giải đấu
 */
public class GiaiDauDialog extends JDialog {
    private final GiaiDauService giaiDauService;
    private final GiaiDau originalGiaiDau;
    private final boolean isEditMode;

    private JTextField tenGiaiField;
    private JTextField ngayBdField;
    private JTextField ngayKtField;
    private JTextField idUserField;
    private JButton btnSave, btnCancel;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public GiaiDauDialog(Window parent, String title, GiaiDau giaiDau, GiaiDauService giaiDauService) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.giaiDauService = giaiDauService;
        this.originalGiaiDau = giaiDau;
        this.isEditMode = giaiDau != null;

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
        tenGiaiField = new JTextField(30);
        ngayBdField = new JTextField(20);
        ngayKtField = new JTextField(20);
        idUserField = new JTextField(10);

        btnSave = new JButton(isEditMode ? "Cập nhật" : "Thêm mới");
        btnCancel = new JButton("Hủy");

        // Set placeholder text
        ngayBdField.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy HH:mm");
        ngayKtField.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy HH:mm");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Main form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Tên giải đấu
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tên giải đấu *:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(tenGiaiField, gbc);

        // Ngày bắt đầu
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Ngày bắt đầu:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(ngayBdField, gbc);

        // Ngày kết thúc
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Ngày kết thúc:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(ngayKtField, gbc);

        // ID User
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("ID User *:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(idUserField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add some padding
        formPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void setupEventHandlers() {
        btnSave.addActionListener(e -> saveGiaiDau());
        btnCancel.addActionListener(e -> dispose());
    }

    private void loadData() {
        if (originalGiaiDau != null) {
            tenGiaiField.setText(originalGiaiDau.getTenGiai());
            ngayBdField.setText(
                    originalGiaiDau.getNgayBd() != null ? originalGiaiDau.getNgayBd().format(DATE_TIME_FORMATTER) : "");
            ngayKtField.setText(
                    originalGiaiDau.getNgayKt() != null ? originalGiaiDau.getNgayKt().format(DATE_TIME_FORMATTER) : "");
            idUserField.setText(originalGiaiDau.getIdUser() != null ? originalGiaiDau.getIdUser().toString() : "");
        }
    }

    private void saveGiaiDau() {
        try {
            // Validation
            String tenGiai = tenGiaiField.getText().trim();
            if (tenGiai.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên giải đấu không được để trống!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String idUserText = idUserField.getText().trim();
            if (idUserText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID User không được để trống!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Long idUser;
            try {
                idUser = Long.parseLong(idUserText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID User phải là số!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Parse dates
            LocalDateTime ngayBd = parseDateTime(ngayBdField.getText().trim());
            LocalDateTime ngayKt = parseDateTime(ngayKtField.getText().trim());

            // Validate dates
            if (ngayBd != null && ngayKt != null && ngayBd.isAfter(ngayKt)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu không thể sau ngày kết thúc!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEditMode) {
                // Update existing
                originalGiaiDau.setTenGiai(tenGiai);
                originalGiaiDau.setNgayBd(ngayBd);
                originalGiaiDau.setNgayKt(ngayKt);
                originalGiaiDau.setIdUser(idUser);

                boolean updated = giaiDauService.updateGiaiDau(originalGiaiDau);
                if (updated) {
                    JOptionPane.showMessageDialog(this, "Cập nhật giải đấu thành công!",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể cập nhật giải đấu!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Create new
                GiaiDau newGiaiDau = giaiDauService.createGiaiDau(tenGiai, ngayBd, ngayKt, idUser);
                JOptionPane.showMessageDialog(this, "Thêm giải đấu thành công!\nID: " + newGiaiDau.getId(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateTimeStr.trim(), DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            // Try alternative formats
            try {
                // Try without time
                if (dateTimeStr.matches("\\d{2}/\\d{2}/\\d{4}")) {
                    return LocalDateTime.parse(dateTimeStr.trim() + " 00:00", DATE_TIME_FORMATTER);
                }
                // Try with different separator
                if (dateTimeStr.matches("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}")) {
                    return LocalDateTime.parse(dateTimeStr.trim().replace("-", "/"), DATE_TIME_FORMATTER);
                }
            } catch (DateTimeParseException ex) {
                // Ignore
            }

            throw new IllegalArgumentException("Định dạng ngày tháng không hợp lệ. Sử dụng: dd/MM/yyyy HH:mm");
        }
    }
}
