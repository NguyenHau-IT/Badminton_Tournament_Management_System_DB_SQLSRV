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

import com.example.btms.config.Prefs;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.player.VanDongVien;
import com.example.btms.model.result.KetQuaDoi;
import com.example.btms.model.team.ChiTietDoi;
import com.example.btms.model.team.DangKiDoi;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.player.VanDongVienRepository;
import com.example.btms.repository.result.KetQuaDoiRepository;
import com.example.btms.repository.team.ChiTietDoiRepository;
import com.example.btms.repository.team.DangKiDoiRepository;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.result.KetQuaDoiService;
import com.example.btms.service.team.ChiTietDoiService;
import com.example.btms.service.team.DangKiDoiService;

/**
 * Trang xem tổng sắp huy chương và danh sách VĐV đạt huy chương (theo giải hiện
 * tại).
 * - Trái: Bảng tổng sắp theo CLB (Vàng/Bạc/Đồng/Tổng)
 * - Phải: Danh sách huy chương theo nội dung: Nội dung, Hạng, Đội, CLB, VĐV 1,
 * VĐV 2
 *
 * Nguồn dữ liệu: KET_QUA_DOI (theo nội dung), ánh xạ sang đội và thành viên từ
 * DANG_KI_DOI + CHI_TIET_DOI + VAN_DONG_VIEN
 */
public class TongSapHuyChuongPanel extends JPanel {

    private final Prefs prefs = new Prefs();
    private final NoiDungService noiDungService;
    private final KetQuaDoiService ketQuaDoiService;
    private final DangKiDoiService dangKiDoiService;
    private final ChiTietDoiService chiTietDoiService;
    private final VanDongVienService vdvService;
    private final CauLacBoService clbService;

    private final JLabel lblGiai = new JLabel();
    private final JComboBox<Object> cboNoiDung = new JComboBox<>();
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JButton btnExportPdf = new JButton("Xuất PDF…");

