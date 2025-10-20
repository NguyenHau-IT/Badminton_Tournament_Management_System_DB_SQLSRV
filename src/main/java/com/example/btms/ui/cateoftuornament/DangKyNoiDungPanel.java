package com.example.btms.ui.cateoftuornament;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;


/**
 * Tab đăng ký nội dung cho giải đấu đã chọn (dựa theo Prefs.selectedGiaiDauId)
 */
public class DangKyNoiDungPanel extends JPanel {

    // --- Header controls
    private final JLabel lblGiaiInfo = new JLabel("Giải đã chọn: (chưa chọn)");
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JButton btnShowAll = new JButton("Hiện tất cả");
    private final JButton btnAdd = new JButton("Thêm nội dung");
    private final JButton btnEdit = new JButton("Sửa nội dung");
    private final JTextField txtSearch = new JTextField(20);
    private final JLabel lblCount = new JLabel("0/0 nội dung");

    // --- Table
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Đăng ký", "ID", "Nội dung", "Tuổi dưới", "Tuổi trên", "Giới tính", "Đánh đôi" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 0;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return switch (c) {
                case 0 -> Boolean.class;
                case 1, 3, 4 -> Integer.class;
                default -> String.class;
            };
        }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    public DangKyNoiDungPanel() {
        setLayout(new BorderLayout(8, 8));

        // top
        JPanel top = new JPanel(new BorderLayout(0, 4));
        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        infoRow.add(lblGiaiInfo);
        JPanel controlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controlRow.add(btnRefresh);
        controlRow.add(new JLabel("Tìm kiếm:"));
        controlRow.add(txtSearch);
        controlRow.add(lblCount);
        controlRow.add(btnShowAll);
        controlRow.add(btnAdd);
        controlRow.add(btnEdit);
        top.add(infoRow, BorderLayout.NORTH);
        top.add(controlRow, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // table
        table.setRowSorter(sorter);
        TableColumn checkCol = table.getColumnModel().getColumn(0);
        checkCol.setMaxWidth(90);
        checkCol.setMinWidth(80);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 1; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(2).setCellRenderer(left);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    // --- Getters cho Controller
    public JLabel getLblGiaiInfo() {
        return lblGiaiInfo;
    }

    public JButton getBtnRefresh() {
        return btnRefresh;
    }

    public JButton getBtnShowAll() {
        return btnShowAll;
    }

    public JButton getBtnAdd() {
        return btnAdd;
    }

    public JButton getBtnEdit() {
        return btnEdit;
    }

    public JTextField getTxtSearch() {
        return txtSearch;
    }

    public JLabel getLblCount() {
        return lblCount;
    }

    public JTable getTable() {
        return table;
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public TableRowSorter<DefaultTableModel> getSorter() {
        return sorter;
    }

    // tiện ích
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