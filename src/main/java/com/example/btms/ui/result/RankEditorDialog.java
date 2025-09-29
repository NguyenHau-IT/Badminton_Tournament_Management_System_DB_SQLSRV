package com.example.btms.ui.result;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.example.btms.model.category.NoiDung;
import com.example.btms.model.result.KetQuaCaNhan;
import com.example.btms.model.result.KetQuaDoi;
import com.example.btms.model.team.DangKiDoi;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.result.KetQuaCaNhanService;
import com.example.btms.service.result.KetQuaDoiService;
import com.example.btms.service.team.DangKiDoiService;
import com.example.btms.service.club.CauLacBoService;

/**
 * Dialog gộp: sửa thứ hạng cho cả đơn và đôi trong cùng một cửa sổ.
 * - Chọn nội dung: tự nhận biết là đơn hay đôi (dựa theo flag team).
 * - Với đơn: combobox chọn VĐV cho hạng 1/2/3 (đồng có thể 2 VĐV)
 * - Với đôi: combobox chọn Đội cho hạng 1/2/3 (đồng có thể 2 đội)
 */
public class RankEditorDialog extends JDialog {
    private final int idGiai;
    private final NoiDungService noiDungService;
    private final KetQuaCaNhanService kqCaNhanService;
    private final KetQuaDoiService kqDoiService;
    private final VanDongVienService vdvService;
    private final DangKiDoiService doiService;
    private final CauLacBoService clbService;

    // Table-like grid: one row per nội dung, columns for ranks
    private final JPanel tablePanel = new JPanel(new GridBagLayout());
    private final List<RowControls> rows = new ArrayList<>();

