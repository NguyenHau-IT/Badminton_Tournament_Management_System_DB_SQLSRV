package com.example.btms.util.report;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.btms.config.Prefs;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.club.CauLacBo;
import com.example.btms.model.player.VanDongVien;
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
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Export danh sách đăng ký ĐỘI (đôi) ra PDF: tất cả, theo CLB, theo nội dung.
 */
public final class TeamRegistrationPdfExporter {
    private TeamRegistrationPdfExporter() {
    }

    public static void exportAll(Connection conn, int idGiai, File out, String giaiName) throws Exception {
        Data d = loadData(conn, idGiai);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            Document doc = new Document(PageSize.A4.rotate()); // ngang để rộng bảng
            PdfWriter.getInstance(doc, fos);
            doc.open();
            addHeader(doc, giaiName, "DANH SÁCH ĐĂNG KÝ ĐỘI (TẤT CẢ)");
            PdfPTable table = buildTable(d.fontHeader);
            List<Row> rows = new ArrayList<>();
            for (NoiDung nd : d.noiDungs) {
                List<DangKiDoi> teams = d.teamsByNoiDung.getOrDefault(nd.getId(), List.of());
                for (DangKiDoi t : teams)
                    rows.add(toRow(d, nd, t));
            }
            fillRows(table, rows, d.fontNormal);
            doc.add(table);
            doc.close();
        }
    }

    public static void exportByClub(Connection conn, int idGiai, File out, String giaiName) throws Exception {
        Data d = loadData(conn, idGiai);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, fos);
            doc.open();
            addHeader(doc, giaiName, "DANH SÁCH ĐĂNG KÝ ĐỘI THEO CLB");
            // Group by CLB
            Map<Integer, List<Row>> byClub = new LinkedHashMap<>();
            for (NoiDung nd : d.noiDungs) {
                for (DangKiDoi t : d.teamsByNoiDung.getOrDefault(nd.getId(), List.of())) {
                    Row r = toRow(d, nd, t);
                    int key = r.clbId == null ? -1 : r.clbId;
                    byClub.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
                }
            }
            for (Map.Entry<Integer, List<Row>> e : byClub.entrySet()) {
                String clbName = (e.getKey() == -1) ? "(Không có CLB)"
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
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, fos);
            doc.open();
            addHeader(doc, giaiName, "DANH SÁCH ĐĂNG KÝ ĐỘI THEO NỘI DUNG");
            for (NoiDung nd : d.noiDungs) {
                List<Row> rows = d.teamsByNoiDung.getOrDefault(nd.getId(), List.of())
                        .stream().map(t -> toRow(d, nd, t)).collect(Collectors.toList());
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
        final List<NoiDung> noiDungs;
        final Map<Integer, List<DangKiDoi>> teamsByNoiDung;
        final Map<Integer, CauLacBo> clbById;
        final Map<Integer, VanDongVien> vdvById;
        final ChiTietDoiService chiTietSvc;
        final Font fontHeader, fontNormal, fontSection;

        Data(List<NoiDung> noiDungs, Map<Integer, List<DangKiDoi>> teamsByNoiDung,
                Map<Integer, CauLacBo> clbById, Map<Integer, VanDongVien> vdvById,
                ChiTietDoiService chiTietSvc,
                Font fontHeader, Font fontNormal, Font fontSection) {
            this.noiDungs = noiDungs;
            this.teamsByNoiDung = teamsByNoiDung;
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
        // Load registered doubles contents
        NoiDungService ndSvc = new NoiDungService(new NoiDungRepository(conn));
        List<NoiDung> ndAll = ndSvc.getAllNoiDung();
        Map<String, Integer>[] maps = new NoiDungRepository(conn).loadCategories();
        java.util.Set<Integer> doubleIds = new java.util.LinkedHashSet<>(maps[1].values());
        List<NoiDung> doubles = ndAll.stream()
                .filter(nd -> nd.getId() != null && doubleIds.contains(nd.getId()))
                .sorted(java.util.Comparator.comparing(NoiDung::getId))
                .collect(Collectors.toList());

        DangKiDoiRepository doiRepo = new DangKiDoiRepository(conn);
        DangKiDoiService doiSvc = new DangKiDoiService(doiRepo);
        ChiTietDoiService chiTietSvc = new ChiTietDoiService(conn, doiRepo, new ChiTietDoiRepository(conn));

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
        } catch (Exception ex) {
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            fontNormal = new Font(bf, 12);
            fontHeader = new Font(fontNormal);
            fontHeader.setStyle(Font.BOLD);
            fontSection = new Font(fontNormal);
            fontSection.setStyle(Font.BOLD);
        }

        return new Data(doubles, teamsByNd, clbById, vdvById, chiTietSvc,
                fontHeader, fontNormal, fontSection);
    }

    private static void addHeader(Document doc, String giaiName, String title) throws DocumentException {
        Prefs prefs = new Prefs();
        String logoPath = prefs.get("report.logo.path", "");
        Rectangle page = doc.getPageSize();
        // Title block
        Paragraph pTitle = new Paragraph(safe(title), docFont(18, Font.BOLD));
        pTitle.setAlignment(Element.ALIGN_CENTER);
        pTitle.setSpacingAfter(4f);
        Paragraph pSub = new Paragraph(
                safe(giaiName) + "\n" + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()),
                docFont(11, Font.NORMAL));
        pSub.setAlignment(Element.ALIGN_CENTER);
        pSub.setSpacingAfter(8f);
        // Optional logo on top-right
        if (logoPath != null && !logoPath.isBlank()) {
            try {
                Image img = Image.getInstance(logoPath);
                float maxW = page.getWidth() * 0.15f;
                float maxH = page.getHeight() * 0.15f;
                img.scaleToFit(maxW, maxH);
                img.setAlignment(Image.RIGHT);
                doc.add(img);
            } catch (Exception ignore) {
            }
        }
        doc.add(pTitle);
        doc.add(pSub);
    }

    private static Font docFont(float size, int style) {
        try {
            com.lowagie.text.FontFactory.registerDirectories();
            Font f = com.lowagie.text.FontFactory.getFont("Times New Roman", BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    size, style);
            if (f != null && f.getBaseFont() != null)
                return f;
        } catch (Exception ignore) {
        }
        try {
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            return new Font(bf, size, style);
        } catch (Exception e) {
            return new Font(Font.HELVETICA, size, style);
        }
    }

    private static PdfPTable buildTable(Font fontHeader) {
        float[] widths = { 6f, 16f, 16f, 32f, 30f }; // STT, Nội dung, CLB, Tên đội, Thành viên
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

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