    private final DefaultTableModel tallyModel = new DefaultTableModel(
            new Object[] { "CLB", "Vàng", "Bạc", "Đồng", "Tổng" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tallyTable = new JTable(tallyModel);

    private final DefaultTableModel medalistModel = new DefaultTableModel(
            new Object[] { "Nội dung", "Hạng", "Đội", "CLB", "VĐV 1", "VĐV 2" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable medalistTable = new JTable(medalistModel);

    // Cached base font for PDF (Unicode)
    private transient com.lowagie.text.pdf.BaseFont pdfBaseFont;

    public TongSapHuyChuongPanel(Connection conn,
            CauLacBoService clbService) {
        Objects.requireNonNull(conn, "Connection null");
        this.noiDungService = new NoiDungService(new NoiDungRepository(conn));
        this.ketQuaDoiService = new KetQuaDoiService(new KetQuaDoiRepository(conn));
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
        p.add(btnExportPdf);
        btnRefresh.addActionListener(e -> reloadData());
        cboNoiDung.addActionListener(e -> reloadData());
        // Trước khi xuất, mở hộp thoại xem trước
        btnExportPdf.addActionListener(e -> showPreviewDialog());
        return p;
    }

    private JSplitPane buildCenter() {
        tallyTable.setRowHeight(24);
        medalistTable.setRowHeight(24);
        JScrollPane left = new JScrollPane(tallyTable);
        left.setBorder(BorderFactory.createTitledBorder("Bảng tổng sắp theo CLB"));
        JScrollPane right = new JScrollPane(medalistTable);
        right.setBorder(BorderFactory.createTitledBorder("Danh sách VĐV đạt huy chương"));
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
        // Hỗ trợ cả 2 key lịch sử
        int id = prefs.getInt("selectedGiaiDauId", -1);
        if (id <= 0)
            id = prefs.getInt("selected_giaidau_id", -1);
        return id;
    }

    private void loadNoiDungOptions() {
        try {
            List<NoiDung> all = noiDungService.getAllNoiDung();
            List<NoiDung> doublesOnly = new ArrayList<>();
            for (NoiDung nd : all) {
                if (Boolean.TRUE.equals(nd.getTeam()))
                    doublesOnly.add(nd);
            }
            cboNoiDung.removeAllItems();
            cboNoiDung.addItem(new NoiDungItem(null, "Tất cả nội dung (đôi)"));
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
            // Determine category IDs to include
            List<Integer> ndIds = new ArrayList<>();
            Map<Integer, String> ndNames = new HashMap<>();
            NoiDungItem sel = getSelectedNoiDungItem();
            if (sel.id == null) {
                for (NoiDung nd : noiDungService.getAllNoiDung()) {
                    if (Boolean.TRUE.equals(nd.getTeam())) {
                        ndIds.add(nd.getId());
                        ndNames.put(nd.getId(), nd.getTenNoiDung());
                    }
                }
            } else {
                ndIds.add(sel.id);
                ndNames.put(sel.id, sel.label);
            }

            // Aggregate tally and collect medalists
            Map<Integer, MedalTally> tally = new LinkedHashMap<>(); // key: idClb
            medalistModel.setRowCount(0);

            for (Integer idNd : ndIds) {
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
                    Integer thuHangObj = r.getThuHang();
                    int rank = (thuHangObj == null) ? 0 : thuHangObj;
                    switch (rank) {
                        case 1 -> mt.vang++;
                        case 2 -> mt.bac++;
                        case 3 -> mt.dong++;
                        default -> {
                        }
                    }
                    // Medalist line
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
                            key = key.substring(0, sep).trim(); // strip club suffix if present
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
                                /* NOSONAR: best-effort enrich medalist names */ }
                        }
                    }
                    medalistModel.addRow(new Object[] { ndNames.get(idNd), medalTxt, team, clbName, v1, v2 });
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
            for (var en : entries) {
                String clbName = "CLB #" + en.getKey();
                try {
                    var c = clbService.findOne(en.getKey());
                    if (c != null && c.getTenClb() != null)
                        clbName = c.getTenClb();
                } catch (RuntimeException ignore) {
                }
                MedalTally mt = en.getValue();
                tallyModel.addRow(new Object[] { clbName, mt.vang, mt.bac, mt.dong, mt.total() });
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

            // Build PDF using OpenPDF
            com.lowagie.text.Document doc = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4, 36, 36,
                    36, 36);
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(path)) {
                com.lowagie.text.pdf.PdfWriter writer = com.lowagie.text.pdf.PdfWriter.getInstance(doc, fos);
                // Attach header/footer event (logo + date/time + page x/y)
                writer.setPageEvent(new ReportPageEvent(tryLoadReportLogo(), ensureBaseFont()));
                doc.open();

                // Title
                String title = "Tổng sắp huy chương";
                String giai = lblGiai.getText();
                if (giai != null && !giai.isBlank())
                    title = title + " - " + giai.replaceFirst("^Giải: ", "");
                com.lowagie.text.Font titleFont = pdfFont(18f, com.lowagie.text.Font.BOLD);
                com.lowagie.text.Paragraph pTitle = new com.lowagie.text.Paragraph(title, titleFont);
                pTitle.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                pTitle.setSpacingAfter(10f);
                doc.add(pTitle);

                // Selected content label
                Object sel = cboNoiDung.getSelectedItem();
                if (sel instanceof NoiDungItem ni) {
                    String subt = (ni.id == null) ? "Tất cả nội dung (đôi)" : ("Nội dung: " + ni.label);
                    com.lowagie.text.Font subFont = pdfFont(11f, com.lowagie.text.Font.NORMAL);
                    com.lowagie.text.Paragraph pSub = new com.lowagie.text.Paragraph(subt, subFont);
                    pSub.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                    pSub.setSpacingAfter(12f);
                    doc.add(pSub);
                }

                // Tally table (CLB, Vàng, Bạc, Đồng, Tổng)
                writePdfTableFromSwing(doc, tallyTable, "Bảng tổng sắp theo CLB");

                doc.add(new com.lowagie.text.Paragraph(" ")); // spacer

                // Medalist table
                writePdfTableFromSwing(doc, medalistTable, "Danh sách VĐV đạt huy chương");

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
        String ndLabel = "tat-ca-doi";
        Object sel = cboNoiDung.getSelectedItem();
        if (sel instanceof NoiDungItem ni && ni.label != null && !ni.label.isBlank()) {
            ndLabel = ni.id == null ? "tat-ca-doi" : normalizeFileName(ni.label);
        }
        return normalizeFileName(ten) + "_tong-sap-huy-chuong_" + ndLabel + ".pdf";
    }

    private void showPreviewDialog() {
        // Tạo bản sao dữ liệu để xem trước (tránh ảnh hưởng bảng chính)
        JTable previewTally = new JTable(copyModel((DefaultTableModel) tallyTable.getModel()));
        JTable previewMedal = new JTable(copyModel((DefaultTableModel) medalistTable.getModel()));
        previewTally.setRowHeight(24);
        previewMedal.setRowHeight(24);

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(previewTally), new JScrollPane(previewMedal));
        sp.setResizeWeight(0.45);
        sp.setDividerLocation(0.45);

        String title = "Xem trước báo cáo - Tổng sắp huy chương";
        javax.swing.JDialog dlg = new javax.swing.JDialog(
                SwingUtilities.getWindowAncestor(this), title, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(8, 8));

        String giai = lblGiai.getText();
        Object sel = cboNoiDung.getSelectedItem();
        String sub = null;
        if (sel instanceof NoiDungItem ni) {
            sub = (ni.id == null) ? "Tất cả nội dung (đôi)" : ("Nội dung: " + ni.label);
        }
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        header.add(new JLabel(giai));
        if (sub != null)
            header.add(new JLabel(" | " + sub));

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
        dlg.add(sp, BorderLayout.CENTER);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.setSize(1100, 650);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
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
            pdfTable.addCell(cell);
        }
        // Rows
        com.lowagie.text.Font bodyFont = pdfFont(10f, com.lowagie.text.Font.NORMAL);
        int rows = table.getRowCount();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Object v = table.getValueAt(r, c);
                String s = v == null ? "" : v.toString();
                com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(
                        new com.lowagie.text.Phrase(s, bodyFont));
                cell.setPadding(4f);
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
                || header.equals("dong");
    }

    private boolean looksNumericValue(String s) {
        if (s == null)
            return false;
        return s.matches("^-?\\d+(?:[.,]\\d+)?$");
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
            float maxH = 28f; // header height
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
        private final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        private final java.util.Date printDate = new java.util.Date();

        ReportPageEvent(com.lowagie.text.Image logo, com.lowagie.text.pdf.BaseFont baseFont) {
            this.logo = logo;
            this.baseFont = baseFont;
        }

        @Override
        public void onEndPage(com.lowagie.text.pdf.PdfWriter writer, com.lowagie.text.Document document) {
            com.lowagie.text.pdf.PdfContentByte cb = writer.getDirectContent();
            float left = document.left();
            float right = document.right();
            float top = document.top();
            float bottom = document.bottom();

            // Header logo at top-left
            if (logo != null) {
                try {
                    float x = left;
                    float y = top + 6f; // a bit above the top margin
                    logo.setAbsolutePosition(x, y - logo.getScaledHeight());
                    cb.addImage(logo);
                } catch (com.lowagie.text.DocumentException e) {
                    System.err.println("addImage DocumentException: " + e.getMessage());
                }
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
}
