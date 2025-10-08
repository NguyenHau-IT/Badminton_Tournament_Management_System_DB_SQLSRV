package com.example.btms.ui.player;

import java.awt.BorderLayout;
import java.time.format.DateTimeFormatter;
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
import com.example.btms.model.player.VanDongVien;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.player.VanDongVienService;

public class VanDongVienManagementPanel extends JPanel {
    private final VanDongVienService vdvService;
    private final CauLacBoService clbService;
    private static final DateTimeFormatter DOB_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private JTable table;
    private DefaultTableModel model;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private JTextField txtSearch;
    private JLabel lblCount;
    private TableRowSorter<DefaultTableModel> sorter;

    public VanDongVienManagementPanel(VanDongVienService vdvService, CauLacBoService clbService) {
        this.vdvService = vdvService;
        this.clbService = clbService;
        init();
        layoutUi();
        reload();
    }

    private void init() {
        model = new DefaultTableModel(new Object[] { "ID", "Họ tên", "Ngày sinh", "Giới tính", "CLB" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
            List<VanDongVien> list = vdvService.findAll();
            model.setRowCount(0);
            for (VanDongVien v : list) {
                String dob = (v.getNgaySinh() != null) ? v.getNgaySinh().format(DOB_FMT) : "";
                String gt = "";
                if (v.getGioiTinh() != null) {
                    String g = v.getGioiTinh().trim();
                    if (g.equalsIgnoreCase("m"))
                        gt = "Nam";
                    else if (g.equalsIgnoreCase("f"))
                        gt = "Nữ";
                    else if (!g.isEmpty())
                        gt = g; // fallback hiển thị nguyên gốc nếu khác m/f
                }
                String clb = "";
                if (v.getIdClb() != null) {
                    try {
                        // Tối giản: tải tên CLB bằng cách duyệt all (tránh thêm repo phụ);
                        for (CauLacBo c : clbService.findAll()) {
                            if (c.getId() != null && c.getId().equals(v.getIdClb())) {
                                clb = c.getTenClb();
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
                model.addRow(new Object[] { v.getId(), v.getHoTen(), dob, gt, clb });
            }
            updateFilter();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAdd() {
        VanDongVienDialog dialog = new VanDongVienDialog(null, "Thêm VĐV", null, vdvService, clbService);
        dialog.setVisible(true);
        reload();
        updateCountLabel();
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn VĐV để sửa", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        Integer id = (Integer) model.getValueAt(modelRow, 0);
        try {
            VanDongVien current = vdvService.findOne(id);
            VanDongVienDialog dialog = new VanDongVienDialog(null, "Sửa VĐV", current, vdvService, clbService);
            dialog.setVisible(true);
            reload();
            updateCountLabel();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lấy thông tin: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn VĐV để xóa", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        Integer id = (Integer) model.getValueAt(modelRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa VĐV này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;
        try {
            vdvService.delete(id);
            JOptionPane.showMessageDialog(this, "Đã xóa VĐV", "Thành công",
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
        RowFilter<DefaultTableModel, Integer> fHoTen = RowFilter.regexFilter("(?i)" + pattern, 1);
        RowFilter<DefaultTableModel, Integer> fClb = RowFilter.regexFilter("(?i)" + pattern, 4);
        sorter.setRowFilter(RowFilter.orFilter(java.util.List.of(fHoTen, fClb)));
        updateCountLabel();
    }

    private void updateCountLabel() {
        int visible = table.getRowCount();
        int total = model.getRowCount();
        lblCount.setText(visible + "/" + total + " vận động viên");
    }

    /** Public refresh API for MainFrame and tree context menu. */
    public void refreshAll() {
        reload();
        updateCountLabel();
    }
}
