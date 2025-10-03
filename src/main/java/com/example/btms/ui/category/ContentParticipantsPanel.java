package com.example.btms.ui.category;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.team.ChiTietDoi;
import com.example.btms.model.team.DangKiDoi;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.club.CauLacBoRepository;
import com.example.btms.repository.player.VanDongVienRepository;
import com.example.btms.repository.team.ChiTietDoiRepository;
import com.example.btms.repository.team.DangKiDoiRepository;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.player.DangKiCaNhanService;
import com.example.btms.service.team.ChiTietDoiService;
import com.example.btms.service.team.DangKiDoiService;
import com.example.btms.ui.team.DangKyDoiDialog;

/**
 * Trang xem người tham gia theo nội dung:
 * - Nếu nội dung đơn: danh sách VĐV đăng ký.
 * - Nếu nội dung đôi: danh sách đội + ghép 2 thành viên.
 */
public class ContentParticipantsPanel extends JPanel {
    private final Prefs prefs = new Prefs();
    private final Connection conn; // lưu lại connection để dùng cho dialog & repo trực tiếp
    private final NoiDungService noiDungService;
    private final VanDongVienService vdvService;
    private final DangKiCaNhanService dkCaNhanService;
    private final DangKiDoiService doiService;
    private final ChiTietDoiService chiTietDoiService;
    private final CauLacBoService clbService;

    private final JComboBox<NoiDung> cboNoiDung = new JComboBox<>();
    private final JLabel lblInfo = new JLabel("Chưa chọn nội dung");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Tên", "CLB", "Thành viên" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    private final JButton btnReload = new JButton("Tải lại");
    private final javax.swing.JTextField txtSearch = new javax.swing.JTextField(16);
    private final JButton btnEditTeam = new JButton("Sửa đội");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnTransfer = new JButton("Chuyển nội dung");

    public ContentParticipantsPanel(Connection conn) {
        this.conn = conn;
        this.noiDungService = new NoiDungService(new NoiDungRepository(conn));
        this.vdvService = new VanDongVienService(new VanDongVienRepository(conn));
        this.dkCaNhanService = new DangKiCaNhanService(
                new com.example.btms.repository.player.DangKiCaNhanRepository(conn));
        // dùng 2 instance repo team (độc lập) vì service cần
        DangKiDoiRepository doiRepo = new DangKiDoiRepository(conn);
        this.doiService = new DangKiDoiService(doiRepo);
        this.chiTietDoiService = new ChiTietDoiService(conn, doiRepo, new ChiTietDoiRepository(conn));
        this.clbService = new CauLacBoService(new CauLacBoRepository(conn));

        setLayout(new BorderLayout(8, 8));
        add(buildTop(), BorderLayout.NORTH);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadNoiDungCombo();
    }

