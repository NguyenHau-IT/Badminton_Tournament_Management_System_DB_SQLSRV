package com.example.badmintoneventtechnology.ui.tournament;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.example.badmintoneventtechnology.model.tournament.Giai;
import com.example.badmintoneventtechnology.service.db.DatabaseService;

public class GiaiTab extends JPanel {
    private final DatabaseService service;
    private JTable table;
    private DefaultTableModel model;
    private JButton btnAdd, btnEdit, btnDelete;

    public GiaiTab(DatabaseService service) {
        this.service = service;
        setLayout(new BorderLayout());
        initUI();
        // Không load data ngay trong constructor
        // loadData();
    }

    private void initUI() {
        model = new DefaultTableModel(
                new Object[] { "ID", "Tên giải", "Cấp độ", "Địa điểm", "Thành phố", "Ngày bắt đầu", "Ngày kết thúc" },
                0);
        table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAdd = new JButton("Thêm");
        btnEdit = new JButton("Sửa");
        btnDelete = new JButton("Xoá");
        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        add(btnPanel, BorderLayout.NORTH);

        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
    }

    private void loadData() {
        model.setRowCount(0);
        // Kiểm tra connection trước khi truy vấn
        if (!service.isConnected()) {
            return;
        }
        try {
            List<Giai> list = service.getAllGiai();
            for (Giai g : list) {
                model.addRow(new Object[] { g.getGiaiId(), g.getTen(), g.getCapDo(), g.getDiaDiem(), g.getThanhPho(),
                        g.getNgayBd(), g.getNgayKt() });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu giải: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog() {
        if (!service.isConnected()) {
            JOptionPane.showMessageDialog(this, "Chưa kết nối database!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        GiaiFormDialog dialog = new GiaiFormDialog(null);
        if (dialog.showDialog()) {
            Giai g = dialog.getGiai();
            service.addGiai(g);
            loadData();
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row == -1)
            return;
        if (!service.isConnected()) {
            JOptionPane.showMessageDialog(this, "Chưa kết nối database!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        Giai g = service.getGiaiById(id);
        GiaiFormDialog dialog = new GiaiFormDialog(g);
        if (dialog.showDialog()) {
            Giai updated = dialog.getGiai();
            updated.setGiaiId(id);
            service.updateGiai(updated);
            loadData();
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1)
            return;
        if (!service.isConnected()) {
            JOptionPane.showMessageDialog(this, "Chưa kết nối database!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Xoá giải này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.deleteGiai(id);
            loadData();
        }
    }

    // Thêm method để load data từ bên ngoài khi đã kết nối DB
    public void refreshData() {
        loadData();
    }
}
