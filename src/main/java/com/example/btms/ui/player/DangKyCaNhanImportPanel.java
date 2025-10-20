package com.example.btms.ui.player;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.example.btms.config.Prefs;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.player.DangKiCaNhanService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.util.ui.IconUtil;

public class DangKyCaNhanImportPanel extends JFrame {
    private final Connection conn;
    private final Prefs prefs;
    private final NoiDungService noiDungService;
    private final DangKiCaNhanService dkService;
    private final VanDongVienService vdvService;
    private final CauLacBoService clbService;
    private final Runnable onDone;

    public static void showImportWindow(
            Connection conn,
            Prefs prefs,
            NoiDungService noiDungService,
            DangKiCaNhanService dkService,
            VanDongVienService vdvService,
            CauLacBoService clbService,
            Runnable onDone) {
        SwingUtilities.invokeLater(() -> {
            DangKyCaNhanImportPanel frame = new DangKyCaNhanImportPanel(
                    conn, prefs, noiDungService, dkService, vdvService, clbService, onDone);
            frame.setVisible(true);
        });
    }

    private final JTextField txtFile = new JTextField(36);
    private final JButton btnBrowse = new JButton("Chọn CSV...");
    private final JButton btnPreview = new JButton("Xem trước");
    private final JButton btnImport = new JButton("Nhập");
    private final JButton btnClose = new JButton("Đóng");
    private final JTextArea taLog = new JTextArea(8, 60);

    private final DefaultTableModel previewModel = new DefaultTableModel(
            new Object[] { "Dòng", "Họ tên", "CLB", "Nội dung", "TuổiMin", "TuổiMax", "Ghi chú" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable previewTable = new JTable(previewModel);

    private final DefaultTableModel successModel = new DefaultTableModel(
            new Object[] { "Dòng", "Họ tên", "CLB", "Nội dung", "Ghi chú" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable successTable = new JTable(successModel);

    private final DefaultTableModel errorModel = new DefaultTableModel(
            new Object[] { "Dòng", "Họ tên", "CLB", "Nội dung", "Lý do" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable errorTable = new JTable(errorModel);

    public DangKyCaNhanImportPanel(
            Connection conn,
            Prefs prefs,
            NoiDungService noiDungService,
            DangKiCaNhanService dkService,
            VanDongVienService vdvService,
            CauLacBoService clbService,
            Runnable onDone) {
        this.conn = conn;
        this.prefs = prefs;
        this.noiDungService = noiDungService;
        this.dkService = dkService;
        this.vdvService = vdvService;
        this.clbService = clbService;
        this.onDone = onDone != null ? onDone : () -> {
        };

        initializeFrame();
    }

    private void initializeFrame() {
        // Cấu hình JFrame
        setTitle("Nhập danh sách đăng ký cá nhân từ CSV");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Apply icon if available
        try {
            IconUtil.applyTo(this);
        } catch (Exception e) {
            // Ignore if IconUtil not available
        }

        // Tạo content panel
        JPanel contentPanel = new JPanel(new BorderLayout(8, 8));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setContentPane(contentPanel);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        txtFile.setEditable(false);
        row1.add(new JLabel("File CSV:"));
        row1.add(txtFile);
        row1.add(btnBrowse);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        row2.add(btnPreview);
        row2.add(btnImport);
        row2.add(btnClose);

        top.add(row1, BorderLayout.NORTH);
        top.add(row2, BorderLayout.CENTER);
        contentPanel.add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Danh sách đọc được", new JScrollPane(previewTable));
        JSplitPane resultSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(successTable), new JScrollPane(errorTable));
        resultSplit.setResizeWeight(0.5);
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        resultPanel.add(resultSplit, BorderLayout.CENTER);
        tabs.addTab("Kết quả", resultPanel);

        JPanel center = new JPanel(new BorderLayout());
        center.add(tabs, BorderLayout.CENTER);
        taLog.setEditable(false);
        center.add(new JScrollPane(taLog), BorderLayout.SOUTH);
        contentPanel.add(center, BorderLayout.CENTER);

        btnBrowse.addActionListener(e -> onBrowse());
        btnPreview.addActionListener(e -> onPreview());
        btnImport.addActionListener(e -> onImport());
        btnClose.addActionListener(e -> closeWindow());
    }

    private void onBrowse() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file CSV đăng ký cá nhân");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV files", "csv"));
        int r = chooser.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            txtFile.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void onImport() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        if (idGiai <= 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
            return;
        }
        String path = txtFile.getText();
        if (path == null || path.isBlank()) {
            JOptionPane.showMessageDialog(this, "Chưa chọn file CSV.");
            return;
        }
        java.io.File file = new java.io.File(path);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "File không tồn tại.");
            return;
        }

        successModel.setRowCount(0);
        errorModel.setRowCount(0);
        System.out.println("id giải:" + idGiai);
        ImportResult result = performImport(idGiai, file);
        taLog.append(String.format(
                "\nNhập xong: %d đăng ký mới, %d trùng, %d bỏ qua, %d lỗi, tạo %d VĐV mới, %d CLB mới.\n",
                result.ok, result.duplicates, result.skipped, result.bad, result.createdPlayers, result.createdClubs));
        if (onDone != null) {
            onDone.run();
        }
    }

