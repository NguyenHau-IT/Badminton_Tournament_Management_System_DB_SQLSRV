package com.example.btms.ui.category;

import java.awt.BorderLayout;
import java.awt.Cursor;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;


public class NoiDungManagementPanel extends JPanel {
    // ---- UI
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JButton btnAdd = new JButton("Thêm mới");
    private final JButton btnEdit = new JButton("Sửa");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JComboBox<String> cmbColumn = new JComboBox<>();
    private final JTextField txtFilter = new JTextField(15);
    private final JLabel lblCount = new JLabel("0/0");
    private final TableRowSorter<DefaultTableModel> sorter;

    public NoiDungManagementPanel() {
        String[] columns = { "ID", "Tên nội dung", "Tuổi dưới", "Tuổi trên", "Giới tính", "Thể loại" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // căn giữa 1 số cột
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        int[] centerCols = { 0, 2, 3, 4, 5 };
        for (int c : centerCols) {
            if (c < table.getColumnModel().getColumnCount())
                table.getColumnModel().getColumn(c).setCellRenderer(center);
        }
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        // combo filter (bỏ cột ID)
        for (int i = 1; i < tableModel.getColumnCount(); i++) {
            cmbColumn.addItem(tableModel.getColumnName(i));
        }

        setLayout(new BorderLayout(10, 10));
        JPanel top = new JPanel();
        top.add(new JLabel("Lọc theo:"));
        top.add(cmbColumn);
        top.add(txtFilter);
        top.add(lblCount);
        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);
        top.add(btnRefresh);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    // ---- Getters để Controller dùng
    public JTable getTable() {
        return table;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
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

    public JComboBox<String> getCmbColumn() {
        return cmbColumn;
    }

    public JTextField getTxtFilter() {
        return txtFilter;
    }

    public JLabel getLblCount() {
        return lblCount;
    }

    public TableRowSorter<DefaultTableModel> getSorter() {
        return sorter;
    }

    // tiện ích nhỏ
    public void setBusy(boolean busy) {
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        setEnabled(!busy);
    }

    public void info(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}