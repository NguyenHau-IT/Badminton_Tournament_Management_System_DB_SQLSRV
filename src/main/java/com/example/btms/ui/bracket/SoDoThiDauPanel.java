package com.example.btms.ui.bracket;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.table.DefaultTableModel;

import com.example.btms.config.Prefs;
import com.example.btms.model.bracket.SoDoCaNhan;
import com.example.btms.model.bracket.SoDoDoi;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.draw.BocThamDoi;
import com.example.btms.model.result.KetQuaDoi;
import com.example.btms.repository.bracket.SoDoCaNhanRepository;
import com.example.btms.repository.bracket.SoDoDoiRepository;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.club.CauLacBoRepository;
import com.example.btms.repository.draw.BocThamCaNhanRepository;
import com.example.btms.repository.draw.BocThamDoiRepository;
import com.example.btms.repository.player.VanDongVienRepository;
import com.example.btms.repository.result.KetQuaCaNhanRepository;
import com.example.btms.repository.result.KetQuaDoiRepository;
import com.example.btms.service.bracket.SoDoCaNhanService;
import com.example.btms.service.bracket.SoDoDoiService;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.draw.BocThamCaNhanService;
import com.example.btms.service.draw.BocThamDoiService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.result.KetQuaCaNhanService;
import com.example.btms.service.result.KetQuaDoiService;
import com.example.btms.service.team.DoiService;

/**
 * Trang "Sơ đồ thi đấu" hiển thị bracket loại trực tiếp 16 -> 1 (5 cột)
 * theo các công thức được yêu cầu:
 * TOA_DO_X = 35 + (COL - 1) * 160
 * TOA_DO_Y = 62 + THU_TU * (40 * 2^(COL-1))
 * VI_TRI = THU_TU + 1 (số thứ tự hiển thị)
 *
 * Cột 1: 16 chỗ (vòng 1)
 * Cột 2: 8 chỗ (tứ kết)
 * Cột 3: 4 chỗ (bán kết)
 * Cột 4: 2 chỗ (chung kết)
 * Cột 5: 1 chỗ (vô địch)
 *
 * Nguồn tên đội lấy từ bảng BOC_THAM_DOI (thứ tự bắt đầu 0) nếu có; nếu thiếu
 * thì hiển thị "Slot n".
 */
public class SoDoThiDauPanel extends JPanel {

