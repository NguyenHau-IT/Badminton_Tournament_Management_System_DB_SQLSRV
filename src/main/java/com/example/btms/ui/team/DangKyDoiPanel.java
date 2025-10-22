package com.example.btms.ui.team;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.example.btms.config.Prefs;
import com.example.btms.desktop.controller.category.ChangeCategoryTransferController;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.club.CauLacBo;
import com.example.btms.model.team.DangKiDoi;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.club.CauLacBoRepository;
import com.example.btms.repository.player.VanDongVienRepository;
import com.example.btms.repository.team.ChiTietDoiRepository;
import com.example.btms.repository.team.DangKiDoiRepository;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.team.ChiTietDoiService;
import com.example.btms.service.team.DangKiDoiService;
import com.example.btms.ui.category.ChangeCategoryTransferDialog;

/**
 * Tab đăng ký đội (đôi) cho nội dung thi đấu đã đăng ký ở giải hiện tại.
 */
public class DangKyDoiPanel extends JPanel {
    private final Connection conn; // giữ connection để lọc nội dung đã đăng ký
    private final Prefs prefs;
    private final NoiDungService noiDungService;
    private final DangKiDoiService teamService;
    private final ChiTietDoiService detailService;
    private final VanDongVienService vdvService;
    private final CauLacBoService clbService;

    private final DefaultTableModel model = new DefaultTableModel(
            // Giữ cột ID_TEAM & ID_Nội dung trong model để thao tác, nhưng sẽ ẩn khỏi
            // JTable
            new Object[] { "ID_TEAM", "Tên đội", "CLB", "ID_Nội dung", "Nội dung" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0, 3 -> Integer.class; // ID ẩn
                default -> String.class;
            };
        }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    private final JLabel lblHeader = new JLabel("Đăng ký đội: (chưa chọn giải)");
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JButton btnAdd = new JButton("Thêm đội");
    private final JButton btnEdit = new JButton("Sửa đội");
    private final JButton btnDelete = new JButton("Xóa đội");
    private final JButton btnDeleteAll = new JButton("Xóa tất cả");
    private final JButton btnTransfer = new JButton("Chuyển nội dung");
    private final JLabel lblCount = new JLabel("0 đội");
    private final javax.swing.JTextField txtSearch = new javax.swing.JTextField(16);
    private final JComboBox<String> cboFilterField = new JComboBox<>(new String[] { "Tên đội", "CLB", "Nội dung" });

    public DangKyDoiPanel(Connection conn) {
        this.conn = conn;
        this.prefs = new Prefs();
        this.noiDungService = new NoiDungService(new NoiDungRepository(conn));
        this.teamService = new DangKiDoiService(new DangKiDoiRepository(conn));
        this.detailService = new ChiTietDoiService(conn, new DangKiDoiRepository(conn), new ChiTietDoiRepository(conn));
        this.vdvService = new VanDongVienService(new VanDongVienRepository(conn));
        this.clbService = new CauLacBoService(new CauLacBoRepository(conn));

        setLayout(new BorderLayout(8, 8));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);

        JPanel top = new JPanel(new BorderLayout());
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(lblHeader);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(btnRefresh);
        row2.add(new JLabel("Lọc theo:"));
        row2.add(cboFilterField);
        row2.add(new JLabel("Tìm:"));
        row2.add(txtSearch);
        row2.add(lblCount);
        row2.add(btnAdd);
        row2.add(btnEdit);
        row2.add(btnDelete);
        row2.add(btnDeleteAll);
        row2.add(btnTransfer);
        top.add(row1, BorderLayout.NORTH);
        top.add(row2, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Ẩn hai cột ID khỏi giao diện (nhưng vẫn tồn tại trong model)
        hideIdColumns();

        btnRefresh.addActionListener(e -> reload());
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnDeleteAll.addActionListener(e -> onDeleteAll());
        btnTransfer.addActionListener(e -> onOpenTransferDialog());
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
        cboFilterField.addActionListener(e -> updateFilter());

        reload();
    }

