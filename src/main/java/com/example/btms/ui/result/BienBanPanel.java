package com.example.btms.ui.result;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

import com.example.btms.config.Prefs;
import com.example.btms.model.bracket.SoDoCaNhan;
import com.example.btms.model.bracket.SoDoDoi;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.match.ChiTietTranDau;
import com.example.btms.model.match.ChiTietVan;
import com.example.btms.model.player.VanDongVien;
import com.example.btms.repository.bracket.SoDoCaNhanRepository;
import com.example.btms.repository.bracket.SoDoDoiRepository;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.match.ChiTietTranDauRepository;
import com.example.btms.repository.match.ChiTietVanRepository;
import com.example.btms.repository.player.VanDongVienRepository;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.match.ChiTietTranDauService;
import com.example.btms.service.match.ChiTietVanService;
import com.example.btms.service.player.VanDongVienService;

/**
 * Trang "Biên bản" hiển thị danh sách các trận (theo giải/nội dung) bên trái
 * và nhật ký set/điểm theo thời gian (CHI_TIET_VAN) bên phải.
 */
public class BienBanPanel extends JPanel {
    private final Prefs prefs = new Prefs();
    private final Connection connection; // lưu lại kết nối để mở cửa sổ chi tiết
    private final NoiDungService noiDungService;
    private final ChiTietTranDauService tranDauService;
    private final ChiTietVanService vanService;
    private final SoDoCaNhanRepository soDoCaNhanRepo;
    private final SoDoDoiRepository soDoDoiRepo;
    private final VanDongVienService vdvService;

    private final JLabel lblGiai = new JLabel();
    private final JComboBox<Object> cboNoiDung = new JComboBox<>();
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JButton btnOpenDetail = new JButton("Xem chi tiết...");
    private final javax.swing.JTextField txtSearchId = new javax.swing.JTextField(24);
    private final JButton btnSearchId = new JButton("Tìm ID");

