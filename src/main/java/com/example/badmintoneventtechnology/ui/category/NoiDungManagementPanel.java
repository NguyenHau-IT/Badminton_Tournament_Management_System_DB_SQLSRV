package com.example.badmintoneventtechnology.ui.category;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.SwingConstants;

import com.example.badmintoneventtechnology.model.category.NoiDung;
import com.example.badmintoneventtechnology.service.category.NoiDungService;

public class NoiDungManagementPanel extends JPanel {
    private final NoiDungService noiDungService;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;

    public NoiDungManagementPanel(NoiDungService noiDungService) {
        this.noiDungService = noiDungService;
        initializeComponents();
        setupLayout();
        loadData();
    }

    private void initializeComponents() {
        String[] columns = { "ID", "Tên nội dung", "Tuổi dưới", "Tuổi trên", "Giới tính", "Thể loại" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Center align selected columns (ID, Tuổi dưới, Tuổi trên, Giới tính, Thể loại)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        int[] centerCols = { 0, 2, 3, 4, 5 };
        for (int col : centerCols) {
            if (col < table.getColumnModel().getColumnCount()) {
                table.getColumnModel().getColumn(col).setCellRenderer(centerRenderer);
            }
        }

        // Center align table header text
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader()
                .getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        btnAdd = new JButton("Thêm mới");
        btnEdit = new JButton("Sửa");
        btnDelete = new JButton("Xóa");
        btnRefresh = new JButton("Làm mới");

        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
        btnRefresh.addActionListener(e -> loadData());
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRefresh);
        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadData() {
        try {
            List<NoiDung> list = noiDungService.getAllNoiDung();
            tableModel.setRowCount(0);
            for (NoiDung nd : list) {
                String gt = Optional.ofNullable(nd.getGioiTinh()).orElse("").trim();

                String genderText = "f".equalsIgnoreCase(gt) ? "Nữ" : "m".equalsIgnoreCase(gt) ? "Nam" : "Nam, Nữ";

                String teamText = Boolean.TRUE.equals(nd.getTeam()) ? "Đôi" : "Đơn";

                tableModel.addRow(new Object[] {
                        nd.getId(),
                        nd.getTenNoiDung(),
                        nd.getTuoiDuoi(),
                        nd.getTuoiTren(),
                        genderText,
                        teamText
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog() {
        NoiDungDialog dialog = new NoiDungDialog(null, "Thêm nội dung", null, noiDungService);
        dialog.setVisible(true);
        loadData();
    }

    private void showEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nội dung để sửa", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer id = (Integer) tableModel.getValueAt(selectedRow, 0);
        try {
            Optional<NoiDung> ndOpt = noiDungService.getNoiDungById(id);
            if (ndOpt.isPresent()) {
                NoiDungDialog dialog = new NoiDungDialog(null, "Sửa nội dung", ndOpt.get(), noiDungService);
                dialog.setVisible(true);
                loadData();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi lấy thông tin nội dung: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nội dung để xóa", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer id = (Integer) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa nội dung này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = noiDungService.deleteNoiDung(id);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Xóa nội dung thành công!", "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa nội dung", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa nội dung: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
