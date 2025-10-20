package com.example.btms.ui.club;

import java.awt.BorderLayout;
import java.awt.Cursor;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;


public class CauLacBoManagementPanel extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Tên CLB", "Tên ngắn" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    private final JButton btnAdd = new JButton("Thêm mới");
    private final JButton btnEdit = new JButton("Sửa");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JTextField txtSearch = new JTextField(15);
    private final JLabel lblCount = new JLabel("0/0");

    public CauLacBoManagementPanel() {
        setLayout(new BorderLayout(10, 10));

        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);

        JPanel top = new JPanel();
        top.add(new JLabel("Tìm:"));
        top.add(txtSearch);
        top.add(lblCount);
        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);
        top.add(btnRefresh);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    // ---- Getters cho Controller
    public JTable getTable() {
        return table;
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public TableRowSorter<DefaultTableModel> getSorter() {
        return sorter;
    }

    public JButton getBtnAdd() {
        return btnAdd;
    }

    public JButton getBtnEdit() {
        return btnEdit;
    }

    public JButton getBtnDelete() {
        return btnDelete;
    }

    public JButton getBtnRefresh() {
        return btnRefresh;
    }

    public JTextField getTxtSearch() {
        return txtSearch;
    }

    public JLabel getLblCount() {
        return lblCount;
    }

    // tiện ích nhỏ
    public void info(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    public void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void setBusy(boolean busy) {
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        setEnabled(!busy);
    }
}