package com.example.btms.ui.team;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import javax.swing.JButton;
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

/**
 * Tab đăng ký đội (đôi) cho nội dung thi đấu đã đăng ký ở giải hiện tại.
 */
public class DangKyDoiPanel extends JPanel {
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
    private final JLabel lblCount = new JLabel("0 đội");
    private final javax.swing.JTextField txtSearch = new javax.swing.JTextField(16);

    public DangKyDoiPanel(Connection conn) {
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
        row2.add(new JLabel("Tìm:"));
        row2.add(txtSearch);
        row2.add(lblCount);
        row2.add(btnAdd);
        row2.add(btnEdit);
        row2.add(btnDelete);
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

        reload();
    }

    private void reload() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        String tenGiai = prefs.get("selectedGiaiDauName", "");
        if (idGiai <= 0) {
            lblHeader.setText("Đăng ký đội: (chưa chọn giải)");
            model.setRowCount(0);
            lblCount.setText("0 đội");
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giải đấu trước trong tab 'Chọn giải đấu'.");
            return;
        }
        lblHeader.setText(
                "Đăng ký đội cho giải: " + (tenGiai != null && !tenGiai.isBlank() ? tenGiai : ("ID=" + idGiai)));

        // Chỉ lấy các nội dung là đôi đã đăng ký cho giải này
        model.setRowCount(0);
        try {
            List<NoiDung> all = noiDungService.getAllNoiDung();
            for (NoiDung nd : all) {
                if (!Boolean.TRUE.equals(nd.getTeam()))
                    continue; // chỉ nội dung đôi
                // Vì chưa có service exists theo giải tại đây, dựa vào tab ĐăngKyNoiDungPanel
                // đã đăng ký
                // Để an toàn tối thiểu: vẫn cho phép chọn nội dung bất kỳ đôi; hoặc có thể lọc
                // bằng chiTietGiaiDauService nếu truyền vào
                List<DangKiDoi> teams = teamService.listTeams(idGiai, nd.getId());
                for (DangKiDoi t : teams) {
                    String clbName = "";
                    if (t.getIdCauLacBo() != null) {
                        try {
                            for (CauLacBo c : clbService.findAll()) {
                                if (c.getId() != null && c.getId().equals(t.getIdCauLacBo())) {
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
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q.trim()), 1));
    }

    private void onAdd() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (idGiai <= 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
            return;
        }

        // Chọn nội dung đôi để thêm đội
        NoiDung nd = chooseDoublesCategory();
        if (nd == null)
            return;

        DangKyDoiDialog dlg = new DangKyDoiDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this),
                "Thêm đội",
                teamService, detailService, vdvService, clbService,
                idGiai, nd,
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
        Optional<NoiDung> nd = Optional.empty();
        try {
            nd = noiDungService.getNoiDungById(idNoiDung);
        } catch (java.sql.SQLException ignore) {
        }
        if (nd.isEmpty()) {
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
            idClbInit = team.getIdCauLacBo();
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

        DangKyDoiDialog dlg = new DangKyDoiDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this),
                "Sửa đội",
                teamService, detailService, vdvService, clbService,
                prefs.getInt("selectedGiaiDauId", -1), nd.get(),
                idTeam, tenTeam, idClbInit, idVdv1Init, idVdv2Init);
        dlg.setVisible(true);
        reload();
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

    private NoiDung chooseDoublesCategory() {
        try {
            List<NoiDung> list = noiDungService.getAllNoiDung();
            java.util.List<NoiDung> doubles = new java.util.ArrayList<>();
            for (NoiDung nd : list)
                if (Boolean.TRUE.equals(nd.getTeam()))
                    doubles.add(nd);
            if (doubles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chưa có nội dung ĐÔI.");
                return null;
            }

            NoiDung[] arr = doubles.toArray(NoiDung[]::new);
            NoiDung sel = (NoiDung) javax.swing.JOptionPane.showInputDialog(
                    this,
                    "Chọn nội dung ĐÔI:",
                    "Nội dung",
                    javax.swing.JOptionPane.PLAIN_MESSAGE,
                    null,
                    arr,
                    arr[0]);
            return sel;
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nội dung: " + ex.getMessage());
            return null;
        }
    }

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
}
