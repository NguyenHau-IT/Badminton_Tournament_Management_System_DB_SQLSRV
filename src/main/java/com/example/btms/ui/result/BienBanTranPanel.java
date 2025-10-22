package com.example.btms.ui.result;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics2D;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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
    private final JButton btnToggleMode = new JButton("Chế độ: Xem");
    private final JButton btnExportPdf = new JButton("Xuất PDF (Dọc)");
    private final JButton btnExportPdfLandscape = new JButton("Xuất PDF (Ngang)");

    // Container hiển thị các bảng set (mỗi set một bảng 4x31)
    private final JPanel setsContainer = new JPanel();
    // DateTimeFormatter hiện không dùng trong chế độ bảng

    private String currentMatchId;
    private boolean editMode = false; // false: Xem, true: Sửa

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
        top.add(new JLabel("|"));
        btnToggleMode.addActionListener(e -> {
            editMode = !editMode;
            btnToggleMode.setText(editMode ? "Chế độ: Sửa" : "Chế độ: Xem");
            reload();
        });
        top.add(btnToggleMode);
        top.add(new JLabel("|"));
        btnExportPdf.addActionListener(e -> exportToPdf(false));
        top.add(btnExportPdf);
        top.add(new JLabel("|"));
        btnExportPdfLandscape.addActionListener(e -> exportToPdf(true));
        top.add(btnExportPdfLandscape);
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
            if (editMode) {
                setsContainer.add(createTokenEditorPanel(v));
            }
            setsContainer.add(Box.createVerticalStrut(8));
        }
        setsContainer.revalidate();
        setsContainer.repaint();
    }

    // Tạo bảng cho một set. Đôi: 4 hàng (2 trên P1, 2 dưới P2). Đơn: 2 hàng (P1 một
    // hàng, P2 một hàng)
    private JPanel createSetTablePanel(ChiTietVan v) {
        // Header cột: bỏ đánh số cột → để trống
        String[] headers = new String[31];
        for (int i = 0; i < 31; i++) {
            headers[i] = "";
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
        wrapper.setBorder(BorderFactory.createTitledBorder(
                null, // không có viền
                title, // tiêu đề
                TitledBorder.LEFT, // căn trái
                TitledBorder.TOP // căn trên
        ));

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
            JPanel panelNoScroll = wrapWithRowHeader(emptyTable, rowHeaders, isSingles, 36);
            wrapper.add(panelNoScroll);
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

            JPanel panelNoScroll = wrapWithRowHeader(table, rowHeaders, isSingles, colWidth);
            wrapper.add(panelNoScroll);
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
                JPanel panelNoScroll = wrapWithRowHeader(table, rowHeaders, isSingles, colWidth);
                wrapper.add(panelNoScroll);
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

    /**
     * Panel chỉnh sửa token cho một set: xem/xóa/thêm P1/P2/SWAP và lưu ngay.
     */
    private JPanel createTokenEditorPanel(ChiTietVan v) {
        JPanel editor = new JPanel(new BorderLayout(6, 6));
        editor.setBorder(BorderFactory.createTitledBorder("Chỉnh sửa token (Set " + v.getSetNo() + ")"));

        // Model token
        javax.swing.DefaultListModel<String> model = new javax.swing.DefaultListModel<>();
        List<String> toks = parseTokens(v.getDauThoiGian());
        for (String t : toks)
            model.addElement(t);

        // Danh sách token
        JList<String> list = new JList<>(model);
        list.setVisibleRowCount(6);
        JScrollPane sp = new JScrollPane(list);
        editor.add(sp, BorderLayout.CENTER);

        // Nút thao tác
        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.Y_AXIS));
        JButton addA = new JButton("+1 A");
        JButton addB = new JButton("+1 B");
        JButton addSwap = new JButton("Thêm SWAP");
        JButton del = new JButton("Xóa đã chọn");
        addSwap.setEnabled(v.getSetNo() != null && v.getSetNo() == 2); // SWAP chỉ áp dụng ý nghĩa ở set 2

        addA.addActionListener(e -> {
            model.addElement("P1@" + System.currentTimeMillis());
            saveTokensForSet(v.getIdTranDau(), v.getSetNo(), listModelToTokens(model));
        });
        addB.addActionListener(e -> {
            model.addElement("P2@" + System.currentTimeMillis());
            saveTokensForSet(v.getIdTranDau(), v.getSetNo(), listModelToTokens(model));
        });
        addSwap.addActionListener(e -> {
            model.addElement("SWAP@" + System.currentTimeMillis());
            saveTokensForSet(v.getIdTranDau(), v.getSetNo(), listModelToTokens(model));
        });
        del.addActionListener(e -> {
            int[] sel = list.getSelectedIndices();
            if (sel == null || sel.length == 0)
                return;
            // Không cho xóa hết để tránh DAU_THOI_GIAN rỗng (service không cho phép)
            if (sel.length >= model.size()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Không thể xóa hết tất cả token. Hãy giữ lại ít nhất 1 token hoặc thêm token mới trước.",
                        "Xóa token", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Xóa từ cuối để không lệch index
            for (int i = sel.length - 1; i >= 0; i--)
                model.removeElementAt(sel[i]);
            saveTokensForSet(v.getIdTranDau(), v.getSetNo(), listModelToTokens(model));
        });

        for (JButton b : new JButton[] { addA, addB, addSwap, del }) {
            b.setAlignmentX(LEFT_ALIGNMENT);
            btns.add(b);
            btns.add(Box.createVerticalStrut(6));
        }
        editor.add(btns, BorderLayout.EAST);

        return editor;
    }

    private static List<String> parseTokens(String raw) {
        List<String> out = new ArrayList<>();
        if (raw == null || raw.isBlank())
            return out;
        String[] parts = raw.split(";");
        for (String s : parts) {
            String t = s.trim();
            if (t.isEmpty())
                continue;
            // Chỉ chấp nhận 3 loại token
            if (t.startsWith("P1@") || t.startsWith("P2@") || t.startsWith("SWAP@")) {
                out.add(t);
            }
        }
        return out;
    }

    private static List<String> listModelToTokens(javax.swing.DefaultListModel<String> m) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < m.size(); i++)
            out.add(m.get(i));
        return out;
    }

    private void saveTokensForSet(String matchId, Integer setNo, List<String> tokens) {
        if (matchId == null || setNo == null)
            return;
        try {
            if (tokens == null || tokens.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Danh sách token trống. Hãy thêm P1/P2 hoặc SWAP.",
                        "Lưu token", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            int[] totals = computeTotals(tokens);
            String dau = String.join("; ", tokens);
            vanService.update(matchId, setNo, totals[0], totals[1], dau);
            // Sau khi lưu, reload để cập nhật bảng hiển thị
            reload();
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Lỗi lưu token: " + ex.getMessage(),
                    "Lưu token", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private static int[] computeTotals(List<String> tokens) {
        int a = 0, b = 0;
        if (tokens == null)
            return new int[] { 0, 0 };
        for (String t : tokens) {
            if (t == null)
                continue;
            String s = t.trim();
            if (s.startsWith("P1@"))
                a++;
            else if (s.startsWith("P2@"))
                b++;
        }
        return new int[] { a, b };
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

    private static JPanel wrapWithRowHeader(JTable table, String[] rowHeaders, boolean isSingles, int colWidth) {
        int rowHeight = table.getRowHeight();
        int rows = table.getRowCount();
        int headerWidth = computeHeaderWidth(rowHeaders, table);
        int tableWidth = table.getColumnCount() * colWidth;
        int tableHeight = rows * rowHeight;

        // Row header list
        JList<String> rowHeader = new JList<>(rowHeaders);
        rowHeader.setFixedCellWidth(headerWidth);
        rowHeader.setFixedCellHeight(rowHeight);
        rowHeader.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel l = new JLabel(value == null ? "" : value);
            l.setHorizontalAlignment(JLabel.CENTER);
            l.setVerticalAlignment(JLabel.CENTER);
            l.setOpaque(true);
            int bottom = 1;
            l.setBorder(BorderFactory.createMatteBorder(0, 0, bottom, 1, table.getGridColor()));
            l.setToolTipText(value);
            return l;
        });
        rowHeader.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rowHeader.setPreferredSize(new Dimension(headerWidth, tableHeight));

        // Ensure the JTable sizes to show all content (no inner scrollbars)
        table.setPreferredSize(new Dimension(tableWidth, tableHeight));

        JPanel container = new JPanel(new BorderLayout());
        container.add(rowHeader, BorderLayout.WEST);
        container.add(table, BorderLayout.CENTER);
        container.setPreferredSize(new Dimension(headerWidth + tableWidth, tableHeight + 2));
        return container;
    }

    /**
     * Tính chiều rộng cột tiêu đề hàng (tên VĐV/đội) theo độ dài chuỗi và font hiện
     * tại,
     * có giới hạn min/max để giao diện ổn định.
     */
    private static int computeHeaderWidth(String[] rowHeaders, JTable table) {
        int min = 160; // rộng hơn mặc định để tên dài đỡ bị cắt
        int max = 360; // tránh quá rộng chiếm chỗ cột điểm
        int pad = 40; // đệm cho khoảng cách/tràn viền
        int w = min;
        try {
            java.awt.FontMetrics fm = table.getFontMetrics(table.getFont());
            if (rowHeaders != null && fm != null) {
                for (String s : rowHeaders) {
                    if (s == null)
                        s = "";
                    w = Math.max(w, fm.stringWidth(s) + pad);
                }
            }
        } catch (Exception ignore) {
        }
        if (w < min)
            w = min;
        if (w > max)
            w = max;
        return w;
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

    /* =================== PDF EXPORT =================== */
    private void exportToPdf(boolean landscape) {
        try {
            if (setsContainer.getComponentCount() == 0) {
                javax.swing.JOptionPane.showMessageDialog(this, "Chưa có dữ liệu để xuất PDF.",
                        "Xuất PDF", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Chọn file
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Lưu biên bản thành PDF");
            String suffix = landscape ? "-ngang" : "";
            fc.setSelectedFile(
                    new java.io.File(
                            "bien-ban-" + (currentMatchId != null ? currentMatchId : "match") + suffix + ".pdf"));
            int ans = fc.showSaveDialog(this);
            if (ans != JFileChooser.APPROVE_OPTION)
                return;
            java.io.File out = fc.getSelectedFile();

            // Đảm bảo layout đầy đủ trước khi chụp
            setsContainer.revalidate();
            setsContainer.doLayout();
            Dimension pref = setsContainer.getPreferredSize();
            int compW = Math.max(pref.width, setsContainer.getWidth());
            int compH = pref.height;
            if (compW <= 0 || compH <= 0) {
                javax.swing.JOptionPane.showMessageDialog(this, "Không thể render nội dung.",
                        "Xuất PDF", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }

            // PDF (A4) bằng OpenPDF
            com.lowagie.text.Document doc = new com.lowagie.text.Document(
                    landscape ? com.lowagie.text.PageSize.A4.rotate() : com.lowagie.text.PageSize.A4);
            com.lowagie.text.pdf.PdfWriter.getInstance(doc, new java.io.FileOutputStream(out));
            doc.open();

            float pageW = doc.getPageSize().getWidth() - doc.leftMargin() - doc.rightMargin();
            float pageH = doc.getPageSize().getHeight() - doc.topMargin() - doc.bottomMargin();

            // Render theo từng lát dọc để tránh ảnh quá cao
            int slice = Math.min(compH, 1600); // px mỗi trang
            for (int y = 0; y < compH; y += slice) {
                int h = Math.min(slice, compH - y);
                java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(compW, h,
                        java.awt.image.BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = img.createGraphics();
                g2.setColor(java.awt.Color.WHITE);
                g2.fillRect(0, 0, compW, h);
                g2.translate(0, -y);
                setsContainer.paint(g2);
                g2.dispose();

                com.lowagie.text.Image pic = com.lowagie.text.Image.getInstance(img, null);
                pic.scaleToFit(pageW, pageH);
                doc.add(pic);
            }

            doc.close();
            javax.swing.JOptionPane.showMessageDialog(this, "Đã xuất PDF: " + out.getAbsolutePath(),
                    "Xuất PDF", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi xuất PDF: " + ex.getMessage(),
                    "Xuất PDF", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