    private void onDeleteAll() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        String tenGiai = prefs.get("selectedGiaiDauName", "");
        if (idGiai <= 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
            return;
        }
        // Determine if we are scoped to a specific doubles content by inspecting table
        // selection
        Integer idNdScope = null;
        try {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int mr = table.convertRowIndexToModel(row);
                idNdScope = (Integer) model.getValueAt(mr, 3); // ID_Nội dung (ẩn)
            }
        } catch (Exception ignore) {
        }
        String scope = (idNdScope != null) ? "của nội dung đang chọn trong giải" : "của giải";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa TẤT CẢ đăng ký đội " + scope + "\n" +
                        (tenGiai != null && !tenGiai.isBlank() ? ("- " + tenGiai + "\n") : "") +
                        "Lưu ý: Nếu bảng CHI_TIET_DOI không có FK ON DELETE CASCADE, thành viên đội có thể còn lại.\n" +
                        "Hành động này không thể hoàn tác.",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION)
            return;
        try {
            int deleted = (idNdScope != null)
                    ? teamService.deleteAllByGiaiAndNoiDung(idGiai, idNdScope)
                    : teamService.deleteAllByGiai(idGiai);
            reload();
            JOptionPane.showMessageDialog(this,
                    (idNdScope != null)
                            ? ("Đã xóa " + deleted + " đội của nội dung.")
                            : ("Đã xóa " + deleted + " đội của giải."),
                    "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Xóa tất cả thất bại: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reload() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        String tenGiai = prefs.get("selectedGiaiDauName", "");
        if (idGiai <= 0) {
            lblHeader.setText("Đăng ký đội: (chưa chọn giải)");
            model.setRowCount(0);
            lblCount.setText("0 đội");
            return;
        }
        lblHeader.setText(
                "Đăng ký đội cho giải: " + (tenGiai != null && !tenGiai.isBlank() ? tenGiai : ("ID=" + idGiai)));

        // Chỉ lấy các nội dung là ĐÔI đã đăng ký cho giải này (dùng loadCategories)
        model.setRowCount(0);
        try {
            List<NoiDung> registeredDoubles = loadRegisteredDoubleCategories();
            for (NoiDung nd : registeredDoubles) {
                List<DangKiDoi> teams = teamService.listTeams(idGiai, nd.getId());
                for (DangKiDoi t : teams) {
                    String clbName = "";
                    if (t.getIdClb() != null) {
                        try {
                            for (CauLacBo c : clbService.findAll()) {
                                if (c.getId() != null && c.getId().equals(t.getIdClb())) {
                                    clbName = c.getTenClb();
                                    break;
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    model.addRow(
                            new Object[] { t.getIdTeam(), t.getTenTeam(), clbName, nd.getId(), nd.getTenNoiDung() });
                }
            }
            lblCount.setText(model.getRowCount() + " đội");
            updateFilter();
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải đội: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFilter() {
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) {
            sorter.setRowFilter(null);
            return;
        }
        String selected = (String) cboFilterField.getSelectedItem();
        int colIndex; // model column index for filtering
        if ("CLB".equals(selected)) {
            colIndex = 2; // CLB
        } else if ("Nội dung".equals(selected)) {
            colIndex = 4; // Nội dung
        } else {
            colIndex = 1; // Tên đội (default)
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q.trim()), colIndex));
    }

    private void onAdd() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (idGiai <= 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
            return;
        }

        // Lấy danh sách nội dung ĐÔI đã đăng ký cho giải (loadCategories)
        List<NoiDung> doubles;
        try {
            doubles = loadRegisteredDoubleCategories();
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nội dung: " + ex.getMessage());
            return;
        }
        if (doubles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có nội dung ĐÔI đã đăng ký cho giải.");
            return;
        }
        NoiDung initial = doubles.get(0);

        DangKyDoiDialog dlg = new DangKyDoiDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this),
                conn,
                "Thêm đội",
                teamService, detailService, vdvService, clbService,
                idGiai, doubles, initial,
                null, null, null, null, null);
        dlg.setVisible(true);
        reload();
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đội để sửa.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        Integer idTeam = (Integer) model.getValueAt(modelRow, 0);
        Integer idNoiDung = (Integer) model.getValueAt(modelRow, 3); // vẫn lấy từ cột ẩn

        // Lấy nội dung theo id
        Optional<NoiDung> tmpNdOpt;
        try {
            tmpNdOpt = noiDungService.getNoiDungById(idNoiDung);
        } catch (java.sql.SQLException ignore) {
            tmpNdOpt = Optional.empty();
        }
        final Optional<NoiDung> ndOpt = tmpNdOpt; // final để dùng trong lambda phía dưới
        if (ndOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy nội dung.");
            return;
        }

        // Lấy thông tin đội (CLB) và thành viên để prefill dialog
        String tenTeam = (String) model.getValueAt(modelRow, 1);
        Integer idClbInit = null;
        Integer idVdv1Init = null;
        Integer idVdv2Init = null;
        try {
            // Lấy đội để biết CLB
            com.example.btms.model.team.DangKiDoi team = teamService.getTeam(idTeam);
            idClbInit = team.getIdClb();
        } catch (RuntimeException ex) {
            // Không chặn sửa nếu lỗi, chỉ log
            System.err.println("Không lấy được đội: " + ex.getMessage());
        }
        try {
            java.util.List<com.example.btms.model.team.ChiTietDoi> members = detailService
                    .listMembers(idTeam);
            // Sắp xếp để ổn định (theo ID_VDV tăng dần)
            members.sort(java.util.Comparator
                    .comparing(com.example.btms.model.team.ChiTietDoi::getIdVdv));
            if (!members.isEmpty())
                idVdv1Init = members.get(0).getIdVdv();
            if (members.size() > 1)
                idVdv2Init = members.get(1).getIdVdv();
        } catch (RuntimeException ex) {
            System.err.println("Không lấy được thành viên đội: " + ex.getMessage());
        }

        // Lấy danh sách ND đôi đã đăng ký
        List<NoiDung> doubles;
        try {
            doubles = loadRegisteredDoubleCategories();
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nội dung: " + ex.getMessage());
            return;
        }
        if (doubles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có nội dung ĐÔI đã đăng ký cho giải.");
            return;
        }
        // Bảo đảm nội dung hiện tại có trong danh sách (phòng khi DB lệch)
        if (ndOpt.isPresent()) {
            boolean exists = false;
            Integer currentId = ndOpt.get().getId();
            for (NoiDung d : doubles) {
                if (d.getId() != null && d.getId().equals(currentId)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                doubles.add(ndOpt.get());
            }
        }

        DangKyDoiDialog dlg = new DangKyDoiDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this),
                conn,
                "Sửa đội",
                teamService, detailService, vdvService, clbService,
                prefs.getInt("selectedGiaiDauId", -1), doubles, ndOpt.get(),
                idTeam, tenTeam, idClbInit, idVdv1Init, idVdv2Init);
        dlg.setVisible(true);
        reload();
    }

    /**
     * Lấy danh sách nội dung ĐÔI đã đăng ký cho giải hiện tại (dựa trên
     * CHI_TIET_GIAI_DAU)
     */
    private List<NoiDung> loadRegisteredDoubleCategories() throws java.sql.SQLException {
        java.util.List<NoiDung> result = new java.util.ArrayList<>();
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (idGiai <= 0 || conn == null)
            return result;
        // maps[1] = doubles map (ten->id)
        Map<String, Integer>[] maps = new NoiDungRepository(conn).loadCategories();
        Map<String, Integer> doublesMap = maps[1];
        if (doublesMap == null || doublesMap.isEmpty())
            return result;
        // Lấy tất cả nội dung rồi đối chiếu ID để tạo đối tượng đầy đủ (tuổi, giới
        // tính, ...)
        List<NoiDung> all = noiDungService.getAllNoiDung();
        java.util.Set<Integer> idNeeded = new java.util.HashSet<>(doublesMap.values());
        for (NoiDung nd : all) {
            if (nd.getId() != null && idNeeded.contains(nd.getId()))
                result.add(nd);
        }
        // Sắp xếp theo ID để ổn định
        result.sort(java.util.Comparator.comparing(NoiDung::getId));
        return result;
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đội để xóa.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        Integer idTeam = (Integer) model.getValueAt(modelRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa đội này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;
        try {
            detailService.removeAll(idTeam); // xóa thành viên đội trước
            teamService.deleteTeam(idTeam);
            JOptionPane.showMessageDialog(this, "Đã xóa đội thành công.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            reload();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Xóa thất bại: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onOpenTransferDialog() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (idGiai <= 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
            return;
        }

        java.util.List<NoiDung> doubles;
        try {
            doubles = loadRegisteredDoubleCategories();
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nội dung: " + ex.getMessage());
            return;
        }
        if (doubles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có nội dung ĐÔI đã đăng ký.");
            return;
        }

        // Lấy nội dung theo dòng đang chọn, nếu không có thì lấy phần tử đầu
        NoiDung initial = null;
        if (table.getSelectedRow() >= 0) {
            int mr = table.convertRowIndexToModel(table.getSelectedRow());
            Integer idNd = (Integer) model.getValueAt(mr, 3); // col 3 = ID_Nội dung (ẩn)
            for (NoiDung nd : doubles) {
                if (nd.getId() != null && nd.getId().equals(idNd)) {
                    initial = nd;
                    break;
                }
            }
        }
        if (initial == null)
            initial = doubles.get(0);

        // --- 1 UI + 1 Controller (TEAM MODE) ---
        var view = new ChangeCategoryTransferDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this),
                /* teamMode = */ true,
                doubles,
                initial);

        new ChangeCategoryTransferController(
                view,
                prefs,
                /* teamMode = */ true,
                // singles services (không dùng ở team mode)
                null, // DangKiCaNhanService
                null, // VanDongVienService
                // teams services
                teamService, // DangKiDoiService
                detailService, // ChiTietDoiService
                clbService, // CauLacBoService
                this::reload // onChanged
        );

        view.setVisible(true); // MODELESS như trước
    }

    // chooseDoublesCategory() không còn dùng sau khi lọc bằng loadCategories()

    /** Ẩn cột ID_TEAM và ID_Nội dung khỏi JTable (giữ trong model để thao tác) */
    private void hideIdColumns() {
        try {
            javax.swing.table.TableColumnModel columnModel = table.getColumnModel();
            java.util.List<String> hide = java.util.List.of("ID_TEAM", "ID_Nội dung");
            for (String h : hide) {
                int modelIndex = model.findColumn(h);
                if (modelIndex < 0)
                    continue;
                int viewIndex = table.convertColumnIndexToView(modelIndex);
                if (viewIndex < 0)
                    continue; // already hidden or not found
                javax.swing.table.TableColumn col = columnModel.getColumn(viewIndex);
                col.setMinWidth(0);
                col.setMaxWidth(0);
                col.setPreferredWidth(0);
            }
        } catch (Exception ignored) {
        }
    }

    /** Public refresh API for MainFrame and tree context menu. */
    public void refreshAll() {
        reload();
    }
}
