package com.example.btms.ui.result;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.example.btms.config.Prefs;
import com.example.btms.model.bracket.SoDoCaNhan;
import com.example.btms.model.bracket.SoDoDoi;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.match.ChiTietVan;
import com.example.btms.model.player.VanDongVien;
import com.example.btms.repository.bracket.SoDoCaNhanRepository;
import com.example.btms.repository.bracket.SoDoDoiRepository;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.match.ChiTietVanRepository;
import com.example.btms.repository.player.VanDongVienRepository;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.match.ChiTietVanService;
import com.example.btms.service.player.VanDongVienService;

/**
 * Panel hiển thị biên bản cho 1 trận duy nhất.
 */
public class BienBanTranPanel extends JPanel {
    private final Prefs prefs = new Prefs();
    private final NoiDungService noiDungService;
    // private final ChiTietTranDauService tranDauService; // không dùng trong bản
    // vẽ bảng
    private final ChiTietVanService vanService;
    private final SoDoCaNhanRepository soDoCaNhanRepo;
    private final SoDoDoiRepository soDoDoiRepo;
    private final VanDongVienService vdvService;

    private final JLabel lblTitle = new JLabel("Biên bản trận");
    private final JLabel lblInfo = new JLabel();
    private final JButton btnRefresh = new JButton("Làm mới");

    // Container hiển thị các bảng set (mỗi set một bảng 4x31)
    private final JPanel setsContainer = new JPanel();
    // DateTimeFormatter hiện không dùng trong chế độ bảng

    private String currentMatchId;

    public BienBanTranPanel(Connection conn, String matchId) {
        Objects.requireNonNull(conn);
        this.noiDungService = new NoiDungService(new NoiDungRepository(conn));
        // this.tranDauService = new ChiTietTranDauService(new
        // ChiTietTranDauRepository(conn));
        this.vanService = new ChiTietVanService(new ChiTietVanRepository(conn));
        this.soDoCaNhanRepo = new SoDoCaNhanRepository(conn);
        this.soDoDoiRepo = new SoDoDoiRepository(conn);
        this.vdvService = new VanDongVienService(new VanDongVienRepository(conn));

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.add(lblTitle);
        top.add(new JLabel("|"));
        top.add(lblInfo);
        btnRefresh.addActionListener(e -> reload());
        top.add(new JLabel("|"));
        top.add(btnRefresh);
        add(top, BorderLayout.NORTH);

        // Khu vực trung tâm: danh sách các bảng theo từng set
        setsContainer.setLayout(new BoxLayout(setsContainer, BoxLayout.Y_AXIS));
        add(new JScrollPane(setsContainer), BorderLayout.CENTER);

        setMatchId(matchId);
    }

    public void setMatchId(String id) {
        this.currentMatchId = id;
        reload();
    }

    public void reload() {
        if (currentMatchId == null || currentMatchId.isBlank()) {
            renderEmpty("(Chưa chọn trận)");
            return;
        }
        loadMatchDetail(currentMatchId);
    }

    private void loadMatchDetail(String idTranDau) {
        try {
            MatchSummary ms = findSummaryByMatchId(idTranDau);
            lblInfo.setText("ID: " + idTranDau
                    + (ms != null && ms.ndLabel != null && !ms.ndLabel.isBlank() ? (" | " + ms.ndLabel) : ""));

            List<ChiTietVan> sets = new ArrayList<>();
            try {
                sets = vanService.listByMatch(idTranDau);
            } catch (RuntimeException ignore) {
            }
            renderSets(sets);
        } catch (RuntimeException ex) {
            renderEmpty("Lỗi tải biên bản: " + ex.getMessage());
        }
    }

    private void renderEmpty(String message) {
        setsContainer.removeAll();
        JLabel lb = new JLabel(message);
        lb.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel p = new JPanel(new BorderLayout());
        p.add(lb, BorderLayout.CENTER);
        setsContainer.add(p);
        setsContainer.revalidate();
        setsContainer.repaint();
    }

    private void renderSets(List<ChiTietVan> sets) {
        setsContainer.removeAll();
        if (sets == null || sets.isEmpty()) {
            renderEmpty("(Chưa có dữ liệu set)");
            return;
        }
        for (ChiTietVan v : sets) {
            JPanel panel = createSetTablePanel(v);
            setsContainer.add(panel);
            setsContainer.add(Box.createVerticalStrut(8));
        }
        setsContainer.revalidate();
        setsContainer.repaint();
    }

