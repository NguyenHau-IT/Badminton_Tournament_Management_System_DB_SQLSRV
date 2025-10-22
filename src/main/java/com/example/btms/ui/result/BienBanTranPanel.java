package com.example.btms.ui.result;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
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
import com.example.btms.service.match.ChiTietTranDauService;
import com.example.btms.repository.match.ChiTietTranDauRepository;
import com.example.btms.repository.tuornament.GiaiDauRepository;
import com.example.btms.model.tournament.GiaiDau;
import com.example.btms.service.match.ChiTietVanService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.team.DoiService;

/**
 * Panel hiển thị biên bản cho 1 trận duy nhất.
 */
public class BienBanTranPanel extends JPanel {
    private final Prefs prefs = new Prefs();
    private final NoiDungService noiDungService;
    private final ChiTietTranDauService tranDauService;
    private final GiaiDauRepository giaiRepo;
    // vẽ bảng
    private final ChiTietVanService vanService;
    private final SoDoCaNhanRepository soDoCaNhanRepo;
    private final SoDoDoiRepository soDoDoiRepo;
    private final VanDongVienService vdvService;
    private final DoiService doiService;

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
        this.tranDauService = new ChiTietTranDauService(new ChiTietTranDauRepository(conn));
        this.giaiRepo = new GiaiDauRepository(conn);
        this.vanService = new ChiTietVanService(new ChiTietVanRepository(conn));
        this.soDoCaNhanRepo = new SoDoCaNhanRepository(conn);
        this.soDoDoiRepo = new SoDoDoiRepository(conn);
        this.vdvService = new VanDongVienService(new VanDongVienRepository(conn));
        this.doiService = new DoiService(conn);

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
        // top.add(btnExportPdf);
        // top.add(new JLabel("|"));
        btnExportPdfLandscape.addActionListener(e -> exportToPdf(true));
        top.add(btnExportPdfLandscape);
        add(top, BorderLayout.NORTH);

        // Khu vực trung tâm: danh sách các bảng theo từng set
        setsContainer.setLayout(new BoxLayout(setsContainer, BoxLayout.Y_AXIS));
        add(new JScrollPane(setsContainer), BorderLayout.CENTER);

