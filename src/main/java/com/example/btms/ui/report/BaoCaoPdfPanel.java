package com.example.btms.ui.report;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.sql.Connection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.example.btms.config.Prefs;
import com.example.btms.repository.club.CauLacBoRepository;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.ui.bracket.SoDoThiDauPanel;
import com.example.btms.ui.result.TongSapHuyChuongPanel;
import com.example.btms.util.report.RegistrationPdfExporter;

/**
 * Trang "Báo cáo (PDF)" – gom tất cả tính năng xuất PDF về một nơi.
 * - Đăng ký (tất cả/theo CLB/theo nội dung)
 * - Sơ đồ thi đấu (1 file tất cả nội dung / mỗi nội dung 1 file)
 * - Huy chương (tận dụng panel hiện có để xuất)
 */
public class BaoCaoPdfPanel extends JPanel {
    private final Connection conn;

    public BaoCaoPdfPanel(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel();
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        content.add(buildHeader());
        content.add(buildRegistrationExport());
        content.add(buildBracketExport());
        content.add(buildMedalExport());

        add(new JScrollPane(content), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel title = new JLabel("BÁO CÁO (PDF)");
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        JLabel hint = new JLabel("Tổng hợp các chức năng xuất PDF theo giải đang chọn");
        hint.setForeground(new Color(100, 100, 100));
        p.add(title, BorderLayout.NORTH);
        p.add(hint, BorderLayout.SOUTH);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return p;
    }

    private JPanel buildRegistrationExport() {
        JPanel box = new JPanel();
        box.setLayout(new BorderLayout());
        box.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Danh sách đăng ký (PDF)", TitledBorder.LEADING, TitledBorder.TOP));

        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton btnAll = new JButton("Tất cả");
        JButton btnByClub = new JButton("Theo CLB");
        JButton btnByContent = new JButton("Theo nội dung");
        line.add(new JLabel("Xuất danh sách đăng ký: "));
        line.add(btnAll);
        line.add(btnByClub);
        line.add(btnByContent);

        btnAll.addActionListener(e -> doExportRegistrations(Mode.ALL));
        btnByClub.addActionListener(e -> doExportRegistrations(Mode.BY_CLUB));
        btnByContent.addActionListener(e -> doExportRegistrations(Mode.BY_CONTENT));

        box.add(line, BorderLayout.CENTER);
        return box;
    }

    private JPanel buildBracketExport() {
        JPanel box = new JPanel();
        box.setLayout(new BorderLayout());
        box.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Sơ đồ thi đấu (PDF)", TitledBorder.LEADING, TitledBorder.TOP));

        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton btnOneFile = new JButton("Xuất 1 file (tất cả nội dung)");
        JButton btnEach = new JButton("Mỗi nội dung 1 file");
        line.add(new JLabel("Xuất sơ đồ thi đấu: "));
        line.add(btnOneFile);
        line.add(btnEach);

        btnOneFile.addActionListener(e -> doExportBracketsSingleFile());
        btnEach.addActionListener(e -> doExportBracketsEachFile());

        box.add(line, BorderLayout.CENTER);
        return box;
    }

    private JPanel buildMedalExport() {
        JPanel box = new JPanel();
        box.setLayout(new BorderLayout());
        box.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Báo cáo huy chương (PDF)", TitledBorder.LEADING, TitledBorder.TOP));

        // Tận dụng panel hiện có – chứa các nút Xuất PDF (Tổng sắp, Danh sách, Tổng
        // hợp)
        try {
            CauLacBoService clbService = new CauLacBoService(new CauLacBoRepository(conn));
            TongSapHuyChuongPanel medalPanel = new TongSapHuyChuongPanel(conn, clbService);
            medalPanel.setPreferredSize(new Dimension(200, 320));
            box.add(medalPanel, BorderLayout.CENTER);
        } catch (Exception ex) {
            JPanel err = new JPanel(new FlowLayout(FlowLayout.LEFT));
            err.add(new JLabel("Không thể khởi tạo panel huy chương: " + ex.getMessage()));
            box.add(err, BorderLayout.CENTER);
        }
        return box;
    }

    private enum Mode {
        ALL, BY_CLUB, BY_CONTENT
    }

    private void doExportRegistrations(Mode mode) {
        try {
            ensureTournamentSelected();
            int idGiai = new Prefs().getInt("selectedGiaiDauId", -1);
            String giaiName = new Prefs().get("selectedGiaiDauName", "Giải đấu");
            if (idGiai <= 0)
                throw new IllegalStateException("Chưa chọn giải");

            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("PDF", "pdf"));
            String base = switch (mode) {
                case ALL -> "dangky_all";
                case BY_CLUB -> "dangky_theo-clb";
                case BY_CONTENT -> "dangky_theo-noidung";
            };
            fc.setSelectedFile(new File(base + ".pdf"));
            int r = fc.showSaveDialog(this);
            if (r != JFileChooser.APPROVE_OPTION)
                return;
            File f = fc.getSelectedFile();
            if (f == null)
                return;
            if (!f.getName().toLowerCase().endsWith(".pdf")) {
                f = new File(f.getAbsolutePath() + ".pdf");
            }

            switch (mode) {
                case ALL -> RegistrationPdfExporter.exportAll(conn, idGiai, f, giaiName);
                case BY_CLUB -> RegistrationPdfExporter.exportByClub(conn, idGiai, f, giaiName);
                case BY_CONTENT -> RegistrationPdfExporter.exportByNoiDung(conn, idGiai, f, giaiName);
            }
            JOptionPane.showMessageDialog(this, "Đã xuất: " + f.getAbsolutePath(), "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi xuất PDF: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doExportBracketsSingleFile() {
        try {
            ensureTournamentSelected();
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Xuất PDF sơ đồ (tất cả nội dung)");
            fc.setFileFilter(new FileNameExtensionFilter("PDF", "pdf"));
            String tour = new Prefs().get("selectedGiaiDauName", "giai-dau");
            String safe = normalizeFileNameUnderscore(tour);
            fc.setSelectedFile(new File(safe + "_so_do_thi_dau.pdf"));
            int r = fc.showSaveDialog(this);
            if (r != JFileChooser.APPROVE_OPTION)
                return;
            File f = fc.getSelectedFile();
            if (f == null)
                return;
            if (!f.getName().toLowerCase().endsWith(".pdf")) {
                f = new File(f.getAbsolutePath() + ".pdf");
            }
            SoDoThiDauPanel p = new SoDoThiDauPanel(conn);
            boolean ok = p.exportAllBracketsToSinglePdf(f);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã xuất: " + f.getAbsolutePath(), "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xuất PDF.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doExportBracketsEachFile() {
        try {
            ensureTournamentSelected();
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Chọn thư mục để lưu các PDF sơ đồ");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            fc.setSelectedFile(new File(System.getProperty("user.home", ".")));
            int r = fc.showSaveDialog(this);
            if (r != JFileChooser.APPROVE_OPTION)
                return;
            File dir = fc.getSelectedFile();
            if (dir == null)
                return;
            SoDoThiDauPanel p = new SoDoThiDauPanel(conn);
            int created = p.exportEachBracketToDirectory(dir);
            if (created > 0)
                JOptionPane.showMessageDialog(this, "Đã xuất " + created + " file vào: " + dir.getAbsolutePath(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            else
                JOptionPane.showMessageDialog(this, "Không có nội dung để xuất.", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ensureTournamentSelected() {
        int id = new Prefs().getInt("selectedGiaiDauId", -1);
        if (id <= 0)
            throw new IllegalStateException("Chưa chọn giải");
    }

    private static String normalizeFileNameUnderscore(String s) {
        if (s == null)
            return "giai-dau";
        String x = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-zA-Z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("(^_|_$)", "")
                .toLowerCase();
        if (x.isBlank())
            return "giai-dau";
        return x;
    }
}
