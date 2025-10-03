package com.example.btms.ui.player;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.example.btms.config.Prefs;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.cateoftuornament.ChiTietGiaiDau;
import com.example.btms.repository.cateoftuornament.ChiTietGiaiDauRepository;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.cateoftuornament.ChiTietGiaiDauService;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.player.DangKiCaNhanService;
import com.example.btms.service.player.VanDongVienService;

/**
 * Panel riêng cho chức năng nhập CSV đăng ký cá nhân.
 */
public class DangKyCaNhanImportPanel extends JPanel {
    @SuppressWarnings("unused")
    private final Connection conn;
    private final Prefs prefs;
    private final NoiDungService noiDungService;
    private final DangKiCaNhanService dkService;
    private final VanDongVienService vdvService;
    private final CauLacBoService clbService;
    private final Runnable onDone;

    private final JTextField txtFile = new JTextField(36);
    private final JButton btnBrowse = new JButton("Chọn CSV...");
    private final JButton btnPreview = new JButton("Xem trước");
    private final JButton btnImport = new JButton("Nhập");
    private final JButton btnClose = new JButton("Đóng");
    private final JTextArea taLog = new JTextArea(8, 60);

    // Preview & results tables
    private final DefaultTableModel previewModel = new DefaultTableModel(
            new Object[] { "Dòng", "Họ tên", "CLB", "Nội dung (file)", "TuổiMin", "TuổiMax", "Ghi chú" }, 0) {
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

        setLayout(new BorderLayout(8, 8));

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

        add(top, BorderLayout.NORTH);

        // Tabs center: Preview + Results (success + errors)
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
        add(center, BorderLayout.CENTER);

        btnBrowse.addActionListener(e -> onBrowse());
        btnPreview.addActionListener(e -> onPreview());
        btnImport.addActionListener(e -> onImport());
        btnClose.addActionListener(e -> closeDialog());

        // No default nội dung combobox anymore
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

        // clear result tables
        successModel.setRowCount(0);
        errorModel.setRowCount(0);
        ImportResult result = performImport(idGiai, file);
        taLog.append(String.format(
                "\nNhập xong: %d đăng ký mới, %d trùng, %d bỏ qua, %d lỗi, tạo %d VĐV mới, %d CLB mới.\n",
                result.ok, result.duplicates, result.skipped, result.bad, result.createdPlayers, result.createdClubs));
        onDone.run();
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

        // Build caches for ND matching
        java.util.Map<String, NoiDung> ndByName = new java.util.HashMap<>();
        java.util.List<NoiDung> singlesList = new java.util.ArrayList<>();
        try {
            for (NoiDung nd : noiDungService.getAllNoiDung()) {
                if (nd != null && nd.getTenNoiDung() != null && !Boolean.TRUE.equals(nd.getTeam())) {
                    ndByName.put(normalize(nd.getTenNoiDung()), nd);
                    singlesList.add(nd);
                }
            }
        } catch (java.sql.SQLException | RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nội dung: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        previewModel.setRowCount(0);
        try (BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String header = br.readLine();
            int lineNo = 1;
            if (header == null)
                return;
            char sep = detectSeparator(header);
            java.util.List<String> cols = parseCsvLine(header, sep);
            java.util.Map<String, Integer> idx = new java.util.HashMap<>();
            for (int i = 0; i < cols.size(); i++)
                idx.put(normalize(cols.get(i)), i);

            Integer iName = firstIdx(idx, "vdv", "hoten", "ho ten", "name", "ho va ten");
            Integer iClb = firstIdx(idx, "clb", "cau lac bo", "caulacbo", "club");
            // (DOB not used in preview)
            Integer iNd = firstIdx(idx, "noidung", "category", "noi dung", "noi dung thi dau");
            Integer iNdGender = firstIdx(idx, "gioi tinh noi dung", "gioi tinh nd", "gender noi dung", "gender nd");
            Integer iAgeMin = firstIdx(idx, "tuoi min", "tuoi duoi", "age min", "min age");
            Integer iAgeMax = firstIdx(idx, "tuoi max", "tuoi tren", "age max", "max age");

            boolean looksLikeHeader = (iName != null) || (iClb != null) || (iNd != null) || (iAgeMin != null)
                    || (iAgeMax != null);
            if (!looksLikeHeader) {
                int size = cols.size();
                iClb = (size > 0) ? 0 : null;
                // skip short club in preview columns
                iName = (size > 2) ? 2 : null;
                // skip DOB column in preview mapping (index 3)
                iNd = (size > 5) ? 5 : null;
                iNdGender = (size > 6) ? 6 : null;
                iAgeMin = (size > 8) ? 8 : null;
                iAgeMax = (size > 9) ? 9 : null;

                // treat the first line as data
                String name0 = getCell(cols, iName);
                if (name0 != null && !name0.isBlank()) {
                    String clb0 = getCell(cols, iClb);
                    String ndText0 = getCell(cols, iNd);
                    Integer min0 = parseIntSafe(iAgeMin != null ? getCell(cols, iAgeMin) : null);
                    Integer max0 = parseIntSafe(iAgeMax != null ? getCell(cols, iAgeMax) : null);
                    String note0 = buildNoteForPreview(ndByName, singlesList, ndText0,
                            iNdGender != null ? getCell(cols, iNdGender) : null, min0, max0);
                    previewModel.addRow(new Object[] { lineNo, name0, clb0, ndText0, min0, max0, note0 });
                }
            }

            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty())
                    continue;
                java.util.List<String> cells = parseCsvLine(line, sep);
                String name = getCell(cells, iName);
                if (name == null || name.isBlank())
                    continue;
                String clb = getCell(cells, iClb);
                String ndText = getCell(cells, iNd);
                Integer min = parseIntSafe(iAgeMin != null ? getCell(cells, iAgeMin) : null);
                Integer max = parseIntSafe(iAgeMax != null ? getCell(cells, iAgeMax) : null);
                String note = buildNoteForPreview(ndByName, singlesList, ndText,
                        iNdGender != null ? getCell(cells, iNdGender) : null, min, max);
                previewModel.addRow(new Object[] { lineNo, name, clb, ndText, min, max, note });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không đọc được file: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildNoteForPreview(java.util.Map<String, NoiDung> ndByName, java.util.List<NoiDung> singlesList,
            String ndText, String ndGenderText, Integer minAge, Integer maxAge) {
        NoiDung nd = null;
        if (ndText != null && !ndText.isBlank())
            nd = ndByName.get(normalize(ndText));
        if (nd == null && (ndGenderText != null || minAge != null || maxAge != null)) {
            String ndGender = parseGenderSafe(ndGenderText);
            java.util.List<NoiDung> candidates = new java.util.ArrayList<>();
            for (NoiDung cand : singlesList) {
                if (ndGender != null) {
                    String g = cand.getGioiTinh();
                    if (g != null && !g.isBlank()) {
                        if (!g.substring(0, 1).equalsIgnoreCase(ndGender.substring(0, 1)))
                            continue;
                    }
                }
                if (minAge != null && cand.getTuoiDuoi() != null && !cand.getTuoiDuoi().equals(minAge))
                    continue;
                if (maxAge != null && cand.getTuoiTren() != null && !cand.getTuoiTren().equals(maxAge))
                    continue;
                candidates.add(cand);
            }
            if (candidates.size() == 1)
                nd = candidates.get(0);
        }
        if (nd != null)
            return "OK: " + nd.getTenNoiDung();
        // Không dùng ND mặc định nữa; thông báo dự định tạo hoặc thiếu thông tin
        if ((ndText != null && !ndText.isBlank()) || ndGenderText != null || minAge != null || maxAge != null)
            return "Sẽ tạo ND mới";
        return "Thiếu thông tin nội dung";
    }

    private void closeDialog() {
        java.awt.Container p = getParent();
        while (p != null && !(p instanceof JDialog))
            p = p.getParent();
        if (p instanceof JDialog d)
            d.dispose();
    }

    private static class ImportResult {
        int ok, skipped, createdPlayers, createdClubs, duplicates, bad;
    }

    private ImportResult performImport(int idGiai, java.io.File file) {
        ImportResult stats = new ImportResult();
        // Chuẩn bị cache: nội dung theo tên + list đơn; CLB theo tên; VĐV theo
        // (tên|idCLB)
        java.util.Map<String, NoiDung> ndByName = new java.util.HashMap<>();
        java.util.List<NoiDung> singlesList = new java.util.ArrayList<>();
        try {
            for (NoiDung nd : noiDungService.getAllNoiDung()) {
                if (nd != null && nd.getTenNoiDung() != null && !Boolean.TRUE.equals(nd.getTeam())) {
                    ndByName.put(normalize(nd.getTenNoiDung()), nd);
                    singlesList.add(nd);
                }
            }
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi SQL khi tải nội dung: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return stats;
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nội dung: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return stats;
        }

        java.util.Map<String, Integer> clbByName = new java.util.HashMap<>();
        clbService.findAll().forEach(c -> {
            if (c != null && c.getTenClb() != null)
                clbByName.put(normalize(c.getTenClb()), c.getId());
        });
        java.util.Map<String, Integer> vdvByKey = new java.util.HashMap<>(); // key = name|clbId
        vdvService.findAll().forEach(v -> {
            String clubKey = (v.getIdClb() != null) ? String.valueOf(v.getIdClb()) : "0";
            vdvByKey.put(normalize(v.getHoTen()) + "|" + clubKey, v.getId());
        });

        int lineNo = 0;
        try (BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String header = br.readLine();
            lineNo = 1;
            if (header == null)
                return stats;
            char sep = detectSeparator(header);
            java.util.List<String> cols = parseCsvLine(header, sep);
            java.util.Map<String, Integer> idx = new java.util.HashMap<>();
            for (int i = 0; i < cols.size(); i++)
                idx.put(normalize(cols.get(i)), i);

            Integer iName = firstIdx(idx, "vdv", "hoten", "ho ten", "name", "ho va ten");
            Integer iClb = firstIdx(idx, "clb", "cau lac bo", "caulacbo", "club");
            Integer iClbShort = firstIdx(idx, "clb ngan", "ten ngan", "ten ngan clb", "clb viet tat", "viet tat clb");
            Integer iDob = firstIdx(idx, "ngaysinh", "dob", "birth", "birthdate", "ngay thang nam sinh");
            Integer iGender = firstIdx(idx, "gioitinh", "gender", "sex");
            Integer iNd = firstIdx(idx, "noidung", "category", "noi dung", "noi dung thi dau");
            Integer iNdGender = firstIdx(idx, "gioi tinh noi dung", "gioi tinh nd", "gender noi dung", "gender nd");
            Integer iAgeMin = firstIdx(idx, "tuoi min", "tuoi duoi", "age min", "min age");
            Integer iAgeMax = firstIdx(idx, "tuoi max", "tuoi tren", "age max", "max age");

            // Heuristic: if we couldn't detect any header-like column, treat the first line
            // as data with the fixed order:
            // 0: CLB; 1: CLB NGẮN; 2: HỌ VÀ TÊN; 3: NGÀY THÁNG NĂM SINH; 4: GIỚI TÍNH;
            // 5: NỘI DUNG THI ĐẤU; 6: GIỚI TÍNH NỘI DUNG; 7: NỘI DUNG ĐỒNG ĐỘI; 8: TUỔI
            // MIN; 9: TUỔI MAX
            boolean looksLikeHeader = (iName != null) || (iClb != null) || (iNd != null) || (iAgeMin != null)
                    || (iAgeMax != null) || (iDob != null) || (iGender != null);
            boolean processFirstAsData = false;
            if (!looksLikeHeader) {
                // map by index if present
                int size = cols.size();
                iClb = (size > 0) ? 0 : null;
                iClbShort = (size > 1) ? 1 : null;
                iName = (size > 2) ? 2 : null;
                iDob = (size > 3) ? 3 : null;
                iGender = (size > 4) ? 4 : null;
                iNd = (size > 5) ? 5 : null;
                iNdGender = (size > 6) ? 6 : null;
                // index 7 is team event name (ignored for đơn)
                iAgeMin = (size > 8) ? 8 : null;
                iAgeMax = (size > 9) ? 9 : null;
                processFirstAsData = (iName != null);
            }

            if (processFirstAsData) {
                // Xử lý ngay dòng đầu như dữ liệu
                java.util.List<String> cells = cols;
                String name = getCell(cells, iName);
                if (name != null && !name.isBlank()) {
                    String ndName = iNd != null ? getCell(cells, iNd) : null;
                    NoiDung nd = null;
                    if (ndName != null && !ndName.isBlank())
                        nd = ndByName.get(normalize(ndName));
                    if (nd == null && (iNdGender != null || iAgeMin != null || iAgeMax != null)) {
                        String ndGenderText = iNdGender != null ? getCell(cells, iNdGender) : null;
                        String ndGender = parseGenderSafe(ndGenderText);
                        Integer minAge = parseIntSafe(iAgeMin != null ? getCell(cells, iAgeMin) : null);
                        Integer maxAge = parseIntSafe(iAgeMax != null ? getCell(cells, iAgeMax) : null);
                        java.util.List<NoiDung> candidates = new java.util.ArrayList<>();
                        for (NoiDung cand : singlesList) {
                            if (ndGender != null) {
                                String g = cand.getGioiTinh();
                                if (g != null && !g.isBlank()) {
                                    if (!g.substring(0, 1).equalsIgnoreCase(ndGender.substring(0, 1)))
                                        continue;
                                }
                            }
                            if (minAge != null && cand.getTuoiDuoi() != null && !cand.getTuoiDuoi().equals(minAge))
                                continue;
                            if (maxAge != null && cand.getTuoiTren() != null && !cand.getTuoiTren().equals(maxAge))
                                continue;
                            candidates.add(cand);
                        }
                        if (candidates.size() == 1)
                            nd = candidates.get(0);
                    }
                    if (nd == null) {
                        // Tạo ND mới nếu có đủ thông tin
                        NoiDung created = tryCreateNoiDungAndLinkToTournament(idGiai,
                                ndName, iNdGender != null ? getCell(cells, iNdGender) : null,
                                parseIntSafe(iAgeMin != null ? getCell(cells, iAgeMin) : null),
                                parseIntSafe(iAgeMax != null ? getCell(cells, iAgeMax) : null));
                        if (created != null) {
                            nd = created;
                            ndByName.put(normalize(created.getTenNoiDung()), created);
                            singlesList.add(created);
                        }
                    }
                    // Không dùng ND mặc định: nếu vẫn không có ND thì bỏ qua với lỗi
                    if (nd != null && !Boolean.TRUE.equals(nd.getTeam())) {
                        // Đảm bảo ND đã gắn với giải hiện tại
                        ensureNdLinkedToTournament(idGiai, nd,
                                parseIntSafe(iAgeMin != null ? getCell(cells, iAgeMin) : null),
                                parseIntSafe(iAgeMax != null ? getCell(cells, iAgeMax) : null));
                        String clbName = iClb != null ? getCell(cells, iClb) : null;
                        String clbShort = iClbShort != null ? getCell(cells, iClbShort) : null;
                        Integer idClb = null;
                        if (clbName != null && !clbName.isBlank()) {
                            String n = normalize(clbName);
                            idClb = clbByName.get(n);
                            if (idClb == null) {
                                try {
                                    var created = clbService.create(clbName.trim(),
                                            (clbShort != null && !clbShort.isBlank()) ? clbShort.trim() : null);
                                    idClb = created.getId();
                                    clbByName.put(n, idClb);
                                    stats.createdClubs++;
                                } catch (RuntimeException ignore) {
                                    /* keep null */ }
                            }
                        }

                        String key = normalize(name) + "|" + String.valueOf(idClb != null ? idClb : 0);
                        Integer idVdv = vdvByKey.get(key);
                        if (idVdv == null) {
                            java.time.LocalDate dob = parseDateSafe(iDob != null ? getCell(cells, iDob) : null);
                            String gender = parseGenderSafe(iGender != null ? getCell(cells, iGender) : null);
                            try {
                                var created = vdvService.create(name.trim(), dob, idClb, gender);
                                idVdv = created.getId();
                                vdvByKey.put(key, idVdv);
                                stats.createdPlayers++;
                            } catch (RuntimeException ex) {
                                stats.bad++;
                                // do not proceed to register
                                idVdv = null;
                            }
                        }

                        if (idVdv != null) {
                            try {
                                dkService.register(idGiai, nd.getId(), idVdv);
                                stats.ok++;
                                String clb = getCell(cells, iClb);
                                successModel.addRow(new Object[] { lineNo, name, clb, nd.getTenNoiDung(), "OK" });
                            } catch (RuntimeException ex) {
                                stats.duplicates++;
                                String clb = getCell(cells, iClb);
                                errorModel.addRow(
                                        new Object[] { lineNo, name, clb, nd.getTenNoiDung(), "Trùng đăng ký" });
                            }
                        }
                    } else {
                        stats.bad++;
                        String clb = getCell(cells, iClb);
                        errorModel.addRow(new Object[] { lineNo, name, clb, nd != null ? nd.getTenNoiDung() : "",
                                "Thiếu/không hợp lệ ND" });
                    }
                } else {
                    stats.skipped++;
                }
            }

            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty())
                    continue;
                java.util.List<String> cells = parseCsvLine(line, sep);
                String name = getCell(cells, iName);
                if (name == null || name.isBlank()) {
                    stats.skipped++;
                    continue;
                }

                String ndName = iNd != null ? getCell(cells, iNd) : null;
                NoiDung nd = null;
                if (ndName != null && !ndName.isBlank())
                    nd = ndByName.get(normalize(ndName));
                if (nd == null && (iNdGender != null || iAgeMin != null || iAgeMax != null)) {
                    String ndGenderText = iNdGender != null ? getCell(cells, iNdGender) : null;
                    String ndGender = parseGenderSafe(ndGenderText);
                    Integer minAge = parseIntSafe(iAgeMin != null ? getCell(cells, iAgeMin) : null);
                    Integer maxAge = parseIntSafe(iAgeMax != null ? getCell(cells, iAgeMax) : null);
                    java.util.List<NoiDung> candidates = new java.util.ArrayList<>();
                    for (NoiDung cand : singlesList) {
                        if (ndGender != null) {
                            String g = cand.getGioiTinh();
                            if (g != null && !g.isBlank()) {
                                if (!g.substring(0, 1).equalsIgnoreCase(ndGender.substring(0, 1)))
                                    continue;
                            }
                        }
                        if (minAge != null && cand.getTuoiDuoi() != null && !cand.getTuoiDuoi().equals(minAge))
                            continue;
                        if (maxAge != null && cand.getTuoiTren() != null && !cand.getTuoiTren().equals(maxAge))
                            continue;
                        candidates.add(cand);
                    }
                    if (candidates.size() == 1)
                        nd = candidates.get(0);
                }
                if (nd == null) {
                    // thử tạo ND mới
                    NoiDung created = tryCreateNoiDungAndLinkToTournament(idGiai,
                            ndName, iNdGender != null ? getCell(cells, iNdGender) : null,
                            parseIntSafe(iAgeMin != null ? getCell(cells, iAgeMin) : null),
                            parseIntSafe(iAgeMax != null ? getCell(cells, iAgeMax) : null));
                    if (created != null) {
                        nd = created;
                        ndByName.put(normalize(created.getTenNoiDung()), created);
                        singlesList.add(created);
                    }
                }
                if (nd == null || Boolean.TRUE.equals(nd.getTeam())) {
                    stats.bad++;
                    String clb = getCell(cells, iClb);
                    errorModel.addRow(new Object[] { lineNo, name, clb, "", "Thiếu/không hợp lệ ND" });
                    continue;
                }

                // Đảm bảo ND đã gắn với giải hiện tại
                ensureNdLinkedToTournament(idGiai, nd,
                        parseIntSafe(iAgeMin != null ? getCell(cells, iAgeMin) : null),
                        parseIntSafe(iAgeMax != null ? getCell(cells, iAgeMax) : null));

                String clbName = iClb != null ? getCell(cells, iClb) : null;
                String clbShort = iClbShort != null ? getCell(cells, iClbShort) : null;
                Integer idClb = null;
                if (clbName != null && !clbName.isBlank()) {
                    String n = normalize(clbName);
                    idClb = clbByName.get(n);
                    if (idClb == null) {
                        try {
                            var created = clbService.create(clbName.trim(),
                                    (clbShort != null && !clbShort.isBlank()) ? clbShort.trim() : null);
                            idClb = created.getId();
                            clbByName.put(n, idClb);
                            stats.createdClubs++;
                        } catch (RuntimeException ignore) {
                            /* giữ null */ }
                    }
                }

                String key = normalize(name) + "|" + String.valueOf(idClb != null ? idClb : 0);
                Integer idVdv = vdvByKey.get(key);
                if (idVdv == null) {
                    java.time.LocalDate dob = parseDateSafe(iDob != null ? getCell(cells, iDob) : null);
                    String gender = parseGenderSafe(iGender != null ? getCell(cells, iGender) : null);
                    try {
                        var created = vdvService.create(name.trim(), dob, idClb, gender);
                        idVdv = created.getId();
                        vdvByKey.put(key, idVdv);
                        stats.createdPlayers++;
                    } catch (RuntimeException ex) {
                        stats.bad++;
                        continue;
                    }
                }

                try {
                    dkService.register(idGiai, nd.getId(), idVdv);
                    stats.ok++;
                    String clb = clbName;
                    successModel.addRow(new Object[] { lineNo, name, clb, nd.getTenNoiDung(), "OK" });
                } catch (RuntimeException ex) {
                    stats.duplicates++;
                    String clb = clbName;
                    errorModel.addRow(new Object[] { lineNo, name, clb, nd.getTenNoiDung(), "Trùng đăng ký" });
                }
            }
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi dòng " + lineNo + ": " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
        return stats;
    }

    // === helpers (copy from panel cũ) ===
    private static String getCell(java.util.List<String> cells, Integer idx) {
        if (idx == null)
            return null;
        if (idx < 0 || idx >= cells.size())
            return null;
        String v = cells.get(idx);
        return v != null ? v.trim() : null;
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

    private static Integer firstIdx(java.util.Map<String, Integer> idx, String... keys) {
        for (String k : keys) {
            Integer i = idx.get(normalize(k));
            if (i != null)
                return i;
        }
        return null;
    }

    private static String normalize(String s) {
        if (s == null)
            return "";
        String x = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .trim();
        x = x.replaceAll("[\\s_]+", " ");
        return x;
    }

    private static java.time.LocalDate parseDateSafe(String text) {
        if (text == null || text.isBlank())
            return null;
        String t = text.trim();
        try {
            if (t.matches("\\d{4}-\\d{2}-\\d{2}"))
                return java.time.LocalDate.parse(t);
        } catch (Exception ignore) {
        }
        try {
            var f = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return java.time.LocalDate.parse(t, f);
        } catch (Exception ignore) {
        }
        return null;
    }

    private static String parseGenderSafe(String text) {
        if (text == null)
            return null;
        String t = normalize(text);
        if (t.startsWith("m") || t.contains("nam") || t.contains("male"))
            return "m";
        if (t.startsWith("f") || t.contains("nu") || t.contains("nư") || t.contains("female"))
            return "f";
        return null;
    }

    private static Integer parseIntSafe(String text) {
        if (text == null)
            return null;
        text = text.trim();
        if (text.isEmpty())
            return null;
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    /**
     * Cố gắng tạo "Nội dung" mới (ĐƠN) nếu chưa có và gắn với giải hiện tại.
     * Quy tắc tên: nếu ndName null/blank, tạo tên theo mẫu "Đơn {genderLabel}
     * {min}-{max}" khi có dữ liệu.
     * genderText: "m" hoặc "f" (hàm parseGenderSafe đã xử lý trước đó), có thể
     * null.
     * Trả về NoiDung đã tạo, hoặc null nếu không đủ dữ liệu để tạo.
     */
    private NoiDung tryCreateNoiDungAndLinkToTournament(int idGiai, String ndName,
            String genderText, Integer minAge, Integer maxAge) {
        try {
            String gender = parseGenderSafe(genderText);
            // Xác định tên nếu chưa có
            String name = (ndName != null && !ndName.isBlank()) ? ndName.trim() : null;
            if (name == null) {
                String genderLabel = (gender != null && gender.startsWith("m")) ? "nam"
                        : (gender != null && gender.startsWith("f")) ? "nữ" : null;
                if (genderLabel != null && minAge != null && maxAge != null) {
                    name = "Đơn " + genderLabel + " " + minAge + "-" + maxAge;
                } else if (genderLabel != null) {
                    name = "Đơn " + genderLabel;
                }
            }
            if (name == null)
                return null; // thiếu dữ liệu để tạo tên ND

            // Tạo NoiDung (ĐƠN)
            NoiDung newNd = new NoiDung(null, name, minAge != null ? minAge : 0, maxAge != null ? maxAge : 200,
                    gender, false);
            NoiDung created = noiDungService.createNoiDung(newNd);

            // Gắn vào CHI_TIET_GIAI_DAU (ưu tiên dùng tham số đầu vào)
            ChiTietGiaiDauService ctgdService = new ChiTietGiaiDauService(new ChiTietGiaiDauRepository(this.conn));
            int tuMin = (minAge != null)
                    ? minAge
                    : java.util.Optional.ofNullable(created.getTuoiDuoi()).orElse(0);
            int tuMax = (maxAge != null)
                    ? maxAge
                    : java.util.Optional.ofNullable(created.getTuoiTren()).orElse(200);
            ctgdService.create(new ChiTietGiaiDau(idGiai, created.getId(), tuMin, tuMax));
            return created;
        } catch (java.sql.SQLException | RuntimeException ex) {
            // Không tạo được, cứ trả null để luồng chính dùng ND mặc định nếu có
            return null;
        }
    }

    /**
     * Đảm bảo ND đã có bản ghi trong CHI_TIET_GIAI_DAU cho giải hiện tại; nếu chưa
     * có thì tạo.
     */
    private void ensureNdLinkedToTournament(int idGiai, NoiDung nd, Integer minAge, Integer maxAge) {
        if (nd == null || nd.getId() == null)
            return;
        try {
            ChiTietGiaiDauService ctgdService = new ChiTietGiaiDauService(new ChiTietGiaiDauRepository(this.conn));
            if (!ctgdService.exists(idGiai, nd.getId())) {
                int tuMin = (minAge != null) ? minAge
                        : (nd.getTuoiDuoi() != null ? nd.getTuoiDuoi() : 0);
                int tuMax = (maxAge != null) ? maxAge
                        : (nd.getTuoiTren() != null ? nd.getTuoiTren() : 200);
                ctgdService.create(new ChiTietGiaiDau(idGiai, nd.getId(), tuMin, tuMax));
            }
        } catch (RuntimeException ex) {
            // ignore linking failure; registration may still proceed depending on
            // constraints
        }
    }
}