        setMatchId(matchId);
    }

    // Helper to draw a string centered inside a rectangle (will scale down font if
    // necessary)
    private static void drawCenteredString(Graphics2D g, String text, int x, int y, int w, int h,
            java.awt.Font baseFont) {
        if (text == null)
            text = "";
        java.awt.FontMetrics fm = g.getFontMetrics(baseFont);
        int textW = fm.stringWidth(text);
        float fontSize = baseFont.getSize2D();
        java.awt.Font f = baseFont;
        if (textW > w - 4) {
            float scale = (float) (w - 4) / (float) textW;
            if (scale < 0.5f)
                scale = 0.5f;
            f = baseFont.deriveFont(fontSize * scale);
            fm = g.getFontMetrics(f);
            textW = fm.stringWidth(text);
        }
        int textH = fm.getAscent();
        int tx = x + (w - textW) / 2;
        int ty = y + (h + textH) / 2 - fm.getDescent();
        java.awt.Font old = g.getFont();
        g.setFont(f);
        g.drawString(text, tx, ty);
        g.setFont(old);
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
            // nếu đôi, hiển thị tên từng VĐV thay vì tên đội
            String[] playersA = getTeamPlayerNames(nameA, ms != null ? ms.ndLabel : null);
            String[] playersB = getTeamPlayerNames(nameB, ms != null ? ms.ndLabel : null);
            rowHeaders = new String[] {
                    playersA.length > 0 ? playersA[0] : nameA,
                    playersA.length > 1 ? playersA[1] : nameA,
                    playersB.length > 0 ? playersB[0] : nameB,
                    playersB.length > 1 ? playersB[1] : nameB
            };
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

            JTable table = new JTable(model) {
                @Override
                public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row,
                        int column) {
                    java.awt.Component c = super.prepareRenderer(renderer, row, column);

                    // Tô màu xám cho các hàng P2
                    if (getRowCount() == 2 && row == 1) {
                        c.setBackground(new Color(200, 200, 200));
                    } else if (getRowCount() == 4 && (row == 2 || row == 3)) {
                        c.setBackground(new Color(200, 200, 200));
                    } else {
                        c.setBackground(getBackground());
                    }

                    // Thêm viền dưới đậm cho các hàng cụ thể
                    if (c instanceof javax.swing.JComponent) {
                        javax.swing.JComponent jc = (javax.swing.JComponent) c;
                        if ((getRowCount() == 2 && row == 0) || (getRowCount() == 4 && row == 1)) {
                            jc.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.BLACK));
                        } else {
                            jc.setBorder(null);
                        }
                    }

                    return c;
                }

                @Override
                public void paint(Graphics g) {
                    // Vẽ background xám trước cho PDF export
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(new Color(200, 200, 200));
                    int rowHeight = getRowHeight();
                    if (getRowCount() == 2) {
                        // Hàng 2 (index 1)
                        g2d.fillRect(0, rowHeight, getWidth(), rowHeight);
                    } else if (getRowCount() == 4) {
                        // Hàng 3,4 (index 2,3)
                        g2d.fillRect(0, rowHeight * 2, getWidth(), rowHeight * 2);
                    }
                    g2d.dispose();

                    // Vẽ nội dung table
                    super.paint(g);

                    // Vẽ viền đậm sau cùng
                    g2d = (Graphics2D) g.create();
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new java.awt.BasicStroke(3.0f));
                    if (getRowCount() == 2) {
                        // Viền dưới hàng 1 (index 0)
                        g2d.drawLine(0, rowHeight, getWidth(), rowHeight);
                    } else if (getRowCount() == 4) {
                        // Viền dưới hàng 2 (index 1)
                        g2d.drawLine(0, rowHeight * 2, getWidth(), rowHeight * 2);
                    }
                    g2d.dispose();
                }
            };
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
                JTable table = new JTable(model) {
                    @Override
                    public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row,
                            int column) {
                        java.awt.Component c = super.prepareRenderer(renderer, row, column);

                        // Tô màu xám cho các hàng P2
                        if (getRowCount() == 2 && row == 1) {
                            c.setBackground(new Color(200, 200, 200));
                        } else if (getRowCount() == 4 && (row == 2 || row == 3)) {
                            c.setBackground(new Color(200, 200, 200));
                        } else {
                            c.setBackground(getBackground());
                        }

                        // Thêm viền dưới đậm cho các hàng cụ thể
                        if (c instanceof javax.swing.JComponent) {
                            javax.swing.JComponent jc = (javax.swing.JComponent) c;
                            if ((getRowCount() == 2 && row == 0) || (getRowCount() == 4 && row == 1)) {
                                jc.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.BLACK));
                            } else {
                                jc.setBorder(null);
                            }
                        }

                        return c;
                    }

                    @Override
                    public void paint(Graphics g) {
                        // Vẽ background xám trước cho PDF export
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setColor(new Color(200, 200, 200));
                        int rowHeight = getRowHeight();
                        if (getRowCount() == 2) {
                            // Hàng 2 (index 1)
                            g2d.fillRect(0, rowHeight, getWidth(), rowHeight);
                        } else if (getRowCount() == 4) {
                            // Hàng 3,4 (index 2,3)
                            g2d.fillRect(0, rowHeight * 2, getWidth(), rowHeight * 2);
                        }
                        g2d.dispose();

                        // Vẽ nội dung table
                        super.paint(g);

                        // Vẽ viền đậm sau cùng
                        g2d = (Graphics2D) g.create();
                        g2d.setColor(Color.BLACK);
                        g2d.setStroke(new java.awt.BasicStroke(3.0f));
                        if (getRowCount() == 2) {
                            // Viền dưới hàng 1 (index 0)
                            g2d.drawLine(0, rowHeight, getWidth(), rowHeight);
                        } else if (getRowCount() == 4) {
                            // Viền dưới hàng 2 (index 1)
                            g2d.drawLine(0, rowHeight * 2, getWidth(), rowHeight * 2);
                        }
                        g2d.dispose();
                    }
                };
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
        JTable table = new JTable(model) {
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row,
                    int column) {
                java.awt.Component c = super.prepareRenderer(renderer, row, column);

                // Tô màu xám cho các hàng P2
                if (getRowCount() == 2 && row == 1) {
                    c.setBackground(new Color(200, 200, 200));
                } else if (getRowCount() == 4 && (row == 2 || row == 3)) {
                    c.setBackground(new Color(200, 200, 200));
                } else {
                    c.setBackground(getBackground());
                }

                // Thêm viền dưới đậm cho các hàng cụ thể
                if (c instanceof javax.swing.JComponent) {
                    javax.swing.JComponent jc = (javax.swing.JComponent) c;
                    if ((getRowCount() == 2 && row == 0) || (getRowCount() == 4 && row == 1)) {
                        jc.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.BLACK));
                    } else {
                        jc.setBorder(null);
                    }
                }

                return c;
            }

            @Override
            public void paint(Graphics g) {
                // Vẽ background xám trước cho PDF export
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(200, 200, 200));
                int rowHeight = getRowHeight();
                if (getRowCount() == 2) {
                    // Hàng 2 (index 1)
                    g2d.fillRect(0, rowHeight, getWidth(), rowHeight);
                } else if (getRowCount() == 4) {
                    // Hàng 3,4 (index 2,3)
                    g2d.fillRect(0, rowHeight * 2, getWidth(), rowHeight * 2);
                }
                g2d.dispose();

                // Vẽ nội dung table
                super.paint(g);

                // Vẽ viền đậm sau cùng
                g2d = (Graphics2D) g.create();
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new java.awt.BasicStroke(3.0f));
                if (getRowCount() == 2) {
                    // Viền dưới hàng 1 (index 0)
                    g2d.drawLine(0, rowHeight, getWidth(), rowHeight);
                } else if (getRowCount() == 4) {
                    // Viền dưới hàng 2 (index 1)
                    g2d.drawLine(0, rowHeight * 2, getWidth(), rowHeight * 2);
                }
                g2d.dispose();
            }
        };
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
        // Apply shaded background for P2 rows
        applyShadedRowRenderer(table);
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

            // Apply same shaded background for the left labels
            if (table.getRowCount() == 2 && index == 1) {
                l.setBackground(new Color(200, 200, 200));
            } else if (table.getRowCount() == 4 && (index == 2 || index == 3)) {
                l.setBackground(new Color(200, 200, 200));
            } else {
                l.setBackground(list.getBackground());
            }

            // Add thicker bottom border for specific rows to match table cells
            if ((table.getRowCount() == 2 && index == 0) || (table.getRowCount() == 4 && index == 1)) {
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 1, Color.BLACK));
            } else {
                int bottom = 1;
                l.setBorder(BorderFactory.createMatteBorder(0, 0, bottom, 1, table.getGridColor()));
            }

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

        // Add thick black border around entire table
        container.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.BLACK));

        return container;
    }

    /**
     * Apply a TableCellRenderer that shades the lower rows (P2) with a light gray.
     */
    private static void applyShadedRowRenderer(JTable table) {
        javax.swing.table.TableCellRenderer r = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table1, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table1, value, isSelected, hasFocus, row, column);
                // Shade the P2 rows (for 2-row tables row index 1, for 4-row tables rows 2 and
                // 3)
                if (table1.getRowCount() == 2 && row == 1) {
                    setBackground(new Color(200, 200, 200));
                } else if (table1.getRowCount() == 4 && (row == 2 || row == 3)) {
                    setBackground(new Color(200, 200, 200));
                } else {
                    setBackground(table1.getBackground());
                }

                // Add thicker bottom border for specific rows
                if ((table1.getRowCount() == 2 && row == 1) || (table1.getRowCount() == 4 && row == 2)) {
                    // Thicker bottom border for P2 separator
                    setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.BLACK));
                } else {
                    setBorder(null);
                }

                setHorizontalAlignment(JLabel.CENTER);
                return this;
            }
        };
        // Apply for all cell types so all cells get shaded consistently
        table.setDefaultRenderer(Object.class, r);
        table.setDefaultRenderer(Number.class, r);
        table.setDefaultRenderer(Integer.class, r);
        table.setDefaultRenderer(Long.class, r);
        table.setDefaultRenderer(Double.class, r);
        table.setDefaultRenderer(Float.class, r);
        table.setDefaultRenderer(String.class, r);
        table.setDefaultRenderer(Boolean.class, r);

        // Also set the table's row background colors directly
        for (int row = 0; row < table.getRowCount(); row++) {
            if ((table.getRowCount() == 2 && row == 1) || (table.getRowCount() == 4 && (row == 2 || row == 3))) {
                table.setRowSelectionAllowed(false);
            }
        }
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
                Integer IdA = null, IdB = null;
                for (SoDoCaNhan x : soDoCaNhanRepo.list(idGiai, nd.getId())) {
                    if (id.equals(x.getIdTranDau())) {
                        String name = safeVdvName(x.getIdVdv());
                        if (a == null) {
                            a = name;
                            IdA = x.getIdVdv();
                        } else if (b == null && !Objects.equals(a, name)) {
                            b = name;
                            IdB = x.getIdVdv();
                        }
                    }
                }
                if (a != null || b != null) {
                    return new MatchSummary(nd.getTenNoiDung(), true, a, b, IdA, IdB);
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
                    return new MatchSummary(nd.getTenNoiDung(), false, a, b, null, null);
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

    /**
     * Lấy tên 2 VĐV trong đội, trả về mảng tối đa 2 phần tử
     */
    private String[] getTeamPlayerNames(String teamName, String noiDungLabel) {
        if (teamName == null || teamName.isBlank()) {
            return new String[0];
        }

        try {
            int idGiai = prefs.getInt("selectedGiaiDauId", -1);
            if (idGiai == -1) {
                return new String[0];
            }

            // Tìm ID nội dung từ tên
            int idNoiDung = -1;
            try {
                List<NoiDung> allNoiDung = noiDungService.getNoiDungByTuornament(idGiai);
                for (NoiDung nd : allNoiDung) {
                    if (Objects.equals(nd.getTenNoiDung(), noiDungLabel)) {
                        idNoiDung = nd.getId();
                        break;
                    }
                }
            } catch (Exception ignore) {
            }

            if (idNoiDung == -1) {
                return new String[0];
            }

            // Lấy team ID từ tên đội
            // xoá các kí tự tính từu dấu - trở về sau
            int dashIndex = teamName.indexOf("-");
            if (dashIndex != -1) {
                teamName = teamName.substring(0, dashIndex);
            }
            int teamId = doiService.getTeamIdByTeamName(teamName, idNoiDung, idGiai);
            if (teamId == -1) {
                return new String[0];
            }

            // Lấy danh sách VĐV trong đội
            VanDongVien[] players = doiService.getTeamPlayers(teamId);
            if (players == null || players.length == 0) {
                return new String[0];
            }

            String[] names = new String[players.length];
            for (int i = 0; i < players.length; i++) {
                VanDongVien p = players[i];
                names[i] = (p != null && p.getHoTen() != null && !p.getHoTen().isBlank())
                        ? p.getHoTen()
                        : ("VĐV#" + (p != null ? p.getId() : i));
            }
            return names;

        } catch (Exception e) {
            // Nếu có lỗi, trả về tên đội gốc
            return new String[] { teamName };
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
        final Integer IdA;
        final Integer IdB;

        MatchSummary(String ndLabel, boolean isSingles, String nameA, String nameB, Integer IdA, Integer IdB) {
            this.ndLabel = ndLabel;
            this.isSingles = isSingles;
            this.nameA = nameA;
            this.nameB = nameB;
            this.IdA = IdA;
            this.IdB = IdB;
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
            String suffix = "";
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

            // PDF (A4) bằng OpenPDF - use smaller margins so content appears larger
            com.lowagie.text.Rectangle pageSize = landscape ? com.lowagie.text.PageSize.A4.rotate()
                    : com.lowagie.text.PageSize.A4;
            // margins in points (12pt ~ 0.17 inch). Reduced margins to maximize usable
            // area.
            float marginPts = 12f;
            com.lowagie.text.Document doc = new com.lowagie.text.Document(pageSize, marginPts, marginPts, marginPts,
                    marginPts);
            com.lowagie.text.pdf.PdfWriter.getInstance(doc, new java.io.FileOutputStream(out));
            doc.open();

            float pageW = doc.getPageSize().getWidth() - doc.leftMargin() - doc.rightMargin();
            float pageH = doc.getPageSize().getHeight() - doc.topMargin() - doc.bottomMargin();

            // Render header + content into one full image, then slice into pages.
            // Make header larger and add padding between header and tables
            int headerH = 240; // increased header height to fit larger fonts
            int paddingBetween = 10; // px gap between header and sets (reduced per request)
            int totalH = headerH + paddingBetween + compH;

            java.awt.image.BufferedImage full = new java.awt.image.BufferedImage(compW, totalH,
                    java.awt.image.BufferedImage.TYPE_INT_RGB);
            Graphics2D gFull = full.createGraphics();
            gFull.setColor(java.awt.Color.WHITE);
            gFull.fillRect(0, 0, compW, totalH);

            // Gather match / tournament metadata to render into the header
            String matchIdVal = currentMatchId != null ? currentMatchId : "";
            String sanVal = "";
            String noidungVal = "";
            String ngayVal = "";
            String giaiVal = "";
            String startTimeVal = "";
            String endTimeVal = "";
            String durationVal = "";
            try {
                if (currentMatchId != null) {
                    try {
                        var ct = tranDauService.get(currentMatchId);
                        if (ct != null && ct.getSan() != null) {
                            sanVal = String.valueOf(ct.getSan());
                        }
                        if (ct != null && ct.getBatDau() != null) {
                            try {
                                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter
                                        .ofPattern("dd/MM/yyyy");
                                ngayVal = ct.getBatDau().toLocalDate().format(df);
                            } catch (Exception ignore) {
                                ngayVal = ct.getBatDau().toLocalDate().toString();
                            }
                            try {
                                java.time.format.DateTimeFormatter tf = java.time.format.DateTimeFormatter
                                        .ofPattern("HH:mm");
                                startTimeVal = ct.getBatDau().toLocalTime().format(tf);
                            } catch (Exception ignore) {
                                startTimeVal = "";
                            }
                        }
                        if (ct != null && ct.getKetThuc() != null) {
                            try {
                                java.time.format.DateTimeFormatter tf2 = java.time.format.DateTimeFormatter
                                        .ofPattern("HH:mm");
                                endTimeVal = ct.getKetThuc().toLocalTime().format(tf2);
                            } catch (Exception ignore) {
                                endTimeVal = "";
                            }
                            try {
                                long mins = java.time.Duration.between(ct.getBatDau(), ct.getKetThuc()).toMinutes();
                                durationVal = String.valueOf(mins);
                            } catch (Exception ignore) {
                                durationVal = "";
                            }
                        }
                    } catch (Exception ignore) {
                    }
                }
            } catch (Exception ignore) {
            }
            try {
                MatchSummary ms = findSummaryByMatchId(currentMatchId);
                if (ms != null && ms.ndLabel != null)
                    noidungVal = ms.ndLabel;
            } catch (Exception ignore) {
            }
            try {
                int idGiai = prefs.getInt("selectedGiaiDauId", -1);
                if (idGiai != -1) {
                    try {
                        java.util.Optional<GiaiDau> opt = giaiRepo.findById(idGiai);
                        if (opt != null && opt.isPresent()) {
                            giaiVal = opt.get().getTenGiai();
                        }
                    } catch (Exception ignore) {
                    }
                }
            } catch (Exception ignore) {
            }

            // Draw the header at the top (with populated values)
            // Fetch match summary to get team/club names for the "Đơn vị" fields
            String clubLeft = "";
            String clubRight = "";
            try {
                MatchSummary ms = findSummaryByMatchId(currentMatchId);
                if (ms != null) {
                    if (ms.isSingles) {
                        clubLeft = vdvService.getClubNameById(ms.IdA);
                        clubRight = vdvService.getClubNameById(ms.IdB);
                    } else {
                        clubLeft = ms.nameA != null ? ms.nameA : "";
                        clubRight = ms.nameB != null ? ms.nameB : "";
                        // If the team string has a '-', take the substring after the first dash as the
                        // club name
                        if (clubLeft.contains("-")) {
                            String[] parts = clubLeft.split("-", 2);
                            clubLeft = parts.length > 1 ? parts[1].trim() : parts[0].trim();
                        }
                        if (clubRight.contains("-")) {
                            String[] parts2 = clubRight.split("-", 2);
                            clubRight = parts2.length > 1 ? parts2[1].trim() : parts2[0].trim();
                        }
                    }
                }
            } catch (Exception ignore) {
            }
            paintPdfHeader(gFull, compW, matchIdVal, sanVal, noidungVal, ngayVal, giaiVal, startTimeVal,
                    endTimeVal, durationVal, clubLeft, clubRight);

            // Draw the sets content below the header with padding
            gFull.translate(0, headerH + paddingBetween);
            setsContainer.paint(gFull);
            gFull.dispose();

            // Slice the big image into page-sized pieces
            int slice = Math.min(totalH, 1600); // px mỗi trang
            for (int y = 0; y < totalH; y += slice) {
                int h = Math.min(slice, totalH - y);
                java.awt.image.BufferedImage img = full.getSubimage(0, y, compW, h);
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

    /**
     * Paint the top header area for the PDF export. This draws the tournament title
     * and the small form shown in the screenshot.
     */
    private void paintPdfHeader(Graphics2D g, int width, String matchId, String san, String noidung, String ngay,
            String giai, String startTime, String endTime, String durationMinutes, String clubLeft, String clubRight) {
        int margin = 12;
        int y = margin;
        // Title (larger)
        java.awt.Font titleFont = new java.awt.Font("Serif", java.awt.Font.BOLD, 35);
        g.setFont(titleFont);
        g.setColor(java.awt.Color.BLACK);
        String title = "BIÊN BẢN THI ĐẤU";
        java.awt.FontMetrics fm = g.getFontMetrics(titleFont);
        int tx = (width - fm.stringWidth(title)) / 2;
        g.drawString(title, tx, y + fm.getAscent());
        y += fm.getHeight() + 6;

        // Subtitle line (larger)
        java.awt.Font sub = new java.awt.Font("Serif", java.awt.Font.PLAIN, 20);
        g.setFont(sub);
        String subText = giai != null && !giai.isBlank() ? giai : "MÔN CẦU LÔNG";
        fm = g.getFontMetrics(sub);
        tx = (width - fm.stringWidth(subText)) / 2;
        g.drawString(subText, tx, y + fm.getAscent());
        y += fm.getHeight() + 10;

        // Layout sizes
        int boxH = 84;
        int leftW = Math.min(260, width / 4);
        int rightW = leftW;
        int centerW = width - leftW - rightW - margin * 4;

        // Left column labels with underlines (and fill values)
        int lx = margin;
        int ly = y;
        g.setFont(sub);
        String[] leftLabels = new String[] { "Mã số trận:", "Sân số:", "Nội dung:", "Ngày:", "Giờ dự kiến:" };
        int lineLen = leftW - 20;
        int curY = ly + 14;
        // corresponding values
        String[] leftValues = new String[] { matchId != null ? matchId : "", san != null ? san : "",
                noidung != null ? noidung : "",
                ngay != null ? ngay : "", "" };
        for (String lbl : leftLabels) {
            int idx = java.util.Arrays.asList(leftLabels).indexOf(lbl);
            g.drawString(lbl, lx + 6, curY);
            int startX = lx + 6 + g.getFontMetrics().stringWidth(lbl) + 6;
            int endX = lx + 6 + lineLen;
            // draw underline
            g.drawLine(startX, curY + 2, endX, curY + 2);
            // draw value left-aligned in the underline area
            if (idx >= 0 && idx < leftValues.length) {
                String v = leftValues[idx];
                if (v != null && !v.isBlank()) {
                    g.drawString(v, startX + 2, curY);
                }
            }
            curY += 14 + 6;
        }

        // Right column labels with underlines
        int rx = width - rightW - margin;
        int ry = ly;
        String[] rightLabels = new String[] { "T.gian bắt đầu:", "T.gian kết thúc:", "T.gian thi đấu:", "T.T chính:",
                "T.T giao cầu:" };
        curY = ry + 14;
        // corresponding values for right side
        String[] rightValues = new String[] { startTime != null ? startTime : "", endTime != null ? endTime : "",
                durationMinutes != null && !durationMinutes.isBlank() ? (durationMinutes + " phút") : "", "", "" };
        for (int i = 0; i < rightLabels.length; i++) {
            String lbl = rightLabels[i];
            g.drawString(lbl, rx + 6, curY);
            int startX = rx + 6 + g.getFontMetrics().stringWidth(lbl) + 6;
            int endX = rx + 6 + lineLen;
            g.drawLine(startX, curY + 2, endX, curY + 2);
            // draw value left-aligned in underline area
            if (i < rightValues.length) {
                String v = rightValues[i];
                if (v != null && !v.isBlank()) {
                    g.drawString(v, startX + 2, curY);
                }
            }
            curY += 14 + 6;
        }

        // Center small block: 4 columns x 6 rows with merged cells
        int cx = lx + leftW + margin;
        // move center block lower to avoid overlapping left-side underlines
        int cy = y + 36; // was y + 6, increased so the left fields (Mã số trận, ...) remain visible
        int blockW = centerW;
        int blockH = boxH - 12;
        int rowH = Math.max(4, blockH / 6);

        java.awt.FontMetrics subFm = g.getFontMetrics(sub);
        int smallColW = Math.max(24, subFm.stringWidth("000") + 12); // width to fit 3-digit numbers
        // ensure we have space for two small columns
        int remaining = blockW - smallColW * 2;
        if (remaining < 40) {
            // fallback: distribute evenly
            int base = blockW / 4;
            int c1w = base, c2w = base, c3w = base;
            int[] xs = new int[] { cx, cx + c1w, cx + c1w + c2w, cx + c1w + c2w + c3w, cx + blockW };
            // draw outer rect and vertical lines normally
            g.drawRect(cx, cy, blockW, rowH * 6);
            for (int i = 1; i < 4; i++)
                g.drawLine(xs[i], cy, xs[i], cy + rowH * 6);
            // fill column 4 (rightmost) with gray background
            java.awt.Color prev = g.getColor();
            g.setColor(new Color(200, 200, 200));
            int col4Start = xs[3];
            int col4Width = xs[4] - xs[3];
            g.fillRect(col4Start + 1, cy + 1, col4Width - 1, rowH * 6 - 1);
            g.setColor(prev);
            // draw horizontal lines with a thicker stroke to emphasize them
            java.awt.Stroke oldStroke = null;
            if (g instanceof java.awt.Graphics2D) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
                oldStroke = g2.getStroke();
                g2.setStroke(new java.awt.BasicStroke(2.0f));
                for (int r = 1; r < 6; r++) {
                    int yLine = cy + r * rowH;
                    // for column 4 we want top 3 rows merged => only draw full-width line at r==3
                    if (r == 3) {
                        g2.drawLine(cx, yLine, cx + blockW, yLine);
                    } else {
                        // draw only up to the start of column 4 (xs[3]) to avoid splitting merged cell
                        int col4StartLocal = xs[3];
                        g2.drawLine(cx, yLine, col4StartLocal, yLine);
                    }
                }
                g2.setStroke(oldStroke);
            } else {
                for (int r = 1; r < 6; r++)
                    g.drawLine(cx, cy + r * rowH, cx + blockW, cy + r * rowH);
            }
            // draw participant names into merged cells (fallback layout)
            try {
                MatchSummary msLocal = findSummaryByMatchId(matchId);
                if (msLocal != null) {
                    if (msLocal.isSingles) {
                        String a = msLocal.nameA != null ? msLocal.nameA : "";
                        String b = msLocal.nameB != null ? msLocal.nameB : "";
                        // draw a centered in top-left merged cell, b centered in top-right merged cell
                        int leftWfb = xs[1] - xs[0];
                        int rightWfb = xs[4] - xs[3];
                        int topH = rowH * 3;
                        drawCenteredString(g, a, xs[0] + 2, cy + 2, leftWfb - 4, topH - 4, sub);
                        drawCenteredString(g, b, xs[3] + 2, cy + 2, rightWfb - 4, topH - 4, sub);
                    } else {
                        String[] pa = getTeamPlayerNames(msLocal.nameA, msLocal.ndLabel);
                        String[] pb = getTeamPlayerNames(msLocal.nameB, msLocal.ndLabel);
                        int leftWfb = xs[1] - xs[0];
                        int rightWfb = xs[4] - xs[3];
                        // top and bottom halves
                        drawCenteredString(g, pa.length > 0 ? pa[0] : "", xs[0] + 2, cy + 2, leftWfb - 4, rowH * 3 - 4,
                                sub);
                        drawCenteredString(g, pa.length > 1 ? pa[1] : "", xs[0] + 2, cy + rowH * 3 + 2, leftWfb - 4,
                                rowH * 3 - 4, sub);
                        drawCenteredString(g, pb.length > 0 ? pb[0] : "", xs[3] + 2, cy + 2, rightWfb - 4, rowH * 3 - 4,
                                sub);
                        drawCenteredString(g, pb.length > 1 ? pb[1] : "", xs[3] + 2, cy + rowH * 3 + 2, rightWfb - 4,
                                rowH * 3 - 4, sub);
                    }
                }
            } catch (Exception ignore) {
            }
        } else {
            int col2w = smallColW;
            int col3w = smallColW;
            int col1w = remaining / 2;
            int col4w = remaining - col1w;

            // x positions
            int x0 = cx;
            int x1 = x0 + col1w;
            int x2 = x1 + col2w;
            int x3 = x2 + col3w;

            // Draw outer and vertical grid lines normally
            g.drawRect(cx, cy, blockW, rowH * 6);
            g.drawLine(x1, cy, x1, cy + rowH * 6);
            g.drawLine(x2, cy, x2, cy + rowH * 6);
            g.drawLine(x3, cy, x3, cy + rowH * 6);
            // fill column 4 (rightmost) with gray background before merged white fills
            java.awt.Color prev2 = g.getColor();
            g.setColor(new Color(200, 200, 200));
            g.fillRect(x3 + 1, cy + 1, col4w - 1, rowH * 6 - 1);
            g.setColor(prev2);
            // Draw horizontal grid lines thicker to make them more prominent
            if (g instanceof java.awt.Graphics2D) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
                java.awt.Stroke old = g2.getStroke();
                g2.setStroke(new java.awt.BasicStroke(2.0f));
                for (int r = 1; r < 6; r++) {
                    int yLine = cy + r * rowH;
                    if (r == 3) {
                        g2.drawLine(cx, yLine, cx + blockW, yLine);
                    } else {
                        // draw only to x3 so col4 keeps merged rows
                        g2.drawLine(cx, yLine, x3, yLine);
                    }
                }
                g2.setStroke(old);
            } else {
                for (int r = 1; r < 6; r++)
                    g.drawLine(cx, cy + r * rowH, cx + blockW, cy + r * rowH);
            }

            // Merge and paint merged areas
            java.awt.Color old = g.getColor();
            g.setColor(java.awt.Color.WHITE);
            // col1 top & bottom (3-row merges)
            g.fillRect(x0 + 1, cy + 1, col1w - 1, rowH * 3 - 1);
            g.fillRect(x0 + 1, cy + rowH * 3 + 1, col1w - 1, rowH * 3 - 1);
            // (col4 background already painted gray above)
            // col2 and col3 merged pairs (1-2,3-4,5-6)
            for (int i = 0; i < 3; i++) {
                int my = cy + i * 2 * rowH;
                g.fillRect(x1 + 1, my + 1, col2w - 1, rowH * 2 - 1);
                g.fillRect(x2 + 1, my + 1, col3w - 1, rowH * 2 - 1);
            }
            g.setColor(old);
            // draw participant names into merged cells (main layout)
            try {
                MatchSummary msMain = findSummaryByMatchId(matchId);
                if (msMain != null) {
                    if (msMain.isSingles) {
                        String a = msMain.nameA != null ? msMain.nameA : "";
                        String b = msMain.nameB != null ? msMain.nameB : "";
                        // draw centered in top-left merged cell, top-right merged cell
                        drawCenteredString(g, a, x0 + 2, cy + 2, col1w - 4, rowH * 3 - 4, sub);
                        drawCenteredString(g, b, x3 + 2, cy + 2, col4w - 4, rowH * 3 - 4, sub);
                    } else {
                        String[] pa = getTeamPlayerNames(msMain.nameA, msMain.ndLabel);
                        String[] pb = getTeamPlayerNames(msMain.nameB, msMain.ndLabel);
                        // left team players
                        drawCenteredString(g, pa.length > 0 ? pa[0] : "", x0 + 2, cy + 2, col1w - 4, rowH * 3 - 4, sub);
                        drawCenteredString(g, pa.length > 1 ? pa[1] : "", x0 + 2, cy + rowH * 3 + 2, col1w - 4,
                                rowH * 3 - 4, sub);
                        // right team players
                        drawCenteredString(g, pb.length > 0 ? pb[0] : "", x3 + 2, cy + 2, col4w - 4, rowH * 3 - 4, sub);
                        drawCenteredString(g, pb.length > 1 ? pb[1] : "", x3 + 2, cy + rowH * 3 + 2, col4w - 4,
                                rowH * 3 - 4, sub);
                    }
                }
            } catch (Exception ignore) {
            }

            // Draw merged borders
            g.drawRect(x0, cy, col1w, rowH * 3);
            g.drawRect(x0, cy + rowH * 3, col1w, rowH * 3);
            g.drawRect(x3, cy, col4w, rowH * 3);
            g.drawRect(x3, cy + rowH * 3, col4w, rowH * 3);
            for (int i = 0; i < 3; i++) {
                int my = cy + i * 2 * rowH;
                g.drawRect(x1, my, col2w, rowH * 2);
                g.drawRect(x2, my, col3w, rowH * 2);
            }
        }

        // Draw two "Đơn vị" labels and underlines for club names under the center block
        // shortened underline and fill with club names if available
        try {
            int gapBelow = 10;
            int unitY = cy + rowH * 6 + gapBelow + g.getFontMetrics(sub).getAscent();
            String unitLabel = "Đơn vị:";
            int labelWidth = g.getFontMetrics(sub).stringWidth(unitLabel);
            // shorten underline to a fixed 80 px
            int underlineLen = 80;

            // left unit (left half of center block)
            int leftLabelX = cx + 6;
            int leftStartX = leftLabelX + labelWidth + 8;
            int leftAreaEnd = cx + (blockW / 2) - 6;
            int availableLeftLen = Math.max(40, leftAreaEnd - leftStartX);
            g.drawString(unitLabel, leftLabelX, unitY);
            // determine desired underline length based on club name width
            int underlineLenLeft = underlineLen;
            if (clubLeft != null && !clubLeft.isBlank()) {
                java.awt.FontMetrics fmLeft = g.getFontMetrics(sub);
                int desired = fmLeft.stringWidth(clubLeft) + 6;
                underlineLenLeft = Math.min(Math.max(underlineLen, desired), availableLeftLen);
            } else {
                underlineLenLeft = Math.min(underlineLen, availableLeftLen);
            }
            int leftEndX = leftStartX + underlineLenLeft;
            g.drawLine(leftStartX, unitY + 2, leftEndX, unitY + 2);
            if (clubLeft != null && !clubLeft.isBlank()) {
                String cl = clubLeft;
                java.awt.FontMetrics fmLeft = g.getFontMetrics(sub);
                int textW = fmLeft.stringWidth(cl);
                if (textW + 4 > underlineLenLeft) {
                    // scale font down so the name fits underline
                    float scale = (float) (underlineLenLeft - 4) / (float) textW;
                    if (scale < 0.5f)
                        scale = 0.5f; // don't go too small
                    java.awt.Font oldFont = g.getFont();
                    g.setFont(sub.deriveFont(sub.getSize2D() * scale));
                    g.drawString(cl, leftStartX + 2, unitY);
                    g.setFont(oldFont);
                } else {
                    g.drawString(cl, leftStartX + 2, unitY);
                }
            }

            // right unit (right half of center block)
            int rightLabelX = cx + (blockW / 2) + 6;
            int rightStartX = rightLabelX + labelWidth + 8;
            int rightAreaEnd = cx + blockW - 6;
            int availableRightLen = Math.max(40, rightAreaEnd - rightStartX);
            g.drawString(unitLabel, rightLabelX, unitY);
            int underlineLenRight = underlineLen;
            if (clubRight != null && !clubRight.isBlank()) {
                java.awt.FontMetrics fmRight = g.getFontMetrics(sub);
                int desiredR = fmRight.stringWidth(clubRight) + 6;
                underlineLenRight = Math.min(Math.max(underlineLen, desiredR), availableRightLen);
            } else {
                underlineLenRight = Math.min(underlineLen, availableRightLen);
            }
            int rightEndX = rightStartX + underlineLenRight;
            g.drawLine(rightStartX, unitY + 2, rightEndX, unitY + 2);
            if (clubRight != null && !clubRight.isBlank()) {
                String cr = clubRight;
                java.awt.FontMetrics fmRight = g.getFontMetrics(sub);
                int textW = fmRight.stringWidth(cr);
                if (textW + 4 > underlineLenRight) {
                    float scale = (float) (underlineLenRight - 4) / (float) textW;
                    if (scale < 0.5f)
                        scale = 0.5f;
                    java.awt.Font oldFont = g.getFont();
                    g.setFont(sub.deriveFont(sub.getSize2D() * scale));
                    g.drawString(cr, rightStartX + 2, unitY);
                    g.setFont(oldFont);
                } else {
                    g.drawString(cr, rightStartX + 2, unitY);
                }
            }
        } catch (Exception ex) {
            // ignore
        }

        // done
    }
}