    /**
     * Chọn nội dung theo ID (dùng khi người dùng chọn từ cây chức năng) và tải lại
     * danh sách.
     */
    public void selectNoiDungById(Integer idNoiDung) {
        if (idNoiDung == null)
            return;
        var ndComboModel = cboNoiDung.getModel();
        for (int i = 0; i < ndComboModel.getSize(); i++) {
            NoiDung nd = ndComboModel.getElementAt(i);
            if (nd != null && idNoiDung.equals(nd.getId())) {
                cboNoiDung.setSelectedIndex(i);
                break;
            }
        }
        reloadParticipants();
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        line.add(new JLabel("Nội dung:"));
        cboNoiDung.setPrototypeDisplayValue(new NoiDung(0, "XXXXXXXXXXXXXXXXXXXXXXXX", null, null, null, false));
        cboNoiDung.addActionListener(e -> reloadParticipants());
        line.add(cboNoiDung);
        line.add(btnReload);
        btnReload.addActionListener(e -> reloadParticipants());
        // Nhóm thao tác chính: Chuyển ND (mới), Sửa đội (chỉ ĐÔI), Xóa
        line.add(btnTransfer);
        line.add(btnEditTeam);
        line.add(btnDelete);
        line.add(new JLabel("Tìm:"));
        line.add(txtSearch);
        line.add(lblInfo);
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
        p.add(line, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        btnEditTeam.addActionListener(e -> onEditTeam());
        btnTransfer.addActionListener(e -> onOpenTransferDialog());
        btnDelete.addActionListener(e -> onDelete());
        // Both singles/team change actions will open the unified transfer dialog
        updateButtonsState();
        cboNoiDung.addActionListener(e -> {
            reloadParticipants();
            updateButtonsState();
        });
        table.getSelectionModel().addListSelectionListener(e -> updateButtonsState());
        return p;
    }

    private void updateFilter() {
        String q = txtSearch.getText();
        if (q == null || q.isBlank()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q.trim())));
    }

    private void loadNoiDungCombo() {
        try {
            // Chỉ lấy nội dung đã đăng ký cho giải (loadCategories)
            var repo = new com.example.btms.repository.category.NoiDungRepository(conn);
            java.util.Map<String, Integer>[] maps = repo.loadCategories(); // [0]=singles, [1]=doubles
            java.util.Set<Integer> allowedIds = new java.util.LinkedHashSet<>();
            allowedIds.addAll(maps[0].values());
            allowedIds.addAll(maps[1].values());
            List<NoiDung> all;
            try {
                all = noiDungService.getAllNoiDung();
            } catch (java.sql.SQLException se) {
                throw new RuntimeException(se);
            }
            java.util.List<NoiDung> filtered = new java.util.ArrayList<>();
            for (NoiDung nd : all) {
                if (nd.getId() != null && allowedIds.contains(nd.getId()))
                    filtered.add(nd);
            }
            // Giữ thứ tự theo ID
            filtered.sort(java.util.Comparator.comparing(NoiDung::getId));
            cboNoiDung.setModel(new DefaultComboBoxModel<>(filtered.toArray(NoiDung[]::new)));
            if (!filtered.isEmpty())
                cboNoiDung.setSelectedIndex(0);
            reloadParticipants();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nội dung: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reloadParticipants() {
        model.setRowCount(0);
        NoiDung nd = (NoiDung) cboNoiDung.getSelectedItem();
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (nd == null) {
            lblInfo.setText("Chưa chọn nội dung");
            return;
        }
        if (idGiai <= 0) {
            lblInfo.setText("Chưa chọn giải");
            return;
        }
        boolean isDoi = Boolean.TRUE.equals(nd.getTeam());
        String tenGiai = prefs.get("selectedGiaiDauName", "");
        String giaiLabel = (tenGiai != null && !tenGiai.isBlank()) ? tenGiai : ("Giải ID=" + idGiai);
        lblInfo.setText((isDoi ? "ĐỘI" : "CÁ NHÂN") + " - " + giaiLabel);
        try {
            if (isDoi) {
                List<DangKiDoi> teams = doiService.listTeams(idGiai, nd.getId());
                for (DangKiDoi t : teams) {
                    // CLB name
                    String clbName = "";
                    try {
                        var teamFull = doiService.getTeam(Objects.requireNonNull(t.getIdTeam()));
                        if (teamFull.getIdCauLacBo() != null) {
                            try {
                                com.example.btms.model.club.CauLacBo clb = clbService.findOne(teamFull.getIdCauLacBo());
                                clbName = clb != null ? clb.getTenClb() : "";
                            } catch (Exception ignore) {
                            }
                        }
                    } catch (Exception ignore) {
                    }

                    // Members
                    String members;
                    try {
                        List<ChiTietDoi> list = chiTietDoiService.listMembers(Objects.requireNonNull(t.getIdTeam()));
                        if (list.isEmpty()) {
                            members = "(chưa có thành viên)";
                        } else {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < list.size(); i++) {
                                var m = list.get(i);
                                try {
                                    var v = vdvService.findOne(m.getIdVdv());
                                    if (i > 0)
                                        sb.append(" & ");
                                    sb.append(v.getHoTen());
                                } catch (Exception ignore) {
                                }
                            }
                            members = sb.toString();
                        }
                    } catch (RuntimeException ex) {
                        members = "(lỗi lấy thành viên)";
                    }
                    model.addRow(new Object[] { t.getIdTeam(), t.getTenTeam(), clbName, members });
                }
            } else {
                Map<String, Integer> map = vdvService.loadSinglesNames(nd.getId(), idGiai);
                for (Map.Entry<String, Integer> e : map.entrySet()) {
                    String clbName = "";
                    try {
                        var v = vdvService.findOne(e.getValue());
                        if (v.getIdClb() != null) {
                            try {
                                com.example.btms.model.club.CauLacBo clb = clbService.findOne(v.getIdClb());
                                clbName = clb != null ? clb.getTenClb() : "";
                            } catch (Exception ignore) {
                            }
                        }
                    } catch (RuntimeException ignore) {
                    }
                    model.addRow(new Object[] { e.getValue(), e.getKey(), clbName, "" });
                }
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
        updateButtonsState();
    }

    private void updateButtonsState() {
        NoiDung nd = (NoiDung) cboNoiDung.getSelectedItem();
        boolean isDoi = nd != null && Boolean.TRUE.equals(nd.getTeam());
        boolean hasSelection = table.getSelectedRow() >= 0;
        btnEditTeam.setEnabled(isDoi && hasSelection);
        btnDelete.setEnabled(hasSelection);
        btnTransfer.setEnabled(true);
    }

    /** Public refresh API for MainFrame and tree context menu. */
    public void refreshAll() {
        loadNoiDungCombo();
        // reloadParticipants() is called by loadNoiDungCombo
    }

    private void onEditTeam() {
        NoiDung nd = (NoiDung) cboNoiDung.getSelectedItem();
        if (nd == null || !Boolean.TRUE.equals(nd.getTeam()))
            return;
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Chọn đội để sửa.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Integer idTeam = (Integer) model.getValueAt(modelRow, 0);

        // Lấy thông tin đội hiện tại
        Integer idClbInit = null;
        String tenTeam = (String) model.getValueAt(modelRow, 1);
        try {
            var team = doiService.getTeam(idTeam);
            idClbInit = team.getIdCauLacBo();
            if (team.getTenTeam() != null)
                tenTeam = team.getTenTeam();
        } catch (RuntimeException ex) {
            System.err.println("Không lấy được đội: " + ex.getMessage());
        }

        // Lấy thành viên đội
        Integer idVdv1Init = null;
        Integer idVdv2Init = null;
        try {
            List<ChiTietDoi> members = chiTietDoiService.listMembers(idTeam);
            members.sort(java.util.Comparator.comparing(ChiTietDoi::getIdVdv));
            if (!members.isEmpty())
                idVdv1Init = members.get(0).getIdVdv();
            if (members.size() > 1)
                idVdv2Init = members.get(1).getIdVdv();
        } catch (RuntimeException ex) {
            System.err.println("Không lấy được thành viên đội: " + ex.getMessage());
        }

        // Danh sách nội dung đôi đã đăng ký (dialog sẽ disable đổi ND trong chế độ sửa)
        List<NoiDung> doubles;
        try {
            doubles = loadRegisteredDoubles();
            if (doubles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chưa có nội dung ĐÔI đã đăng ký.");
                return;
            }
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nội dung: " + ex.getMessage());
            return;
        }

        // Mở dialog sửa (bao gồm tên, CLB, thành viên)
        DangKyDoiDialog dlg = new DangKyDoiDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this),
                conn,
                "Sửa đội",
                doiService, // teamService
                chiTietDoiService,
                vdvService,
                clbService,
                prefs.getInt("selectedGiaiDauId", -1),
                doubles,
                nd,
                idTeam,
                tenTeam,
                idClbInit,
                idVdv1Init,
                idVdv2Init);
        dlg.setVisible(true);
        reloadParticipants();
    }

    // Đã thay bằng nút Chuyển nội dung (btnTransfer)

    private List<NoiDung> loadRegisteredDoubles() throws java.sql.SQLException {
        var repo = new com.example.btms.repository.category.NoiDungRepository(conn);
        java.util.Map<String, Integer>[] maps = repo.loadCategories(); // [1]=doubles
        java.util.Set<Integer> ids = new java.util.LinkedHashSet<>(maps[1].values());
        List<NoiDung> all = noiDungService.getAllNoiDung();
        java.util.List<NoiDung> doubles = new java.util.ArrayList<>();
        for (NoiDung nd : all) {
            if (nd.getId() != null && ids.contains(nd.getId()) && Boolean.TRUE.equals(nd.getTeam()))
                doubles.add(nd);
        }
        doubles.sort(java.util.Comparator.comparing(NoiDung::getId));
        return doubles;
    }

    private List<NoiDung> loadRegisteredSingles() throws java.sql.SQLException {
        var repo = new com.example.btms.repository.category.NoiDungRepository(conn);
        java.util.Map<String, Integer>[] maps = repo.loadCategories(); // [0]=singles
        java.util.Set<Integer> ids = new java.util.LinkedHashSet<>(maps[0].values());
        List<NoiDung> all = noiDungService.getAllNoiDung();
        java.util.List<NoiDung> singles = new java.util.ArrayList<>();
        for (NoiDung nd : all) {
            if (nd.getId() != null && ids.contains(nd.getId()) && !Boolean.TRUE.equals(nd.getTeam()))
                singles.add(nd);
        }
        singles.sort(java.util.Comparator.comparing(NoiDung::getId));
        return singles;
    }

    // Đã thay bằng nút Chuyển nội dung (btnTransfer)

    private void onOpenTransferDialog() {
        NoiDung curNd = (NoiDung) cboNoiDung.getSelectedItem();
        if (curNd == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn nội dung.");
            return;
        }
        boolean teamMode = Boolean.TRUE.equals(curNd.getTeam());
        try {
            java.util.List<NoiDung> list = teamMode ? loadRegisteredDoubles() : loadRegisteredSingles();
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        teamMode ? "Không có nội dung ĐÔI đã đăng ký." : "Không có nội dung ĐƠN đã đăng ký.");
                return;
            }
            ChangeCategoryTransferDialog dlg = new ChangeCategoryTransferDialog(
                    javax.swing.SwingUtilities.getWindowAncestor(this),
                    prefs,
                    teamMode,
                    list,
                    curNd,
                    // singles services
                    dkCaNhanService,
                    vdvService,
                    // teams services
                    doiService,
                    chiTietDoiService,
                    clbService,
                    () -> reloadParticipants());
            dlg.setVisible(true);
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nội dung: " + ex.getMessage());
        }
    }

    /**
     * Xóa đăng ký: nếu ND đôi thì xóa đội (và thành viên), nếu ND đơn thì xóa đăng
     * ký cá nhân.
     */
    private void onDelete() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một dòng để xóa.");
            return;
        }
        NoiDung nd = (NoiDung) cboNoiDung.getSelectedItem();
        if (nd == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn nội dung.");
            return;
        }
        int c = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa đăng ký này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION)
            return;

        int modelRow = table.convertRowIndexToModel(viewRow);
        try {
            if (Boolean.TRUE.equals(nd.getTeam())) {
                // Xóa đội đăng ký: ID cột 0 là ID_TEAM
                Integer idTeam = (Integer) model.getValueAt(modelRow, 0);
                chiTietDoiService.removeAll(idTeam);
                doiService.deleteTeam(idTeam);
            } else {
                // Xóa đăng ký cá nhân: cần (idGiai, idNoiDung, idVdv)
                int idGiai = prefs.getInt("selectedGiaiDauId", -1);
                if (idGiai <= 0) {
                    JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
                    return;
                }
                Integer idVdv = (Integer) model.getValueAt(modelRow, 0); // cột 0 là ID_VDV với đơn
                dkCaNhanService.delete(idGiai, nd.getId(), idVdv);
            }
            JOptionPane.showMessageDialog(this, "Đã xóa thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            reloadParticipants();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Xóa thất bại: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
