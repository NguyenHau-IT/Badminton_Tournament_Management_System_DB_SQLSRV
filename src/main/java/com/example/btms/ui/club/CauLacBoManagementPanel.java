package com.example.btms.ui.club;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.example.btms.model.club.CauLacBo;
import com.example.btms.service.club.CauLacBoService;

public class CauLacBoManagementPanel extends JPanel {
    private final CauLacBoService service;

    private JTable table;
    private DefaultTableModel model;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private JTextField txtSearch;
    private JLabel lblCount;
    private TableRowSorter<DefaultTableModel> sorter;

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

        // Sorter + filter
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        btnAdd = new JButton("Thêm mới");
        btnEdit = new JButton("Sửa");
        btnDelete = new JButton("Xóa");
        btnRefresh = new JButton("Làm mới");

        txtSearch = new JTextField(15);
        lblCount = new JLabel("0/0");

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> reload());

        // Filter as you type across Tên CLB (col 1) and Tên ngắn (col 2)
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFilter();
            }
        });
    }

    private void layoutUi() {
        setLayout(new BorderLayout(10, 10));
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

    private void reload() {
        try {
            List<CauLacBo> list = service.findAll();
            model.setRowCount(0);
            for (CauLacBo c : list) {
                model.addRow(new Object[] { c.getId(), c.getTenClb(), c.getTenNgan() });
            }
            // re-apply filter and update counts after reload
            updateFilter();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAdd() {
        CauLacBoDialog dialog = new CauLacBoDialog(null, "Thêm CLB", null, service);
        dialog.setVisible(true);
        reload();
        updateCountLabel();
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn CLB để sửa", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        Integer id = (Integer) model.getValueAt(modelRow, 0);
        try {
            CauLacBo current = service.findOne(id);
            CauLacBoDialog dialog = new CauLacBoDialog(null, "Sửa CLB", current, service);
            dialog.setVisible(true);
            reload();
            updateCountLabel();
        } catch (IllegalArgumentException | IllegalStateException | java.util.NoSuchElementException ex) {
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
        int modelRow = table.convertRowIndexToModel(row);
        Integer id = (Integer) model.getValueAt(modelRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa CLB này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;
        try {
            service.delete(id);
            JOptionPane.showMessageDialog(this, "Đã xóa CLB", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            reload();
            updateCountLabel();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Xóa thất bại: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFilter() {
        String q = txtSearch.getText();
        if (q == null || q.trim().isEmpty()) {
            sorter.setRowFilter(null);
            updateCountLabel();
            return;
        }
        String pattern = java.util.regex.Pattern.quote(q.trim());
        RowFilter<DefaultTableModel, Integer> f1 = RowFilter.regexFilter("(?i)" + pattern, 1);
        RowFilter<DefaultTableModel, Integer> f2 = RowFilter.regexFilter("(?i)" + pattern, 2);
        sorter.setRowFilter(RowFilter.orFilter(java.util.List.of(f1, f2)));
        updateCountLabel();
    }

    private void updateCountLabel() {
        int visible = table.getRowCount();
        int total = model.getRowCount();
        lblCount.setText(visible + "/" + total + " câu lạc bộ");
    }

    /** Public refresh API for MainFrame and tree context menu. */
    public void refreshAll() {
        reload();
        updateCountLabel();
    }
}