    private final BracketCanvas canvas = new BracketCanvas();
    private final JButton btnSave = new JButton("Lưu");
    private final JButton btnSeedFromDraw = new JButton("Gán theo bốc thăm");
    private final JButton btnReloadSaved = new JButton("Tải sơ đồ đã lưu");
    private final JButton btnDeleteAll = new JButton("Xóa sơ đồ + kết quả + bốc thăm");
    private final JToggleButton btnEdit = new JToggleButton("Chế độ sửa");
    private final JButton btnSaveResults = new JButton("Lưu kết quả");
    private final JButton btnExportBracketPdf = new JButton("Xuất sơ đồ PDF");
    // Medals table (EAST)
    private final JTable medalTable = new JTable();
    private final DefaultTableModel medalModel = new DefaultTableModel(new Object[] { "Kết quả" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    // Track last saved medals to avoid redundant writes during auto-save
    private String lastSavedMedalKey = null;
    // Cached base font for PDF (Unicode)
    private transient com.lowagie.text.pdf.BaseFont pdfBaseFont;

    // Nội dung được chọn (không dùng combobox nữa)
    private final java.util.List<NoiDung> noiDungList = new java.util.ArrayList<>();
    private NoiDung selectedNoiDung = null;
    private final JLabel lblGiai = new JLabel();
    // Remember a pending selection when combo items haven't loaded yet
    private Integer pendingSelectNoiDungId = null;
    private final JLabel lblNoiDungValue = new JLabel(); // hiển thị tên nội dung khi dùng chế độ label

    // Services
    private final Prefs prefs = new Prefs();
    private final NoiDungService noiDungService;
    private final BocThamDoiService bocThamService;
    private final BocThamCaNhanService bocThamCaNhanService;
    private final SoDoCaNhanService soDoCaNhanService;
    private final SoDoDoiService soDoDoiService;
    private final DoiService doiService;
    private final CauLacBoService clbService;
    private final KetQuaDoiService ketQuaDoiService;
    private final KetQuaCaNhanService ketQuaCaNhanService;
    private final VanDongVienService vdvService;

    public SoDoThiDauPanel(Connection conn) { // giữ signature cũ để MainFrame không phải đổi nhiều
        Objects.requireNonNull(conn, "Connection null");
        this.noiDungService = new NoiDungService(new NoiDungRepository(conn));
        this.bocThamService = new BocThamDoiService((Connection) conn, new BocThamDoiRepository((Connection) conn));
        this.soDoDoiService = new SoDoDoiService(new SoDoDoiRepository((Connection) conn));
        this.doiService = new DoiService(conn);
        this.clbService = new CauLacBoService(new CauLacBoRepository((Connection) conn));
        this.ketQuaDoiService = new KetQuaDoiService(new KetQuaDoiRepository((Connection) conn));
        this.bocThamCaNhanService = new BocThamCaNhanService(new BocThamCaNhanRepository((Connection) conn));
        this.soDoCaNhanService = new SoDoCaNhanService(new SoDoCaNhanRepository((Connection) conn));
        this.ketQuaCaNhanService = new KetQuaCaNhanService(new KetQuaCaNhanRepository((Connection) conn));
        this.vdvService = new VanDongVienService(new VanDongVienRepository((Connection) conn));

        setLayout(new BorderLayout(8, 8));
        add(buildTop(), BorderLayout.NORTH);
        add(new JScrollPane(canvas), BorderLayout.CENTER);
        add(buildRightPanel(), BorderLayout.EAST);
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        // Load ngay (đồng bộ) để tránh race khi caller gọi selectNoiDungById rồi
        // auto-save
        loadNoiDungOptions();
        updateGiaiLabel();
        loadBestAvailable();
    }

    /**
     * Programmatically select a specific Nội dung by its id in the combo box.
     * If found, it will trigger the existing action listener to reload the bracket.
     */
    public void selectNoiDungById(Integer id) {
        if (id == null)
            return;
        for (NoiDung it : noiDungList) {
            if (it != null && it.getId() != null && it.getId().equals(id)) {
                selectedNoiDung = it;
                updateNoiDungLabelText();
                loadBestAvailable();
                return;
            }
        }
        // Not found now; remember for when options are (re)loaded
        pendingSelectNoiDungId = id;
    }

    /**
     * Bật/tắt chế độ chỉ-hiển-thị bằng label cho trường Nội dung.
     * Khi bật, ẩn combobox và hiện label với tên nội dung đang chọn.
     */
    public void setNoiDungLabelMode(boolean on) {
        lblNoiDungValue.setVisible(true);
        updateNoiDungLabelText();
        revalidate();
        repaint();
    }

    /**
     * Public method for parent containers to force data reload (e.g., when tab is
     * selected).
     */
    public void reloadData() {
        try {
            updateGiaiLabel();
            loadBestAvailable();
        } catch (Exception ignore) {
        }
    }

    /**
     * Tự động nạp sơ đồ từ dữ liệu bốc thăm và lưu ngay vào CSDL.
     * Dùng cho workflow tự động (chuột phải ở cây điều hướng).
     */
    public void autoSeedFromDrawAndSave() {
        try {
            // Nếu nội dung chưa sẵn sàng (vừa được set pending), cố gắng nạp lại ngay
            if (selectedNoiDung == null && pendingSelectNoiDungId != null) {
                loadNoiDungOptions();
            }
            loadFromBocTham();
            saveBracket();
        } catch (Throwable ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Không thể tự động tạo & lưu sơ đồ: " + ex.getMessage(),
                    "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateNoiDungLabelText() {
        NoiDung nd = selectedNoiDung;
        String name = (nd != null && nd.getTenNoiDung() != null) ? nd.getTenNoiDung().trim() : "";
        lblNoiDungValue.setText(name);
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        line.add(lblGiai);
        line.add(new JLabel(" | Nội dung:"));
        // Luôn dùng label để hiển thị tên nội dung; không hiển thị combobox
        lblNoiDungValue.setVisible(true);
        line.add(lblNoiDungValue);
        line.add(btnReloadSaved);
        line.add(btnSeedFromDraw);
        line.add(btnDeleteAll);
        line.add(btnEdit);
        line.add(btnSaveResults);
        line.add(btnExportBracketPdf);
        line.add(btnSave);
        p.add(line, BorderLayout.CENTER);

        btnSave.addActionListener(e -> saveBracket());
        btnSaveResults.addActionListener(e -> saveMedalResults());
        btnReloadSaved.addActionListener(e -> loadSavedSoDo());
        btnSeedFromDraw.addActionListener(e -> loadFromBocTham());
        btnDeleteAll.addActionListener(e -> deleteBracketAndResults());
        btnEdit.addActionListener(e -> canvas.setEditMode(btnEdit.isSelected()));
        btnExportBracketPdf.addActionListener(e -> exportBracketPdf());
        return p;
    }

    private JPanel buildRightPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Kết quả"));
        medalTable.setModel(medalModel);
        medalTable.setTableHeader(null); // ẩn header để đúng 1 cột đơn giản
        medalTable.setRowHeight(26);
        medalTable.setShowGrid(false);
        JScrollPane sp = new JScrollPane(medalTable);
        sp.setPreferredSize(new Dimension(240, 140));
        p.add(sp, BorderLayout.CENTER);
        // Khởi tạo 4 dòng rỗng
        refreshMedalTable("", "", "", "");
        return p;
    }

    // ===== Export bracket to PDF =====
    private void exportBracketPdf() {
        try {
            // Render canvas to image
            java.awt.Dimension pref = canvas.getPreferredSize();
            if (pref != null)
                canvas.setSize(pref);
            int w = Math.max(1, canvas.getWidth());
            int h = Math.max(1, canvas.getHeight());
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h,
                    java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2 = img.createGraphics();
            g2.setColor(java.awt.Color.WHITE);
            g2.fillRect(0, 0, w, h);
            // Ensure high-quality rendering like paintComponent
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            canvas.printAll(g2);
            g2.dispose();

            // Choose file path
            javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
            chooser.setDialogTitle("Lưu sơ đồ thi đấu (PDF)");
            chooser.setSelectedFile(new java.io.File(suggestBracketPdfFileName()));
            int res = chooser.showSaveDialog(this);
            if (res != javax.swing.JFileChooser.APPROVE_OPTION)
                return;
            java.io.File file = chooser.getSelectedFile();
            String path = file.getAbsolutePath().toLowerCase().endsWith(".pdf") ? file.getAbsolutePath()
                    : file.getAbsolutePath() + ".pdf";

            // Create PDF (A4 landscape) and embed image scaled to fit
            // - smaller side margins for bigger bracket
            // - keep top margin for header logos/title
            com.lowagie.text.Document doc = new com.lowagie.text.Document(
                    com.lowagie.text.PageSize.A4.rotate(), 12, 18, 84, 28);
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(path)) {
                com.lowagie.text.pdf.PdfWriter writer = com.lowagie.text.pdf.PdfWriter.getInstance(doc, fos);
                // Header/footer event with tournament name and optional logos
                String tournament = new Prefs().get("selectedGiaiDauName", null);
                if (tournament == null || tournament.isBlank()) {
                    String giaiLbl = lblGiai.getText();
                    if (giaiLbl != null)
                        tournament = giaiLbl.replaceFirst("^Giải: ", "").trim();
                    if (tournament == null || tournament.isBlank())
                        tournament = "Giải đấu";
                }
                // Ensure a Unicode base font is initialized before page event uses it
                pdfFont(12f, com.lowagie.text.Font.NORMAL);
                writer.setPageEvent(new ReportPageEvent(tryLoadReportLogo(), tryLoadSponsorLogo(), ensureBaseFont(),
                        tournament));

                doc.open();
                // Title
                String ndName = lblNoiDungValue.getText();
                String titleStr = (ndName != null && !ndName.isBlank()) ? ("SƠ ĐỒ THI ĐẤU - " + ndName)
                        : "SƠ ĐỒ THI ĐẤU";
                com.lowagie.text.Font titleFont = pdfFont(18f, com.lowagie.text.Font.BOLD);
                com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph(titleStr, titleFont);
                title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(6f);
                doc.add(title);

                // Auto-trim white borders so the bracket can scale larger and sit closer to the
                // left
                java.awt.image.BufferedImage trimmed = trimWhiteBorders(img, 8);
                com.lowagie.text.Image pdfImg = com.lowagie.text.Image.getInstance(trimmed, null);
                float maxW = doc.getPageSize().getWidth() - doc.leftMargin() - doc.rightMargin();
                // Reserve a conservative block for the title so image stays on the same page
                float titleReserve = 32f; // pts
                float maxH = doc.getPageSize().getHeight() - doc.topMargin() - doc.bottomMargin() - titleReserve;
                // Compute scale to fit both width and height, then reduce slightly to ensure it
                // stays on one page
                float scaleW = maxW / trimmed.getWidth();
                float scaleH = maxH / trimmed.getHeight();
                float scale = Math.min(scaleW, scaleH) * 0.96f; // shrink by 4%
                if (scale <= 0f)
                    scale = 1f;
                pdfImg.scalePercent(scale * 100f);
                // Align to the left margin so it looks "sát trái"
                pdfImg.setAlignment(com.lowagie.text.Element.ALIGN_LEFT);
                pdfImg.setSpacingBefore(0f);
                pdfImg.setSpacingAfter(0f);
                doc.add(pdfImg);
                doc.close();
            }
            javax.swing.JOptionPane.showMessageDialog(this, "Đã xuất PDF:\n" + path,
                    "Xuất sơ đồ thi đấu", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi xuất sơ đồ PDF: " + ex.getMessage(),
                    "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Export the currently selected nội dung's bracket to a specific file path (no
     * dialogs).
     */
    public boolean exportBracketPdfToFile(java.io.File file) {
        if (file == null)
            return false;
        try {
            // Render canvas to image
            java.awt.Dimension pref = canvas.getPreferredSize();
            if (pref != null)
                canvas.setSize(pref);
            int w = Math.max(1, canvas.getWidth());
            int h = Math.max(1, canvas.getHeight());
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h,
                    java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2 = img.createGraphics();
            g2.setColor(java.awt.Color.WHITE);
            g2.fillRect(0, 0, w, h);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            canvas.printAll(g2);
            g2.dispose();

            String path = file.getAbsolutePath().toLowerCase().endsWith(".pdf") ? file.getAbsolutePath()
                    : file.getAbsolutePath() + ".pdf";

            com.lowagie.text.Document doc = new com.lowagie.text.Document(
                    com.lowagie.text.PageSize.A4.rotate(), 12, 18, 84, 28);
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(path)) {
                com.lowagie.text.pdf.PdfWriter writer = com.lowagie.text.pdf.PdfWriter.getInstance(doc, fos);
                String tournament = new Prefs().get("selectedGiaiDauName", null);
                if (tournament == null || tournament.isBlank()) {
                    String giaiLbl = lblGiai.getText();
                    if (giaiLbl != null)
                        tournament = giaiLbl.replaceFirst("^Giải: ", "").trim();
                    if (tournament == null || tournament.isBlank())
                        tournament = "Giải đấu";
                }
                pdfFont(12f, com.lowagie.text.Font.NORMAL);
                writer.setPageEvent(new ReportPageEvent(tryLoadReportLogo(), tryLoadSponsorLogo(), ensureBaseFont(),
                        tournament));
                doc.open();
                String ndName = lblNoiDungValue.getText();
                String titleStr = (ndName != null && !ndName.isBlank()) ? ("SƠ ĐỒ THI ĐẤU - " + ndName)
                        : "SƠ ĐỒ THI ĐẤU";
                com.lowagie.text.Font titleFont = pdfFont(18f, com.lowagie.text.Font.BOLD);
                com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph(titleStr, titleFont);
                title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(6f);
                doc.add(title);

                java.awt.image.BufferedImage trimmed = trimWhiteBorders(img, 8);
                com.lowagie.text.Image pdfImg = com.lowagie.text.Image.getInstance(trimmed, null);
                float maxW = doc.getPageSize().getWidth() - doc.leftMargin() - doc.rightMargin();
                float titleReserve = 32f;
                float maxH = doc.getPageSize().getHeight() - doc.topMargin() - doc.bottomMargin() - titleReserve;
                float scaleW = maxW / trimmed.getWidth();
                float scaleH = maxH / trimmed.getHeight();
                float scale = Math.min(scaleW, scaleH) * 0.96f;
                if (scale <= 0f)
                    scale = 1f;
                pdfImg.scalePercent(scale * 100f);
                pdfImg.setAlignment(com.lowagie.text.Element.ALIGN_LEFT);
                pdfImg.setSpacingBefore(0f);
                pdfImg.setSpacingAfter(0f);
                doc.add(pdfImg);
                doc.close();
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /** Export all nội dung brackets into a single multi-page PDF. */
    public boolean exportAllBracketsToSinglePdf(java.io.File file) {
        if (file == null)
            return false;
        try {
            if (noiDungList == null || noiDungList.isEmpty())
                return false;
            // Build filtered list: only nội dung that have draw or saved bracket
            java.util.List<NoiDung> targets = new java.util.ArrayList<>();
            int idGiai = prefs.getInt("selectedGiaiDauId", -1);
            for (NoiDung nd : noiDungList) {
                if (nd == null || nd.getId() == null)
                    continue;
                boolean include = false;
                try {
                    if (Boolean.TRUE.equals(nd.getTeam())) {
                        var ds = bocThamService.list(idGiai, nd.getId());
                        include = (ds != null && !ds.isEmpty());
                        if (!include) {
                            var sodo = soDoDoiService.list(idGiai, nd.getId());
                            include = (sodo != null && !sodo.isEmpty());
                        }
                    } else {
                        var ds = bocThamCaNhanService.list(idGiai, nd.getId());
                        include = (ds != null && !ds.isEmpty());
                        if (!include) {
                            var sodo = soDoCaNhanService.list(idGiai, nd.getId());
                            include = (sodo != null && !sodo.isEmpty());
                        }
                    }
                } catch (RuntimeException ignore) {
                }
                if (include)
                    targets.add(nd);
            }
            if (targets.isEmpty())
                return false;
            String tournament = new Prefs().get("selectedGiaiDauName", null);
            if (tournament == null || tournament.isBlank()) {
                String giaiLbl = lblGiai.getText();
                if (giaiLbl != null)
                    tournament = giaiLbl.replaceFirst("^Giải: ", "").trim();
                if (tournament == null || tournament.isBlank())
                    tournament = "Giải đấu";
            }
            String path = file.getAbsolutePath().toLowerCase().endsWith(".pdf") ? file.getAbsolutePath()
                    : file.getAbsolutePath() + ".pdf";
            com.lowagie.text.Document doc = new com.lowagie.text.Document(
                    com.lowagie.text.PageSize.A4.rotate(), 12, 18, 84, 28);
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(path)) {
                com.lowagie.text.pdf.PdfWriter writer = com.lowagie.text.pdf.PdfWriter.getInstance(doc, fos);
                pdfFont(12f, com.lowagie.text.Font.NORMAL);
                writer.setPageEvent(new ReportPageEvent(tryLoadReportLogo(), tryLoadSponsorLogo(), ensureBaseFont(),
                        tournament));
                doc.open();
                NoiDung old = selectedNoiDung;
                for (int i = 0; i < targets.size(); i++) {
                    NoiDung nd = targets.get(i);
                    if (nd == null || nd.getId() == null)
                        continue;
                    // Select and load content
                    selectedNoiDung = nd;
                    updateNoiDungLabelText();
                    loadBestAvailable();
                    // Title
                    String ndName = (nd.getTenNoiDung() != null) ? nd.getTenNoiDung().trim() : "";
                    String titleStr = !ndName.isBlank() ? ("SƠ ĐỒ THI ĐẤU - " + ndName) : "SƠ ĐỒ THI ĐẤU";
                    com.lowagie.text.Font titleFont = pdfFont(18f, com.lowagie.text.Font.BOLD);
                    com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph(titleStr, titleFont);
                    title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                    title.setSpacingAfter(6f);
                    doc.add(title);

                    // Render
                    java.awt.Dimension pref = canvas.getPreferredSize();
                    if (pref != null)
                        canvas.setSize(pref);
                    int w = Math.max(1, canvas.getWidth());
                    int h = Math.max(1, canvas.getHeight());
                    java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h,
                            java.awt.image.BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g2 = img.createGraphics();
                    g2.setColor(java.awt.Color.WHITE);
                    g2.fillRect(0, 0, w, h);
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    canvas.printAll(g2);
                    g2.dispose();

                    java.awt.image.BufferedImage trimmed = trimWhiteBorders(img, 8);
                    com.lowagie.text.Image pdfImg = com.lowagie.text.Image.getInstance(trimmed, null);
                    float maxW = doc.getPageSize().getWidth() - doc.leftMargin() - doc.rightMargin();
                    float titleReserve = 32f;
                    float maxH = doc.getPageSize().getHeight() - doc.topMargin() - doc.bottomMargin() - titleReserve;
                    float scaleW = maxW / trimmed.getWidth();
                    float scaleH = maxH / trimmed.getHeight();
                    float scale = Math.min(scaleW, scaleH) * 0.96f;
                    if (scale <= 0f)
                        scale = 1f;
                    pdfImg.scalePercent(scale * 100f);
                    pdfImg.setAlignment(com.lowagie.text.Element.ALIGN_LEFT);
                    pdfImg.setSpacingBefore(0f);
                    pdfImg.setSpacingAfter(0f);
                    doc.add(pdfImg);

                    if (i < targets.size() - 1)
                        doc.newPage();
                }
                // restore previous selection
                selectedNoiDung = old;
                updateNoiDungLabelText();
                doc.close();
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Export mỗi nội dung thành một file PDF trong thư mục chỉ định. Trả về số file
     * đã tạo.
     */
    public int exportEachBracketToDirectory(java.io.File dir) {
        if (dir == null)
            return 0;
        if (!dir.exists())
            dir.mkdirs();
        if (!dir.isDirectory())
            return 0;
        int count = 0;
        String tournament = new Prefs().get("selectedGiaiDauName", null);
        if (tournament == null || tournament.isBlank()) {
            String giaiLbl = lblGiai.getText();
            if (giaiLbl != null)
                tournament = giaiLbl.replaceFirst("^Giải: ", "").trim();
            if (tournament == null || tournament.isBlank())
                tournament = "Giải đấu";
        }
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        NoiDung old = selectedNoiDung;
        for (NoiDung nd : noiDungList) {
            if (nd == null || nd.getId() == null)
                continue;
            // Skip nội dung without draw and without saved bracket
            boolean include = false;
            try {
                if (Boolean.TRUE.equals(nd.getTeam())) {
                    var ds = bocThamService.list(idGiai, nd.getId());
                    include = (ds != null && !ds.isEmpty());
                    if (!include) {
                        var sodo = soDoDoiService.list(idGiai, nd.getId());
                        include = (sodo != null && !sodo.isEmpty());
                    }
                } else {
                    var ds = bocThamCaNhanService.list(idGiai, nd.getId());
                    include = (ds != null && !ds.isEmpty());
                    if (!include) {
                        var sodo = soDoCaNhanService.list(idGiai, nd.getId());
                        include = (sodo != null && !sodo.isEmpty());
                    }
                }
            } catch (RuntimeException ignore) {
            }
            if (!include)
                continue;
            try {
                selectedNoiDung = nd;
                updateNoiDungLabelText();
                loadBestAvailable();
                String fileName = suggestBracketPdfFileName();
                java.io.File f = new java.io.File(dir, fileName);
                // write single PDF for this nội dung
                com.lowagie.text.Document doc = new com.lowagie.text.Document(
                        com.lowagie.text.PageSize.A4.rotate(), 12, 18, 84, 28);
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(f)) {
                    com.lowagie.text.pdf.PdfWriter writer = com.lowagie.text.pdf.PdfWriter.getInstance(doc, fos);
                    pdfFont(12f, com.lowagie.text.Font.NORMAL);
                    writer.setPageEvent(new ReportPageEvent(tryLoadReportLogo(), tryLoadSponsorLogo(), ensureBaseFont(),
                            tournament));
                    doc.open();
                    String ndName = (nd.getTenNoiDung() != null) ? nd.getTenNoiDung().trim() : "";
                    String titleStr = !ndName.isBlank() ? ("SƠ ĐỒ THI ĐẤU - " + ndName) : "SƠ ĐỒ THI ĐẤU";
                    com.lowagie.text.Font titleFont = pdfFont(18f, com.lowagie.text.Font.BOLD);
                    com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph(titleStr, titleFont);
                    title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                    title.setSpacingAfter(6f);
                    doc.add(title);

                    java.awt.Dimension pref = canvas.getPreferredSize();
                    if (pref != null)
                        canvas.setSize(pref);
                    int w = Math.max(1, canvas.getWidth());
                    int h = Math.max(1, canvas.getHeight());
                    java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h,
                            java.awt.image.BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g2 = img.createGraphics();
                    g2.setColor(java.awt.Color.WHITE);
                    g2.fillRect(0, 0, w, h);
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    canvas.printAll(g2);
                    g2.dispose();

                    java.awt.image.BufferedImage trimmed = trimWhiteBorders(img, 8);
                    com.lowagie.text.Image pdfImg = com.lowagie.text.Image.getInstance(trimmed, null);
                    float maxW = doc.getPageSize().getWidth() - doc.leftMargin() - doc.rightMargin();
                    float titleReserve = 32f;
                    float maxH = doc.getPageSize().getHeight() - doc.topMargin() - doc.bottomMargin() - titleReserve;
                    float scaleW = maxW / trimmed.getWidth();
                    float scaleH = maxH / trimmed.getHeight();
                    float scale = Math.min(scaleW, scaleH) * 0.96f;
                    if (scale <= 0f)
                        scale = 1f;
                    pdfImg.scalePercent(scale * 100f);
                    pdfImg.setAlignment(com.lowagie.text.Element.ALIGN_LEFT);
                    pdfImg.setSpacingBefore(0f);
                    pdfImg.setSpacingAfter(0f);
                    doc.add(pdfImg);
                    doc.close();
                    count++;
                }
            } catch (Exception ignore) {
            }
        }
        // restore previous selection
        selectedNoiDung = old;
        updateNoiDungLabelText();
        return count;
    }

    private String suggestBracketPdfFileName() {
        String ten = new Prefs().get("selectedGiaiDauName", "giai-dau");
        String ndLabel = lblNoiDungValue.getText();
        if (ndLabel == null || ndLabel.isBlank())
            ndLabel = "noi-dung";
        String base = normalizeFileName(ten) + "_so-do-thi-dau_" + normalizeFileName(ndLabel) + ".pdf";
        return base;
    }

    // Filename normalizer (local copy): remove diacritics, keep [a-zA-Z0-9-_],
    // collapse dashes
    private static String normalizeFileName(String s) {
        if (s == null)
            return "file";
        String x = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-zA-Z0-9-_]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("(^-|-$)", "")
                .toLowerCase();
        if (x.isBlank())
            return "file";
        return x;
    }

    // ===== PDF helpers for header/footer and Unicode fonts (aligned with tally
    // reports) =====
    // Trim near-white borders from the rendered canvas to maximize useful area in
    // PDF
    private static java.awt.image.BufferedImage trimWhiteBorders(java.awt.image.BufferedImage src, int padding) {
        if (src == null)
            return null;
        int w = src.getWidth();
        int h = src.getHeight();
        int minX = w, minY = h, maxX = -1, maxY = -1;
        final int TH = 245; // threshold to treat as white/near-white
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                boolean nearWhite = (r >= TH && g >= TH && b >= TH);
                if (!nearWhite) {
                    if (x < minX)
                        minX = x;
                    if (y < minY)
                        minY = y;
                    if (x > maxX)
                        maxX = x;
                    if (y > maxY)
                        maxY = y;
                }
            }
        }
        if (maxX < 0 || maxY < 0) {
            // All white; return as is
            return src;
        }
        int pad = Math.max(0, padding);
        int x0 = Math.max(0, minX - pad);
        int y0 = Math.max(0, minY - pad);
        int x1 = Math.min(w - 1, maxX + pad);
        int y1 = Math.min(h - 1, maxY + pad);
        int nw = Math.max(1, x1 - x0 + 1);
        int nh = Math.max(1, y1 - y0 + 1);
        java.awt.image.BufferedImage sub = src.getSubimage(x0, y0, nw, nh);
        // Copy to a fresh RGB image to avoid retaining a reference to the original
        // buffer
        java.awt.image.BufferedImage out = new java.awt.image.BufferedImage(nw, nh,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2 = out.createGraphics();
        g2.drawImage(sub, 0, 0, null);
        g2.dispose();
        return out;
    }

    private com.lowagie.text.Font pdfFont(float size, int style) {
        try {
            if (pdfBaseFont == null) {
                // Attempt to load a Unicode-capable Windows font (Arial/Tahoma/Segoe UI)
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

    // Load left logo (tournament/org) from settings, scale to header height
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

    // Load right logo (sponsor) from settings, scale to header height
    private com.lowagie.text.Image tryLoadSponsorLogo() {
        try {
            String logoPath = new Prefs().get("report.sponsor.logo.path", "");
            if (logoPath == null || logoPath.isBlank())
                return null;
            java.io.File f = new java.io.File(logoPath);
            if (!f.exists())
                return null;
            com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(logoPath);
            float maxH = 40f; // align visual height with left logo
            if (img.getScaledHeight() > maxH) {
                float k = maxH / img.getScaledHeight();
                img.scalePercent(k * 100f);
            }
            return img;
        } catch (com.lowagie.text.BadElementException e) {
            System.err.println("Sponsor logo load BadElementException: " + e.getMessage());
            return null;
        } catch (java.io.IOException e) {
            System.err.println("Sponsor logo load IOException: " + e.getMessage());
            return null;
        }
    }

    // Page event for header/footer (logos + tournament title + footer info)
    private static final class ReportPageEvent extends com.lowagie.text.pdf.PdfPageEventHelper {
        private final com.lowagie.text.Image leftLogo;
        private final com.lowagie.text.Image rightLogo;
        private final com.lowagie.text.pdf.BaseFont baseFont;
        private final String tournamentName;
        private final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        private final java.util.Date printDate = new java.util.Date();

        ReportPageEvent(com.lowagie.text.Image leftLogo, com.lowagie.text.Image rightLogo,
                com.lowagie.text.pdf.BaseFont baseFont, String tournamentName) {
            this.leftLogo = leftLogo;
            this.rightLogo = rightLogo;
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

            // Header left logo at top-left, drawn above the content area to avoid overlap
            if (leftLogo != null) {
                try {
                    float x = left;
                    float y = top + 6f; // place image fully in header area
                    leftLogo.setAbsolutePosition(x, y);
                    cb.addImage(leftLogo);
                } catch (com.lowagie.text.DocumentException e) {
                    System.err.println("addImage DocumentException: " + e.getMessage());
                }
            }

            // Header right logo at top-right
            if (rightLogo != null) {
                try {
                    float x = right - rightLogo.getScaledWidth();
                    float y = top + 6f; // align with left logo
                    rightLogo.setAbsolutePosition(x, y);
                    cb.addImage(rightLogo);
                } catch (com.lowagie.text.DocumentException e) {
                    System.err.println("addImage sponsor DocumentException: " + e.getMessage());
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

    /**
     * Xoá toàn bộ sơ đồ đã lưu của Nội dung đang chọn và đồng thời xoá luôn
     * kết quả huy chương (nếu có) trong CSDL.
     * Không chỉ xoá UI, mà xoá dữ liệu lưu trong bảng SODO_* và KET_QUA_*.
     */
    private void deleteBracketAndResults() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        NoiDung nd = selectedNoiDung;
        if (idGiai <= 0 || nd == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải hoặc nội dung", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xoá toàn bộ sơ đồ, kết quả huy chương và danh sách bốc thăm của nội dung này?\n\n" +
                        "Hành động này sẽ xoá dữ liệu trong CSDL và không thể hoàn tác.",
                "Xác nhận xoá", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean isTeam = Boolean.TRUE.equals(nd.getTeam());
        int idNoiDung = nd.getId();

        try {
            // 1) Xoá toàn bộ bản ghi sơ đồ theo loại nội dung
            if (isTeam) {
                List<SoDoDoi> olds = soDoDoiService.list(idGiai, idNoiDung);
                for (SoDoDoi r : olds) {
                    try {
                        soDoDoiService.delete(idGiai, idNoiDung, r.getViTri());
                    } catch (Exception ignore) {
                    }
                }
            } else {
                List<SoDoCaNhan> olds = soDoCaNhanService.list(idGiai, idNoiDung);
                for (SoDoCaNhan r : olds) {
                    try {
                        soDoCaNhanService.delete(idGiai, idNoiDung, r.getViTri());
                    } catch (Exception ignore) {
                    }
                }
            }

            // 2) Xoá kết quả huy chương (ranks 1,2,3)
            for (int rank : new int[] { 1, 2, 3 }) {
                try {
                    if (isTeam) {
                        ketQuaDoiService.delete(idGiai, idNoiDung, rank);
                    } else {
                        ketQuaCaNhanService.delete(idGiai, idNoiDung, rank);
                    }
                } catch (Exception ignore) {
                }
            }

            // 3) Xoá danh sách bốc thăm (draws)
            try {
                if (isTeam) {
                    var drawList = bocThamService.list(idGiai, idNoiDung);
                    for (var r : drawList) {
                        try {
                            bocThamService.delete(idGiai, idNoiDung, r.getThuTu());
                        } catch (Exception ignore) {
                        }
                    }
                } else {
                    var drawList = bocThamCaNhanService.list(idGiai, idNoiDung);
                    for (var r : drawList) {
                        try {
                            bocThamCaNhanService.delete(idGiai, idNoiDung, r.getIdVdv());
                        } catch (Exception ignore) {
                        }
                    }
                }
            } catch (Exception ignore) {
            }

            // 4) Dọn UI: xoá text ô, reset bảng huy chương và cache
            clearAllSlots();
            refreshMedalTable("", "", "", "");
            lastSavedMedalKey = null;

            JOptionPane.showMessageDialog(this, "Đã xoá sơ đồ, kết quả và bốc thăm (nếu có)", "Hoàn tất",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xoá: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveBracket() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        NoiDung nd = selectedNoiDung;
        if (idGiai <= 0 || nd == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải hoặc nội dung", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idNoiDung = nd.getId();
        try {
            boolean isTeam = Boolean.TRUE.equals(nd.getTeam());
            if (isTeam) {
                // Xóa toàn bộ sơ đồ cũ (đội)
                List<SoDoDoi> olds = soDoDoiService.list(idGiai, idNoiDung);
                for (SoDoDoi r : olds) {
                    try {
                        soDoDoiService.delete(idGiai, idNoiDung, r.getViTri());
                    } catch (Exception ignore) {
                    }
                }
                // Lưu các ô đang hiển thị (đội)
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                for (BracketCanvas.Slot s : canvas.getSlots()) {
                    if (s.text != null && !s.text.isBlank()) {
                        // Hiển thị đang ở dạng "TEN_TEAM - TEN_CLB" => tách lấy TEN_TEAM để tra ID_CLB
                        String teamName = extractTeamNameFromDisplay(s.text.trim());
                        Integer idClb = null;
                        try {
                            int found = doiService.getIdClbByTeamName(teamName, idNoiDung, idGiai);
                            if (found > 0)
                                idClb = found;
                        } catch (RuntimeException ignored) {
                        }
                        Integer soDo = s.col;
                        try {
                            if (idClb != null)
                                soDo = bocThamService.getSoDo(idGiai, idNoiDung, idClb);
                        } catch (RuntimeException ignored) {
                        }
                        soDoDoiService.create(idGiai, idNoiDung, idClb, s.text.trim(), s.x, s.y, s.order, soDo, now);
                    }
                }
            } else {
                // Xóa toàn bộ sơ đồ cũ (cá nhân)
                List<SoDoCaNhan> olds = soDoCaNhanService.list(idGiai, idNoiDung);
                for (SoDoCaNhan r : olds) {
                    try {
                        soDoCaNhanService.delete(idGiai, idNoiDung, r.getViTri());
                    } catch (Exception ignore) {
                    }
                }
                // Map name -> idVdv từ đăng kí cá nhân
                java.util.Map<String, Integer> nameToId = vdvService.loadSinglesNames(idNoiDung, idGiai);
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                for (BracketCanvas.Slot s : canvas.getSlots()) {
                    if (s.text != null && !s.text.isBlank()) {
                        Integer idVdv = resolveVdvIdFromDisplay(nameToId, s.text.trim());
                        if (idVdv == null)
                            continue; // bỏ qua nếu không xác định được
                        Integer soDo = s.col;
                        try {
                            var bt = bocThamCaNhanService.getOne(idGiai, idNoiDung, idVdv);
                            if (bt != null && bt.getSoDo() != null)
                                soDo = bt.getSoDo();
                        } catch (RuntimeException ignore) {
                        }
                        soDoCaNhanService.create(idGiai, idNoiDung, idVdv, s.x, s.y, s.order, soDo, now);
                    }
                }
            }
            JOptionPane.showMessageDialog(this, "Đã lưu sơ đồ vào CSDL", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu sơ đồ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // xuất ra file đã loại bỏ; hiện tại nút Lưu sẽ lưu trực tiếp vào CSDL

    // Removed: resetDefaultTeams and shuffleTeams — no longer used after UI change

    /* ===================== DB wiring ===================== */
    private void loadNoiDungOptions() {
        try {
            Integer idGiai = prefs.getInt("selectedGiaiDauId", -1);
            List<NoiDung> all = noiDungService.getNoiDungByTuornament(idGiai);
            noiDungList.clear();
            if (all != null)
                noiDungList.addAll(all);
            if (!noiDungList.isEmpty())
                selectedNoiDung = noiDungList.get(0);
            if (pendingSelectNoiDungId != null) {
                for (NoiDung it : noiDungList) {
                    if (it != null && it.getId() != null && it.getId().equals(pendingSelectNoiDungId)) {
                        selectedNoiDung = it;
                        break;
                    }
                }
                pendingSelectNoiDungId = null;
            }
            updateNoiDungLabelText();
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nội dung: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateGiaiLabel() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        String ten = prefs.get("selectedGiaiDauName", null);
        if (idGiai > 0) {
            lblGiai.setText("Giải: " + (ten != null && !ten.isBlank() ? ten : ("ID=" + idGiai)));
        } else {
            lblGiai.setText("Giải: (chưa chọn)");
        }
    }

    private void loadFromBocTham() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        NoiDung nd = selectedNoiDung;
        if (idGiai <= 0 || nd == null) {
            return;
        }
        boolean isTeam = Boolean.TRUE.equals(nd.getTeam());
        if (isTeam) {
            List<BocThamDoi> list;
            try {
                list = bocThamService.list(idGiai, nd.getId());
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải sơ đồ từ DB: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Decide seeding column and block size based on number of participants
            int N = list.size();
            int M; // block size in that column
            int seedCol; // 1..5
            if (N >= 9) {
                M = 16;
                seedCol = 1;
            } else if (N >= 5) {
                M = 8;
                seedCol = 2;
            } else if (N >= 3) {
                M = 4;
                seedCol = 3;
            } else if (N >= 2) {
                M = 2;
                seedCol = 4;
            } else {
                M = 1;
                seedCol = 4;
            }
            if (N > 16) {
                JOptionPane.showMessageDialog(this, "Số đội vượt quá 16 – chưa hỗ trợ trong sơ đồ 16.", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                N = 16;
                M = 16;
                seedCol = 1;
            }
            List<Integer> pos = computeTopHeavyPositionsWithinBlock(Math.min(N, M), M);
            List<String> namesByT = new ArrayList<>();
            for (int t = 0; t < M; t++)
                namesByT.add(null);
            for (int i = 0; i < Math.min(N, M); i++) {
                int t = pos.get(i);
                BocThamDoi row = list.get(i);
                String team = row.getTenTeam() != null ? row.getTenTeam().trim() : "";
                String club = "";
                try {
                    Integer idClb = row.getIdClb();
                    if (idClb == null && team != null && !team.isBlank()) {
                        idClb = doiService.getIdClbByTeamName(team, nd.getId(), idGiai);
                    }
                    if (idClb != null) {
                        var c = clbService.findOne(idClb);
                        if (c != null && c.getTenClb() != null)
                            club = c.getTenClb().trim();
                    }
                } catch (RuntimeException ignore) {
                }
                String display = club.isBlank() ? team : (team + " - " + club);
                namesByT.set(t, display);
            }
            canvas.clearTextOverrides();
            if (M >= 2) {
                int pairCount = M / 2;
                for (int p = 0; p < pairCount; p++) {
                    String a = namesByT.get(2 * p);
                    String b = namesByT.get(2 * p + 1);
                    boolean hasA = a != null && !a.isBlank();
                    boolean hasB = b != null && !b.isBlank();
                    if (hasA ^ hasB) {
                        String winner = hasA ? a : b;
                        namesByT.set(2 * p, null);
                        namesByT.set(2 * p + 1, null);
                        if (seedCol < 5)
                            canvas.setTextOverride(seedCol + 1, p, winner);
                    }
                }
            }
            canvas.setParticipantsForColumn(namesByT, seedCol);
            canvas.repaint();
            updateMedalsFromCanvas();
        } else {
            List<com.example.btms.model.draw.BocThamCaNhan> list;
            try {
                list = bocThamCaNhanService.list(idGiai, nd.getId());
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải bốc thăm cá nhân: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            int N = list.size();
            int M;
            int seedCol;
            if (N >= 9) {
                M = 16;
                seedCol = 1;
            } else if (N >= 5) {
                M = 8;
                seedCol = 2;
            } else if (N >= 3) {
                M = 4;
                seedCol = 3;
            } else if (N >= 2) {
                M = 2;
                seedCol = 4;
            } else {
                M = 1;
                seedCol = 4;
            }
            if (N > 16) {
                JOptionPane.showMessageDialog(this, "Số VĐV vượt quá 16 – chưa hỗ trợ trong sơ đồ 16.", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                N = 16;
                M = 16;
                seedCol = 1;
            }
            List<Integer> pos = computeTopHeavyPositionsWithinBlock(Math.min(N, M), M);
            List<String> namesByT = new ArrayList<>();
            for (int t = 0; t < M; t++)
                namesByT.add(null);
            for (int i = 0; i < Math.min(N, M); i++) {
                int t = pos.get(i);
                var row = list.get(i);
                String display;
                try {
                    var vdv = vdvService.findOne(row.getIdVdv());
                    String name = vdv.getHoTen() != null ? vdv.getHoTen().trim() : ("VDV#" + row.getIdVdv());
                    String club = vdvService.getClubNameById(row.getIdVdv());
                    display = (club != null && !club.isBlank()) ? (name + " - " + club.trim()) : name;
                } catch (RuntimeException ignore) {
                    display = "VDV#" + row.getIdVdv();
                }
                namesByT.set(t, display);
            }
            canvas.clearTextOverrides();
            if (M >= 2) {
                int pairCount = M / 2;
                for (int p = 0; p < pairCount; p++) {
                    String a = namesByT.get(2 * p);
                    String b = namesByT.get(2 * p + 1);
                    boolean hasA = a != null && !a.isBlank();
                    boolean hasB = b != null && !b.isBlank();
                    if (hasA ^ hasB) {
                        String winner = hasA ? a : b;
                        namesByT.set(2 * p, null);
                        namesByT.set(2 * p + 1, null);
                        if (seedCol < 5)
                            canvas.setTextOverride(seedCol + 1, p, winner);
                    }
                }
            }
            canvas.setParticipantsForColumn(namesByT, seedCol);
            canvas.repaint();
            updateMedalsFromCanvas();
        }
    }

    private boolean loadSavedSoDo() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        NoiDung nd = selectedNoiDung;
        if (idGiai <= 0 || nd == null)
            return false;
        boolean isTeam = Boolean.TRUE.equals(nd.getTeam());
        if (isTeam) {
            List<SoDoDoi> list;
            try {
                list = soDoDoiService.list(idGiai, nd.getId());
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải sơ đồ đã lưu: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (list == null || list.isEmpty())
                return false;
            List<String> blanks = new ArrayList<>();
            for (int i = 0; i < 16; i++)
                blanks.add("");
            canvas.setParticipantsForColumn(blanks, 1);
            canvas.clearTextOverrides();
            for (SoDoDoi r : list) {
                BracketCanvas.Slot slot = canvas.findByOrder(r.getViTri());
                if (slot != null) {
                    canvas.setTextOverride(slot.col, slot.thuTu, r.getTenTeam());
                }
            }
            canvas.refreshAfterOverrides();
            canvas.repaint();
            updateMedalsFromCanvas();
            return true;
        } else {
            List<SoDoCaNhan> list;
            try {
                list = soDoCaNhanService.list(idGiai, nd.getId());
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải sơ đồ (cá nhân) đã lưu: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (list == null || list.isEmpty())
                return false;
            List<String> blanks = new ArrayList<>();
            for (int i = 0; i < 16; i++)
                blanks.add("");
            canvas.setParticipantsForColumn(blanks, 1);
            canvas.clearTextOverrides();
            for (SoDoCaNhan r : list) {
                BracketCanvas.Slot slot = canvas.findByOrder(r.getViTri());
                if (slot != null) {
                    String display;
                    try {
                        var vdv = vdvService.findOne(r.getIdVdv());
                        String name = vdv.getHoTen() != null ? vdv.getHoTen().trim() : ("VDV#" + r.getIdVdv());
                        String club = vdvService.getClubNameById(r.getIdVdv());
                        display = (club != null && !club.isBlank()) ? (name + " - " + club.trim()) : name;
                    } catch (RuntimeException ignore) {
                        display = "VDV#" + r.getIdVdv();
                    }
                    canvas.setTextOverride(slot.col, slot.thuTu, display);
                }
            }
            canvas.refreshAfterOverrides();
            canvas.repaint();
            updateMedalsFromCanvas();
            return true;
        }
    }

    private void loadBestAvailable() {
        boolean have = loadSavedSoDo();
        if (!have) {
            loadFromBocTham();
        }
        updateMedalsFromCanvas();
        canvas.repaint();
    }

    private void clearAllSlots() {
        // Giữ cấu trúc, xóa text
        List<String> blanks = new ArrayList<>();
        for (int i = 0; i < 16; i++)
            blanks.add("");
        canvas.setParticipantsForColumn(blanks, 1);
        canvas.clearTextOverrides();
        canvas.repaint();
        updateMedalsFromCanvas();
    }

    // Top-heavy fill: always put ceil(n/2) to top half first, then bottom half
    private static List<Integer> computeTopHeavyPositionsWithinBlock(int N, int M) {
        List<Integer> out = new ArrayList<>(N);
        fillTopHeavy(0, M, N, out);
        return out;
    }

    private static void fillTopHeavy(int start, int block, int n, List<Integer> out) {
        if (n <= 0)
            return;
        if (block == 1) {
            out.add(start);
            return;
        }
        int half = block / 2;
        int nTop = (n + 1) / 2; // ceil
        int nBot = n - nTop; // floor
        fillTopHeavy(start, half, nTop, out);
        fillTopHeavy(start + half, half, nBot, out);
    }

    // ===== Medal logic =====
    private void updateMedalsFromCanvas() {
        // Gold: cột 5, ô duy nhất
        String gold = getTextAt(5, 0);
        // Silver: cột 4, ô có parent là gold; chọn ô nào có text khác gold
        // Thực tế, ở vòng chung kết (cột 4, có 2 ô), silver là đội còn lại không phải
        // gold
        String semiA = getTextAt(4, 0);
        String semiB = getTextAt(4, 1);
        String silver = null;
        if (gold != null && !gold.isBlank()) {
            if (semiA != null && semiA.equals(gold))
                silver = semiB;
            else if (semiB != null && semiB.equals(gold))
                silver = semiA;
        }
        // Bronzes: 2 đội thua ở bán kết: cột 3 có 4 ô, nhưng bán kết là cột 4 đầu vào
        // từ cột 3.
        // Ở đây cấu trúc đã mapping: cột 3 => 4 chỗ, cột 4 => 2 chỗ.
        // Lấy 2 đội vào chung kết (semiA, semiB), mỗi đội có parent từ cột 3:
        // cột 3 chỉ chứa text nếu người dùng đã điền/seed; nếu rỗng dùng textOverrides
        // hiện có.
        // Ta tìm 2 cặp bán kết: (col3 thuTu 0,1)->semiA; (col3 thuTu 2,3)->semiB
        String semiA1 = getTextAt(3, 0);
        String semiA2 = getTextAt(3, 1);
        String semiB1 = getTextAt(3, 2);
        String semiB2 = getTextAt(3, 3);
        // Người thắng mỗi bán kết là semiA và semiB; bronze là người còn lại trong mỗi
        // cặp
        String bronze1 = null;
        String bronze2 = null;
        if (semiA != null && !semiA.isBlank()) {
            if (semiA.equals(semiA1))
                bronze1 = semiA2;
            else if (semiA.equals(semiA2))
                bronze1 = semiA1;
        }
        if (semiB != null && !semiB.isBlank()) {
            if (semiB.equals(semiB1))
                bronze2 = semiB2;
            else if (semiB.equals(semiB2))
                bronze2 = semiB1;
        }
        String g = safe(gold).trim();
        String s = safe(silver).trim();
        String b1 = safe(bronze1).trim();
        String b2 = safe(bronze2).trim();
        refreshMedalTable(g, s, b1, b2);

        // Auto-save medals when a champion exists and the snapshot changed
        String snapshotKey = String.join("|", g, s, b1, b2);
        if (!g.isBlank() && !snapshotKey.equals(lastSavedMedalKey)) {
            lastSavedMedalKey = snapshotKey;
            persistMedals(true); // silent
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String getTextAt(int col, int thuTu) {
        // Truy vấn slot hiện tại từ canvas
        for (var s : canvas.getSlots()) {
            if (s.col == col && s.thuTu == thuTu)
                return s.text;
        }
        return null;
    }

    private void refreshMedalTable(String gold, String silver, String bronze1, String bronze2) {
        medalModel.setRowCount(0);
        medalModel.addRow(new Object[] { "Vàng: " + gold });
        medalModel.addRow(new Object[] { "Bạc: " + silver });
        medalModel.addRow(new Object[] { "Đồng: " + bronze1 });
        medalModel.addRow(new Object[] { "Đồng: " + bronze2 });
    }

    // ===== Persist medals =====
    private void saveMedalResults() {
        persistMedals(false);
    }

    // Core implementation for persisting medals. When silent=true, suppress
    // dialogs.
    private void persistMedals(boolean silent) {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        NoiDung nd = selectedNoiDung;
        if (idGiai <= 0 || nd == null) {
            if (!silent) {
                JOptionPane.showMessageDialog(this, "Chưa chọn giải hoặc nội dung", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
            }
            return;
        }
        int idNoiDung = nd.getId();
        try {
            String vangRaw = extractNameFromMedalRow(0);
            String bacRaw = extractNameFromMedalRow(1);
            String dong1Raw = extractNameFromMedalRow(2);
            String dong2Raw = extractNameFromMedalRow(3);
            MedalSet medals = sanitizeMedals(vangRaw, bacRaw, dong1Raw, dong2Raw);
            String vang = medals.gold;
            String bac = medals.silver;
            String dong1 = medals.bronze1;
            String dong2 = medals.bronze2;
            boolean isTeam = Boolean.TRUE.equals(nd.getTeam());
            if (isTeam) {
                List<KetQuaDoi> items = new ArrayList<>();
                if (!isBlank(vang))
                    items.add(buildKetQua(idGiai, idNoiDung, vang, 1));
                if (!isBlank(bac))
                    items.add(buildKetQua(idGiai, idNoiDung, bac, 2));
                if (!isBlank(dong1))
                    items.add(buildKetQua(idGiai, idNoiDung, dong1, 3));
                if (!isBlank(dong2))
                    items.add(buildKetQua(idGiai, idNoiDung, dong2, 3));
                ketQuaDoiService.replaceMedals(idGiai, idNoiDung, items);
            } else {
                // Singles: replace medals in bulk to allow two bronzes
                java.util.Map<String, Integer> map = vdvService.loadSinglesNames(idNoiDung, idGiai);
                List<com.example.btms.model.result.KetQuaCaNhan> items = new ArrayList<>();
                if (!isBlank(vang)) {
                    Integer idVang = resolveVdvIdFromDisplay(map, vang);
                    if (idVang != null)
                        items.add(new com.example.btms.model.result.KetQuaCaNhan(idGiai, idNoiDung, idVang, 1));
                }
                if (!isBlank(bac)) {
                    Integer idBac = resolveVdvIdFromDisplay(map, bac);
                    if (idBac != null)
                        items.add(new com.example.btms.model.result.KetQuaCaNhan(idGiai, idNoiDung, idBac, 2));
                }
                if (!isBlank(dong1)) {
                    Integer idD1 = resolveVdvIdFromDisplay(map, dong1);
                    if (idD1 != null)
                        items.add(new com.example.btms.model.result.KetQuaCaNhan(idGiai, idNoiDung, idD1, 3));
                }
                if (!isBlank(dong2)) {
                    Integer idD2 = resolveVdvIdFromDisplay(map, dong2);
                    if (idD2 != null)
                        items.add(new com.example.btms.model.result.KetQuaCaNhan(idGiai, idNoiDung, idD2, 3));
                }
                ketQuaCaNhanService.replaceMedals(idGiai, idNoiDung, items);
            }
            if (!silent) {
                JOptionPane.showMessageDialog(this, "Đã lưu kết quả huy chương", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (RuntimeException ex) {
            if (!silent) {
                JOptionPane.showMessageDialog(this, "Lỗi lưu kết quả: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                lastSavedMedalKey = null;
            }
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String extractNameFromMedalRow(int row) {
        if (row < 0 || row >= medalModel.getRowCount())
            return "";
        Object v = medalModel.getValueAt(row, 0);
        if (v == null)
            return "";
        String s = v.toString();
        int idx = s.indexOf(":");
        if (idx >= 0 && idx + 1 < s.length())
            return s.substring(idx + 1).trim();
        return s.trim();
    }

    // Enforce: max 1 gold, 1 silver, 2 bronzes for one nội dung; also remove blanks
    // and duplicates across ranks.
    private static class MedalSet {
        final String gold;
        final String silver;
        final String bronze1;
        final String bronze2;

        MedalSet(String g, String s, String b1, String b2) {
            this.gold = g;
            this.silver = s;
            this.bronze1 = b1;
            this.bronze2 = b2;
        }
    }

    private MedalSet sanitizeMedals(String rawGold, String rawSilver, String rawBronze1, String rawBronze2) {
        String g = safe(rawGold).trim();
        String s = safe(rawSilver).trim();
        String b1 = safe(rawBronze1).trim();
        String b2 = safe(rawBronze2).trim();
        // Remove blanks
        if (g.isBlank())
            g = "";
        if (s.isBlank())
            s = "";
        if (b1.isBlank())
            b1 = "";
        if (b2.isBlank())
            b2 = "";
        // Deduplicate across different ranks: priority order gold > silver > bronze1 >
        // bronze2
        if (!g.isBlank()) {
            if (s.equals(g))
                s = "";
            if (b1.equals(g))
                b1 = "";
            if (b2.equals(g))
                b2 = "";
        }
        if (!s.isBlank()) {
            if (b1.equals(s))
                b1 = "";
            if (b2.equals(s))
                b2 = "";
        }
        // Ensure at most two distinct bronzes
        if (b1.isBlank() && !b2.isBlank()) {
            // normalize to fill bronze1 first
            b1 = b2;
            b2 = "";
        } else if (!b1.isBlank() && !b2.isBlank() && b1.equals(b2)) {
            // drop duplicate
            b2 = "";
        }
        // Update the UI table to reflect sanitized values
        refreshMedalTable(g, s, b1, b2);
        return new MedalSet(g, s, b1, b2);
    }

    private KetQuaDoi buildKetQua(int idGiai, int idNoiDung, String teamDisplay, int thuHang) {
        String teamName = extractTeamNameFromDisplay(teamDisplay);
        Integer idClb = null;
        try {
            idClb = doiService.getIdClbByTeamName(teamName, idNoiDung, idGiai);
        } catch (RuntimeException ignore) {
        }
        if (idClb == null || idClb <= 0) {
            throw new IllegalStateException("Không xác định được CLB cho đội: " + teamDisplay);
        }
        return new KetQuaDoi(idGiai, idNoiDung, idClb, teamDisplay, thuHang);
    }

    private String extractTeamNameFromDisplay(String display) {
        if (display == null)
            return "";
        String teamName = display.trim();
        int sep = teamName.indexOf(" - ");
        if (sep >= 0)
            teamName = teamName.substring(0, sep).trim();
        return teamName;
    }

    private Integer resolveVdvIdFromDisplay(java.util.Map<String, Integer> nameToId, String display) {
        if (display == null)
            return null;
        String name = display;
        int sep = display.indexOf(" - ");
        if (sep >= 0)
            name = display.substring(0, sep).trim();
        return nameToId.get(name);
    }

    /* ===================== Canvas ===================== */
    private class BracketCanvas extends JPanel {
        private static final int COLUMNS = 5; // 16 -> 1
        private static final int[] SPOTS = { 16, 8, 4, 2, 1 };
        private static final int CELL_WIDTH = 200; // tăng chiều ngang ô (rộng hơn để hiển thị tên)
        private static final int CELL_HEIGHT = 30;
        // Ô cuối (vô địch) sẽ to hơn một chút để nổi bật
        private static final int FINAL_CELL_WIDTH = 300;
        private static final int FINAL_CELL_HEIGHT = 36;
        // Độ dịch lên cho các vòng trong (cột > 1) để nằm hơi cao hơn so với chính giữa
        private static final int INNER_UP_OFFSET = 20; // px
        // Độ dịch sang phải cơ sở cho mỗi mức sâu hơn (cột > 1). Tổng offset =
        // (col-1)*BASE
        private static final int BASE_INNER_RIGHT_OFFSET = 60; // px mỗi cột (giãn thêm theo chiều ngang)
        // Giảm khoảng trống phía trên (trước đây dùng 62)
        private static final int START_Y = 10; // px
        // Canvas width will be expanded dynamically based on the longest name

        private List<String> participants = new ArrayList<>(); // tên tại cột seedColumn
        private int seedColumn = 1; // cột sẽ hiển thị danh sách ban đầu (1..5)
        private final java.util.Map<Integer, String> textOverrides = new java.util.HashMap<>();
        private boolean editMode = false;

        private static class Slot {
            int col; // 1..5
            int thuTu; // 0-based among spots in that column
            int x;
            int y;
            String text;
            int order; // 1-based continuous numbering across columns left->right, top->bottom
        }

        private final List<Slot> slots = new ArrayList<>();

        private int cellWidthForCol(int col) {
            return (col == COLUMNS) ? FINAL_CELL_WIDTH : CELL_WIDTH;
        }

        private int cellHeightForCol(int col) {
            return (col == COLUMNS) ? FINAL_CELL_HEIGHT : CELL_HEIGHT;
        }

        BracketCanvas() {
            setOpaque(true);
            setBackground(Color.WHITE);
            setFont(getFont().deriveFont(Font.PLAIN, 12f));
            rebuildSlots();
            // simple edit: double-click a slot to edit text when edit mode is on
            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!editMode)
                        return;
                    if (e.getClickCount() >= 2 && e.getButton() == MouseEvent.BUTTON1) {
                        Slot s = getSlotAt(e.getPoint());
                        if (s != null) {
                            String newText = JOptionPane.showInputDialog(BracketCanvas.this, "Sửa tên đội:", s.text);
                            if (newText != null) {
                                setTextOverride(s.col, s.thuTu, newText.trim());
                                // cập nhật ngay cả trong cache slots
                                s.text = newText.trim();
                                repaint();
                                SoDoThiDauPanel.this.updateMedalsFromCanvas();
                            }
                        }
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    maybeShowContextMenu(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    maybeShowContextMenu(e);
                }

                private void maybeShowContextMenu(MouseEvent e) {
                    if (!editMode)
                        return;
                    if (!e.isPopupTrigger())
                        return;
                    Slot s = getSlotAt(e.getPoint());
                    if (s == null)
                        return;
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem mAdvance = new JMenuItem("Vẽ bản ghi");
                    JMenuItem mBack = new JMenuItem("Xoá bản ghi");
                    mAdvance.setEnabled(s.col < COLUMNS && s.text != null && !s.text.isBlank());
                    mBack.setEnabled(s.col > 1 && s.text != null && !s.text.isBlank());
                    mAdvance.addActionListener(ev -> {
                        Slot parent = parentOf(s);
                        if (parent != null) {
                            setTextOverride(parent.col, parent.thuTu, s.text);
                            parent.text = s.text;
                            repaint();
                            SoDoThiDauPanel.this.updateMedalsFromCanvas();
                            // lưu vào db theo trạng thái hiện tại của panel
                            int idGiai = SoDoThiDauPanel.this.prefs.getInt("selectedGiaiDauId", -1);
                            NoiDung ndSel = SoDoThiDauPanel.this.selectedNoiDung;
                            if (idGiai > 0 && ndSel != null) {
                                int idNoiDung = ndSel.getId();
                                if (Boolean.TRUE.equals(ndSel.getTeam())) {
                                    Integer idClb = null;
                                    try {
                                        String teamName = SoDoThiDauPanel.this
                                                .extractTeamNameFromDisplay(s.text.trim());
                                        int found = SoDoThiDauPanel.this.doiService.getIdClbByTeamName(teamName,
                                                idNoiDung, idGiai);
                                        if (found > 0)
                                            idClb = found;
                                    } catch (RuntimeException ignored) {
                                    }
                                    Integer soDo = parent.col;
                                    try {
                                        if (idClb != null)
                                            soDo = SoDoThiDauPanel.this.bocThamService.getSoDo(idGiai, idNoiDung,
                                                    idClb);
                                    } catch (RuntimeException ignored) {
                                    }
                                    SoDoThiDauPanel.this.soDoDoiService.create(
                                            idGiai,
                                            idNoiDung,
                                            idClb,
                                            s.text.trim(),
                                            parent.x,
                                            parent.y,
                                            parent.order,
                                            soDo,
                                            java.time.LocalDateTime.now());
                                } else {
                                    java.util.Map<String, Integer> map = SoDoThiDauPanel.this.vdvService
                                            .loadSinglesNames(idNoiDung, idGiai);
                                    Integer idVdv = SoDoThiDauPanel.this.resolveVdvIdFromDisplay(map, s.text.trim());
                                    if (idVdv != null) {
                                        Integer soDo = parent.col;
                                        try {
                                            var bt = SoDoThiDauPanel.this.bocThamCaNhanService.getOne(idGiai, idNoiDung,
                                                    idVdv);
                                            if (bt != null && bt.getSoDo() != null)
                                                soDo = bt.getSoDo();
                                        } catch (RuntimeException ignored) {
                                        }
                                        SoDoThiDauPanel.this.soDoCaNhanService.create(
                                                idGiai,
                                                idNoiDung,
                                                idVdv,
                                                parent.x,
                                                parent.y,
                                                parent.order,
                                                soDo,
                                                java.time.LocalDateTime.now());
                                    }
                                }
                            }
                        }
                    });
                    mBack.addActionListener(ev -> {
                        // Xoá bản ghi: chỉ xoá dữ liệu tại ô hiện tại, KHÔNG đẩy về nhánh trước
                        if (s.text == null || s.text.isBlank()) {
                            return;
                        }
                        // Kiểm tra: nếu VĐV/Đội chỉ có 1 bản ghi trong sơ đồ thì KHÔNG xoá
                        int idGiai = SoDoThiDauPanel.this.prefs.getInt("selectedGiaiDauId", -1);
                        NoiDung ndSel = SoDoThiDauPanel.this.selectedNoiDung;
                        if (idGiai > 0 && ndSel != null) {
                            try {
                                if (Boolean.TRUE.equals(ndSel.getTeam())) {
                                    String display = s.text.trim();
                                    // Ưu tiên so theo ID_CLB nếu xác định được từ tên đội
                                    Integer idClb = null;
                                    try {
                                        String teamName = SoDoThiDauPanel.this.extractTeamNameFromDisplay(display);
                                        int found = SoDoThiDauPanel.this.doiService.getIdClbByTeamName(teamName,
                                                ndSel.getId(), idGiai);
                                        if (found > 0)
                                            idClb = found;
                                    } catch (RuntimeException ignored) {
                                    }
                                    List<SoDoDoi> all = SoDoThiDauPanel.this.soDoDoiService.list(idGiai, ndSel.getId());
                                    long cnt = 0;
                                    if (idClb != null && idClb > 0) {
                                        for (SoDoDoi r : all) {
                                            if (r.getIdClb() != null && r.getIdClb() == idClb)
                                                cnt++;
                                        }
                                    } else {
                                        for (SoDoDoi r : all) {
                                            String tt = r.getTenTeam();
                                            if (tt != null && tt.trim().equals(display))
                                                cnt++;
                                        }
                                    }
                                    if (cnt <= 1) {
                                        JOptionPane.showMessageDialog(BracketCanvas.this,
                                                "Đội này chỉ còn 1 bản ghi trong sơ đồ – không xoá để tránh mất dữ liệu.",
                                                "Không thể xoá", JOptionPane.WARNING_MESSAGE);
                                        return;
                                    }
                                } else {
                                    String display = s.text.trim();
                                    java.util.Map<String, Integer> map = SoDoThiDauPanel.this.vdvService
                                            .loadSinglesNames(ndSel.getId(), idGiai);
                                    Integer idVdv = SoDoThiDauPanel.this.resolveVdvIdFromDisplay(map, display);
                                    if (idVdv == null) {
                                        JOptionPane.showMessageDialog(BracketCanvas.this,
                                                "Không xác định được VĐV từ ô đang chọn; không thể xoá an toàn.",
                                                "Không thể xoá", JOptionPane.WARNING_MESSAGE);
                                        return;
                                    }
                                    List<SoDoCaNhan> all = SoDoThiDauPanel.this.soDoCaNhanService.list(idGiai,
                                            ndSel.getId());
                                    long cnt = 0;
                                    for (SoDoCaNhan r : all) {
                                        if (r.getIdVdv() == idVdv)
                                            cnt++;
                                    }
                                    if (cnt <= 1) {
                                        JOptionPane.showMessageDialog(BracketCanvas.this,
                                                "VĐV này chỉ còn 1 bản ghi trong sơ đồ – không xoá để tránh mất dữ liệu.",
                                                "Không thể xoá", JOptionPane.WARNING_MESSAGE);
                                        return;
                                    }
                                }
                            } catch (RuntimeException ex) {
                                // Nếu lỗi khi kiểm tra, ưu tiên an toàn: không xoá
                                JOptionPane.showMessageDialog(BracketCanvas.this,
                                        "Không thể kiểm tra số bản ghi: " + ex.getMessage(),
                                        "Không thể xoá", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        }

                        // Sau khi qua kiểm tra, tiến hành xoá DB tại ô hiện tại (theo VI_TRI)
                        if (idGiai > 0 && ndSel != null) {
                            try {
                                if (Boolean.TRUE.equals(ndSel.getTeam())) {
                                    SoDoThiDauPanel.this.soDoDoiService.delete(idGiai, ndSel.getId(), s.order);
                                } else {
                                    SoDoThiDauPanel.this.soDoCaNhanService.delete(idGiai, ndSel.getId(), s.order);
                                }
                            } catch (RuntimeException ignore) {
                            }
                        }

                        // Xoá text ở ô hiện tại (UI)
                        setTextOverride(s.col, s.thuTu, "");
                        s.text = "";
                        repaint();
                        SoDoThiDauPanel.this.updateMedalsFromCanvas();
                    });
                    menu.add(mAdvance);
                    menu.add(mBack);
                    menu.show(BracketCanvas.this, e.getX(), e.getY());
                }
            };
            addMouseListener(ma);
        }

        void setEditMode(boolean on) {
            this.editMode = on;
            setCursor(on ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
                    : java.awt.Cursor.getDefaultCursor());
        }

        void setParticipantsForColumn(List<String> names, int seedCol) {
            this.participants = names != null ? names : new ArrayList<>();
            if (seedCol < 1 || seedCol > COLUMNS)
                seedCol = 1;
            this.seedColumn = seedCol;
            rebuildSlots();
        }

        void clearTextOverrides() {
            textOverrides.clear();
        }

        void setTextOverride(int col, int thuTu, String text) {
            int key = (col << 16) | (thuTu & 0xFFFF);
            if (text == null || text.isBlank()) {
                textOverrides.remove(key);
            } else {
                textOverrides.put(key, text);
            }
        }

        List<Slot> getSlots() {
            return slots;
        }

        void refreshAfterOverrides() {
            // Rebuild slots so newly set overrides are reflected in cached Slot.text
            rebuildSlots();
        }

        Slot findByOrder(int order) {
            for (Slot s : slots) {
                if (s.order == order)
                    return s;
            }
            return null;
        }

        private Slot getSlotAt(Point p) {
            for (Slot s : slots) {
                int w = cellWidthForCol(s.col), h = cellHeightForCol(s.col);
                if (p.x >= s.x && p.x <= s.x + w && p.y >= s.y && p.y <= s.y + h)
                    return s;
            }
            return null;
        }

        private void rebuildSlots() {
            slots.clear();
            int orderCounter = 1; // continuous numbering
            for (int col = 1; col <= COLUMNS; col++) {
                int spotCount = SPOTS[col - 1];
                int verticalStep = (int) (40 * Math.pow(2, col - 1)); // bước của cột hiện tại
                for (int t = 0; t < spotCount; t++) {
                    int x = 35 + (col - 1) * 200 + (col > 1 ? (col - 1) * BASE_INNER_RIGHT_OFFSET : 0); // dịch phải
                                                                                                        // (giãn ngang)
                    int baseY = START_Y + t * verticalStep;
                    // Cột 1 giữ nguyên. Cột >1: giữa hai ô con rồi đẩy lên một chút.
                    int y;
                    if (col == 1) {
                        y = baseY;
                    } else {
                        y = baseY + verticalStep / 2 - INNER_UP_OFFSET;
                        if (y < 0)
                            y = 0; // an toàn
                    }
                    Slot s = new Slot();
                    s.col = col;
                    s.thuTu = t;
                    s.x = x;
                    s.y = y;
                    s.order = orderCounter++;
                    if (s.col == seedColumn) {
                        s.text = (t < participants.size() && participants.get(t) != null) ? participants.get(t) : "";
                    } else if (s.col == COLUMNS) {
                        s.text = "";
                    } else {
                        s.text = "";
                    }
                    // apply text override if any
                    String ovr = textOverrides.get((s.col << 16) | (s.thuTu & 0xFFFF));
                    if (ovr != null) {
                        s.text = ovr;
                    }
                    slots.add(s);
                }
            }
            // Cập nhật preferred size dựa trên xa nhất, gồm cả phần tràn tên ở bên phải
            int baseMaxX = 35 + (COLUMNS - 1) * 200 + cellWidthForCol(COLUMNS) + 40; // khớp với spacing mới + ô cuối to
                                                                                     // hơn
            int fontSize = getBracketNameFontSize();
            Font f = getFont().deriveFont(Font.PLAIN, (float) fontSize);
            java.awt.FontMetrics fm = getFontMetrics(f);
            int maxRight = baseMaxX;
            for (Slot s : slots) {
                if (s.text != null && !s.text.isBlank()) {
                    int textW = fm.stringWidth(s.text);
                    int right = s.x + 10 + textW; // x + padding + text width
                    if (right > maxRight)
                        maxRight = right;
                }
            }
            int maxX = Math.max(baseMaxX, maxRight + 40); // thêm 40px để có khoảng trống
            int lastColSpots = SPOTS[0];
            int maxY = START_Y + (lastColSpots) * (40) + 400; // dư chút để scroll
            setPreferredSize(new Dimension(maxX, maxY));
            revalidate();
        }

        private Slot parentOf(Slot s) {
            if (s == null)
                return null;
            if (s.col >= COLUMNS)
                return null;
            return find(s.col + 1, s.thuTu / 2);
        }

        // childrenOf not used; parentOf is sufficient for our flows

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Vẽ nhãn cột
            g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
            g2.setFont(getFont());
            // Vẽ ô
            for (Slot s : slots) {
                int w = cellWidthForCol(s.col);
                int h = cellHeightForCol(s.col);
                // Fill with light background
                g2.setColor(new Color(250, 250, 250));
                g2.fillRoundRect(s.x, s.y, w, h, 8, 8);
                // Draw border on three sides (left, top, bottom) — remove right border
                g2.setColor(new Color(120, 120, 120));
                // top
                g2.drawLine(s.x, s.y, s.x + w, s.y);
                // bottom
                g2.drawLine(s.x, s.y + h, s.x + w, s.y + h);
                // left
                g2.drawLine(s.x, s.y, s.x, s.y + h);
                // Text (ẩn số thứ tự slot để gọn gàng)
                if (s.text != null && !s.text.isBlank()) {
                    g2.setColor(Color.BLACK);
                    // Đọc cỡ chữ từ prefs (10..24), mặc định 12
                    int fontSize = getBracketNameFontSize();
                    Font nameFont = getFont().deriveFont(Font.PLAIN, (float) fontSize);
                    g2.setFont(nameFont);
                    // Hiển thị toàn bộ tên, cho phép tràn ra ngoài ô (không dùng …)
                    String txt = s.text;
                    // baseline y: padding top 6 + ascent
                    int ascent = g2.getFontMetrics().getAscent();
                    int textY = s.y + Math.max(18, 6 + ascent);
                    g2.drawString(txt, s.x + 10, textY);
                }
            }

            // Vẽ đường nối giữa các cột (pair -> parent)
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(170, 170, 170));
            for (int col = 1; col < COLUMNS; col++) {
                int spotCount = SPOTS[col - 1];
                for (int t = 0; t < spotCount; t += 2) {
                    Slot a = find(col, t);
                    Slot b = find(col, t + 1);
                    Slot parent = find(col + 1, t / 2);
                    if (a == null || b == null || parent == null)
                        continue;
                    int rx = a.x + cellWidthForCol(a.col); // right edge of child slots (same for a and b)
                    int ay = a.y + cellHeightForCol(a.col) / 2;
                    int by = b.y + cellHeightForCol(b.col) / 2;
                    int px = parent.x;
                    int py = parent.y + cellHeightForCol(parent.col) / 2;
                    int yTop = Math.min(ay, by);
                    int yBot = Math.max(ay, by);
                    int midY = (ay + by) / 2;
                    // Vertical spine at the right edge of child slots, trimmed 15px at both ends
                    int trim = 15;
                    int vTop = yTop + trim;
                    int vBot = yBot - trim;
                    if (vBot < vTop) { // guard in case spacing is too tight
                        int m = (yTop + yBot) / 2;
                        vTop = m;
                        vBot = m;
                    }
                    g2.drawLine(rx, vTop, rx, vBot);
                    // Single horizontal from spine to the parent's x at the middle Y
                    g2.drawLine(rx, midY, px, midY);
                    // Short vertical at the parent side to meet parent's center
                    if (py != midY) {
                        g2.drawLine(px, Math.min(py, midY), px, Math.max(py, midY));
                    }
                }
            }

            g2.dispose();
        }

        private Slot find(int col, int thuTu) {
            for (Slot s : slots) {
                if (s.col == col && s.thuTu == thuTu)
                    return s;
            }
            return null;
        }

        private int getBracketNameFontSize() {
            try {
                int v = SoDoThiDauPanel.this.prefs.getInt("bracket.nameFontSize", 12);
                if (v < 10)
                    v = 10;
                if (v > 24)
                    v = 24;
                return v;
            } catch (RuntimeException ignore) {
                return 12;
            }
        }
    }
}