    public RankEditorDialog(java.awt.Window owner,
            int idGiai,
            NoiDungService noiDungService,
            KetQuaCaNhanService kqCaNhanService,
            KetQuaDoiService kqDoiService,
            VanDongVienService vdvService,
            DangKiDoiService doiService,
            CauLacBoService clbService) {
        super(owner instanceof Frame ? (Frame) owner : null, "Sửa thứ hạng", ModalityType.APPLICATION_MODAL);
        this.idGiai = idGiai;
        this.noiDungService = Objects.requireNonNull(noiDungService);
        this.kqCaNhanService = Objects.requireNonNull(kqCaNhanService);
        this.kqDoiService = Objects.requireNonNull(kqDoiService);
        this.vdvService = Objects.requireNonNull(vdvService);
        this.doiService = Objects.requireNonNull(doiService);
        this.clbService = Objects.requireNonNull(clbService);

        setLayout(new BorderLayout(8, 8));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenterTable(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
        // Increase dialog size to give more room for the rank columns
        setSize(1400, 600);
        setLocationRelativeTo(owner);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        JLabel title = new JLabel("Chỉnh sửa thứ hạng theo nội dung (mỗi nội dung 1 hàng)");
        title.setFont(title.getFont().deriveFont(title.getFont().getStyle() | java.awt.Font.BOLD, 13f));
        p.add(title);
        return p;
    }

    private JScrollPane buildCenterTable() {
        tablePanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        rebuildRows();
        JScrollPane sp = new JScrollPane(tablePanel);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JPanel buildBottom() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnReload = new JButton("Làm mới");
        JButton btnClose = new JButton("Đóng");
        p.add(btnReload);
        p.add(btnClose);
        btnReload.addActionListener(e -> rebuildRows());
        btnClose.addActionListener(e -> dispose());
        return p;
    }

    /* ===== helpers ===== */
    private static void selectId(JComboBox<IdName> combo, Integer id) {
        DefaultComboBoxModel<IdName> m = (DefaultComboBoxModel<IdName>) combo.getModel();
        if (id == null) {
            combo.setSelectedIndex(-1);
            return;
        }
        for (int i = 0; i < m.getSize(); i++) {
            if (m.getElementAt(i).id == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(-1);
    }

    private static <T> void selectItem(JComboBox<T> combo, T item) {
        if (item == null) {
            combo.setSelectedIndex(-1);
            return;
        }
        DefaultComboBoxModel<T> m = (DefaultComboBoxModel<T>) combo.getModel();
        for (int i = 0; i < m.getSize(); i++) {
            if (Objects.equals(m.getElementAt(i), item)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(-1);
    }

    private static String normalizeTeam(String name) {
        if (name == null)
            return "";
        String s = name.trim();
        int sep = s.indexOf(" - ");
        if (sep >= 0)
            s = s.substring(0, sep).trim();
        return s.toLowerCase();
    }

    /* ===== table (rows) ===== */
    private void rebuildRows() {
        rows.clear();
        tablePanel.removeAll();
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.gridy = 0;
        gc.gridx = 0;
        gc.anchor = GridBagConstraints.WEST;
        // Header
        addHeaderCell(gc, 0, "Nội dung");
        addHeaderCell(gc, 1, "Hạng 1 (Vàng)");
        addHeaderCell(gc, 2, "Hạng 2 (Bạc)");
        addHeaderCell(gc, 3, "Hạng 3 (Đồng) #1");
        addHeaderCell(gc, 4, "Hạng 3 (Đồng) #2");
        addHeaderCell(gc, 5, "");

        try {
            List<NoiDung> all = noiDungService.getNoiDungByTuornament(idGiai);
            int row = 1;
            for (NoiDung nd : all) {
                boolean isTeam = Boolean.TRUE.equals(nd.getTeam());
                RowControls rc = new RowControls(nd.getId(), nd.getTenNoiDung(), isTeam);
                buildRowControls(rc);
                rows.add(rc);

                // place components
                addCell(row, 0, new JLabel(rc.tenNoiDung));
                addCell(row, 1, rc.vang);
                addCell(row, 2, rc.bac);
                addCell(row, 3, rc.dong1);
                addCell(row, 4, rc.dong2);
                JButton btn = new JButton("Lưu");
                btn.addActionListener(e -> saveRow(rc));
                addCell(row, 5, btn);
                row++;
            }
            // Đồng bộ kích thước tất cả combobox sau khi tạo xong các hàng
            applyUniformComboSizes();
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nội dung: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }

        // fill stretch
        GridBagConstraints fill = new GridBagConstraints();
        fill.gridx = 0;
        fill.gridy = rows.size() + 2;
        fill.weightx = 1;
        fill.weighty = 1;
        fill.fill = GridBagConstraints.BOTH;
        tablePanel.add(new JPanel(), fill);
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    /**
     * Cố định kích thước tất cả combobox (Vàng/Bạc/Đồng) để đều nhau.
     * Tính kích thước lớn nhất hiện có trong các combobox và áp cho tất cả.
     */
    private void applyUniformComboSizes() {
        int maxW = 0;
        int maxH = 0;
        for (RowControls rc : rows) {
            java.awt.Dimension dv = ((JComboBox<?>) rc.vang).getPreferredSize();
            java.awt.Dimension db = ((JComboBox<?>) rc.bac).getPreferredSize();
            java.awt.Dimension d1 = ((JComboBox<?>) rc.dong1).getPreferredSize();
            java.awt.Dimension d2 = ((JComboBox<?>) rc.dong2).getPreferredSize();
            maxW = Math.max(maxW, Math.max(Math.max(dv.width, db.width), Math.max(d1.width, d2.width)));
            maxH = Math.max(maxH, Math.max(Math.max(dv.height, db.height), Math.max(d1.height, d2.height)));
        }
        if (maxW <= 0) {
            // fallback width if nothing computed yet
            maxW = 220;
        }
        if (maxH <= 0) {
            maxH = 28;
        }
        java.awt.Dimension fixed = new java.awt.Dimension(maxW, maxH);
        for (RowControls rc : rows) {
            setComboFixedSize((JComboBox<?>) rc.vang, fixed);
            setComboFixedSize((JComboBox<?>) rc.bac, fixed);
            setComboFixedSize((JComboBox<?>) rc.dong1, fixed);
            setComboFixedSize((JComboBox<?>) rc.dong2, fixed);
        }
    }

    private static void setComboFixedSize(JComboBox<?> cb, java.awt.Dimension d) {
        cb.setPreferredSize(d);
        cb.setMinimumSize(d);
        cb.setMaximumSize(d);
    }

    private void addHeaderCell(GridBagConstraints base, int col, String text) {
        GridBagConstraints c = (GridBagConstraints) base.clone();
        c.gridx = col;
        c.gridy = base.gridy;
        c.weightx = (col == 0 ? 0.4 : 0.15);
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel lb = new JLabel(text);
        lb.setFont(lb.getFont().deriveFont(lb.getFont().getStyle() | java.awt.Font.BOLD));
        tablePanel.add(lb, c);
    }

    private void addCell(int row, int col, java.awt.Component comp) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = col;
        c.gridy = row;
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = (col == 0 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE);
        c.anchor = GridBagConstraints.WEST;
        if (col == 0)
            c.weightx = 1.0;
        tablePanel.add(comp, c);
    }

    private void buildRowControls(RowControls rc) {
        if (!rc.isTeam) {
            // Singles options
            Map<String, Integer> map = vdvService.loadSinglesNames(rc.idNoiDung, idGiai);
            List<IdName> opts = map.entrySet().stream()
                    .map(e -> {
                        int vdvId = e.getValue();
                        String club = safeClubNameByVdvId(vdvId);
                        String label = (e.getKey() != null ? e.getKey().trim() : "")
                                + (club.isBlank() ? "" : " - " + club);
                        return new IdName(vdvId, label);
                    })
                    .sorted(java.util.Comparator.comparing(IdName::name, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
            rc.vang = new JComboBox<>(new DefaultComboBoxModel<>(opts.toArray(IdName[]::new)));
            rc.bac = new JComboBox<>(new DefaultComboBoxModel<>(opts.toArray(IdName[]::new)));
            rc.dong1 = new JComboBox<>(new DefaultComboBoxModel<>(opts.toArray(IdName[]::new)));
            rc.dong2 = new JComboBox<>(new DefaultComboBoxModel<>(opts.toArray(IdName[]::new)));

            // Preselect by existing
            List<KetQuaCaNhan> kqSinglesList = kqCaNhanService.list(idGiai, rc.idNoiDung);
            Integer gold = null, silver = null;
            List<Integer> bronzes = new ArrayList<>();
            for (KetQuaCaNhan r : kqSinglesList) {
                if (r == null)
                    continue;
                switch (r.getThuHang()) {
                    case 1 -> gold = r.getIdVdv();
                    case 2 -> silver = r.getIdVdv();
                    case 3 -> bronzes.add(r.getIdVdv());
                    default -> {
                    }
                }
            }
            selectId(castId(rc.vang), gold);
            selectId(castId(rc.bac), silver);
            selectId(castId(rc.dong1), bronzes.isEmpty() ? null : bronzes.get(0));
            selectId(castId(rc.dong2), bronzes.size() > 1 ? bronzes.get(1) : null);
        } else {
            // Doubles options
            List<DangKiDoi> teams = doiService.listTeams(idGiai, rc.idNoiDung);
            if (teams == null)
                teams = java.util.Collections.emptyList();
            Map<String, TeamItem> index = new HashMap<>();
            List<TeamItem> opts = new ArrayList<>();
            for (DangKiDoi t : teams) {
                String club = safeClubNameByClbId(t.getIdCauLacBo());
                TeamItem ti = new TeamItem(t.getIdTeam(), t.getIdCauLacBo(), t.getTenTeam(), club);
                opts.add(ti);
                index.put(normalizeTeam(t.getTenTeam()), ti);
            }
            opts = opts.stream()
                    .sorted(java.util.Comparator.comparing(TeamItem::toString, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
            rc.vang = new JComboBox<>(new DefaultComboBoxModel<>(opts.toArray(TeamItem[]::new)));
            rc.bac = new JComboBox<>(new DefaultComboBoxModel<>(opts.toArray(TeamItem[]::new)));
            rc.dong1 = new JComboBox<>(new DefaultComboBoxModel<>(opts.toArray(TeamItem[]::new)));
            rc.dong2 = new JComboBox<>(new DefaultComboBoxModel<>(opts.toArray(TeamItem[]::new)));

            List<KetQuaDoi> kqDoublesList = kqDoiService.list(idGiai, rc.idNoiDung);
            TeamItem gold = null, silver = null;
            List<TeamItem> bronzes = new ArrayList<>();
            for (KetQuaDoi r : kqDoublesList) {
                if (r == null)
                    continue;
                TeamItem ti = index.get(normalizeTeam(r.getTenTeam()));
                if (ti == null)
                    continue;
                Integer th = r.getThuHang();
                if (th != null && th == 1) {
                    gold = ti;
                } else if (th != null && th == 2) {
                    silver = ti;
                } else if (th != null && th == 3) {
                    bronzes.add(ti);
                }
            }
            selectItem(castTeam(rc.vang), gold);
            selectItem(castTeam(rc.bac), silver);
            selectItem(castTeam(rc.dong1), bronzes.isEmpty() ? null : bronzes.get(0));
            selectItem(castTeam(rc.dong2), bronzes.size() > 1 ? bronzes.get(1) : null);
        }
    }

    private void saveRow(RowControls rc) {
        try {
            if (!rc.isTeam) {
                // Singles save per rank
                saveSinglesRank(rc.idNoiDung, 1, (IdName) rc.vang.getSelectedItem());
                saveSinglesRank(rc.idNoiDung, 2, (IdName) rc.bac.getSelectedItem());
                saveSinglesRank(rc.idNoiDung, 3, (IdName) rc.dong1.getSelectedItem());
                saveSinglesRank(rc.idNoiDung, 3, (IdName) rc.dong2.getSelectedItem());
            } else {
                saveDoublesRank(rc.idNoiDung, 1, (TeamItem) rc.vang.getSelectedItem());
                saveDoublesRank(rc.idNoiDung, 2, (TeamItem) rc.bac.getSelectedItem());
                saveDoublesRank(rc.idNoiDung, 3, (TeamItem) rc.dong1.getSelectedItem());
                saveDoublesRank(rc.idNoiDung, 3, (TeamItem) rc.dong2.getSelectedItem());
            }
            JOptionPane.showMessageDialog(this, "Đã lưu: " + rc.tenNoiDung);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveSinglesRank(int idNoiDung, int rank, IdName sel) {
        if (sel == null) {
            int opt = JOptionPane.showConfirmDialog(this, "Xóa kết quả hạng " + rank + "? (" + idNoiDung + ")",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                kqCaNhanService.delete(idGiai, idNoiDung, rank);
            }
            return;
        }
        if (kqCaNhanService.existsByRank(idGiai, idNoiDung, rank)) {
            kqCaNhanService.update(idGiai, idNoiDung, rank, sel.id);
        } else {
            kqCaNhanService.create(idGiai, idNoiDung, sel.id, rank);
        }
    }

    private void saveDoublesRank(int idNoiDung, int rank, TeamItem sel) {
        if (sel == null) {
            int opt = JOptionPane.showConfirmDialog(this, "Xóa kết quả hạng " + rank + "? (" + idNoiDung + ")",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                kqDoiService.delete(idGiai, idNoiDung, rank);
            }
            return;
        }
        if (kqDoiService.exists(idGiai, idNoiDung, rank)) {
            kqDoiService.update(idGiai, idNoiDung, rank, sel.idClb, sel.tenTeam);
        } else {
            kqDoiService.create(idGiai, idNoiDung, sel.idClb, sel.tenTeam, rank);
        }
    }

    // small casts helpers to keep generics happy
    @SuppressWarnings("unchecked")
    private static JComboBox<IdName> castId(JComboBox<?> cb) {
        return (JComboBox<IdName>) cb;
    }

    @SuppressWarnings("unchecked")
    private static JComboBox<TeamItem> castTeam(JComboBox<?> cb) {
        return (JComboBox<TeamItem>) cb;
    }

    private static final class RowControls {
        final int idNoiDung;
        final String tenNoiDung;
        final boolean isTeam;
        JComboBox<?> vang;
        JComboBox<?> bac;
        JComboBox<?> dong1;
        JComboBox<?> dong2;

        RowControls(int id, String name, boolean isTeam) {
            this.idNoiDung = id;
            this.tenNoiDung = name;
            this.isTeam = isTeam;
        }
    }

    /* === DTOs === */
    private record IdName(int id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private static final class TeamItem {
        final int idTeam;
        final Integer idClb;
        final String tenTeam;
        final String tenClb;

        TeamItem(int idTeam, Integer idClb, String tenTeam, String tenClb) {
            this.idTeam = idTeam;
            this.idClb = idClb;
            this.tenTeam = tenTeam == null ? "" : tenTeam.trim();
            this.tenClb = tenClb == null ? "" : tenClb.trim();
        }

        @Override
        public String toString() {
            if (tenClb.isBlank())
                return tenTeam;
            return tenTeam + " - " + tenClb;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof TeamItem t) && t.idTeam == idTeam;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(idTeam);
        }
    }

    // ==== club helpers (safe) ====
    private String safeClubNameByVdvId(Integer vdvId) {
        if (vdvId == null)
            return "";
        try {
            String s = vdvService.getClubNameById(vdvId);
            return s != null ? s.trim() : "";
        } catch (Exception ex) {
            return "";
        }
    }

    private String safeClubNameByClbId(Integer idClb) {
        if (idClb == null)
            return "";
        try {
            var clb = clbService.findOne(idClb);
            return clb != null && clb.getTenClb() != null ? clb.getTenClb().trim() : "";
        } catch (Exception ex) {
            return "";
        }
    }

    // NdItem no longer needed in table mode
}
