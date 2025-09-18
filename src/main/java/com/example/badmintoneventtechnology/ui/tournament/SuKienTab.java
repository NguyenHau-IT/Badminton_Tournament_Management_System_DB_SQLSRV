package com.example.badmintoneventtechnology.ui.tournament;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.ListSelectionModel;

import com.example.badmintoneventtechnology.model.tournament.Giai;
import com.example.badmintoneventtechnology.model.tournament.SuKien;
import com.example.badmintoneventtechnology.service.db.DatabaseService;

/**
 * Tab quản lý nội dung thi đấu (SuKien) cho giải đang chọn
 */
public class SuKienTab extends JPanel {
    private final DatabaseService service;
    private DefaultTableModel model;
    private JTable table;
    private JButton btnAdd, btnEdit, btnDelete;
    private Giai selectedGiai;
    private final JLabel lblCurrentGiai = new JLabel("Chưa chọn giải");
    // Filter & sort components
    private JTextField txtFilterTen;
    private JSpinner spFilterAgeMin;
    private JSpinner spFilterAgeMax;
    private JComboBox<String> cmbSort;
    private TableRowSorter<DefaultTableModel> sorter;

    public SuKienTab(DatabaseService service) {
        this.service = service;
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        model = new DefaultTableModel(
                new Object[] { "ID", "Mã", "Tên", "Tuổi nhỏ", "Tuổi lớn nhất", "Trình độ", "Số lượng", "Luật",
                        "Loại bảng" },
                0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Cập nhật trạng thái nút khi chọn dòng
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtons();
            }
        });
        // Double-click để mở sửa nhanh
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    editSuKien();
                }
            }
        });
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAdd = new JButton("Thêm");
        btnEdit = new JButton("Sửa");
        btnDelete = new JButton("Xoá");
        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        top.add(btnPanel, BorderLayout.WEST);

        // Right side with filters + current tournament label
        JPanel rightPanel = new JPanel(new BorderLayout());
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        filterPanel.add(new JLabel("Tên:"));
        txtFilterTen = new JTextField(10);
        filterPanel.add(txtFilterTen);
        filterPanel.add(new JLabel("Tuổi:"));
        spFilterAgeMin = new JSpinner(new SpinnerNumberModel(0, 0, 120, 1));
        spFilterAgeMax = new JSpinner(new SpinnerNumberModel(120, 0, 120, 1));
        filterPanel.add(spFilterAgeMin);
        filterPanel.add(new JLabel("-"));
        filterPanel.add(spFilterAgeMax);
        filterPanel.add(new JLabel("Sắp xếp:"));
        cmbSort = new JComboBox<>(new String[] { "Mặc định", "Tên A-Z", "Tên Z-A", "Tuổi min tăng", "Tuổi min giảm",
                "Tuổi max tăng", "Tuổi max giảm" });
        filterPanel.add(cmbSort);
        JButton btnApply = new JButton("Lọc");
        JButton btnClear = new JButton("Reset");
        filterPanel.add(btnApply);
        filterPanel.add(btnClear);
        rightPanel.add(filterPanel, BorderLayout.NORTH);
        rightPanel.add(lblCurrentGiai, BorderLayout.SOUTH);
        top.add(rightPanel, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        btnAdd.addActionListener(e -> addSuKien());
        btnEdit.addActionListener(e -> editSuKien());
        btnDelete.addActionListener(e -> deleteSuKien());
        btnApply.addActionListener(e -> applyFilter());
        btnClear.addActionListener(e -> clearFilter());
        cmbSort.addActionListener(e -> applySort());
        txtFilterTen.addActionListener(e -> applyFilter());

        updateButtons();
    }

    private void updateButtons() {
        boolean enabled = selectedGiai != null && service.isConnected();
        btnAdd.setEnabled(enabled);
        btnEdit.setEnabled(enabled && table.getSelectedRow() != -1);
        btnDelete.setEnabled(enabled && table.getSelectedRow() != -1);
    }

    public void setSelectedGiai(Giai g) {
        this.selectedGiai = g;
        lblCurrentGiai.setText(g == null ? "Chưa chọn giải" : ("Giải: " + g.getTen()));
        loadData();
        updateButtons();
    }

    private void loadData() {
        model.setRowCount(0);
        if (selectedGiai == null || !service.isConnected())
            return;
        List<SuKien> list = service.getSuKienByGiai(selectedGiai.getGiaiId());
        for (SuKien s : list) {
            int min = 0, max = 0;
            String nt = s.getNhomTuoi();
            if (nt != null && nt.matches("\\d+\\-\\d+")) {
                String[] parts = nt.split("-");
                try {
                    min = Integer.parseInt(parts[0]);
                    max = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                }
            }
            model.addRow(new Object[] { s.getSuKienId(), s.getMa(), s.getTen(), min, max, s.getTrinhDo(),
                    s.getSoLuong(), s.getLuatThiDau(), s.getLoaiBang() });
        }
        // Sau khi load lại dữ liệu thì reset chọn & nút
        if (table.getRowCount() > 0) {
            table.clearSelection();
        }
        updateButtons();
        applySort();
        applyFilter();
    }

    private void addSuKien() {
        if (selectedGiai == null)
            return;
        SuKienFormDialog dlg = new SuKienFormDialog(null);
        if (dlg.showDialog()) {
            SuKien s = dlg.getSuKienResult(selectedGiai.getGiaiId());
            try {
                service.addSuKien(s);
                loadData();
            } catch (RuntimeException ex) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Không thêm được sự kiện:\n" + ex.getMessage()
                                + "\nGợi ý: kiểm tra trùng (mã, nhóm tuổi, trình độ) hoặc mã không hợp lệ.",
                        "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSuKien() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1 || selectedGiai == null)
            return;
        int row = table.convertRowIndexToModel(viewRow);
        int id = (int) model.getValueAt(row, 0);
        SuKien s = new SuKien();
        s.setSuKienId(id);
        s.setGiaiId(selectedGiai.getGiaiId());
        s.setMa((String) model.getValueAt(row, 1));
        s.setTen((String) model.getValueAt(row, 2));
        int tuoiMin = (int) model.getValueAt(row, 3);
        int tuoiMax = (int) model.getValueAt(row, 4);
        s.setNhomTuoi(tuoiMin + "-" + tuoiMax);
        s.setTrinhDo((String) model.getValueAt(row, 5));
        s.setSoLuong((int) model.getValueAt(row, 6));
        s.setLuatThiDau((String) model.getValueAt(row, 7));
        s.setLoaiBang((String) model.getValueAt(row, 8));

        SuKienFormDialog dlg = new SuKienFormDialog(s);
        if (dlg.showDialog()) {
            SuKien updated = dlg.getSuKienResult(selectedGiai.getGiaiId());
            updated.setSuKienId(id);
            service.updateSuKien(updated);
            loadData();
        }
    }

    private void deleteSuKien() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1 || selectedGiai == null)
            return;
        int row = table.convertRowIndexToModel(viewRow);
        int id = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Xoá nội dung này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.deleteSuKien(id);
            loadData();
        }
    }

    private void applyFilter() {
        if (sorter == null)
            return;
        String ten = txtFilterTen.getText().trim().toLowerCase();
        int ageMin = (int) spFilterAgeMin.getValue();
        int ageMax = (int) spFilterAgeMax.getValue();
        RowFilter<DefaultTableModel, Object> rf = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String tenVal = String.valueOf(entry.getValue(2)).toLowerCase();
                int minVal = (int) entry.getValue(3);
                int maxVal = (int) entry.getValue(4);
                boolean matchTen = ten.isEmpty() || tenVal.contains(ten);
                boolean matchAge = (minVal >= ageMin) && (maxVal <= ageMax);
                return matchTen && matchAge;
            }
        };
        sorter.setRowFilter(rf);
    }

    private void clearFilter() {
        txtFilterTen.setText("");
        spFilterAgeMin.setValue(0);
        spFilterAgeMax.setValue(120);
        if (sorter != null)
            sorter.setRowFilter(null);
    }

    private void applySort() {
        if (sorter == null)
            return;
        String sel = (String) cmbSort.getSelectedItem();
        List<RowSorter.SortKey> keys = new ArrayList<>();
        if ("Tên A-Z".equals(sel))
            keys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        else if ("Tên Z-A".equals(sel))
            keys.add(new RowSorter.SortKey(2, SortOrder.DESCENDING));
        else if ("Tuổi min tăng".equals(sel))
            keys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));
        else if ("Tuổi min giảm".equals(sel))
            keys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
        else if ("Tuổi max tăng".equals(sel))
            keys.add(new RowSorter.SortKey(4, SortOrder.ASCENDING));
        else if ("Tuổi max giảm".equals(sel))
            keys.add(new RowSorter.SortKey(4, SortOrder.DESCENDING));
        else { // Mặc định: theo ID
            keys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        }
        sorter.setSortKeys(keys);
    }
}
