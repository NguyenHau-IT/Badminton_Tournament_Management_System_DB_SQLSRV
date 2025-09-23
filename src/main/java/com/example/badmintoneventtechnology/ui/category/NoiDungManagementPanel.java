package com.example.badmintoneventtechnology.ui.category;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.SwingConstants;

import com.example.badmintoneventtechnology.model.category.NoiDung;
import com.example.badmintoneventtechnology.service.category.NoiDungService;

public class NoiDungManagementPanel extends JPanel {
    private final NoiDungService noiDungService;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private JComboBox<String> cmbColumn;
    private JTextField txtFilter;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblCount;

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

        // Sorter for filtering
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

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

        // Filter controls: combo excludes ID column (index 0)
        cmbColumn = new JComboBox<>();
        for (int i = 1; i < tableModel.getColumnCount(); i++) {
            cmbColumn.addItem(tableModel.getColumnName(i));
        }
        txtFilter = new JTextField(15);
        lblCount = new JLabel("0/0");

        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
        btnRefresh.addActionListener(e -> loadData());

        // Only filter when text is entered; changing combo applies only if text exists
        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
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
        cmbColumn.addActionListener(e -> {
            if (!txtFilter.getText().trim().isEmpty()) {
                updateFilter();
            }
            // even if no text, keep count accurate
            updateCountLabel();
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JLabel("Lọc theo:"));
        buttonPanel.add(cmbColumn);
        buttonPanel.add(txtFilter);
        buttonPanel.add(lblCount);
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
            updateCountLabel();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog() {
        NoiDungDialog dialog = new NoiDungDialog(null, "Thêm nội dung", null, noiDungService);
        dialog.setVisible(true);
        loadData();
        updateCountLabel();
    }

    private void showEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nội dung để sửa", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        Integer id = (Integer) tableModel.getValueAt(modelRow, 0);
        try {
            Optional<NoiDung> ndOpt = noiDungService.getNoiDungById(id);
            if (ndOpt.isPresent()) {
                NoiDungDialog dialog = new NoiDungDialog(null, "Sửa nội dung", ndOpt.get(), noiDungService);
                dialog.setVisible(true);
                loadData();
                updateCountLabel();
            }
        } catch (SQLException e) {
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
        int modelRow = table.convertRowIndexToModel(selectedRow);
        Integer id = (Integer) tableModel.getValueAt(modelRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa nội dung này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = noiDungService.deleteNoiDung(id);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Xóa nội dung thành công!", "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                    updateCountLabel();
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa nội dung", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa nội dung: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateFilter() {
        String text = txtFilter.getText();
        if (text == null || text.trim().isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        int selected = cmbColumn.getSelectedIndex();
        if (selected < 0) {
            sorter.setRowFilter(null);
            return;
        }
        int modelColumn = selected + 1; // skip ID column
        try {
            String pattern = java.util.regex.Pattern.quote(text.trim());
            RowFilter<DefaultTableModel, Integer> rf = RowFilter.regexFilter("(?i)" + pattern, modelColumn);
            sorter.setRowFilter(rf);
        } catch (Exception ex) {
            sorter.setRowFilter(null);
        }
        updateCountLabel();
    }

    private void updateCountLabel() {
        int visible = table.getRowCount(); // after sorter/filter applied
        int total = tableModel.getRowCount();
        lblCount.setText(visible + "/" + total + " nội dung");
    }
}