    // Tạo bảng cho một set. Đôi: 4 hàng (2 trên P1, 2 dưới P2). Đơn: 2 hàng (P1 một
    // hàng, P2 một hàng)
    private JPanel createSetTablePanel(ChiTietVan v) {
        // Header cột 1..31
        String[] headers = new String[31];
        for (int i = 0; i < 31; i++) {
            headers[i] = String.valueOf(i + 1);
        }

        // Wrapper chính: có thể chứa nhiều bảng (mỗi bảng 31 cột)
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        String title = "Set " + v.getSetNo();
        int leftScore = 0;
        int rightScore = 0;
        try {
            String tokens = v.getDauThoiGian();
            int c1 = countTokenIgnoringSwap(tokens, "P1@");
            int c2 = countTokenIgnoringSwap(tokens, "P2@");
            leftScore = c1;
            rightScore = c2;
            if (v.getSetNo() == 2 && tokens != null && tokens.contains("SWAP@")) {
                // Với set 2, hiển thị trái/phải theo SWAP
                leftScore = c2;
                rightScore = c1;
            }
            title += " (" + leftScore + " - " + rightScore + ")";
        } catch (Exception ignore) {
        }
        wrapper.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title, TitledBorder.LEFT,
                TitledBorder.TOP));

        // Lấy thông tin tên VĐV/đội và loại nội dung (đơn/đôi)
        MatchSummary ms = findSummaryByMatchId(currentMatchId);
        boolean isSingles = ms != null && ms.isSingles;
        int rows = isSingles ? 2 : 4;
        String nameA = (ms != null && ms.nameA != null) ? ms.nameA : "P1";
        String nameB = (ms != null && ms.nameB != null) ? ms.nameB : "P2";

        // Row header labels theo loại nội dung
        String[] rowHeaders;
        if (isSingles) {
            rowHeaders = new String[] { nameA, nameB };
        } else {
            // nếu đôi, tạm thời dùng tên đội cho cả 2 hàng mỗi bên
            rowHeaders = new String[] { nameA, nameA, nameB, nameB };
        }

        // Duyệt token đầy đủ (bao gồm SWAP@) để xử lý đổi bên ở set 2
        String rawTokens = v.getDauThoiGian();
        String[] parts = (rawTokens == null) ? new String[0] : rawTokens.split(";");
        boolean hasScoreEvent = false;
        for (String r : parts) {
            String t = r.trim();
            if (t.startsWith("P1@") || t.startsWith("P2@")) {
                hasScoreEvent = true;
                break;
            }
        }

        if (!hasScoreEvent) {
            JTable emptyTable = buildEmptyTable(headers, rows);
            JScrollPane sp = wrapWithRowHeader(emptyTable, rowHeaders, isSingles);
            setPreferredSizeFor(sp, emptyTable, 36);
            wrapper.add(sp);
            return wrapper;
        }

        int p1 = 0, p2 = 0; // đếm điểm cho từng bên
        int idx = 0;
        int colWidth = 36;
        boolean swapped = false;
        final boolean allowSwap = v.getSetNo() == 2;
        // lưu lại bảng cuối để chèn tổng điểm
        DefaultTableModel lastModel = null;
        int lastCol = 0;
        while (idx < parts.length) {
            DefaultTableModel model = new DefaultTableModel(rows, 31) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            model.setColumnIdentifiers(headers);

            JTable table = new JTable(model);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setRowSelectionAllowed(false);
            table.setCellSelectionEnabled(false);
            table.setRowHeight(24);
            table.setShowGrid(true);
            table.setGridColor(Color.GRAY);
            table.setIntercellSpacing(new Dimension(1, 1));
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(colWidth);
                table.getColumnModel().getColumn(i).setMinWidth(colWidth);
            }

            int col = 0;
            while (idx < parts.length && col < 31) {
                String t = parts[idx++].trim();
                if (t.isEmpty())
                    continue;
                if (t.startsWith("SWAP@")) {
                    if (allowSwap)
                        swapped = !swapped;
                    continue; // không chiếm cột
                }
                int row = -1;
                if (t.startsWith("P1@")) {
                    p1++;
                    if (isSingles) {
                        row = swapped ? 1 : 0;
                    } else {
                        row = (!swapped ? ((p1 % 2 == 1) ? 0 : 1) : ((p1 % 2 == 1) ? 2 : 3));
                    }
                } else if (t.startsWith("P2@")) {
                    p2++;
                    if (isSingles) {
                        row = swapped ? 0 : 1;
                    } else {
                        row = (!swapped ? ((p2 % 2 == 1) ? 2 : 3) : ((p2 % 2 == 1) ? 0 : 1));
                    }
                } else {
                    continue; // token không hợp lệ
                }
                // Giá trị phải bám theo người ghi điểm (không theo vị trí hàng),
                // để sau SWAP vẫn đúng: P1 ghi thì số P1, P2 ghi thì số P2
                Integer val = t.startsWith("P1@") ? Integer.valueOf(p1) : Integer.valueOf(p2);
                model.setValueAt(val, row, col);
                col++;
            }

            // lưu lại trạng thái cuối trang hiện tại
            lastModel = model;
            lastCol = col;

            JScrollPane sp = wrapWithRowHeader(table, rowHeaders, isSingles);
            setPreferredSizeFor(sp, table, colWidth);
            wrapper.add(sp);
            wrapper.add(Box.createVerticalStrut(6));
        }

        // Sau khi điền hết token: bỏ 2 cột và ghi tổng điểm ở cột kế tiếp
        if (lastModel != null) {
            int targetCol = lastCol + 2; // chừa 2 cột trống
            if (targetCol >= 31) {
                // Không đủ chỗ: tạo trang mới và ghi tại cột thứ 3 (index 2)
                DefaultTableModel model = new DefaultTableModel(rows, 31) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                model.setColumnIdentifiers(headers);
                JTable table = new JTable(model);
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                table.setRowSelectionAllowed(false);
                table.setCellSelectionEnabled(false);
                table.setRowHeight(24);
                table.setShowGrid(true);
                table.setGridColor(Color.GRAY);
                table.setIntercellSpacing(new Dimension(1, 1));
                for (int i = 0; i < table.getColumnCount(); i++) {
                    table.getColumnModel().getColumn(i).setPreferredWidth(colWidth);
                    table.getColumnModel().getColumn(i).setMinWidth(colWidth);
                }
                JScrollPane sp = wrapWithRowHeader(table, rowHeaders, isSingles);
                setPreferredSizeFor(sp, table, colWidth);
                wrapper.add(sp);
                wrapper.add(Box.createVerticalStrut(6));
                lastModel = model;
                targetCol = 2; // để 2 cột trống đầu trang mới
            }
            // Ghi tổng theo hiển thị trái/phải để không bị đổi vị trí ở set 2
            lastModel.setValueAt("<html><b>" + leftScore + "</b></html>", 0, targetCol);
            lastModel.setValueAt("<html><b>" + rightScore + "</b></html>", rows - 1, targetCol);
        }

        return wrapper;
    }

    private static JTable buildEmptyTable(String[] headers, int rows) {
        DefaultTableModel model = new DefaultTableModel(rows, 31) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(headers);
        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setRowHeight(24);
        table.setShowGrid(true);
        table.setGridColor(Color.GRAY);
        table.setIntercellSpacing(new Dimension(1, 1));
        int colWidth = 36;
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidth);
            table.getColumnModel().getColumn(i).setMinWidth(colWidth);
        }
        return table;
    }

    private static JScrollPane wrapWithRowHeader(JTable table, String[] rowHeaders, boolean isSingles) {
        JList<String> rowHeader = new JList<>(rowHeaders);
        rowHeader.setFixedCellWidth(140);
        rowHeader.setFixedCellHeight(table.getRowHeight());
        rowHeader.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel l = new JLabel(value == null ? "" : value);
            l.setHorizontalAlignment(JLabel.CENTER);
            l.setVerticalAlignment(JLabel.CENTER);
            l.setOpaque(true);
            int bottom = 1; // giữ đường kẻ cho mọi hàng; với nội dung đơn giờ chỉ còn 2 hàng nên không cần
                            // gộp
            l.setBorder(BorderFactory.createMatteBorder(0, 0, bottom, 1, table.getGridColor()));
            l.setToolTipText(value);
            return l;
        });
        rowHeader.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane sp = new JScrollPane(table);
        sp.setRowHeaderView(rowHeader);
        return sp;
    }

    private static void setPreferredSizeFor(JScrollPane sp, JTable table, int colWidth) {
        sp.setPreferredSize(new Dimension(
                Math.min(31 * colWidth + 64, 1100),
                table.getRowCount() * table.getRowHeight() + table.getTableHeader().getPreferredSize().height + 6));
    }

    private MatchSummary findSummaryByMatchId(String id) {
        try {
            int idGiai = prefs.getInt("selectedGiaiDauId", -1);
            List<NoiDung> all = noiDungService.getNoiDungByTuornament(idGiai);
            for (NoiDung nd : all) {
                // Cá nhân (đơn)
                String a = null, b = null;
                for (SoDoCaNhan x : soDoCaNhanRepo.list(idGiai, nd.getId())) {
                    if (id.equals(x.getIdTranDau())) {
                        String name = safeVdvName(x.getIdVdv());
                        if (a == null)
                            a = name;
                        else if (b == null && !Objects.equals(a, name))
                            b = name;
                    }
                }
                if (a != null || b != null) {
                    return new MatchSummary(nd.getTenNoiDung(), true, a, b);
                }
                // Đôi (đồng đội)
                a = null;
                b = null;
                for (SoDoDoi x : soDoDoiRepo.list(idGiai, nd.getId())) {
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
                if (a != null || b != null) {
                    return new MatchSummary(nd.getTenNoiDung(), false, a, b);
                }
            }
        } catch (Exception ignore) {
        }
        return null;
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

    private static final class MatchSummary {
        final String ndLabel;
        final boolean isSingles;
        final String nameA; // tên VĐV (đơn) hoặc tên đội (đôi) phía P1
        final String nameB; // tên VĐV (đơn) hoặc tên đội (đôi) phía P2

        MatchSummary(String ndLabel, boolean isSingles, String nameA, String nameB) {
            this.ndLabel = ndLabel;
            this.isSingles = isSingles;
            this.nameA = nameA;
            this.nameB = nameB;
        }
    }
}
