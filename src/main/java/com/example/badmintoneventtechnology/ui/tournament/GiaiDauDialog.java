package com.example.badmintoneventtechnology.ui.tournament;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.ZoneId;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.example.badmintoneventtechnology.config.Prefs;
import com.example.badmintoneventtechnology.model.tournament.GiaiDau;
import com.example.badmintoneventtechnology.service.tournament.GiaiDauService;
import com.toedter.calendar.JDateChooser;

/**
 * Dialog để thêm/sửa giải đấu (chỉ lưu ngày)
 */
public class GiaiDauDialog extends JDialog {
    private final GiaiDauService giaiDauService;
    private final GiaiDau originalGiaiDau;
    private final boolean isEditMode;

    private JTextField tenGiaiField;
    private JDateChooser ngayBdChooser;
    private JDateChooser ngayKtChooser;
    private JButton btnSave, btnCancel;

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

        // Date chooser chỉ có ngày
        ngayBdChooser = new JDateChooser();
        ngayBdChooser.setDateFormatString("dd/MM/yyyy");

        ngayKtChooser = new JDateChooser();
        ngayKtChooser.setDateFormatString("dd/MM/yyyy");

        btnSave = new JButton(isEditMode ? "Cập nhật" : "Thêm mới");
        btnCancel = new JButton("Hủy");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Tên giải đấu
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
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
        formPanel.add(ngayBdChooser, gbc);

        // Ngày kết thúc
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Ngày kết thúc:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(ngayKtChooser, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

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

            // originalGiaiDau.getNgayBd()/getNgayKt() là LocalDate
            if (originalGiaiDau.getNgayBd() != null) {
                java.util.Date dateBd = java.sql.Date.valueOf(originalGiaiDau.getNgayBd());
                ngayBdChooser.setDate(dateBd);
            } else {
                ngayBdChooser.setDate(null);
            }

            if (originalGiaiDau.getNgayKt() != null) {
                java.util.Date dateKt = java.sql.Date.valueOf(originalGiaiDau.getNgayKt());
                ngayKtChooser.setDate(dateKt);
            } else {
                ngayKtChooser.setDate(null);
            }
        }
    }

    private void saveGiaiDau() {
        try {
            // 1) Validate tên
            String tenGiai = tenGiaiField.getText().trim();
            if (tenGiai.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên giải đấu không được để trống!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2) Lấy userId từ Prefs
            Prefs prefs = new Prefs();
            int idUser = prefs.getInt("userId", -1);
            if (idUser <= 0) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy User ID hợp lệ.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3) Lấy ngày từ JDateChooser -> LocalDate
            java.util.Date utilBd = ngayBdChooser.getDate();
            java.util.Date utilKt = ngayKtChooser.getDate();

            LocalDate ngayBd = (utilBd != null)
                    ? utilBd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    : null;
            LocalDate ngayKt = (utilKt != null)
                    ? utilKt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    : null;

            // 4) Validate theo ngày
            if (ngayBd != null && ngayKt != null && ngayBd.isAfter(ngayKt)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu không thể sau ngày kết thúc!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 5) Lưu
            if (isEditMode) {
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
                // Giả định service nhận LocalDate
                GiaiDau newGiaiDau = giaiDauService.createGiaiDau(tenGiai, ngayBd, ngayKt, idUser);
                JOptionPane.showMessageDialog(this, "Thêm giải đấu thành công!\nID: ",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
