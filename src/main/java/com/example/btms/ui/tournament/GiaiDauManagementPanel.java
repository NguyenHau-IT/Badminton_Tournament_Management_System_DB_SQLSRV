package com.example.btms.ui.tournament;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.example.btms.config.Prefs;
import com.example.btms.model.tournament.GiaiDau;
import com.example.btms.service.tournament.GiaiDauService;

/** Panel quản lý CRUD cho giải đấu (độc lập) */
@SuppressWarnings("serial")
public class GiaiDauManagementPanel extends JPanel {
    private final GiaiDauService giaiDauService;
    private final boolean selectionOnly;
    private Integer currentUserId; // ID người dùng hiện tại để filter giải đấu

    private JTable giaiDauTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JButton btnRefresh, btnAdd, btnEdit, btnDelete, btnSearch;
    private JLabel lblUserInfo; // Hiển thị thông tin user hiện tại

    public GiaiDauManagementPanel(GiaiDauService giaiDauService) {
        this(giaiDauService, false);
    }

    public GiaiDauManagementPanel(GiaiDauService giaiDauService, boolean selectionOnly) {
        this.giaiDauService = giaiDauService;
        this.selectionOnly = selectionOnly;

        // Tự động lấy userId từ Preferences nếu đã đăng nhập
        try {
            Prefs prefs = new Prefs();
            this.currentUserId = prefs.getInt("userId", -1);
            if (this.currentUserId == -1) {
                this.currentUserId = null; // Không có user đăng nhập
            }
        } catch (Exception e) {
            this.currentUserId = null;
        }

        initializeComponents();
        setupLayout();
        loadData();
    }

    /**
     * Set ID người dùng hiện tại để filter giải đấu theo user
     * 
     * @param userId ID người dùng, null để hiển thị tất cả giải đấu
     */
    public void setCurrentUserId(Integer userId) {
        this.currentUserId = userId;
        loadData(); // Reload dữ liệu với user ID mới
    }

    /**
     * Lấy ID người dùng hiện tại
     * 
     * @return ID người dùng hoặc null nếu hiển thị tất cả
     */
    public Integer getCurrentUserId() {
        return currentUserId;
    }

    private void initializeComponents() {
        String[] columnNames = { "ID", "Tên Giải", "Ngày Bắt Đầu", "Ngày Kết Thúc", "Trạng Thái" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        giaiDauTable = new JTable(tableModel);
        giaiDauTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Ẩn cột ID
        if (giaiDauTable.getColumnModel().getColumnCount() > 0) {
            giaiDauTable.getColumnModel().getColumn(0).setMinWidth(0);
            giaiDauTable.getColumnModel().getColumn(0).setMaxWidth(0);
            giaiDauTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        }

        searchField = new JTextField(20);
        statusFilter = new JComboBox<>(new String[] { "Tất cả", "Đang hoạt động", "Sắp tới", "Đã kết thúc" });

        btnRefresh = new JButton("Làm mới");
        btnAdd = new JButton("Thêm mới");
        btnEdit = new JButton("Sửa");
        btnDelete = new JButton("Xóa");
        btnSearch = new JButton("Tìm kiếm");

        // User info label
        lblUserInfo = new JLabel();

        // events
        btnRefresh.addActionListener(e -> loadData());
        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
        btnSearch.addActionListener(e -> searchGiaiDau());
        statusFilter.addActionListener(e -> filterByStatus());
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Row 0: User info
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.WEST;
        top.add(lblUserInfo, gbc);

        // Row 1: Search controls
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        top.add(new JLabel("Tìm kiếm:"), gbc);
        gbc.gridx = 1;
        top.add(searchField, gbc);
        gbc.gridx = 2;
        top.add(btnSearch, gbc);
        gbc.gridx = 3;
        top.add(new JLabel("Lọc theo:"), gbc);
        gbc.gridx = 4;
        top.add(statusFilter, gbc);

        // Row 2: Action buttons
        JPanel btns = new JPanel();
        btns.add(btnRefresh);
        if (!selectionOnly) {
            btns.add(btnAdd);
            btns.add(btnEdit);
            btns.add(btnDelete);
        }
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 5;
        top.add(btns, gbc);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(giaiDauTable), BorderLayout.CENTER);
    }

