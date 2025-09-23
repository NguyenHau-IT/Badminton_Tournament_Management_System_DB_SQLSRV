package com.example.badmintoneventtechnology.ui.club;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.example.badmintoneventtechnology.model.club.CauLacBo;
import com.example.badmintoneventtechnology.service.club.CauLacBoService;

public class CauLacBoManagementPanel extends JPanel {
    private final CauLacBoService service;

    private JTable table;
    private DefaultTableModel model;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;

    public CauLacBoManagementPanel(CauLacBoService service) {
        this.service = service;
        init();
        layoutUi();
        reload();
    }

    private void init() {
        model = new DefaultTableModel(new Object[] { "ID", "Tên CLB", "Tên ngắn" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        btnAdd = new JButton("Thêm mới");
        btnEdit = new JButton("Sửa");
        btnDelete = new JButton("Xóa");
        btnRefresh = new JButton("Làm mới");

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> reload());
    }

    private void layoutUi() {
        setLayout(new BorderLayout(10, 10));
        JPanel top = new JPanel();
        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);
        top.add(btnRefresh);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void reload() {
        try {
            List<CauLacBo> list = service.findAll();
            model.setRowCount(0);
            for (CauLacBo c : list) {
                model.addRow(new Object[] { c.getId(), c.getTenClb(), c.getTenNgan() });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAdd() {
        CauLacBoDialog dialog = new CauLacBoDialog(null, "Thêm CLB", null, service);
        dialog.setVisible(true);
        reload();
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn CLB để sửa", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer id = (Integer) model.getValueAt(row, 0);
        try {
            CauLacBo current = service.findOne(id);
            CauLacBoDialog dialog = new CauLacBoDialog(null, "Sửa CLB", current, service);
            dialog.setVisible(true);
            reload();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lấy thông tin: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn CLB để xóa", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer id = (Integer) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa CLB này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;
        try {
            service.delete(id);
            JOptionPane.showMessageDialog(this, "Đã xóa CLB", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            reload();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Xóa thất bại: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