    private final DefaultTableModel matchModel = new DefaultTableModel(
            new Object[] { "Bắt đầu", "Nội dung", "Đối thủ A", "Đối thủ B", "Sân", "ID" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable matchTable = new JTable(matchModel);

    private final JTextArea detailArea = new JTextArea();

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    // Lưu cache id -> summary để hiển thị nhanh
    private final Map<String, MatchSummary> matches = new LinkedHashMap<>();

    public BienBanPanel(Connection conn) {
        Objects.requireNonNull(conn, "Connection null");
        this.connection = conn;
        this.noiDungService = new NoiDungService(new NoiDungRepository(conn));
        this.tranDauService = new ChiTietTranDauService(new ChiTietTranDauRepository(conn));
        this.vanService = new ChiTietVanService(new ChiTietVanRepository(conn));
        this.soDoCaNhanRepo = new SoDoCaNhanRepository(conn);
        this.soDoDoiRepo = new SoDoDoiRepository(conn);
        this.vdvService = new VanDongVienService(new VanDongVienRepository(conn));

        setLayout(new BorderLayout(8, 8));
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        SwingUtilities.invokeLater(() -> {
            updateGiaiLabel();
            loadNoiDungOptions();
            reloadMatches();
        });
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.add(new JLabel("Trang biên bản"));
        p.add(lblGiai);
        p.add(new JLabel(" | Nội dung:"));
        cboNoiDung.setPreferredSize(new Dimension(260, 26));
        cboNoiDung.addActionListener(e -> reloadMatches());
        p.add(cboNoiDung);
        btnRefresh.addActionListener(e -> reloadMatches());
        p.add(btnRefresh);

        // Search by match ID (UUID)
        p.add(new JLabel(" | ID trận:"));
        txtSearchId.setPreferredSize(new Dimension(260, 26));
        p.add(txtSearchId);
        btnSearchId.addActionListener(e -> searchByMatchId());
        // press Enter to search
        txtSearchId.addActionListener(e -> searchByMatchId());
        p.add(btnSearchId);

        // Open per-match window for selected row
        btnOpenDetail.addActionListener(e -> openSelectedMatchDetailWindow());
        p.add(new JLabel(" | "));
        p.add(btnOpenDetail);
        return p;
    }

    private JSplitPane buildCenter() {
        matchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        matchTable.setRowHeight(26);
        matchTable.getSelectionModel().addListSelectionListener(this::onMatchSelected);
        // Double-click to open dedicated match window
        matchTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedMatchDetailWindow();
                }
            }
        });
        JScrollPane left = new JScrollPane(matchTable);
        left.setBorder(BorderFactory.createTitledBorder("Danh sách trận"));

        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        JScrollPane right = new JScrollPane(detailArea);
        right.setBorder(BorderFactory.createTitledBorder("Biên bản trận"));

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        sp.setResizeWeight(0.45);
        sp.setDividerLocation(0.45);
        return sp;
    }

    private void updateGiaiLabel() {
        int idGiai = getSelectedGiaiId();
        String ten = prefs.get("selectedGiaiDauName", null);
        if (idGiai > 0) {
            lblGiai.setText("Giải: " + (ten != null && !ten.isBlank() ? ten : ("ID=" + idGiai)));
        } else {
            lblGiai.setText("Giải: (chưa chọn)");
        }
    }

    private int getSelectedGiaiId() {
        int id = prefs.getInt("selectedGiaiDauId", -1);
        if (id <= 0)
            id = prefs.getInt("selected_giaidau_id", -1);
        return id;
    }

    private void loadNoiDungOptions() {
        try {
            Integer idGiai = prefs.getInt("selectedGiaiDauId", -1);
            List<NoiDung> all = noiDungService.getNoiDungByTuornament(idGiai);
            cboNoiDung.removeAllItems();
            cboNoiDung.addItem(new NoiDungItem(null, "Tất cả nội dung"));
            // ưu tiên đơn trước, sau đó đôi
            List<NoiDung> singles = new ArrayList<>();
            List<NoiDung> doubles = new ArrayList<>();
            for (NoiDung nd : all) {
                if (Boolean.TRUE.equals(nd.getTeam()))
                    doubles.add(nd);
                else
                    singles.add(nd);
            }
            for (NoiDung nd : singles)
                cboNoiDung.addItem(new NoiDungItem(nd.getId(), nd.getTenNoiDung()));
            for (NoiDung nd : doubles)
                cboNoiDung.addItem(new NoiDungItem(nd.getId(), nd.getTenNoiDung()));
            cboNoiDung.setSelectedIndex(0);
        } catch (java.sql.SQLException | RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nội dung: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private NoiDungItem getSelectedNoiDungItem() {
        Object o = cboNoiDung.getSelectedItem();
        return (o instanceof NoiDungItem ni) ? ni : new NoiDungItem(null, "");
    }

    private void reloadMatches() {
        matches.clear();
        matchModel.setRowCount(0);
        int idGiai = getSelectedGiaiId();
        if (idGiai <= 0)
            return;
        NoiDungItem sel = getSelectedNoiDungItem();
        Integer filterNd = sel.id; // may be null
        try {
            // 1) Thu thập tất cả ID_TRAN_DAU từ sơ đồ (đơn + đôi) theo giải và (tuỳ chọn)
            // nội dung
            List<String> collected = new ArrayList<>();
            List<BracketRef> refs = new ArrayList<>();
            if (filterNd == null) {
                // lấy tất cả nội dung
                try {
                    for (NoiDung nd : noiDungService.getNoiDungByTuornament(idGiai)) {
                        collectFromSingles(idGiai, nd.getId(), collected, refs);
                        collectFromDoubles(idGiai, nd.getId(), collected, refs);
                    }
                } catch (java.sql.SQLException sqle) {
                    throw new RuntimeException("Lỗi tải nội dung: " + sqle.getMessage(), sqle);
                }
            } else {
                collectFromSingles(idGiai, filterNd, collected, refs);
                collectFromDoubles(idGiai, filterNd, collected, refs);
            }

            // 2) Dựng bảng bên trái từ CHI_TIET_TRAN_DAU + info đối thủ từ refs
            for (BracketRef ref : refs) {
                if (ref.idTranDau == null || ref.idTranDau.isBlank())
                    continue;
                if (matches.containsKey(ref.idTranDau))
                    continue; // tránh trùng lặp do nhiều slot cùng ID
                ChiTietTranDau m = null;
                try {
                    m = tranDauService.get(ref.idTranDau);
                } catch (Exception ignore) {
                }
                String start = (m != null && m.getBatDau() != null) ? dtf.format(m.getBatDau()) : "";
                String ndLabel = ref.noiDungLabel != null ? ref.noiDungLabel : "";
                String a = ref.sideA != null ? ref.sideA : "";
                String b = ref.sideB != null ? ref.sideB : "";
                String san = (m != null) ? String.valueOf(m.getSan()) : "";
                matchModel.addRow(new Object[] { start, ndLabel, a, b, san, ref.idTranDau });
                matches.put(ref.idTranDau, new MatchSummary(ndLabel, a, b));
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách trận: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void collectFromSingles(int idGiai, int idNoiDung, List<String> sink, List<BracketRef> out) {
        for (SoDoCaNhan r : soDoCaNhanRepo.list(idGiai, idNoiDung)) {
            String id = r.getIdTranDau();
            if (id == null || id.isBlank())
                continue;
            if (!sink.contains(id)) {
                // Tìm 2 đối thủ theo ID_TRAN_DAU từ danh sách
                String a = null, b = null;
                for (SoDoCaNhan x : soDoCaNhanRepo.list(idGiai, idNoiDung)) {
                    if (id.equals(x.getIdTranDau())) {
                        String name = safeVdvName(x.getIdVdv());
                        if (a == null)
                            a = name;
                        else if (b == null && !Objects.equals(a, name))
                            b = name;
                    }
                }
                out.add(new BracketRef(id, noiDungName(idNoiDung), a, b));
                sink.add(id);
            }
        }
    }

    private void collectFromDoubles(int idGiai, int idNoiDung, List<String> sink, List<BracketRef> out) {
        for (SoDoDoi r : soDoDoiRepo.list(idGiai, idNoiDung)) {
            String id = r.getIdTranDau();
            if (id == null || id.isBlank())
                continue;
            if (!sink.contains(id)) {
                String a = null, b = null;
                for (SoDoDoi x : soDoDoiRepo.list(idGiai, idNoiDung)) {
                    if (id.equals(x.getIdTranDau())) {
                        String team = x.getTenTeam();
                        if (team == null)
                            team = "(đội)";
                        if (a == null)
                            a = team;
                        else if (b == null && !Objects.equals(a, team))
                            b = team;
                    }
                }
                out.add(new BracketRef(id, noiDungName(idNoiDung), a, b));
                sink.add(id);
            }
        }
    }

    private String noiDungName(int idNoiDung) {
        try {
            for (NoiDung nd : noiDungService.getAllNoiDung()) {
                if (nd.getId() == idNoiDung)
                    return nd.getTenNoiDung();
            }
        } catch (java.sql.SQLException ignore) {
        }
        return "";
    }

    private String safeVdvName(Integer id) {
        if (id == null)
            return "";
        try {
            VanDongVien v = vdvService.findOne(id);
            return v != null && v.getHoTen() != null ? v.getHoTen() : ("VDV#" + id);
        } catch (Exception e) {
            return "VDV#" + id;
        }
    }

    private void onMatchSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
        int row = matchTable.getSelectedRow();
        if (row < 0) {
            detailArea.setText("");
            return;
        }
        String id = String.valueOf(matchModel.getValueAt(row, 5));
        loadMatchDetail(id);
    }

    private void loadMatchDetail(String idTranDau) {
        StringBuilder sb = new StringBuilder();
        try {
            MatchSummary ms = matches.get(idTranDau);
            ChiTietTranDau m = null;
            try {
                m = tranDauService.get(idTranDau);
            } catch (RuntimeException ignore) {
            }
            sb.append("ID trận: ").append(idTranDau).append('\n');
            if (ms != null) {
                sb.append("Nội dung: ").append(ms.ndLabel).append('\n');
                sb.append("Đối thủ: ").append(nullToEmpty(ms.a)).append(" vs ").append(nullToEmpty(ms.b)).append('\n');
            }
            if (m != null) {
                sb.append("Bắt đầu: ").append(m.getBatDau() != null ? m.getBatDau().toString() : "").append('\n');
                sb.append("Kết thúc: ").append(m.getKetThuc() != null ? m.getKetThuc().toString() : "").append('\n');
                sb.append("Sân: ").append(m.getSan()).append('\n');
            }
            sb.append('\n');
            List<ChiTietVan> sets = new ArrayList<>();
            try {
                sets = vanService.listByMatch(idTranDau);
            } catch (RuntimeException ignore) {
            }
            if (sets == null || sets.isEmpty()) {
                sb.append("(Chưa có dữ liệu set)\n");
            } else {
                for (ChiTietVan v : sets) {
                    String tokens = v.getDauThoiGian();
                    int c1 = countTokenIgnoringSwap(tokens, "P1@");
                    int c2 = countTokenIgnoringSwap(tokens, "P2@");
                    int left = c1;
                    int right = c2;
                    // Nếu set_no = 2 và có SWAP trong dấu thời gian thì hiển thị P2 - P1
                    if (v.getSetNo() == 2 && tokens != null && tokens.contains("SWAP@")) {
                        int tmp = left;
                        left = right;
                        right = tmp;
                    }
                    sb.append("Set ").append(v.getSetNo()).append(": ")
                            .append(left).append(" - ")
                            .append(right).append('\n');
                    if (tokens != null && !tokens.isBlank()) {
                        sb.append("  ").append(tokens).append('\n');
                    }
                    sb.append('\n');
                }
            }
            if (!matches.containsKey(idTranDau)) {
                sb.append("(Lưu ý: ID này không nằm trong danh sách sơ đồ hiện tại)\n");
            }
        } catch (RuntimeException ex) {
            sb.append("Lỗi tải biên bản: ").append(ex.getMessage());
        }
        detailArea.setText(sb.toString());
        detailArea.setCaretPosition(0);
    }

    // Search handler: try select in table; if not found, load details directly
    private void searchByMatchId() {
        String id = txtSearchId.getText();
        if (id == null || id.isBlank()) {
            return;
        }
        String key = id.trim();
        // find in table (exact or contains)
        int foundRow = -1;
        for (int i = 0; i < matchModel.getRowCount(); i++) {
            Object v = matchModel.getValueAt(i, 5);
            if (v == null)
                continue;
            String cell = v.toString();
            if (cell.equalsIgnoreCase(key) || cell.contains(key)) {
                foundRow = i;
                break;
            }
        }
        if (foundRow >= 0) {
            try {
                matchTable.setRowSelectionInterval(foundRow, foundRow);
                matchTable.scrollRectToVisible(matchTable.getCellRect(foundRow, 0, true));
            } catch (Exception ignore) {
            }
            loadMatchDetail(String.valueOf(matchModel.getValueAt(foundRow, 5)));
        } else {
            // Not in current list: attempt to load details directly
            loadMatchDetail(key);
        }
    }

    // Open detail window for the currently selected match
    private void openSelectedMatchDetailWindow() {
        int row = matchTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        String id = String.valueOf(matchModel.getValueAt(row, 5));
        try {
            java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
            java.sql.Connection c = (this.connection != null) ? this.connection : resolveConnection(owner);
            if (c == null) {
                throw new IllegalStateException("Chưa có kết nối CSDL");
            }
            com.example.btms.ui.result.BienBanTranFrame f = new com.example.btms.ui.result.BienBanTranFrame(c, id);
            if (owner != null) {
                f.setLocationRelativeTo(owner);
            }
            f.setVisible(true);
        } catch (Throwable ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Không mở được biên bản trận: " + String.valueOf(ex.getMessage()),
                    "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private java.sql.Connection resolveConnection(java.awt.Window owner) {
        try {
            if (owner instanceof com.example.btms.ui.main.MainFrame mf) {
                return mf.service().current();
            }
        } catch (Throwable ignore) {
        }
        // As a fallback, return null which will cause the frame to error early
        return null;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    // Đếm số lần xuất hiện prefix, bỏ qua token SWAP@
    private static int countTokenIgnoringSwap(String tokens, String prefix) {
        if (tokens == null || tokens.isBlank() || prefix == null || prefix.isEmpty())
            return 0;
        int c = 0;
        String[] parts = tokens.split(";");
        for (String raw : parts) {
            String t = raw.trim();
            if (t.isEmpty() || t.startsWith("SWAP@"))
                continue;
            if (t.startsWith(prefix))
                c++;
        }
        return c;
    }

    private static final class NoiDungItem {
        final Integer id; // null -> all
        final String label;

        NoiDungItem(Integer id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class MatchSummary {
        final String ndLabel;
        final String a;
        final String b;

        MatchSummary(String ndLabel, String a, String b) {
            this.ndLabel = ndLabel;
            this.a = a;
            this.b = b;
        }
    }

    private static final class BracketRef {
        final String idTranDau;
        final String noiDungLabel;
        final String sideA;
        final String sideB;

        BracketRef(String id, String label, String a, String b) {
            this.idTranDau = id;
            this.noiDungLabel = label;
            this.sideA = a;
            this.sideB = b;
        }
    }
}
