package com.example.badmintoneventtechnology.ui.tournament;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.example.badmintoneventtechnology.model.tournament.SuKien;

/**
 * Dialog thêm / sửa sự kiện (nội dung thi đấu)
 */
public class SuKienFormDialog extends JDialog {
    private final SuKien suKien; // null nếu thêm mới
    private boolean ok = false;

    private JComboBox<String> cmbMa;
    private JTextField txtTen;
    private JTextField txtNhomTuoi;
    private JTextField txtTrinhDo;
    private JSpinner spSoLuong;
    private JTextField txtLuatThiDau;
    private JComboBox<String> cmbLoaiBang;

    public SuKienFormDialog(SuKien suKien) {
        this.suKien = suKien;
        setModal(true);
        setTitle(suKien == null ? "Thêm nội dung" : "Sửa nội dung");
        setSize(420, 320);
        setLocationRelativeTo(null);
        initUI();
        if (suKien != null)
            fillForm(suKien);
    }

    private void initUI() {
        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
        form.setBorder(new javax.swing.border.EmptyBorder(12, 16, 12, 16));

        form.add(new JLabel("Mã:"));
        cmbMa = new JComboBox<>(new String[] { "DNM", "DNu", "DoNam", "DoNu", "DoNamNu" });
        form.add(cmbMa);

        form.add(new JLabel("Tên:"));
        txtTen = new JTextField();
        form.add(txtTen);

        form.add(new JLabel("Nhóm tuổi:"));
        txtNhomTuoi = new JTextField();
        form.add(txtNhomTuoi);

        form.add(new JLabel("Trình độ:"));
        txtTrinhDo = new JTextField();
        form.add(txtTrinhDo);

        form.add(new JLabel("Số lượng tối đa:"));
        spSoLuong = new JSpinner(new SpinnerNumberModel(64, 2, 512, 2));
        form.add(spSoLuong);

        form.add(new JLabel("Luật thi đấu:"));
        txtLuatThiDau = new JTextField("3x21 rally");
        form.add(txtLuatThiDau);

        form.add(new JLabel("Loại bảng:"));
        cmbLoaiBang = new JComboBox<>(new String[] { "LOAI_TRUC_TIEP", "VONG_TRON" });
        form.add(cmbLoaiBang);

        add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOK = new JButton("OK");
        JButton btnCancel = new JButton("Hủy");
        btnPanel.add(btnOK);
        btnPanel.add(btnCancel);
        add(btnPanel, BorderLayout.SOUTH);

        btnOK.addActionListener(e -> {
            if (validateForm()) {
                ok = true;
                dispose();
            }
        });
        btnCancel.addActionListener(e -> {
            ok = false;
            dispose();
        });
    }

    private boolean validateForm() {
        if (cmbMa.getSelectedItem() == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Chọn mã nội dung");
            return false;
        }
        if (txtTen.getText().trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Tên không được trống");
            return false;
        }
        return true;
    }

    private void fillForm(SuKien s) {
        cmbMa.setSelectedItem(s.getMa());
        txtTen.setText(s.getTen());
        txtNhomTuoi.setText(s.getNhomTuoi());
        txtTrinhDo.setText(s.getTrinhDo());
        spSoLuong.setValue(s.getSoLuong());
        txtLuatThiDau.setText(s.getLuatThiDau());
        cmbLoaiBang.setSelectedItem(s.getLoaiBang());
    }

    public boolean showDialog() {
        setVisible(true);
        return ok;
    }

    public SuKien getSuKienResult(int giaiId) {
        SuKien s = (suKien == null) ? new SuKien() : suKien;
        s.setGiaiId(giaiId);
        s.setMa((String) cmbMa.getSelectedItem());
        s.setTen(txtTen.getText().trim());
        s.setNhomTuoi(txtNhomTuoi.getText().trim());
        s.setTrinhDo(txtTrinhDo.getText().trim());
        s.setSoLuong((Integer) spSoLuong.getValue());
        s.setLuatThiDau(txtLuatThiDau.getText().trim());
        s.setLoaiBang((String) cmbLoaiBang.getSelectedItem());
        return s;
    }
}
