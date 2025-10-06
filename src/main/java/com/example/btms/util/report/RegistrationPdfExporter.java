package com.example.btms.util.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.btms.config.Prefs;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.club.CauLacBo;
import com.example.btms.model.player.DangKiCaNhan;
import com.example.btms.model.player.VanDongVien;
import com.example.btms.model.team.ChiTietDoi;
import com.example.btms.model.team.DangKiDoi;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.club.CauLacBoRepository;
import com.example.btms.repository.player.DangKiCaNhanRepository;
import com.example.btms.repository.player.VanDongVienRepository;
import com.example.btms.repository.team.ChiTietDoiRepository;
import com.example.btms.repository.team.DangKiDoiRepository;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.player.DangKiCaNhanService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.team.ChiTietDoiService;
import com.example.btms.service.team.DangKiDoiService;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Export danh sách đăng ký ĐƠN + ĐÔI ra PDF: tất cả, theo CLB, theo nội dung.
 * (Giữ tên lớp lịch sử, nhưng giờ bao gồm cả ĐƠN.)
 */
public final class RegistrationPdfExporter {
    private static final Integer NO_CLUB = -1;

    private RegistrationPdfExporter() {
    }

    public static void exportAll(Connection conn, int idGiai, File out, String giaiName) throws Exception {
        Data d = loadData(conn, idGiai);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            Document doc = new Document(PageSize.A4, 36f, 36f, 90f, 36f); // giấy dọc (portrait) + top margin for header
            PdfWriter writer = PdfWriter.getInstance(doc, fos);
            writer.setPageEvent(new HeaderEvent(giaiName, "DANH SÁCH ĐĂNG KÝ (ĐƠN + ĐÔI) - TẤT CẢ"));
            doc.open();
            PdfPTable table = buildTable(d.fontHeader);
            List<Row> rows = new ArrayList<>();
            for (NoiDung nd : d.noiDungs) {
                // ĐÔI
                List<DangKiDoi> teams = d.teamsByNoiDung.getOrDefault(nd.getId(), List.of());
                for (DangKiDoi t : teams)
                    rows.add(toRow(d, nd, t));
                // ĐƠN
                List<DangKiCaNhan> singles = d.singlesByNoiDung.getOrDefault(nd.getId(), List.of());
                for (DangKiCaNhan r : singles)
                    rows.add(toRowSingle(d, nd, r));
            }
            fillRows(table, rows, d.fontNormal);
            doc.add(table);
            doc.close();
        }
    }

    public static void exportByClub(Connection conn, int idGiai, File out, String giaiName) throws Exception {
        Data d = loadData(conn, idGiai);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            Document doc = new Document(PageSize.A4, 36f, 36f, 90f, 36f); // giấy dọc (portrait) + top margin for header
            PdfWriter writer = PdfWriter.getInstance(doc, fos);
            writer.setPageEvent(new HeaderEvent(giaiName, "DANH SÁCH ĐĂNG KÝ THEO CLB (ĐƠN + ĐÔI)"));
            doc.open();
            // Group by CLB
            Map<Integer, List<Row>> byClub = new LinkedHashMap<>();
            for (NoiDung nd : d.noiDungs) {
                // ĐÔI
                for (DangKiDoi t : d.teamsByNoiDung.getOrDefault(nd.getId(), List.of())) {
                    Row r = toRow(d, nd, t);
                    final Integer key = Objects.requireNonNullElse(r.clbId, NO_CLUB);
                    byClub.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
                }
                // ĐƠN
                for (DangKiCaNhan r1 : d.singlesByNoiDung.getOrDefault(nd.getId(), List.of())) {
                    Row r = toRowSingle(d, nd, r1);
                    final Integer key = Objects.requireNonNullElse(r.clbId, NO_CLUB);
                    byClub.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
                }
            }
            List<Map.Entry<Integer, List<Row>>> clubEntries = new ArrayList<>(byClub.entrySet());
            for (int idx = 0; idx < clubEntries.size(); idx++) {
                Map.Entry<Integer, List<Row>> e = clubEntries.get(idx);
                if (idx > 0) {
                    doc.newPage();
                }
                boolean noClub = NO_CLUB.equals(e.getKey());
                String clbName = noClub ? "(Không có CLB)"
                        : Optional.ofNullable(d.clbById.get(e.getKey())).map(CauLacBo::getTenClb).orElse("(N/A)");
                Paragraph sec = new Paragraph("• CLB: " + clbName + " (" + e.getValue().size() + ")", d.fontSection);
                sec.setSpacingBefore(8f);
                sec.setSpacingAfter(3f);
                doc.add(sec);
                PdfPTable table = buildTable(d.fontHeader);
                fillRows(table, e.getValue(), d.fontNormal);
                doc.add(table);
            }
            doc.close();
        }
    }

    public static void exportByNoiDung(Connection conn, int idGiai, File out, String giaiName) throws Exception {
        Data d = loadData(conn, idGiai);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            Document doc = new Document(PageSize.A4, 36f, 36f, 90f, 36f); // giấy dọc (portrait) + top margin for header
            PdfWriter writer = PdfWriter.getInstance(doc, fos);
            writer.setPageEvent(new HeaderEvent(giaiName, "DANH SÁCH ĐĂNG KÝ THEO NỘI DUNG (ĐƠN + ĐÔI)"));
            doc.open();
            for (int idx = 0; idx < d.noiDungs.size(); idx++) {
                NoiDung nd = d.noiDungs.get(idx);
                if (idx > 0) {
                    doc.newPage();
                }
                List<Row> rows = new ArrayList<>();
                // ĐÔI
                rows.addAll(d.teamsByNoiDung.getOrDefault(nd.getId(), List.of())
                        .stream().map(t -> toRow(d, nd, t)).collect(Collectors.toList()));
                // ĐƠN
                rows.addAll(d.singlesByNoiDung.getOrDefault(nd.getId(), List.of())
                        .stream().map(r -> toRowSingle(d, nd, r)).collect(Collectors.toList()));
                Paragraph sec = new Paragraph("• Nội dung: " + safe(nd.getTenNoiDung()) + " (" + rows.size() + ")",
                        d.fontSection);
                sec.setSpacingBefore(8f);
                sec.setSpacingAfter(3f);
                doc.add(sec);
                PdfPTable table = buildTable(d.fontHeader);
                fillRows(table, rows, d.fontNormal);
                doc.add(table);
            }
            doc.close();
        }
    }

    /* -------------------- Internals -------------------- */

    private static class Data {
        final List<NoiDung> noiDungs; // gồm cả nội dung ĐƠN và ĐÔI
        final Map<Integer, List<DangKiDoi>> teamsByNoiDung; // ĐÔI
        final Map<Integer, List<DangKiCaNhan>> singlesByNoiDung; // ĐƠN
        final Map<Integer, CauLacBo> clbById;
        final Map<Integer, VanDongVien> vdvById;
        final ChiTietDoiService chiTietSvc;
        final Font fontHeader, fontNormal, fontSection;

        Data(List<NoiDung> noiDungs,
                Map<Integer, List<DangKiDoi>> teamsByNoiDung,
                Map<Integer, List<DangKiCaNhan>> singlesByNoiDung,
                Map<Integer, CauLacBo> clbById, Map<Integer, VanDongVien> vdvById,
                ChiTietDoiService chiTietSvc,
                Font fontHeader, Font fontNormal, Font fontSection) {
            this.noiDungs = noiDungs;
            this.teamsByNoiDung = teamsByNoiDung;
            this.singlesByNoiDung = singlesByNoiDung;
            this.clbById = clbById;
            this.vdvById = vdvById;
            this.chiTietSvc = chiTietSvc;
            this.fontHeader = fontHeader;
            this.fontNormal = fontNormal;
            this.fontSection = fontSection;
        }
    }

    private static class Row {
        Integer clbId;
        String clb;
        String noiDung;
        String tenDoi;
        String thanhVien; // "A & B"
    }

    private static Data loadData(Connection conn, int idGiai) throws Exception {
        // Load registered contents: both SINGLES and DOUBLES
        NoiDungService ndSvc = new NoiDungService(new NoiDungRepository(conn));
        List<NoiDung> ndAll = ndSvc.getAllNoiDung();
        Map<String, Integer>[] maps = new NoiDungRepository(conn).loadCategories();
        java.util.Set<Integer> singleIds = new java.util.LinkedHashSet<>(maps[0].values());
        java.util.Set<Integer> doubleIds = new java.util.LinkedHashSet<>(maps[1].values());
        List<NoiDung> singles = ndAll.stream()
                .filter(nd -> nd.getId() != null && singleIds.contains(nd.getId()))
                .collect(Collectors.toList());
        List<NoiDung> doubles = ndAll.stream()
                .filter(nd -> nd.getId() != null && doubleIds.contains(nd.getId()))
                .collect(Collectors.toList());
        // Union (preserve order by id)
        Map<Integer, NoiDung> union = new LinkedHashMap<>();
        java.util.function.Consumer<NoiDung> addNd = nd -> {
            if (nd != null && nd.getId() != null)
                union.putIfAbsent(nd.getId(), nd);
        };
        singles.stream().sorted(java.util.Comparator.comparing(NoiDung::getId)).forEach(addNd);
        doubles.stream().sorted(java.util.Comparator.comparing(NoiDung::getId)).forEach(addNd);
        List<NoiDung> allRelevant = new ArrayList<>(union.values());

        DangKiDoiRepository doiRepo = new DangKiDoiRepository(conn);
        DangKiDoiService doiSvc = new DangKiDoiService(doiRepo);
        ChiTietDoiService chiTietSvc = new ChiTietDoiService(conn, doiRepo, new ChiTietDoiRepository(conn));

        DangKiCaNhanService dkcnSvc = new DangKiCaNhanService(new DangKiCaNhanRepository(conn));

        CauLacBoService clbSvc = new CauLacBoService(new CauLacBoRepository(conn));
        Map<Integer, CauLacBo> clbById = new LinkedHashMap<>();
        for (CauLacBo c : clbSvc.findAll()) {
            if (c.getId() != null)
                clbById.put(c.getId(), c);
        }
        VanDongVienService vdvSvc = new VanDongVienService(new VanDongVienRepository(conn));
        Map<Integer, VanDongVien> vdvById = new LinkedHashMap<>();
        for (VanDongVien v : vdvSvc.findAll()) {
            if (v.getId() != null)
                vdvById.put(v.getId(), v);
        }

        Map<Integer, List<DangKiDoi>> teamsByNd = new LinkedHashMap<>();
        for (NoiDung nd : doubles) {
            List<DangKiDoi> teams = doiSvc.listTeams(idGiai, nd.getId());
            teamsByNd.put(nd.getId(), teams);
        }

        Map<Integer, List<DangKiCaNhan>> singlesByNd = new LinkedHashMap<>();
        for (NoiDung nd : singles) {
            List<DangKiCaNhan> regs = dkcnSvc.listByGiaiAndNoiDung(idGiai, nd.getId(), null);
            singlesByNd.put(nd.getId(), regs);
        }

        // Fonts (attempt to support Unicode via registered system fonts)
        Font fontHeader, fontNormal, fontSection;
        try {
            com.lowagie.text.FontFactory.registerDirectories();
            Font base = com.lowagie.text.FontFactory.getFont("Times New Roman", BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    12);
            if (base == null || base.getBaseFont() == null) {
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                fontNormal = new Font(bf, 12);
            } else {
                fontNormal = base;
            }
            fontHeader = new Font(fontNormal);
            fontHeader.setStyle(Font.BOLD);
            fontSection = new Font(fontNormal);
            fontSection.setStyle(Font.BOLD);
        } catch (DocumentException | IOException ex) {
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            fontNormal = new Font(bf, 12);
            fontHeader = new Font(fontNormal);
            fontHeader.setStyle(Font.BOLD);
            fontSection = new Font(fontNormal);
            fontSection.setStyle(Font.BOLD);
        }

        return new Data(allRelevant, teamsByNd, singlesByNd, clbById, vdvById, chiTietSvc,
                fontHeader, fontNormal, fontSection);
    }

    /** Page event to draw header on every page. */
    private static class HeaderEvent extends PdfPageEventHelper {
        private final String tournamentName;
        private final String exportTitle;
        private final Font tournamentFont;
        private final Font exportFont;
        private Image leftLogo; // report.logo.path
        private Image rightLogo; // report.sponsor.logo.path

        HeaderEvent(String giaiName, String title) {
            this.tournamentName = safe(giaiName);
            this.exportTitle = safe(title);
            this.tournamentFont = docFont(18, Font.BOLD);
            this.exportFont = docFont(12, Font.BOLD);
            try {
                Prefs prefs = new Prefs();
                String leftPath = prefs.get("report.logo.path", "");
                if (leftPath != null && !leftPath.isBlank()) {
                    this.leftLogo = Image.getInstance(leftPath);
                }
            } catch (BadElementException | IOException ignore) {
                this.leftLogo = null;
            }
            try {
                Prefs prefs = new Prefs();
                String rightPath = prefs.get("report.sponsor.logo.path", "");
                if (rightPath != null && !rightPath.isBlank()) {
                    this.rightLogo = Image.getInstance(rightPath);
                }
            } catch (BadElementException | IOException ignore) {
                this.rightLogo = null;
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle page = document.getPageSize();
            float pageWidth = page.getWidth();
            float leftMargin = document.leftMargin();
            float rightMargin = document.rightMargin();
            float topY = page.getHeight() - 12f; // 12pt from top edge

            // Tournament name centered (first line)
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                    new Phrase(this.tournamentName, this.tournamentFont), pageWidth / 2f, topY - 16f, 0);
            // Export title centered (second line)
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                    new Phrase(this.exportTitle, this.exportFont), pageWidth / 2f, topY - 36f, 0);

            // Optional left logo (tournament/organization)
            if (leftLogo != null) {
                try {
                    Image img = Image.getInstance(leftLogo);
                    float maxW = (pageWidth - leftMargin - rightMargin) * 0.15f;
                    float maxH = 60f;
                    img.scaleToFit(maxW, maxH);
                    float x = leftMargin;
                    float y = topY - img.getScaledHeight();
                    img.setAbsolutePosition(x, y);
                    writer.getDirectContent().addImage(img);
                } catch (DocumentException ignore) {
                }
            }

            // Optional right logo (sponsor)
            if (rightLogo != null) {
                try {
                    Image img = Image.getInstance(rightLogo);
                    float maxW = (pageWidth - leftMargin - rightMargin) * 0.15f;
                    float maxH = 60f;
                    img.scaleToFit(maxW, maxH);
                    float x = pageWidth - rightMargin - img.getScaledWidth();
                    float y = topY - img.getScaledHeight();
                    img.setAbsolutePosition(x, y);
                    writer.getDirectContent().addImage(img);
                } catch (DocumentException ignore) {
                }
            }
        }
    }

    private static Font docFont(float size, int style) {
        try {
            com.lowagie.text.FontFactory.registerDirectories();
            Font f = com.lowagie.text.FontFactory.getFont("Times New Roman", BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    size, style);
            if (f != null && f.getBaseFont() != null)
                return f;
        } catch (DocumentException ignore) {
        }
        try {
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            return new Font(bf, size, style);
        } catch (DocumentException | IOException e) {
            return new Font(Font.HELVETICA, size, style);
        }
    }

    private static PdfPTable buildTable(Font fontHeader) {
        // Điều chỉnh tỉ lệ cột cho giấy dọc: tổng = 100
        float[] widths = { 6f, 20f, 22f, 26f, 26f }; // STT, Nội dung, CLB, Tên đội, Thành viên
        PdfPTable t = new PdfPTable(widths);
        t.setWidthPercentage(100);
        t.setHeaderRows(1);
        addHeaderCell(t, "STT", fontHeader);
        addHeaderCell(t, "Nội dung", fontHeader);
        addHeaderCell(t, "CLB", fontHeader);
        addHeaderCell(t, "Tên đội", fontHeader);
        addHeaderCell(t, "Thành viên", fontHeader);
        return t;
    }

    private static void addHeaderCell(PdfPTable t, String txt, Font fontHeader) {
        PdfPCell c = new PdfPCell(new Phrase(safe(txt), fontHeader));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setBackgroundColor(new java.awt.Color(235, 235, 235));
        t.addCell(c);
    }

    private static void fillRows(PdfPTable t, List<Row> rows, Font fontNormal) {
        int i = 1;
        for (Row r : rows) {
            addCell(t, String.valueOf(i++), fontNormal, Element.ALIGN_CENTER);
            addCell(t, r.noiDung, fontNormal, Element.ALIGN_LEFT);
            addCell(t, r.clb, fontNormal, Element.ALIGN_LEFT);
            addCell(t, r.tenDoi, fontNormal, Element.ALIGN_LEFT);
            addCell(t, r.thanhVien, fontNormal, Element.ALIGN_LEFT);
        }
    }

    private static void addCell(PdfPTable t, String txt, Font f, int align) {
        PdfPCell c = new PdfPCell(new Phrase(safe(txt), f));
        c.setHorizontalAlignment(align);
        t.addCell(c);
    }

    private static Row toRow(Data d, NoiDung nd, DangKiDoi t) {
        Row r = new Row();
        r.noiDung = safe(nd.getTenNoiDung());
        r.tenDoi = safe(t.getTenTeam());
        r.clbId = t.getIdCauLacBo();
        r.clb = (t.getIdCauLacBo() == null) ? ""
                : safe(Objects.toString(
                        Optional.ofNullable(d.clbById.get(t.getIdCauLacBo())).map(CauLacBo::getTenClb).orElse("")));
        try {
            List<ChiTietDoi> members = d.chiTietSvc.listMembers(t.getIdTeam());
            members.sort(java.util.Comparator.comparing(ChiTietDoi::getIdVdv));
            List<String> names = new ArrayList<>();
            for (ChiTietDoi m : members) {
                VanDongVien v = d.vdvById.get(m.getIdVdv());
                if (v != null)
                    names.add(safe(v.getHoTen()));
            }
            r.thanhVien = names.isEmpty() ? "" : String.join(" & ", names);
        } catch (RuntimeException ex) {
            r.thanhVien = "";
        }
        return r;
    }

    /** Hàng cho ĐƠN: dùng tên VĐV ở cột "Tên đội", để giữ cấu trúc bảng sẵn có. */
    private static Row toRowSingle(Data d, NoiDung nd, DangKiCaNhan reg) {
        Row r = new Row();
        r.noiDung = safe(nd.getTenNoiDung());
        VanDongVien v = d.vdvById.get(reg.getIdVdv());
        if (v != null) {
            r.tenDoi = safe(v.getHoTen());
            r.clbId = v.getIdClb();
            if (v.getIdClb() != null) {
                r.clb = safe(Optional.ofNullable(d.clbById.get(v.getIdClb())).map(CauLacBo::getTenClb).orElse(""));
            } else {
                r.clb = "";
            }
        } else {
            r.tenDoi = "";
            r.clbId = null;
            r.clb = "";
        }
        r.thanhVien = ""; // Đơn: không có danh sách thành viên
        return r;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
