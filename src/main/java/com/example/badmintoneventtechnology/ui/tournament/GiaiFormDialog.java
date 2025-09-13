package com.example.badmintoneventtechnology.ui.tournament;

import com.example.badmintoneventtechnology.model.tournament.Giai;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class GiaiFormDialog extends JDialog {
    private JTextField txtTen, txtCapDo, txtDiaDiem, txtThanhPho;
    private JSpinner spNgayBd, spNgayKt;
    private boolean ok = false;
    private Giai giai;

    public GiaiFormDialog(Giai giai) {
        setModal(true);
        setTitle(giai == null ? "Thêm giải" : "Sửa giải");
        setSize(400, 300);
        setLocationRelativeTo(null);
        this.giai = giai;
        initUI();
        if (giai != null) fillForm(giai);
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        panel.add(new JLabel("Tên giải:"));
        txtTen = new JTextField();
        panel.add(txtTen);
        panel.add(new JLabel("Cấp độ:"));
        txtCapDo = new JTextField();
        panel.add(txtCapDo);
        panel.add(new JLabel("Địa điểm:"));
        txtDiaDiem = new JTextField();
        panel.add(txtDiaDiem);
        panel.add(new JLabel("Thành phố:"));
        txtThanhPho = new JTextField();
        panel.add(txtThanhPho);
        panel.add(new JLabel("Ngày bắt đầu:"));
        spNgayBd = new JSpinner(new SpinnerDateModel());
        panel.add(spNgayBd);
        panel.add(new JLabel("Ngày kết thúc:"));
        spNgayKt = new JSpinner(new SpinnerDateModel());
        panel.add(spNgayKt);
        add(panel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOK = new JButton("OK");
        JButton btnCancel = new JButton("Huỷ");
        btnPanel.add(btnOK);
        btnPanel.add(btnCancel);
        add(btnPanel, BorderLayout.SOUTH);

        btnOK.addActionListener(e -> {
            ok = true;
            dispose();
        });
        btnCancel.addActionListener(e -> {
            ok = false;
            dispose();
        });
    }

    private void fillForm(Giai g) {
        txtTen.setText(g.getTen());
        txtCapDo.setText(g.getCapDo());
        txtDiaDiem.setText(g.getDiaDiem());
        txtThanhPho.setText(g.getThanhPho());
        // Ngày: cần chuyển LocalDate sang java.util.Date
        spNgayBd.setValue(java.sql.Date.valueOf(g.getNgayBd()));
        spNgayKt.setValue(java.sql.Date.valueOf(g.getNgayKt()));
    }

    public boolean showDialog() {
        setVisible(true);
        return ok;
    }

    public Giai getGiai() {
        Giai g = new Giai();
        g.setTen(txtTen.getText());
        g.setCapDo(txtCapDo.getText());
        g.setDiaDiem(txtDiaDiem.getText());
        g.setThanhPho(txtThanhPho.getText());
        java.util.Date bd = (java.util.Date) spNgayBd.getValue();
        java.util.Date kt = (java.util.Date) spNgayKt.getValue();
        g.setNgayBd(new java.sql.Date(bd.getTime()).toLocalDate());
        g.setNgayKt(new java.sql.Date(kt.getTime()).toLocalDate());
        return g;
    }
}
