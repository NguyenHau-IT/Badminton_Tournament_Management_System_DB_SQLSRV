package com.example.btms.ui.result;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;

import com.example.btms.config.Prefs;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.player.VanDongVien;
import com.example.btms.model.result.KetQuaDoi;
import com.example.btms.model.team.ChiTietDoi;
import com.example.btms.model.team.DangKiDoi;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.player.VanDongVienRepository;
import com.example.btms.repository.result.KetQuaCaNhanRepository;
import com.example.btms.repository.result.KetQuaDoiRepository;
import com.example.btms.repository.team.ChiTietDoiRepository;
import com.example.btms.repository.team.DangKiDoiRepository;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.result.KetQuaCaNhanService;
import com.example.btms.service.result.KetQuaDoiService;
import com.example.btms.service.team.ChiTietDoiService;
import com.example.btms.service.team.DangKiDoiService;

/**
 * Trang xem tổng sắp huy chương và danh sách VĐV đạt huy chương (theo giải hiện
 * tại).
 * - Trái: Bảng tổng sắp theo CLB (Vàng/Bạc/Đồng/Tổng)
 * - Phải: Danh sách huy chương theo nội dung: Nội dung, Hạng, CLB, VĐV
 *
 * Nguồn dữ liệu: KET_QUA_DOI (theo nội dung), ánh xạ sang đội và thành viên từ
 * DANG_KI_DOI + CHI_TIET_DOI + VAN_DONG_VIEN
 */
public class TongSapHuyChuongPanel extends JPanel {

    private final Prefs prefs = new Prefs();
    private final NoiDungService noiDungService;
    private final KetQuaDoiService ketQuaDoiService;
    private final KetQuaCaNhanService ketQuaCaNhanService;
    private final DangKiDoiService dangKiDoiService;
    private final ChiTietDoiService chiTietDoiService;
    private final VanDongVienService vdvService;
    private final CauLacBoService clbService;

    private final JLabel lblGiai = new JLabel();
    private final JComboBox<Object> cboNoiDung = new JComboBox<>();
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JButton btnExportPdf = new JButton("Xuất PDF");
    private final JButton btnEditRanks = new JButton("Sửa thứ hạng");