    private void loadData() {
        try {
            List<GiaiDau> giaiDauList;

            if (currentUserId != null) {
                // Load giải đấu theo user ID
                giaiDauList = giaiDauService.getGiaiDauByUserId(currentUserId.longValue());
            } else {
                // Load tất cả giải đấu (chế độ admin hoặc chưa đăng nhập)
                giaiDauList = giaiDauService.getAllGiaiDau();
            }

            updateTable(giaiDauList);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<GiaiDau> list) {
        tableModel.setRowCount(0);
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
        for (GiaiDau gd : list) {
            String sBd = gd.getNgayBd() == null ? "" : fmt.format(java.sql.Date.valueOf(gd.getNgayBd()));
            String sKt = gd.getNgayKt() == null ? "" : fmt.format(java.sql.Date.valueOf(gd.getNgayKt()));
            tableModel.addRow(new Object[] {
                    gd.getId(), gd.getTenGiai(), sBd, sKt, statusText(gd)
            });
        }
    }

    private String statusText(GiaiDau gd) {
        if (gd.isActive())
            return "Đang hoạt động";
        if (gd.isUpcoming())
            return "Sắp tới";
        if (gd.isFinished())
            return "Đã kết thúc";
        return "Không xác định";
    }

    private void showAddDialog() {
        var parent = SwingUtilities.getWindowAncestor(this);
        GiaiDauDialog d = new GiaiDauDialog(parent, "Thêm giải đấu mới", null, giaiDauService);
        d.setVisible(true);
        loadData();
    }

    private void showEditDialog() {
        Integer id = getSelectedIdOrWarn();
        if (id == null)
            return;

        try {
            Optional<GiaiDau> opt = giaiDauService.getGiaiDauById(id);
            if (opt.isPresent()) {
                var parent = SwingUtilities.getWindowAncestor(this);
                GiaiDauDialog d = new GiaiDauDialog(parent, "Sửa giải đấu", opt.get(), giaiDauService);
                d.setVisible(true);
                loadData();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy thông tin giải đấu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        Integer id = getSelectedIdOrWarn();
        if (id == null)
            return;

        String tenGiai = (String) tableModel
                .getValueAt(giaiDauTable.convertRowIndexToModel(giaiDauTable.getSelectedRow()), 1);
        int c = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa giải đấu \"" + tenGiai + "\"?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION)
            return;

        try {
            if (giaiDauService.deleteGiaiDau(id)) {
                JOptionPane.showMessageDialog(this, "Xóa giải đấu thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa giải đấu", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (HeadlessException | SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa giải đấu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchGiaiDau() {
        String q = searchField.getText().trim();
        try {
            List<GiaiDau> searchResults;

            if (q.isEmpty()) {
                // Nếu không có từ khóa tìm kiếm, load theo user ID
                if (currentUserId != null) {
                    searchResults = giaiDauService.getGiaiDauByUserId(currentUserId.longValue());
                } else {
                    searchResults = giaiDauService.getAllGiaiDau();
                }
            } else {
                // Tìm kiếm theo tên và filter theo user nếu cần
                List<GiaiDau> allSearchResults = giaiDauService.searchGiaiDauByName(q);
                if (currentUserId != null) {
                    // Filter kết quả search theo user ID
                    searchResults = allSearchResults.stream()
                            .filter(gd -> currentUserId.equals(gd.getIdUser()))
                            .toList();
                } else {
                    searchResults = allSearchResults;
                }
            }

            updateTable(searchResults);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi tìm kiếm: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterByStatus() {
        String s = (String) statusFilter.getSelectedItem();
        try {
            List<GiaiDau> allResults = switch (s) {
                case "Đang hoạt động" -> giaiDauService.getActiveGiaiDau();
                case "Sắp tới" -> giaiDauService.getUpcomingGiaiDau();
                case "Đã kết thúc" -> giaiDauService.getFinishedGiaiDau();
                default -> {
                    if (currentUserId != null) {
                        yield giaiDauService.getGiaiDauByUserId(currentUserId.longValue());
                    } else {
                        yield giaiDauService.getAllGiaiDau();
                    }
                }
            };

            // Filter theo user ID nếu cần (trừ trường hợp "Tất cả" đã được handle ở trên)
            if (currentUserId != null && s != null && !s.equals("Tất cả")) {
                List<GiaiDau> userFilteredResults = allResults.stream()
                        .filter(gd -> currentUserId.equals(gd.getIdUser()))
                        .toList();
                updateTable(userFilteredResults);
            } else {
                updateTable(allResults);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi lọc dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Integer getSelectedIdOrWarn() {
        int viewRow = giaiDauTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giải đấu!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = giaiDauTable.convertRowIndexToModel(viewRow);
        Object idObj = tableModel.getValueAt(modelRow, 0);
        if (idObj instanceof Integer i)
            return i;
        if (idObj instanceof Long l)
            return l.intValue();
        throw new IllegalArgumentException("ID không phải Integer/Long");
    }

    /** Cho code ngoài gọi refresh */
    public void refreshData() {
        giaiDauService.refreshRepository();
        loadData();
    }

    /**
     * Refresh data with updated user ID from Preferences
     * Gọi khi cần cập nhật user ID từ session mới
     */
    public void refreshWithCurrentUser() {
        try {
            Prefs prefs = new Prefs();
            Integer newUserId = prefs.getInt("userId", -1);
            if (newUserId == -1) {
                newUserId = null;
            }
            setCurrentUserId(newUserId);
        } catch (Exception e) {
            // Giữ nguyên currentUserId hiện tại nếu có lỗi
            loadData();
        }
    }

    /** Lấy entity đang chọn trong bảng (nếu cần) */
    public GiaiDau getSelectedGiaiDau() {
        Integer id = getSelectedIdOrWarn();
        if (id == null)
            return null;
        try {
            return giaiDauService.getGiaiDauById(id).orElse(null);
        } catch (SQLException e) {
            return null;
        }
    }

    /** Chọn giải đấu theo ID trong bảng */
    public void selectTournamentById(int tournamentId) {
        try {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Object idObj = tableModel.getValueAt(i, 0);
                int id = (idObj instanceof Integer) ? (Integer) idObj
                        : (idObj instanceof Long) ? ((Long) idObj).intValue() : -1;
                if (id == tournamentId) {
                    int viewRow = giaiDauTable.convertRowIndexToView(i);
                    giaiDauTable.setRowSelectionInterval(viewRow, viewRow);
                    giaiDauTable.scrollRectToVisible(giaiDauTable.getCellRect(viewRow, 0, true));
                    break;
                }
            }
        } catch (Exception e) {
            // Ignore selection errors
        }
    }
}
