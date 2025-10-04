package com.example.btms.ui.cateoftuornament;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import com.example.btms.config.Prefs;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.cateoftuornament.ChiTietGiaiDau;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.cateoftuornament.ChiTietGiaiDauService;
import com.example.btms.service.tournament.GiaiDauService;
import com.example.btms.model.tournament.GiaiDau;

/**
 * Tab đăng ký nội dung cho giải đấu đã chọn (dựa theo Prefs.selectedGiaiDauId)
 */
public class DangKyNoiDungPanel extends JPanel {

    private final NoiDungService noiDungService;
    private final ChiTietGiaiDauService chiTietService;
    private final Prefs prefs;
    private final GiaiDauService giaiDauService; // optional for resolving tournament name

    private final JLabel lblGiaiInfo = new JLabel("Giải đã chọn: (chưa chọn)");
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JButton btnShowAll = new JButton("Hiện tất cả");
    private final JButton btnAdd = new JButton("Thêm nội dung");
    private final JButton btnEdit = new JButton("Sửa nội dung");
    private final JTextField txtSearch = new JTextField(20);
    private final JLabel lblCount = new JLabel("0/0 nội dung");
    private boolean showAll = false; // mặc định chỉ hiển thị đã đăng ký
    private TableRowSorter<DefaultTableModel> sorter;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Đăng ký", "ID", "Nội dung", "Tuổi dưới", "Tuổi trên", "Giới tính", "Đánh đôi" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0 -> Boolean.class;
                case 1 -> Integer.class;
                case 3, 4 -> Integer.class;
                default -> String.class;
            };
        }
    };

    private final JTable table = new JTable(model);
    private boolean updating = false; // tránh đệ quy khi rollback checkbox

    public DangKyNoiDungPanel(NoiDungService noiDungService, ChiTietGiaiDauService chiTietService, Prefs prefs) {
        this(noiDungService, chiTietService, prefs, null);
    }

    public DangKyNoiDungPanel(NoiDungService noiDungService, ChiTietGiaiDauService chiTietService, Prefs prefs,
            GiaiDauService giaiDauService) {
        this.noiDungService = Objects.requireNonNull(noiDungService);
        this.chiTietService = Objects.requireNonNull(chiTietService);
        this.prefs = Objects.requireNonNull(prefs);
        this.giaiDauService = giaiDauService; // can be null

        setLayout(new BorderLayout(8, 8));

        // Khu vực tiêu đề và điều khiển: tách thành 2 hàng
        JPanel top = new JPanel(new BorderLayout(0, 4));
        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        infoRow.add(lblGiaiInfo); // Hàng riêng cho thông tin giải

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

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Sorter + Filter
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Sự kiện
        btnRefresh.addActionListener(e -> reloadAsync());
        btnShowAll.addActionListener(e -> {
            showAll = !showAll;
            btnShowAll.setText(showAll ? "Chỉ đã đăng ký" : "Hiện tất cả");
            updateFilters();
        });
        btnAdd.addActionListener(e -> onAddNoiDung());
        btnEdit.addActionListener(e -> onEditNoiDung());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFilters();
            }
        });
        table.getModel().addTableModelListener(e -> {
            if (e.getColumn() != 0 || e.getFirstRow() < 0)
                return;
            if (updating)
                return;
            int row = e.getFirstRow();
            handleToggle(row);
        });

        // Width nhẹ cho checkbox
        TableColumn checkCol = table.getColumnModel().getColumn(0);
        checkCol.setMaxWidth(90);
        checkCol.setMinWidth(80);

        // Căn giữa nội dung các cột (trừ cột checkbox)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        for (int i = 1; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Cột "Nội dung" (index 2) canh trái để dễ đọc
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(javax.swing.JLabel.LEFT);
        table.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);

        // Áp dụng filter mặc định và nạp dữ liệu
        updateFilters();
        reloadAsync();
    }

    private void onAddNoiDung() {
        com.example.btms.ui.category.NoiDungDialog dialog = new com.example.btms.ui.category.NoiDungDialog(
                SwingUtilities.getWindowAncestor(this),
                "Thêm nội dung",
                null,
                noiDungService);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            // làm mới danh sách
            reloadAsync();
            // hỏi có đăng ký cho giải hiện tại không
            int idGiai = prefs.getInt("selectedGiaiDauId", -1);
            if (idGiai > 0) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "Bạn có muốn đăng ký nội dung vừa thêm cho giải hiện tại không?",
                        "Đăng ký nội dung",
                        JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    com.example.btms.model.category.NoiDung nd = dialog.getResultNoiDung();
                    if (nd != null && nd.getId() != null) {
                        try {
                            Integer tuoiDuoiObj = nd.getTuoiDuoi();
                            Integer tuoiTrenObj = nd.getTuoiTren();
                            int tuoiDuoi = (tuoiDuoiObj == null) ? 0 : tuoiDuoiObj;
                            int tuoiTren = (tuoiTrenObj == null) ? 0 : tuoiTrenObj;
                            int idNoiDung = nd.getId();
                            com.example.btms.model.cateoftuornament.ChiTietGiaiDau ct = new com.example.btms.model.cateoftuornament.ChiTietGiaiDau(
                                    idGiai,
                                    idNoiDung,
                                    tuoiDuoi,
                                    tuoiTren);
                            chiTietService.create(ct);
                            // cập nhật UI: tick vào ô đăng ký cho dòng tương ứng nếu đang nhìn thấy
                            reloadAsync();
                        } catch (RuntimeException ex) {
                            JOptionPane.showMessageDialog(this, "Không thể đăng ký: " + ex.getMessage(), "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }
    }

    private void onEditNoiDung() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Integer id = (Integer) model.getValueAt(modelRow, 1);
        try {
            Optional<NoiDung> ndOpt = noiDungService.getNoiDungById(id);
            if (ndOpt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy nội dung.");
                return;
            }
            com.example.btms.ui.category.NoiDungDialog dialog = new com.example.btms.ui.category.NoiDungDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Sửa nội dung",
                    ndOpt.get(),
                    noiDungService);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                reloadAsync();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reloadAsync() {
        SwingUtilities.invokeLater(() -> {
            int idGiai = prefs.getInt("selectedGiaiDauId", -1);
            String tenGiai = prefs.get("selectedGiaiDauName", null);
            if (idGiai <= 0) {
                lblGiaiInfo.setText("Giải đã chọn: (chưa chọn)");
                model.setRowCount(0);
                updateCountLabel();
                return;
            }
            if ((tenGiai == null || tenGiai.isBlank()) && giaiDauService != null) {
                try {
                    Optional<GiaiDau> opt = giaiDauService.getGiaiDauById(idGiai);
                    if (opt.isPresent())
                        tenGiai = opt.get().getTenGiai();
                } catch (SQLException ignore) {
                    // ignore and fallback below
                }
            }
            if (tenGiai != null && !tenGiai.isBlank()) {
                lblGiaiInfo.setText("Nội dung thi đấu của giải: " + tenGiai);
            } else {
                // fallback nếu chưa có tên trong Prefs
                lblGiaiInfo.setText("Giải đã chọn: ID=" + idGiai);
            }

            try {
                List<NoiDung> list = noiDungService.getAllNoiDung();
                model.setRowCount(0);
                for (NoiDung nd : list) {
                    boolean registered = chiTietService.exists(idGiai, nd.getId());

                    String gt = Optional.ofNullable(nd.getGioiTinh()).orElse("").trim();

                    String genderText = "f".equalsIgnoreCase(gt) ? "Nữ" : "m".equalsIgnoreCase(gt) ? "Nam" : "Nam, Nữ";

                    String teamText = Boolean.TRUE.equals(nd.getTeam()) ? "Đôi" : "Đơn";

                    model.addRow(new Object[] {
                            registered,
                            nd.getId(),
                            nd.getTenNoiDung(),
                            nd.getTuoiDuoi(),
                            nd.getTuoiTren(),
                            genderText,
                            teamText
                    });
                }
                // Cập nhật filter sau khi nạp dữ liệu
                updateFilters();
                updateCountLabel();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Cập nhật bộ lọc theo trạng thái showAll và nội dung tìm kiếm theo tên (cột
     * "Nội dung").
     */
    private void updateFilters() {
        if (sorter == null)
            return;
        try {
            String q = txtSearch.getText();
            RowFilter<DefaultTableModel, Integer> searchFilter = null;
            if (q != null && !q.trim().isEmpty()) {
                String pattern = Pattern.quote(q.trim());
                searchFilter = RowFilter.regexFilter("(?i)" + pattern, 2); // cột 2: "Nội dung"
            }

            RowFilter<DefaultTableModel, Integer> onlyRegisteredFilter = new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    if (showAll)
                        return true; // hiển thị tất cả
                    Object v = entry.getValue(0); // cột 0: Đăng ký (Boolean)
                    return Boolean.TRUE.equals(v);
                }
            };

            List<RowFilter<DefaultTableModel, Integer>> list = new ArrayList<>();
            list.add(onlyRegisteredFilter);
            if (searchFilter != null)
                list.add(searchFilter);

            sorter.setRowFilter(list.isEmpty() ? null : RowFilter.andFilter(list));
        } catch (Exception ignore) {
            // nếu có lỗi regex, bỏ filter tìm kiếm
            sorter.setRowFilter(showAll ? null : new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    Object v = entry.getValue(0);
                    return Boolean.TRUE.equals(v);
                }
            });
        }
        updateCountLabel();
    }

    private void handleToggle(int row) {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (idGiai <= 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải đấu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            reloadAsync();
            return;
        }

        Boolean checked = (Boolean) model.getValueAt(row, 0);
        Integer idNoiDung = (Integer) model.getValueAt(row, 1);
        Integer tuoiDuoi = (Integer) model.getValueAt(row, 3);
        Integer tuoiTren = (Integer) model.getValueAt(row, 4);

        try {
            boolean exists = chiTietService.exists(idGiai, idNoiDung);
            if (Boolean.TRUE.equals(checked)) {
                if (!exists) {
                    int td = (tuoiDuoi == null) ? 0 : tuoiDuoi;
                    int tt = (tuoiTren == null) ? 0 : tuoiTren;
                    ChiTietGiaiDau ct = new ChiTietGiaiDau(idGiai, idNoiDung, td, tt);
                    chiTietService.create(ct);
                }
            } else {
                if (exists) {
                    chiTietService.delete(idGiai, idNoiDung);
                }
            }
            updateCountLabel();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật đăng ký: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            // rollback UI
            updating = true;
            try {
                model.setValueAt(!Boolean.TRUE.equals(checked), row, 0);
            } finally {
                updating = false;
            }
            updateCountLabel();
        }
    }

    private void updateCountLabel() {
        // Số dòng đang hiển thị sau khi áp dụng filter (kết quả tìm kiếm hiện tại)
        int visible = table.getRowCount();
        // Tổng số nội dung đã đăng ký (tính trên toàn bộ model, không phụ thuộc filter)
        int totalRegistered = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object v = model.getValueAt(i, 0);
            if (Boolean.TRUE.equals(v))
                totalRegistered++;
        }
        lblCount.setText(visible + "/" + totalRegistered + " nội dung");
    }

    /** Public refresh API for MainFrame and tree context menu. */
    public void refreshAll() {
        reloadAsync();
    }

    /**
     * Try to locate and select a specific Nội dung by its ID in the table.
     * This will ensure the row is visible by temporarily showing all and clearing
     * search filter.
     */
    public void selectNoiDungById(Integer idNoiDung) {
        if (idNoiDung == null)
            return;
        // Ensure data is loaded and filters won't hide the target row
        if (!showAll) {
            showAll = true;
            btnShowAll.setText("Chỉ đã đăng ký");
        }
        if (txtSearch.getText() != null && !txtSearch.getText().isBlank()) {
            txtSearch.setText("");
        }
        updateFilters();

        // Find the model row with matching ID in column 1
        int targetModelRow = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object v = model.getValueAt(i, 1);
            if (v instanceof Integer iid && iid.equals(idNoiDung)) {
                targetModelRow = i;
                break;
            }
        }
        if (targetModelRow < 0)
            return;
        int viewRow = table.convertRowIndexToView(targetModelRow);
        if (viewRow >= 0) {
            table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
            table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
        }
    }
}