    private void onPreview() {
        String path = txtFile.getText();
        if (path == null || path.isBlank()) {
            JOptionPane.showMessageDialog(this, "Chưa chọn file CSV.");
            return;
        }
        java.io.File file = new java.io.File(path);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "File không tồn tại.");
            return;
        }

        previewModel.setRowCount(0);
        try (BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String firstLine = stripBOM(br.readLine());
            if (firstLine == null)
                return;
            char sep = detectSeparator(firstLine);
            java.util.List<String> cols = parseCsvLine(firstLine, sep);
            if (!cols.isEmpty())
                cols.set(0, stripBOM(cols.get(0)));

            int lineNo = 1;
            boolean header = isHeaderRow(cols);
            if (!header) {
                previewModel.addRow(new Object[] { lineNo, getCell(cols, 2), getCell(cols, 0),
                        getCell(cols, 5), getCell(cols, 8), getCell(cols, 9), "" });
            }

            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = stripBOM(line);
                if (line.trim().isEmpty())
                    continue;
                java.util.List<String> cells = parseCsvLine(line, sep);
                if (!cells.isEmpty())
                    cells.set(0, stripBOM(cells.get(0)));
                previewModel.addRow(new Object[] { lineNo, getCell(cells, 2), getCell(cells, 0),
                        getCell(cells, 5), getCell(cells, 8), getCell(cells, 9), "" });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không đọc được file: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private ImportResult performImport(int idGiai, java.io.File file) {
        ImportResult stats = new ImportResult();
        int lineNo = 0;
        boolean originalAutoCommit = true;
        try (BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            // Prepare caches to reduce DB roundtrips
            java.util.Map<String, Integer> clubIdByName = new java.util.HashMap<>();
            for (var c : clbService.findAll()) {
                if (c.getTenClb() != null)
                    clubIdByName.put(c.getTenClb().trim().toUpperCase(), c.getId());
            }
            java.util.List<com.example.btms.model.player.VanDongVien> allVdv = vdvService.findAll();
            java.util.List<com.example.btms.model.category.NoiDung> allNd = noiDungService.getAllNoiDung();

            // transaction (best effort)
            try {
                if (conn != null) {
                    originalAutoCommit = conn.getAutoCommit();
                    conn.setAutoCommit(false);
                }
            } catch (SQLException ignore) {
            }

            String firstLine = stripBOM(br.readLine());
            lineNo = 1;
            if (firstLine == null)
                return stats;
            char sep = detectSeparator(firstLine);
            java.util.List<String> cols = parseCsvLine(firstLine, sep);
            if (!cols.isEmpty())
                cols.set(0, stripBOM(cols.get(0)));

            boolean header = isHeaderRow(cols);
            if (!header) {
                // process first data row
                processRow(idGiai, cols, lineNo, stats, clubIdByName, allVdv, allNd);
            }

            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = stripBOM(line);
                if (line.trim().isEmpty())
                    continue;
                java.util.List<String> cells = parseCsvLine(line, sep);
                if (!cells.isEmpty())
                    cells.set(0, stripBOM(cells.get(0)));
                processRow(idGiai, cells, lineNo, stats, clubIdByName, allVdv, allNd);
            }

            try {
                if (conn != null)
                    conn.commit();
            } catch (SQLException ignore) {
            }
        } catch (IOException | SQLException ex) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ignore) {
            }
            JOptionPane.showMessageDialog(this, "Lỗi dòng " + lineNo + ": " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (conn != null)
                    conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException ignore) {
            }
        }
        return stats;
    }

    private boolean isHeaderRow(java.util.List<String> cols) {
        if (cols == null || cols.isEmpty())
            return false;
        String c0 = up(getCell(cols, 0));
        String c2 = up(getCell(cols, 2));
        String c4 = up(getCell(cols, 4));
        String c5 = up(getCell(cols, 5));
        String c6 = up(getCell(cols, 6));
        String c8 = up(getCell(cols, 8));
        String c9 = up(getCell(cols, 9));

        boolean nameHdr = containsAny(c2, "HỌ", "HO ", "HO TEN", "HỌ TÊN", "HỌ VÀ TÊN", "HO VA TEN", "TÊN");
        boolean noiDungHdr = containsAny(c5, "NOI DUNG", "NỘI DUNG", "ND");
        boolean gioiTinhHdr = containsAny(c4, "GIOI TINH", "GIỚI TÍNH") || containsAny(c6, "GIOI TINH", "GIỚI TÍNH");
        boolean tuoiHdr = containsAny(c8, "TUOI", "TUỔI") || containsAny(c9, "TUOI", "TUỔI");
        boolean clubHdr = equalsAny(c0, "CLB", "CÂU LẠC BỘ", "CAU LAC BO", "TEN CLB", "TÊN CLB");

        int score = 0;
        if (nameHdr)
            score++;
        if (noiDungHdr)
            score++;
        if (gioiTinhHdr)
            score++;
        if (tuoiHdr)
            score++;
        if (score >= 1 && clubHdr)
            return true;
        return score >= 2;
    }

    private void processRow(
            int idGiai,
            java.util.List<String> cells,
            int lineNo,
            ImportResult stats,
            java.util.Map<String, Integer> clubIdByName,
            java.util.List<com.example.btms.model.player.VanDongVien> allVdv,
            java.util.List<com.example.btms.model.category.NoiDung> allNd) {
        try {
            String clbName = safe(getCell(cells, 0));
            String clbShort = safe(getCell(cells, 1));
            String hoTen = safe(getCell(cells, 2));
            String ngaySinhStr = safe(getCell(cells, 3));
            String gioiTinhVdv = normalizeGender(safe(getCell(cells, 4)));
            String noiDungTen = safe(getCell(cells, 5));
            String gioiTinhNd = normalizeGender(safe(getCell(cells, 6)));
            // col 7 reserved for team flag in some templates; here we import Singles only
            Integer tuoiMin = parseIntOrNull(getCell(cells, 8));
            Integer tuoiMax = parseIntOrNull(getCell(cells, 9));

            if (hoTen.isBlank()) {
                stats.bad++;
                errorModel.addRow(new Object[] { lineNo, hoTen, clbName, noiDungTen, "Thiếu Họ tên" });
                return;
            }
            if (noiDungTen.isBlank()) {
                stats.bad++;
                errorModel.addRow(new Object[] { lineNo, hoTen, clbName, "", "Thiếu Nội dung" });
                return;
            }

            // 1) Club
            Integer idClb = null;
            if (!clbName.isBlank()) {
                Integer cached = clubIdByName.get(clbName.toUpperCase());
                if (cached != null) {
                    idClb = cached;
                } else {
                    // try find in DB by exact name (case-insensitive) using service list
                    for (var c : clbService.findAll()) {
                        if (c.getTenClb() != null && c.getTenClb().trim().equalsIgnoreCase(clbName)) {
                            idClb = c.getId();
                            clubIdByName.put(clbName.toUpperCase(), idClb);
                            break;
                        }
                    }
                    if (idClb == null) {
                        var newClb = clbService.create(clbName, clbShort.isBlank() ? null : clbShort);
                        idClb = newClb.getId();
                        clubIdByName.put(clbName.toUpperCase(), idClb);
                        stats.createdClubs++;
                    }
                }
            }

            // 2) Player (find by name + same club + date of birth if provided)
            java.time.LocalDate dob = parseDateFlexible(ngaySinhStr);
            com.example.btms.model.player.VanDongVien found = null;
            for (var v : allVdv) {
                boolean nameMatch = v.getHoTen() != null && v.getHoTen().trim().equalsIgnoreCase(hoTen);
                boolean clubMatch = (idClb == null && v.getIdClb() == null)
                        || (idClb != null && idClb.equals(v.getIdClb()));
                boolean dobMatch = (dob == null && v.getNgaySinh() == null)
                        || (dob != null && dob.equals(v.getNgaySinh()));
                if (nameMatch && clubMatch && dobMatch) {
                    found = v;
                    break;
                }
            }
            if (found == null) {
                found = vdvService.create(hoTen, dob, idClb, (gioiTinhVdv != null ? gioiTinhVdv : "O"));
                allVdv.add(found);
                stats.createdPlayers++;
            }

            // 3) Nội dung (Singles only)
            com.example.btms.model.category.NoiDung nd = null;
            for (var n : allNd) {
                if (Boolean.TRUE.equals(n.getTeam()))
                    continue; // singles only here
                if (n.getTenNoiDung() != null && n.getTenNoiDung().trim().equalsIgnoreCase(noiDungTen)) {
                    nd = n;
                    break;
                }
            }
            if (nd == null) {
                com.example.btms.model.category.NoiDung newNd = new com.example.btms.model.category.NoiDung();
                newNd.setTenNoiDung(noiDungTen);
                newNd.setTuoiDuoi(tuoiMin != null ? tuoiMin : 0);
                newNd.setTuoiTren(tuoiMax != null ? tuoiMax : 200);
                newNd.setGioiTinh((gioiTinhNd != null ? gioiTinhNd : "O"));
                newNd.setTeam(false);
                newNd = noiDungService.createNoiDung(newNd);
                allNd.add(newNd);
                nd = newNd;
            }

            // 4) Link nội dung to tournament if not yet
            var ctRepo = new com.example.btms.repository.cateoftuornament.ChiTietGiaiDauRepository(conn);
            var existedLink = ctRepo.getChiTietGiaiDauById(idGiai, nd.getId());
            if (existedLink == null) {
                int ageMin = nz(tuoiMin, nz(nd.getTuoiDuoi(), 0));
                int ageMax = nz(tuoiMax, nz(nd.getTuoiTren(), 200));
                ctRepo.addChiTietGiaiDau(new com.example.btms.model.cateoftuornament.ChiTietGiaiDau(
                        idGiai, nd.getId(), ageMin, ageMax));
            }

            // 5) Register Singles
            boolean exists = dkService.exists(idGiai, nd.getId(), found.getId());
            if (exists) {
                stats.duplicates++;
                successModel.addRow(new Object[] { lineNo, hoTen, clbName, noiDungTen, "Trùng - đã có" });
            } else {
                dkService.register(idGiai, nd.getId(), found.getId());
                stats.ok++;
                successModel.addRow(new Object[] { lineNo, hoTen, clbName, noiDungTen, "OK" });
            }
        } catch (RuntimeException | SQLException ex) {
            stats.bad++;
            String hoTen = safe(getCell(cells, 2));
            String clbName = safe(getCell(cells, 0));
            String ndTen = safe(getCell(cells, 5));
            errorModel.addRow(new Object[] { lineNo, hoTen, clbName, ndTen, ex.getMessage() });
        }
    }

    // ===== utilities for import =====
    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        if (t.isEmpty())
            return null;
        try {
            return Integer.valueOf(t);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    private static java.time.LocalDate parseDateFlexible(String s) {
        if (s == null || s.isBlank())
            return null;
        String t = s.trim();
        java.time.format.DateTimeFormatter[] fmts = new java.time.format.DateTimeFormatter[] {
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy"),
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")
        };
        for (var f : fmts) {
            try {
                return java.time.LocalDate.parse(t, f);
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    private static String normalizeGender(String g) {
        if (g == null)
            return null;
        String t = g.trim().toUpperCase();
        if (t.startsWith("M") || t.startsWith("NAM"))
            return "M";
        if (t.startsWith("F") || t.startsWith("NỮ") || t.startsWith("NU"))
            return "F";
        if (t.startsWith("O"))
            return "O";
        return null; // unknown
    }

    private static String up(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }

    private static boolean containsAny(String s, String... tokens) {
        if (s == null || s.isEmpty())
            return false;
        for (String t : tokens) {
            if (t != null && !t.isEmpty() && s.contains(t))
                return true;
        }
        return false;
    }

    private static boolean equalsAny(String s, String... tokens) {
        if (s == null)
            return false;
        for (String t : tokens) {
            if (t != null && s.equals(t))
                return true;
        }
        return false;
    }

    private static int nz(Integer v, int def) {
        return v != null ? v : def;
    }

    // ================== Helper methods ==================
    private static String stripBOM(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') {
            return s.substring(1);
        }
        return s;
    }

    private static String getCell(java.util.List<String> cells, int idx) {
        if (cells == null || idx < 0 || idx >= cells.size())
            return null;
        return cells.get(idx) != null ? cells.get(idx).trim() : null;
    }

    private static char detectSeparator(String header) {
        int c = header != null ? header.split(",", -1).length : 0;
        int s = header != null ? header.split(";", -1).length : 0;
        return (s > c) ? ';' : ',';
    }

    private static java.util.List<String> parseCsvLine(String line, char sep) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (line == null)
            return out;
        StringBuilder sb = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQ && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQ = !inQ;
                }
            } else if (ch == sep && !inQ) {
                out.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }
        out.add(sb.toString().trim());
        for (int i = 0; i < out.size(); i++) {
            String v = out.get(i);
            if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\""))
                out.set(i, v.substring(1, v.length() - 1));
        }
        return out;
    }

    private void closeWindow() {
        // Gọi onDone callback trước khi đóng
        if (onDone != null) {
            onDone.run();
        }
        dispose();
    }

    private static class ImportResult {
        int ok, skipped, createdPlayers, createdClubs, duplicates, bad;
    }
}
