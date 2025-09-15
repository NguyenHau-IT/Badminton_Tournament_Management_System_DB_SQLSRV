package com.example.badmintoneventtechnology.ui.tournament;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;

import com.example.badmintoneventtechnology.model.tournament.Giai;
import com.example.badmintoneventtechnology.model.tournament.SuKien;

public class GiaiNoiDungTab extends JPanel {
    private Giai selectedGiai;
    private JTable table;
    private DefaultTableModel model;
    private JButton btnAdd, btnEdit, btnDelete;

    public GiaiNoiDungTab() {
        setLayout(new BorderLayout());
        model = new DefaultTableModel(new Object[] { "Tên sự kiện", "Loại" }, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAdd = new JButton("Thêm");
        btnEdit = new JButton("Sửa");
        btnDelete = new JButton("Xoá");
        setBorder(new javax.swing.border.EmptyBorder(12, 16, 12, 16)); // Tạo khoảng cách với viền
        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        add(btnPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> showEditDialog(null));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                SuKien sk = getSuKienAt(row);
                showEditDialog(sk);
            }
        });
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this, "Xoá sự kiện này?", "Xác nhận",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteSuKien(row);
                }
            }
        });
    }

    public void setSelectedGiai(Giai giai) {
        this.selectedGiai = giai;
        reloadData();
    }

    private void reloadData() {
        model.setRowCount(0);
        if (selectedGiai != null) {
            List<SuKien> ds = selectedGiai.getSuKienList();
            for (SuKien sk : ds) {
                model.addRow(new Object[] { sk.getTenSuKien(), sk.getLoai() });
            }
        }
    }

    private SuKien getSuKienAt(int row) {
        if (selectedGiai == null)
            return null;
        List<SuKien> ds = selectedGiai.getSuKienList();
        if (row >= 0 && row < ds.size())
            return ds.get(row);
        return null;
    }

    private void deleteSuKien(int row) {
        if (selectedGiai == null)
            return;
        List<SuKien> ds = selectedGiai.getSuKienList();
        if (row >= 0 && row < ds.size()) {
            ds.remove(row);
            reloadData();
        }
    }

    private void showEditDialog(SuKien sk) {
        JDialog dlg = new JDialog(JOptionPane.getFrameForComponent(this), sk == null ? "Thêm sự kiện" : "Sửa sự kiện",
                true);
        dlg.setSize(300, 180);
        dlg.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new FlowLayout());
        JTextField txtTen = new JTextField(sk != null ? sk.getTenSuKien() : "", 18);
        JTextField txtLoai = new JTextField(sk != null ? sk.getLoai() : "", 12);
        panel.add(new JLabel("Tên sự kiện:"));
        panel.add(txtTen);
        panel.add(new JLabel("Loại:"));
        panel.add(txtLoai);
        JButton btnOK = new JButton("OK");
        JButton btnCancel = new JButton("Huỷ");
        panel.add(btnOK);
        panel.add(btnCancel);
        dlg.add(panel);
        btnOK.addActionListener(e -> {
            String ten = txtTen.getText().trim();
            String loai = txtLoai.getText().trim();
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Tên sự kiện không được để trống!");
                return;
            }
            if (sk == null) {
                SuKien newSk = new SuKien(ten, loai);
                selectedGiai.getSuKienList().add(newSk);
            } else {
                sk.setTenSuKien(ten);
                sk.setLoai(loai);
            }
            reloadData();
            dlg.dispose();
        });
        btnCancel.addActionListener(e -> dlg.dispose());
        dlg.setVisible(true);
    }
}
