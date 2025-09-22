package com.example.badmintoneventtechnology.ui.tournament;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.example.badmintoneventtechnology.model.tournament.GiaiDau;
import com.example.badmintoneventtechnology.service.tournament.GiaiDauService;

/**
 * Panel quản lý CRUD cho giải đấu
 */
public class GiaiDauManagementPanel extends JPanel {
    private final GiaiDauService giaiDauService;
    private JTable giaiDauTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JButton btnRefresh, btnAdd, btnEdit, btnDelete, btnSearch;

    public GiaiDauManagementPanel(GiaiDauService giaiDauService) {
        this.giaiDauService = giaiDauService;
        initializeComponents();
        setupLayout();
        loadData();
    }

    private void initializeComponents() {
        // Table
        String[] columnNames = { "Tên Giải", "Ngày Bắt Đầu", "Ngày Kết Thúc", "Trạng Thái" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép edit trực tiếp trên table
            }
        };
        giaiDauTable = new JTable(tableModel);
        giaiDauTable.setRowSelectionAllowed(true);
        giaiDauTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        // Search và Filter
        searchField = new JTextField(20);
        statusFilter = new JComboBox<>(new String[] { "Tất cả", "Đang hoạt động", "Sắp tới", "Đã kết thúc" });

        // Buttons
        btnRefresh = new JButton("Làm mới");
        btnAdd = new JButton("Thêm mới");
        btnEdit = new JButton("Sửa");
        btnDelete = new JButton("Xóa");
        btnSearch = new JButton("Tìm kiếm");

        // Event handlers
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        btnRefresh.addActionListener(e -> loadData());
        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
        btnSearch.addActionListener(e -> searchGiaiDau());
        statusFilter.addActionListener(e -> filterByStatus());
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Top panel - Search và Filter
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(new JLabel("Tìm kiếm:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        topPanel.add(searchField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        topPanel.add(btnSearch, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        topPanel.add(new JLabel("Lọc theo:"), gbc);

        gbc.gridx = 4;
        gbc.gridy = 0;
        topPanel.add(statusFilter, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        topPanel.add(buttonPanel, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Table
        add(new JScrollPane(giaiDauTable), BorderLayout.CENTER);
    }

    private void loadData() {
        try {
            List<GiaiDau> giaiDaus = giaiDauService.getAllGiaiDau();
            updateTable(giaiDaus);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<GiaiDau> giaiDaus) {
        tableModel.setRowCount(0);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        for (GiaiDau giaiDau : giaiDaus) {
            String ngayBdStr = "";
            if (giaiDau.getNgayBd() != null) {
                java.sql.Date date = java.sql.Date.valueOf(giaiDau.getNgayBd());
                ngayBdStr = formatter.format(date);
            }
            String ngayKtStr = "";
            if (giaiDau.getNgayKt() != null) {
                java.sql.Date date = java.sql.Date.valueOf(giaiDau.getNgayKt());
                ngayKtStr = formatter.format(date);
            }
            Object[] row = {
                    giaiDau.getTenGiai(),
                    ngayBdStr,
                    ngayKtStr,
                    getStatusText(giaiDau),
            };
            tableModel.addRow(row);
        }
    }

    private String getStatusText(GiaiDau giaiDau) {
        if (giaiDau.isActive())
            return "Đang hoạt động";
        if (giaiDau.isUpcoming())
            return "Sắp tới";
        if (giaiDau.isFinished())
            return "Đã kết thúc";
        return "Không xác định";
    }

    private void showAddDialog() {
        java.awt.Window parent = javax.swing.SwingUtilities.getWindowAncestor(this);
        GiaiDauDialog dialog = new GiaiDauDialog(parent,
                "Thêm giải đấu mới", null, giaiDauService);
        dialog.setVisible(true);
        loadData(); // Refresh table
    }

    private void showEditDialog() {
        int selectedRow = giaiDauTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giải đấu để sửa",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object idObj = tableModel.getValueAt(selectedRow, 0);
        Integer id;
        if (idObj instanceof Integer) {
            id = (Integer) idObj;
        } else if (idObj instanceof Long) {
            id = ((Long) idObj).intValue();
        } else {
            throw new IllegalArgumentException("ID không phải kiểu Integer hoặc Long");
        }
        try {
            Optional<GiaiDau> giaiDauOpt = giaiDauService.getGiaiDauById(id.longValue());
            if (giaiDauOpt.isPresent()) {
                java.awt.Window parent = javax.swing.SwingUtilities.getWindowAncestor(this);
                GiaiDauDialog dialog = new GiaiDauDialog(parent,
                        "Sửa giải đấu", giaiDauOpt.get(), giaiDauService);
                dialog.setVisible(true);
                loadData(); // Refresh table
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy thông tin giải đấu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int selectedRow = giaiDauTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giải đấu để xóa",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object idObj = tableModel.getValueAt(selectedRow, 0);
        Integer id;
        if (idObj instanceof Integer) {
            id = (Integer) idObj;
        } else if (idObj instanceof Long) {
            id = ((Long) idObj).intValue();
        } else {
            throw new IllegalArgumentException("ID không phải kiểu Integer hoặc Long");
        }
        String tenGiai = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa giải đấu \"" + tenGiai + "\"?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = giaiDauService.deleteGiaiDau(id);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Xóa giải đấu thành công!",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadData(); // Refresh table
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa giải đấu",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa giải đấu: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void searchGiaiDau() {
        String searchText = searchField.getText().trim();
        try {
            List<GiaiDau> giaiDaus;
            if (searchText.isEmpty()) {
                giaiDaus = giaiDauService.getAllGiaiDau();
            } else {
                giaiDaus = giaiDauService.searchGiaiDauByName(searchText);
            }
            updateTable(giaiDaus);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterByStatus() {
        String status = (String) statusFilter.getSelectedItem();
        try {
            List<GiaiDau> giaiDaus;
            switch (status) {
                case "Đang hoạt động":
                    giaiDaus = giaiDauService.getActiveGiaiDau();
                    break;
                case "Sắp tới":
                    giaiDaus = giaiDauService.getUpcomingGiaiDau();
                    break;
                case "Đã kết thúc":
                    giaiDaus = giaiDauService.getFinishedGiaiDau();
                    break;
                default:
                    giaiDaus = giaiDauService.getAllGiaiDau();
                    break;
            }
            updateTable(giaiDaus);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lọc dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Refresh data khi connection thay đổi
     */
    public void refreshData() {
        giaiDauService.refreshRepository();
        loadData();
    }

    /**
     * Lấy giải đấu được chọn từ table
     */
    public GiaiDau getSelectedGiaiDau() {
        int selectedRow = giaiDauTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }

        Object idObj = tableModel.getValueAt(selectedRow, 0);
        Integer id;
        if (idObj instanceof Integer) {
            id = (Integer) idObj;
        } else if (idObj instanceof Long) {
            id = ((Long) idObj).intValue();
        } else {
            throw new IllegalArgumentException("ID không phải kiểu Integer hoặc Long");
        }
        try {
            Optional<GiaiDau> giaiDauOpt = giaiDauService.getGiaiDauById(id.longValue());
            return giaiDauOpt.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