    private final DefaultTableModel tallyModel = new DefaultTableModel(
            new Object[] { "CLB", "Vàng", "Bạc", "Đồng", "Tổng", "Thứ hạng" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tallyTable = new JTable(tallyModel);

    private final DefaultTableModel medalistModel = new DefaultTableModel(
            new Object[] { "Nội dung", "Hạng", "CLB", "VĐV" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable medalistTable = new JTable(medalistModel);
    private volatile boolean userDraggingDivider = false;

    // Cached base font for PDF (Unicode)
    private transient com.lowagie.text.pdf.BaseFont pdfBaseFont;

    public TongSapHuyChuongPanel(Connection conn,
            CauLacBoService clbService) {
        Objects.requireNonNull(conn, "Connection null");
        this.noiDungService = new NoiDungService(new NoiDungRepository(conn));
        this.ketQuaDoiService = new KetQuaDoiService(new KetQuaDoiRepository(conn));
        this.ketQuaCaNhanService = new KetQuaCaNhanService(new KetQuaCaNhanRepository(conn));
        this.dangKiDoiService = new DangKiDoiService(new DangKiDoiRepository(conn));
        this.chiTietDoiService = new ChiTietDoiService(conn, new DangKiDoiRepository(conn),
                new ChiTietDoiRepository(conn));
        this.vdvService = new VanDongVienService(new VanDongVienRepository(conn));
        this.clbService = Objects.requireNonNull(clbService);

        setLayout(new BorderLayout(8, 8));
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        SwingUtilities.invokeLater(() -> {
            updateGiaiLabel();
            loadNoiDungOptions();
            reloadData();
        });
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.add(new JLabel("Tổng sắp huy chương"));
        p.add(lblGiai);
        p.add(new JLabel(" | Nội dung:"));
        cboNoiDung.setPreferredSize(new Dimension(260, 26));
        p.add(cboNoiDung);
        p.add(btnRefresh);
        p.add(btnEditRanks);
        p.add(btnExportPdf);
        btnRefresh.addActionListener(e -> reloadData());
        cboNoiDung.addActionListener(e -> reloadData());
        // Trước khi xuất, mở hộp thoại xem trước
        btnExportPdf.addActionListener(e -> showPreviewDialog());
        btnEditRanks.addActionListener(e -> openUnifiedRankDialog());
        return p;
    }

    private JSplitPane buildCenter() {
        tallyTable.setRowHeight(24);
        medalistTable.setRowHeight(36);
        // Enable grid lines and subtle borders for readability
        tallyTable.setShowGrid(true);
        tallyTable.setGridColor(new java.awt.Color(200, 200, 200));
        tallyTable.setIntercellSpacing(new Dimension(1, 1));
        medalistTable.setShowGrid(true);
        medalistTable.setGridColor(new java.awt.Color(200, 200, 200));
        medalistTable.setIntercellSpacing(new Dimension(1, 1));
        // Center header titles
        centerHeader(tallyTable);
        centerHeader(medalistTable);
        JScrollPane left = new JScrollPane(tallyTable);
        left.setBorder(BorderFactory.createTitledBorder("Bảng tổng sắp theo CLB"));
        JScrollPane right = new JScrollPane(medalistTable);
        right.setBorder(BorderFactory.createTitledBorder("Danh sách VĐV đạt huy chương"));
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        sp.setContinuousLayout(true);
        sp.setResizeWeight(0.40);
        sp.setDividerLocation(0.40);
        // Maintain ~40% ratio on container resize, but allow user drag to override
        // in-session
        sp.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (!userDraggingDivider) {
                    sp.setDividerLocation(0.40);
                }
            }
        });
        // Detect user dragging divider to avoid snapping back while dragging
        sp.addHierarchyListener(ev -> {
            if ((ev.getChangeFlags() & java.awt.event.HierarchyEvent.DISPLAYABILITY_CHANGED) != 0
                    && sp.isDisplayable()) {
                javax.swing.plaf.SplitPaneUI uiLocal = sp.getUI();
                if (uiLocal instanceof javax.swing.plaf.basic.BasicSplitPaneUI b) {
                    javax.swing.plaf.basic.BasicSplitPaneDivider divider = b.getDivider();
                    if (divider != null) {
                        divider.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mousePressed(java.awt.event.MouseEvent e) {
                                userDraggingDivider = true;
                            }

                            @Override
                            public void mouseReleased(java.awt.event.MouseEvent e) {
                                userDraggingDivider = false;
                            }
                        });
                    }
                }
            }
        });

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
        // Hỗ trợ cả 2 key lịch sử
        int id = prefs.getInt("selectedGiaiDauId", -1);
        if (id <= 0)
            id = prefs.getInt("selected_giaidau_id", -1);
        return id;
    }

    private void loadNoiDungOptions() {
        try {
            Integer idGiai = prefs.getInt("selectedGiaiDauId", -1);
            List<NoiDung> all = noiDungService.getNoiDungByTuornament(idGiai);
            List<NoiDung> doublesOnly = new ArrayList<>();
            List<NoiDung> singlesOnly = new ArrayList<>();
            for (NoiDung nd : all) {
                if (Boolean.TRUE.equals(nd.getTeam())) {
                    doublesOnly.add(nd);
                } else {
                    singlesOnly.add(nd);
                }
            }
            cboNoiDung.removeAllItems();
            cboNoiDung.addItem(new NoiDungItem(null, "Tất cả nội dung"));
            // Liệt kê tất cả nội dung (đơn rồi đôi) để dễ tra cứu
            for (NoiDung nd : singlesOnly) {
                cboNoiDung.addItem(new NoiDungItem(nd.getId(), nd.getTenNoiDung()));
            }
            for (NoiDung nd : doublesOnly) {
                cboNoiDung.addItem(new NoiDungItem(nd.getId(), nd.getTenNoiDung()));
            }
            cboNoiDung.setSelectedIndex(0);
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nội dung: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống khi tải nội dung: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private NoiDungItem getSelectedNoiDungItem() {
        Object o = cboNoiDung.getSelectedItem();
        return (o instanceof NoiDungItem ni) ? ni : new NoiDungItem(null, "");
    }

    private void reloadData() {
        int idGiai = getSelectedGiaiId();
        if (idGiai <= 0) {
            tallyModel.setRowCount(0);
            medalistModel.setRowCount(0);
            return;
        }
        try {
            // Determine category IDs to include (đơn + đôi)
            List<Integer> ndIds = new ArrayList<>();
            Map<Integer, String> ndNames = new HashMap<>();
            Map<Integer, Boolean> ndIsTeam = new HashMap<>();
            NoiDungItem sel = getSelectedNoiDungItem();
            if (sel.id == null) {
                for (NoiDung nd : noiDungService.getAllNoiDung()) {
                    ndIds.add(nd.getId());
                    ndNames.put(nd.getId(), nd.getTenNoiDung());
                    ndIsTeam.put(nd.getId(), Boolean.TRUE.equals(nd.getTeam()));
                }
            } else {
                ndIds.add(sel.id);
                // need team flag for this id
                for (NoiDung nd : noiDungService.getAllNoiDung()) {
                    if (nd.getId().equals(sel.id)) {
                        ndNames.put(sel.id, sel.label);
                        ndIsTeam.put(sel.id, Boolean.TRUE.equals(nd.getTeam()));
                        break;
                    }
                }
            }

            // Aggregate tally and collect medalists
            Map<Integer, MedalTally> tally = new LinkedHashMap<>(); // key: idClb
            medalistModel.setRowCount(0);

            for (Integer idNd : ndIds) {
                boolean isTeam = Boolean.TRUE.equals(ndIsTeam.get(idNd));
                if (isTeam) {
                    List<KetQuaDoi> rows = ketQuaDoiService.list(idGiai, idNd);
                    if (rows == null)
                        continue;
                    // Build map team name -> team id for this category
                    Map<String, Integer> teamNameToId = new HashMap<>();
                    List<DangKiDoi> teams = dangKiDoiService.listTeams(idGiai, idNd);
                    for (DangKiDoi t : teams) {
                        if (t.getTenTeam() != null) {
                            teamNameToId.put(t.getTenTeam().trim().toLowerCase(), t.getIdTeam());
                        }
                    }
                    for (KetQuaDoi r : rows) {
                        if (r == null)
                            continue;
                        Integer idClb = r.getIdClb();
                        if (idClb == null)
                            continue;
                        MedalTally mt = tally.computeIfAbsent(idClb, k -> new MedalTally());
                        Integer thuHang = r.getThuHang();
                        int rank;
                        if (thuHang == null) {
                            rank = 0;
                        } else {
                            rank = thuHang; // auto-unboxing
                        }
                        switch (rank) {
                            case 1 -> mt.vang++;
                            case 2 -> mt.bac++;
                            case 3 -> mt.dong++;
                            default -> {
                            }
                        }
                        // Medalist line for doubles
                        String medalTxt = switch (rank) {
                            case 1 -> "Vàng";
                            case 2 -> "Bạc";
                            case 3 -> "Đồng";
                            default -> String.valueOf(rank);
                        };
                        String clbName = "";
                        try {
                            var c = clbService.findOne(idClb);
                            if (c != null && c.getTenClb() != null)
                                clbName = c.getTenClb();
                        } catch (RuntimeException ignore) {
                        }
                        String team = r.getTenTeam();
                        String v1 = "", v2 = "";
                        if (team != null) {
                            String key = team.trim();
                            int sep = key.indexOf(" - ");
                            if (sep >= 0)
                                key = key.substring(0, sep).trim();
                            Integer tid = teamNameToId.get(key.toLowerCase());
                            if (tid != null) {
                                try {
                                    List<ChiTietDoi> members = chiTietDoiService.listMembers(tid);
                                    if (members != null && !members.isEmpty()) {
                                        if (members.size() >= 1)
                                            v1 = getVdvNameSafe(members.get(0).getIdVdv());
                                        if (members.size() >= 2)
                                            v2 = getVdvNameSafe(members.get(1).getIdVdv());
                                    }
                                } catch (RuntimeException ignore) {
                                    /* best-effort */ }
                            }
                        }
                        String vdvCell = twoLineHtml(v1, v2);
                        medalistModel.addRow(new Object[] { ndNames.get(idNd), medalTxt, clbName, vdvCell });
                    }
                } else {
                    // Singles
                    List<com.example.btms.model.result.KetQuaCaNhan> rows = ketQuaCaNhanService.list(idGiai, idNd);
                    if (rows == null)
                        continue;
                    for (var r : rows) {
                        if (r == null)
                            continue;
                        int idVdv = r.getIdVdv();
                        VanDongVien v;
                        try {
                            v = vdvService.findOne(idVdv);
                        } catch (RuntimeException ex) {
                            v = null;
                        }
                        Integer idClb = (v != null) ? v.getIdClb() : null;
                        if (idClb == null)
                            continue;
                        MedalTally mt = tally.computeIfAbsent(idClb, k -> new MedalTally());
                        Integer thuHang = r.getThuHang();
                        int rank;
                        if (thuHang == null) {
                            rank = 0;
                        } else {
                            rank = thuHang; // auto-unboxing
                        }
                        switch (rank) {
                            case 1 -> mt.vang++;
                            case 2 -> mt.bac++;
                            case 3 -> mt.dong++;
                            default -> {
                            }
                        }
                        String medalTxt = switch (rank) {
                            case 1 -> "Vàng";
                            case 2 -> "Bạc";
                            case 3 -> "Đồng";
                            default -> String.valueOf(rank);
                        };
                        String clbName = "";
                        try {
                            var c = clbService.findOne(idClb);
                            if (c != null && c.getTenClb() != null)
                                clbName = c.getTenClb();
                        } catch (RuntimeException ignore) {
                        }
                        String v1 = (v != null && v.getHoTen() != null) ? v.getHoTen() : ("VDV#" + idVdv);
                        medalistModel.addRow(new Object[] { ndNames.get(idNd), medalTxt, clbName, v1 });
                    }
                }
            }

            // Fill tally table sorted by Vàng, Bạc, Đồng, Tổng desc
            tallyModel.setRowCount(0);
            // Convert to list with club name
            List<Map.Entry<Integer, MedalTally>> entries = new ArrayList<>(tally.entrySet());
            entries.sort(Comparator.<Map.Entry<Integer, MedalTally>>comparingInt(e -> e.getValue().vang)
                    .thenComparingInt(e -> e.getValue().bac)
                    .thenComparingInt(e -> e.getValue().dong)
                    .thenComparingInt(e -> e.getValue().total())
                    .reversed());
            // Compute ranks (competition ranking: 1,2,2,4 ... for ties)
            int idx = 0;
            int currentRank = 0;
            MedalTally prev = null;
            for (var en : entries) {
                idx++;
                String clbName = "CLB #" + en.getKey();
                try {
                    var c = clbService.findOne(en.getKey());
                    if (c != null && c.getTenClb() != null)
                        clbName = c.getTenClb();
                } catch (RuntimeException ignore) {
                }
                MedalTally mt = en.getValue();
                if (prev == null || !sameTally(prev, mt)) {
                    currentRank = idx;
                    prev = mt;
                }
                tallyModel.addRow(new Object[] { clbName, mt.vang, mt.bac, mt.dong, mt.total(), currentRank });
            }

        } catch (java.sql.SQLException ex) { // DB error
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu huy chương: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) { // runtime error
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống khi tải dữ liệu huy chương: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doExportPdf() {
        try {
            javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
            chooser.setDialogTitle("Lưu báo cáo PDF");
            chooser.setSelectedFile(new java.io.File(suggestPdfFileName()));
            int res = chooser.showSaveDialog(this);
            if (res != javax.swing.JFileChooser.APPROVE_OPTION)
                return;
            java.io.File file = chooser.getSelectedFile();
            String path = file.getAbsolutePath().toLowerCase().endsWith(".pdf") ? file.getAbsolutePath()
                    : file.getAbsolutePath() + ".pdf";

            // Build PDF using OpenPDF (increase top margin for a taller header area)
            com.lowagie.text.Document doc = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4, 36, 36,
                    84, 36);
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(path)) {
                com.lowagie.text.pdf.PdfWriter writer = com.lowagie.text.pdf.PdfWriter.getInstance(doc, fos);
                // Header/footer event: show tournament name on all pages
                String tournament = new Prefs().get("selectedGiaiDauName", null);
                if (tournament == null || tournament.isBlank()) {
                    String giaiLbl = lblGiai.getText();
                    if (giaiLbl != null)
                        tournament = giaiLbl.replaceFirst("^Giải: ", "").trim();
                    if (tournament == null || tournament.isBlank())
                        tournament = "Giải đấu";
                }
                // Ensure a Unicode base font is initialized before page event uses it (for
                // Vietnamese diacritics)
                pdfFont(12f, com.lowagie.text.Font.NORMAL);
                writer.setPageEvent(new ReportPageEvent(tryLoadReportLogo(), ensureBaseFont(), tournament));
                doc.open();

                // Page 1: only tally table (no body title)
                com.lowagie.text.Font titleFont1 = pdfFont(18f, com.lowagie.text.Font.BOLD);
                com.lowagie.text.Paragraph pTitle1 = new com.lowagie.text.Paragraph("TỔNG SẮP HUY CHƯƠNG TOÀN ĐOÀN",
                        titleFont1);
                pTitle1.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                pTitle1.setSpacingAfter(12f);
                doc.add(pTitle1);
                writePdfTableFromSwing(doc, tallyTable, null);

                // Page 2: title + medalist table
                doc.newPage();
                com.lowagie.text.Font titleFont2 = pdfFont(18f, com.lowagie.text.Font.BOLD);
                com.lowagie.text.Paragraph pTitle2 = new com.lowagie.text.Paragraph("DANH SÁCH VDV ĐẠT HUY CHƯƠNG",
                        titleFont2);
                pTitle2.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                pTitle2.setSpacingAfter(12f);
                doc.add(pTitle2);
                writePdfTableFromSwing(doc, medalistTable, null);

                doc.close();
            }
            JOptionPane.showMessageDialog(this, "Đã xuất PDF:\n" + path, "Xuất PDF", JOptionPane.INFORMATION_MESSAGE);
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi ghi file PDF: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        } catch (com.lowagie.text.DocumentException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tạo PDF: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống khi xuất PDF: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String suggestPdfFileName() {
        String ten = new Prefs().get("selectedGiaiDauName", "giai-dau");
        String ndLabel = "tat-ca-don-doi";
        Object sel = cboNoiDung.getSelectedItem();
        if (sel instanceof NoiDungItem ni && ni.label != null && !ni.label.isBlank()) {
            ndLabel = ni.id == null ? "tat-ca-don-doi" : normalizeFileName(ni.label);
        }
        return normalizeFileName(ten) + "_tong-sap-huy-chuong_" + ndLabel + ".pdf";
    }

    private void showPreviewDialog() {
        // Bản sao dữ liệu cho preview (tránh ảnh hưởng bảng chính)
        JTable previewTally = new JTable(copyModel((DefaultTableModel) tallyTable.getModel()));
        JTable previewMedal = new JTable(copyModel((DefaultTableModel) medalistTable.getModel()));
        previewTally.setRowHeight(24);
        previewMedal.setRowHeight(36);
        // Grid giống màn hình chính
        previewTally.setShowGrid(true);
        previewTally.setGridColor(new java.awt.Color(200, 200, 200));
        previewTally.setIntercellSpacing(new Dimension(1, 1));
        previewMedal.setShowGrid(true);
        previewMedal.setGridColor(new java.awt.Color(200, 200, 200));
        previewMedal.setIntercellSpacing(new Dimension(1, 1));
        // Center header titles in preview tables
        centerHeader(previewTally);
        centerHeader(previewMedal);

        // Header: chỉ hiển thị tên giải ở tất cả các trang (tab)
        String giaiLbl = lblGiai.getText();
        String tournament = new Prefs().get("selectedGiaiDauName", null);
        if (tournament == null || tournament.isBlank()) {
            if (giaiLbl != null)
                tournament = giaiLbl.replaceFirst("^Giải: ", "").trim();
            if (tournament == null || tournament.isBlank())
                tournament = "Giải đấu";
        }
        JPanel header = new JPanel(new BorderLayout());
        JLabel lbHeader = new JLabel(tournament, JLabel.CENTER);
        lbHeader.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        lbHeader.setFont(lbHeader.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        header.add(lbHeader, BorderLayout.CENTER);

        // Tạo 2 "trang" dạng tab: Trang 1 (tổng sắp), Trang 2 (danh sách VĐV)
        javax.swing.JTabbedPane tabs = new javax.swing.JTabbedPane();

        // Trang 1 - Tổng sắp (giống như 1 tờ giấy A4)
        JPanel page1 = new JPanel(new BorderLayout(8, 8));
        page1.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JLabel lbPage1Title = new JLabel("TỔNG SẮP HUY CHƯƠNG", JLabel.CENTER);
        lbPage1Title.setFont(lbPage1Title.getFont().deriveFont(java.awt.Font.BOLD, 20f));
        // Không hiển thị dòng "Bảng tổng sắp theo CLB" theo yêu cầu
        page1.add(new JScrollPane(previewTally), BorderLayout.CENTER);
        tabs.addTab("Trang 1 - Tổng sắp", page1);

        // Trang 2 - Danh sách VĐV đạt huy chương
        JPanel page2 = new JPanel(new BorderLayout(8, 8));
        page2.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JLabel lbPage2Title = new JLabel("DANH SÁCH VDV ĐẠT HUY CHƯƠNG", JLabel.CENTER);
        lbPage2Title.setFont(lbPage2Title.getFont().deriveFont(java.awt.Font.BOLD, 20f));
        page2.add(lbPage2Title, BorderLayout.NORTH);
        page2.add(new JScrollPane(previewMedal), BorderLayout.CENTER);
        tabs.addTab("Trang 2 - Danh sách VĐV", page2);

        String title = "Xem trước báo cáo - Tổng sắp huy chương (dạng tờ giấy)";
        javax.swing.JDialog dlg = new javax.swing.JDialog(
                SwingUtilities.getWindowAncestor(this), title, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(8, 8));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Xuất PDF…");
        JButton btnClose = new JButton("Đóng");
        south.add(btnClose);
        south.add(btnSave);

        btnSave.addActionListener(ev -> {
            dlg.dispose();
            doExportPdf();
        });
        btnClose.addActionListener(ev -> dlg.dispose());

        dlg.add(header, BorderLayout.NORTH);
        dlg.add(tabs, BorderLayout.CENTER);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.setSize(1100, 750);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void openUnifiedRankDialog() {
        int idGiai = getSelectedGiaiId();
        if (idGiai <= 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải đấu", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        javax.swing.JDialog dlg = new RankEditorDialog(
                SwingUtilities.getWindowAncestor(this),
                idGiai,
                noiDungService,
                ketQuaCaNhanService,
                ketQuaDoiService,
                vdvService,
                dangKiDoiService,
                clbService);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        reloadData();
    }

    private static DefaultTableModel copyModel(DefaultTableModel src) {
        DefaultTableModel dst = new DefaultTableModel();
        // Copy columns
        for (int c = 0; c < src.getColumnCount(); c++) {
            dst.addColumn(src.getColumnName(c));
        }
        // Copy rows
        for (int r = 0; r < src.getRowCount(); r++) {
            Object[] row = new Object[src.getColumnCount()];
            for (int c = 0; c < src.getColumnCount(); c++) {
                row[c] = src.getValueAt(r, c);
            }
            dst.addRow(row);
        }
        return dst;
    }

    private static String normalizeFileName(String s) {
        String x = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-zA-Z0-9-_]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("(^-|-$)", "")
                .toLowerCase();
        if (x.isBlank())
            return "report";
        return x;
    }

    private void writePdfTableFromSwing(com.lowagie.text.Document doc, JTable table, String caption)
            throws com.lowagie.text.DocumentException {
        int cols = table.getColumnCount();
        com.lowagie.text.pdf.PdfPTable pdfTable = new com.lowagie.text.pdf.PdfPTable(cols);
        pdfTable.setWidthPercentage(100f);
        // Caption
        if (caption != null && !caption.isBlank()) {
            com.lowagie.text.Font capFont = pdfFont(12f, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Paragraph cap = new com.lowagie.text.Paragraph(caption, capFont);
            cap.setSpacingBefore(6f);
            cap.setSpacingAfter(4f);
            doc.add(cap);
        }
        // Header
        com.lowagie.text.Font headFont = pdfFont(10f, com.lowagie.text.Font.BOLD);
        for (int c = 0; c < cols; c++) {
            String h = table.getColumnModel().getColumn(c).getHeaderValue().toString();
            com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(
                    new com.lowagie.text.Phrase(h, headFont));
            cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
            cell.setPadding(4f);
            cell.setBorderColor(new java.awt.Color(180, 180, 180));
            cell.setBorderWidth(0.5f);
            cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            pdfTable.addCell(cell);
        }
        // Rows
        com.lowagie.text.Font bodyFont = pdfFont(10f, com.lowagie.text.Font.NORMAL);
        int rows = table.getRowCount();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Object v = table.getValueAt(r, c);
                String s = v == null ? "" : stripHtmlPreserveBreaks(v.toString());
                com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(
                        new com.lowagie.text.Phrase(s, bodyFont));
                cell.setPadding(4f);
                cell.setBorderColor(new java.awt.Color(200, 200, 200));
                cell.setBorderWidth(0.5f);
                // Align numbers to the right for numeric-looking columns
                String header = table.getColumnModel().getColumn(c).getHeaderValue().toString();
                if (looksNumericHeader(header) || looksNumericValue(s)) {
                    cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
                } else {
                    cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_LEFT);
                }
                pdfTable.addCell(cell);
            }
        }
        doc.add(pdfTable);
    }

    private boolean looksNumericHeader(String header) {
        if (header == null)
            return false;
        header = header.trim().toLowerCase();
        return header.equals("vàng") || header.equals("bạc") || header.equals("đồng") || header.equals("tổng")
                || header.equals("hạng") || header.equals("hang") || header.equals("vang") || header.equals("bac")
                || header.equals("dong") || header.equals("thứ hạng") || header.equals("thu hang");
    }

    private boolean looksNumericValue(String s) {
        if (s == null)
            return false;
        return s.matches("^-?\\d+(?:[.,]\\d+)?$");
    }

    // Build a two-line HTML for JTable cell (Swing renders basic HTML when string
    // starts with <html>)
    private static String twoLineHtml(String line1, String line2) {
        String a = line1 == null ? "" : line1.trim();
        String b = line2 == null ? "" : line2.trim();
        if (b.isEmpty()) {
            return a; // single line, no HTML wrapper to keep export simple
        }
        // Escape minimal HTML special chars
        a = a.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        b = b.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html>" + a + "<br/>" + b + "</html>";
    }

    // Convert basic HTML with <br> to plain text with newlines for PDF export
    private static String stripHtmlPreserveBreaks(String html) {
        if (html == null)
            return "";
        String s = html;
        // Only treat as HTML if looks like it
        if (s.contains("<")) {
            s = s.replace("<br>", "\n").replace("<br/>", "\n").replace("<br />", "\n");
            // remove any remaining tags
            s = s.replaceAll("<[^>]+>", "");
            // basic entity decode
            s = s.replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
                    .replace("&quot;", "\"").replace("&apos;", "'");
        }
        return s;
    }

    // Center-align Swing table header titles
    private static void centerHeader(JTable table) {
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            java.awt.Component rendererComp = header.getDefaultRenderer()
                    .getTableCellRendererComponent(table, header.getColumnModel().getColumn(0).getHeaderValue(),
                            false, false, -1, 0);
            if (rendererComp instanceof DefaultTableCellRenderer d) {
                d.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                header.setDefaultRenderer(d);
            } else {
                DefaultTableCellRenderer d = new DefaultTableCellRenderer();
                d.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                header.setDefaultRenderer(d);
            }
        }
    }

    private com.lowagie.text.Font pdfFont(float size, int style) {
        try {
            if (pdfBaseFont == null) {
                // Attempt to load a Unicode-capable Windows font (Arial)
                String[] candidates = new String[] {
                        "C:/Windows/Fonts/arial.ttf",
                        "C:/Windows/Fonts/tahoma.ttf",
                        "C:/Windows/Fonts/segoeui.ttf"
                };
                java.io.File found = null;
                for (String p : candidates) {
                    java.io.File f = new java.io.File(p);
                    if (f.exists()) {
                        found = f;
                        break;
                    }
                }
                if (found != null) {
                    pdfBaseFont = com.lowagie.text.pdf.BaseFont.createFont(found.getAbsolutePath(),
                            com.lowagie.text.pdf.BaseFont.IDENTITY_H, com.lowagie.text.pdf.BaseFont.EMBEDDED);
                } else {
                    // Fallback to built-in Helvetica (may not render all diacritics perfectly)
                    return new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, size, style);
                }
            }
            return new com.lowagie.text.Font(pdfBaseFont, size, style);
        } catch (java.io.IOException | RuntimeException ignore) {
            return new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, size, style);
        }
    }

    // Ensure base font for page event (fall back to default)
    private com.lowagie.text.pdf.BaseFont ensureBaseFont() {
        try {
            if (pdfBaseFont != null)
                return pdfBaseFont;
            return com.lowagie.text.pdf.BaseFont.createFont();
        } catch (com.lowagie.text.DocumentException e) {
            System.err.println("ensureBaseFont DocumentException: " + e.getMessage());
            return null;
        } catch (java.io.IOException e) {
            System.err.println("ensureBaseFont IOException: " + e.getMessage());
            return null;
        }
    }

    // Load logo from settings, scale to header height
    private com.lowagie.text.Image tryLoadReportLogo() {
        try {
            String logoPath = new Prefs().get("report.logo.path", "");
            if (logoPath == null || logoPath.isBlank())
                return null;
            java.io.File f = new java.io.File(logoPath);
            if (!f.exists())
                return null;
            com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(logoPath);
            float maxH = 40f; // allow taller header logo
            if (img.getScaledHeight() > maxH) {
                float k = maxH / img.getScaledHeight();
                img.scalePercent(k * 100f);
            }
            return img;
        } catch (com.lowagie.text.BadElementException e) {
            System.err.println("Logo load BadElementException: " + e.getMessage());
            return null;
        } catch (java.io.IOException e) {
            System.err.println("Logo load IOException: " + e.getMessage());
            return null;
        }
    }

    // Page event for header/footer
    private static final class ReportPageEvent extends com.lowagie.text.pdf.PdfPageEventHelper {
        private final com.lowagie.text.Image logo;
        private final com.lowagie.text.pdf.BaseFont baseFont;
        private final String tournamentName;
        private final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        private final java.util.Date printDate = new java.util.Date();

        @SuppressWarnings("unused")
        ReportPageEvent(com.lowagie.text.Image logo, com.lowagie.text.pdf.BaseFont baseFont, String tournamentName) {
            this.logo = logo;
            this.baseFont = baseFont;
            this.tournamentName = tournamentName;
        }

        @Override
        public void onEndPage(com.lowagie.text.pdf.PdfWriter writer, com.lowagie.text.Document document) {
            com.lowagie.text.pdf.PdfContentByte cb = writer.getDirectContent();
            float left = document.left();
            float right = document.right();
            float top = document.top();
            float bottom = document.bottom();

            // Header logo at top-left, drawn ABOVE the content area to avoid overlap
            if (logo != null) {
                try {
                    float x = left;
                    float y = top + 6f; // place image fully in header area
                    logo.setAbsolutePosition(x, y);
                    cb.addImage(logo);
                } catch (com.lowagie.text.DocumentException e) {
                    System.err.println("addImage DocumentException: " + e.getMessage());
                }
            }

            // Header: tournament name centered on top
            if (tournamentName != null && !tournamentName.isBlank()) {
                cb.beginText();
                try {
                    com.lowagie.text.pdf.BaseFont bf = (baseFont != null)
                            ? baseFont
                            : com.lowagie.text.pdf.BaseFont.createFont();
                    cb.setFontAndSize(bf, 14f);
                } catch (com.lowagie.text.DocumentException e) {
                    System.err.println("header BaseFont DocumentException: " + e.getMessage());
                } catch (java.io.IOException e) {
                    System.err.println("header BaseFont IOException: " + e.getMessage());
                }
                // Place centered within the enlarged header area
                cb.showTextAligned(com.lowagie.text.Element.ALIGN_CENTER, tournamentName, (left + right) / 2f,
                        top + 28f, 0);
                cb.endText();
            }

            // Footer: date/time (left) and page x/y (right)
            cb.beginText();
            try {
                com.lowagie.text.pdf.BaseFont bf = (baseFont != null)
                        ? baseFont
                        : com.lowagie.text.pdf.BaseFont.createFont();
                cb.setFontAndSize(bf, 9f);
            } catch (com.lowagie.text.DocumentException e) {
                System.err.println("footer BaseFont DocumentException: " + e.getMessage());
            } catch (java.io.IOException e) {
                System.err.println("footer BaseFont IOException: " + e.getMessage());
            }
            String leftTxt = "In lúc: " + sdf.format(printDate);
            String rightTxt = String.format("Trang %d", writer.getPageNumber());
            cb.showTextAligned(com.lowagie.text.Element.ALIGN_LEFT, leftTxt, left, bottom - 12f, 0);
            cb.showTextAligned(com.lowagie.text.Element.ALIGN_RIGHT, rightTxt, right, bottom - 12f, 0);
            cb.endText();
        }
    }

    private String getVdvNameSafe(Integer idVdv) {
        if (idVdv == null)
            return "";
        try {
            VanDongVien v = vdvService.findOne(idVdv);
            return v != null && v.getHoTen() != null ? v.getHoTen() : "";
        } catch (RuntimeException e) {
            return "";
        }
    }

    private static final class NoiDungItem {
        final Integer id; // null -> all doubles
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

    private static final class MedalTally {
        int vang, bac, dong;

        int total() {
            return vang + bac + dong;
        }
    }

    // Check if two tallies are identical (for ranking ties)
    private static boolean sameTally(MedalTally a, MedalTally b) {
        if (a == null || b == null)
            return false;
        return a.vang == b.vang && a.bac == b.bac && a.dong == b.dong && a.total() == b.total();
    }
}
