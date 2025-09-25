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
    private final DangKiDoiService doiService;
    private final ChiTietDoiService chiTietDoiService;
    private final CauLacBoService clbService;

    private final JComboBox<NoiDung> cboNoiDung = new JComboBox<>();
    private final JLabel lblInfo = new JLabel("Chưa chọn nội dung");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Tên", "CLB / Thành viên" }, 0) {
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
    private final JButton btnChangeCategory = new JButton("Đổi nội dung đội");

    public ContentParticipantsPanel(Connection conn) {
        this.conn = conn;
        this.noiDungService = new NoiDungService(new NoiDungRepository(conn));
        this.vdvService = new VanDongVienService(new VanDongVienRepository(conn));
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

    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        line.add(new JLabel("Nội dung:"));
        cboNoiDung.setPrototypeDisplayValue(new NoiDung(0, "XXXXXXXXXXXXXXXXXXXXXXXX", null, null, null, false));
        cboNoiDung.addActionListener(e -> reloadParticipants());
        line.add(cboNoiDung);
        line.add(btnReload);
        btnReload.addActionListener(e -> reloadParticipants());
        line.add(btnEditTeam);
        line.add(btnChangeCategory);
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
        btnChangeCategory.addActionListener(e -> onChangeCategory());
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
            List<NoiDung> all = noiDungService.getAllNoiDung();
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
        } catch (Exception ex) {
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
                    String members;
                    try {
                        List<ChiTietDoi> list = chiTietDoiService.listMembers(Objects.requireNonNull(t.getIdTeam()));
                        if (list.isEmpty())
                            members = "(chưa có thành viên)";
                        else {
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
                    } catch (Exception ex) {
                        members = "(lỗi lấy thành viên)";
                    }
                    model.addRow(new Object[] { t.getIdTeam(), t.getTenTeam(), members });
                }
            } else {
                Map<String, Integer> map = vdvService.loadSinglesNames(nd.getId(), idGiai);
                for (Map.Entry<String, Integer> e : map.entrySet()) {
                    model.addRow(new Object[] { e.getValue(), e.getKey(), "" });
                }
            }
        } catch (Exception ex) {
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
        btnChangeCategory.setEnabled(isDoi && hasSelection);
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
        } catch (Exception ex) {
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

    private void onChangeCategory() {
        NoiDung nd = (NoiDung) cboNoiDung.getSelectedItem();
        if (nd == null || !Boolean.TRUE.equals(nd.getTeam()))
            return;
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Chọn đội cần chuyển nội dung.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Integer idTeam = (Integer) model.getValueAt(modelRow, 0);
        try {
            var team = doiService.getTeam(idTeam);
            // Chọn nội dung mới (chỉ nội dung đôi)
            List<NoiDung> doubles = loadRegisteredDoubles();
            if (doubles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có nội dung đôi đã đăng ký.");
                return;
            }
            NoiDung selected = (NoiDung) JOptionPane.showInputDialog(this, "Chọn nội dung mới:", "Đổi nội dung đội",
                    JOptionPane.PLAIN_MESSAGE, null, doubles.toArray(), nd);
            if (selected == null || selected.getId().equals(team.getIdNoiDung()))
                return;
            team.setIdNoiDung(selected.getId());
            new com.example.btms.repository.team.DangKiDoiRepository(getConnectionFromServices()).update(team);
            reloadParticipants();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi đổi nội dung: " + ex.getMessage());
        }
    }

    private java.sql.Connection getConnectionFromServices() {
        return this.conn; // đơn giản: chúng ta đã lưu connection
    }

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
}
