package com.example.badmintoneventtechnology.ui.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class MonitorTab extends JPanel implements AutoCloseable {
    private static final String GROUP = "239.255.50.50";
    private static final int PORT = 50505;

    // --- Model dữ liệu ---
    private final Map<String, Row> sessions = new ConcurrentHashMap<>();

    // --- UI: JList dạng lưới thẻ ---
    private final DefaultListModel<Row> listModel = new DefaultListModel<>();
    private final JList<Row> list = new JList<>(listModel);
    private final java.util.Map<String, MonitorWindow> viewers = new java.util.concurrent.ConcurrentHashMap<>();
    private final javax.swing.JCheckBox autoOpen = new javax.swing.JCheckBox("Tự mở bảng điểm");

    // Phân biệt admin/client mode
    private boolean isAdminMode;
    private String localClientId;

    private Thread rxThread;
    private volatile boolean running = false;
    private NetworkInterface nif;

    // Swing Timer cho refresh UI
    private Timer uiTimer;

    // Debounce timer để tránh refresh quá nhiều
    private Timer debounceTimer;

    // === Field phục vụ tách/ghim tab ===
    private JFrame floatFrame;
    private JTabbedPane dockTabs;
    private int dockIndex = -1;
    private String dockTitle;
    private Icon dockIcon;
    private boolean shuttingDown = false; // tránh tự-đính lại khi app đang đóng

    // Setting: số cột hiển thị
    private int columns = 3;
    private final javax.swing.JComboBox<String> cboColumns = new javax.swing.JComboBox<>(
            new String[] { "1", "2", "3" });
    private final Preferences prefs = Preferences.userNodeForPackage(MonitorTab.class);

    public MonitorTab() {
        this(false, null); // Default to client mode
    }

    public MonitorTab(boolean adminMode, String clientId) {
        super(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        this.isAdminMode = adminMode;
        this.localClientId = clientId;

        // === Thanh header: tiêu đề + nút chuyển chế độ ===
        JPanel header = new JPanel(new BorderLayout());
        String titleText = isAdminMode ? "Giám sát bảng điểm (Admin)" : "Bảng điểm của tôi";
        JLabel title = new JLabel(titleText);
        title.setFont(title.getFont().deriveFont(Font.BOLD));

        // Nút làm mới dữ liệu
        JButton btnRefresh = new JButton("🔄 Làm mới");
        btnRefresh.setToolTipText("Làm mới dữ liệu giám sát");
        btnRefresh.addActionListener(e -> {
            sessions.clear();
            refreshCards();
            restart();
        });

        JButton btnToggle = new JButton("Mở cửa sổ");
        btnToggle.addActionListener(e -> toggleFloat(btnToggle));

        header.add(title, BorderLayout.WEST);
        JPanel right = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        autoOpen.setOpaque(false);
        autoOpen.setSelected(false); // Tắt auto-open để tránh mở nhiều cửa sổ
        right.add(btnRefresh);
        right.add(autoOpen);
        right.add(btnToggle);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Cấu hình JList để hiển thị dạng lưới card (3 card mỗi hàng)
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(-1);
        list.setFixedCellWidth(460); // mặc định, sẽ tự điều chỉnh theo viewport để luôn 3 cột
        list.setFixedCellHeight(220); // chiều cao card đồng đều
        list.setCellRenderer(new CardRenderer());
        list.setBackground(UIManager.getColor("Panel.background"));
        list.putClientProperty("JComponent.roundRect", true); // hint FlatLaf

        JScrollPane scroll = new JScrollPane(list);
        // Tabs bên trong: Danh sách | Cài đặt
        JTabbedPane innerTabs = new JTabbedPane();

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(scroll, BorderLayout.CENTER);
        innerTabs.addTab("Danh sách", listPanel);

        JPanel settingsPanel = buildSettingsPanel();
        innerTabs.addTab("Cài đặt", settingsPanel);

        add(innerTabs, BorderLayout.CENTER);

        // Tải số cột đã lưu
        try {
            int savedCols = Math.max(1, Math.min(3, prefs.getInt("monitor.columns", columns)));
            columns = savedCols;
            try {
                cboColumns.setSelectedItem(String.valueOf(columns));
            } catch (Exception ignore) {
            }
        } catch (Exception ignore) {
        }

        // Tự điều chỉnh độ rộng cell để luôn hiển thị 3 card mỗi hàng
        java.awt.Component viewport = scroll.getViewport();
        viewport.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateCellWidthForColumns();
            }
        });
        // Gọi lần đầu sau khi UI dựng xong
        SwingUtilities.invokeLater(this::updateCellWidthForColumns);

        // Mở viewer khi double click
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int idx = list.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        Row r = listModel.getElementAt(idx);
                        openViewerFor(r);
                    }
                }
            }
        });

        // Cập nhật UI mỗi 5s để giảm tần suất refresh (lọc phiên cũ + sắp xếp + vẽ lại)
        uiTimer = new Timer(5000, e -> refreshCards());
        uiTimer.start();
    }

    /** Gọi từ MainFrame để nghe trên đúng interface (đảm bảo cùng mạng LAN). */
    public void setNetworkInterface(NetworkInterface nif) {
        this.nif = nif;
        restart();
    }

    /** Gọi từ MainFrame để cập nhật mode admin/client */
    public void setAdminMode(boolean adminMode, String clientId) {
        // Cập nhật mode và client ID
        this.isAdminMode = adminMode;
        this.localClientId = clientId;

        // Cập nhật tiêu đề
        SwingUtilities.invokeLater(() -> {
            String titleText = isAdminMode ? "Giám sát bảng điểm (Admin)" : "Bảng điểm của tôi";
            // Tìm và cập nhật JLabel title trong header
            for (Component comp : getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel header = (JPanel) comp;
                    for (Component headerComp : header.getComponents()) {
                        if (headerComp instanceof JLabel && headerComp.getName() == null) {
                            // Giả sử JLabel đầu tiên là title
                            ((JLabel) headerComp).setText(titleText);
                            break;
                        }
                    }
                }
            }
        });

        // Xóa dữ liệu cũ và restart
        sessions.clear();
        refreshCards();
        restart();
    }

    private void restart() {
        stopRx();
        startRx();
    }

    private void startRx() {
        running = true;
        rxThread = new Thread(this::rxLoop, "monitor-rx");
        rxThread.setDaemon(true);
        rxThread.start();
    }

    private void stopRx() {
        running = false;
        if (rxThread != null) {
            try {
                rxThread.interrupt();
            } catch (Exception ignore) {
            }
            rxThread = null;
        }
        sessions.clear();
        refreshCards();
    }

    /** Tính và set độ rộng cell để luôn vừa N cột trong viewport */
    private void updateCellWidthForColumns() {
        try {
            java.awt.Container parent = list.getParent();
            while (parent != null && !(parent instanceof javax.swing.JViewport)) {
                parent = parent.getParent();
            }
            if (!(parent instanceof javax.swing.JViewport))
                return;
            javax.swing.JViewport vp = (javax.swing.JViewport) parent;
            int vw = vp.getExtentSize().width;
            // chừa khoảng margin nhỏ giữa các card
            int hGap = 12;
            int totalGap = (columns + 1) * (hGap / 2);
            int cellWidth = Math.max(320, (vw - totalGap) / columns);
            list.setFixedCellWidth(cellWidth);
            list.revalidate();
            list.repaint();
        } catch (Exception ignore) {
        }
    }

    /** Panel cài đặt số cột */
    private JPanel buildSettingsPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel row = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        row.add(new JLabel("Số cột:"));
        cboColumns.setSelectedItem(String.valueOf(columns));
        row.add(cboColumns);
        p.add(row, BorderLayout.NORTH);

        cboColumns.addActionListener(e -> {
            try {
                String sel = (String) cboColumns.getSelectedItem();
                int col = Integer.parseInt(sel);
                columns = Math.max(1, Math.min(3, col));
                updateCellWidthForColumns();
                try {
                    prefs.putInt("monitor.columns", columns);
                } catch (Exception ignore) {
                }
            } catch (Exception ignore) {
            }
        });

        return p;
    }

    private void rxLoop() {
        MulticastSocket ms = null;
        try {
            ms = new MulticastSocket(PORT);
            InetAddress group = InetAddress.getByName(GROUP);

            if (nif != null) {
                ms.setNetworkInterface(nif);
                ms.joinGroup(new InetSocketAddress(group, PORT), nif);
            } else {
                ms.joinGroup(group);
            }

            byte[] buf = new byte[2048];
            DatagramPacket pkt = new DatagramPacket(buf, buf.length);

            while (running) {
                ms.receive(pkt);
                String json = new String(pkt.getData(), pkt.getOffset(), pkt.getLength(), StandardCharsets.UTF_8);
                handleJson(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ms != null)
                try {
                    ms.close();
                } catch (Exception ignore) {
                }
        }
    }

    private void handleJson(String json) {
        Map<String, String> m = parseFlatJson(json);

        String op = m.get("op");
        String sid = m.get("sid");

        if ("DELETE".equalsIgnoreCase(op)) {
            Row removed = sessions.remove(sid);
            if (removed != null)
                closeViewer(removed.viewerKey());
            return;
        }
        if (!"UPSERT".equalsIgnoreCase(op)) {
            return;
        }

        // Lọc dữ liệu dựa trên mode
        String clientId = m.getOrDefault("client", "");

        Row r = sessions.computeIfAbsent(sid, k -> {
            return new Row();
        });

        r.client = clientId;
        r.host = m.getOrDefault("host", "");
        r.courtId = m.getOrDefault("courtId", ""); // Parse courtId từ broadcast
        r.header = m.getOrDefault("header", "TRẬN ĐẤU");
        r.kind = m.getOrDefault("kind", "HORIZONTAL");
        r.doubles = "true".equalsIgnoreCase(m.getOrDefault("doubles", "false"));
        r.nameA = m.getOrDefault("nameA", "");
        r.nameB = m.getOrDefault("nameB", "");
        r.game = parseInt(m.get("game"));
        r.bestOf = parseInt(m.get("bestOf"));
        r.scoreA = parseInt(m.get("scoreA"));
        r.scoreB = parseInt(m.get("scoreB"));
        r.gamesA = parseInt(m.get("gamesA"));
        r.gamesB = parseInt(m.get("gamesB"));
        r.gameScores = m.getOrDefault("gameScores", ""); // Nhận điểm các ván đã hoàn thành
        // Nếu không có ts, sử dụng thời gian hiện tại
        String tsStr = m.get("ts");
        if (tsStr != null && !tsStr.isEmpty()) {
            r.updated = parseLong(tsStr);
        } else {
            r.updated = System.currentTimeMillis();
        }

        // Sử dụng debounce timer để tránh refresh quá nhiều
        debouncedRefresh();

        if (autoOpen.isSelected())
            openViewerFor(r);
    }

    private void debouncedRefresh() {
        // Hủy timer cũ nếu có
        if (debounceTimer != null && debounceTimer.isRunning()) {
            debounceTimer.stop();
        }

        // Tạo timer mới với delay 500ms để giảm nhảy liên tục
        debounceTimer = new Timer(500, e -> {
            SwingUtilities.invokeLater(this::refreshCards);
            debounceTimer.stop();
        });
        debounceTimer.setRepeats(false);
        debounceTimer.start();
    }

    private void refreshCards() {
        long now = System.currentTimeMillis();

        // loại bỏ phiên cũ > 10s
        final int[] removedCount = { 0 };
        sessions.entrySet().removeIf(e -> {
            boolean shouldRemove = now - e.getValue().updated > 10_000;
            if (shouldRemove) {
                removedCount[0]++;
            }
            return shouldRemove;
        });

        // KHÔNG sắp xếp lại để tránh nhảy nội dung - giữ nguyên thứ tự ban đầu
        // Chỉ cập nhật dữ liệu trong các card hiện có
        updateExistingCardsData();

        // Chỉ thêm card mới nếu cần thiết
        addNewCardsIfNeeded();
    }

    private void updateExistingCardsData() {
        // Cập nhật dữ liệu trong các card hiện có mà không thay đổi vị trí
        for (int i = 0; i < listModel.size(); i++) {
            Row currentRow = listModel.get(i);
            if (currentRow != null) {
                // Tìm Row tương ứng trong sessions
                Row sessionRow = findRowByIdentity(currentRow);
                if (sessionRow != null) {
                    // Cập nhật dữ liệu mà không tạo object mới
                    updateRowData(currentRow, sessionRow);
                } else {
                    // Row này không còn trong sessions (phiên đã kết thúc)
                    // Đánh dấu để xóa sau
                    markRowForRemoval(currentRow);
                }
            }
        }

        // Xóa các card đã bị đánh dấu để xóa
        removeMarkedRows();

        // Chỉ repaint để cập nhật hiển thị, không revalidate
        list.repaint();
    }

    private void markRowForRemoval(Row row) {
        // Đánh dấu Row để xóa bằng cách set tất cả dữ liệu về null/0
        row.nameA = "";
        row.nameB = "";
        row.scoreA = 0;
        row.scoreB = 0;
        row.game = 0;
        row.bestOf = 0;
        row.gamesA = 0;
        row.gamesB = 0;
        row.doubles = false;
        row.kind = "";
        row.courtId = ""; // Reset courtId
        row.gameScores = ""; // Reset điểm các ván đã hoàn thành
        row.updated = 0L;
        // Thêm flag để biết đây là Row cần xóa
        row.markedForRemoval = true;

        // Log để debug
        System.out.println("Đánh dấu card để xóa: " + row.client + " - " + row.header);
    }

    private void removeMarkedRows() {
        // Xóa các Row đã được đánh dấu để xóa
        for (int i = listModel.size() - 1; i >= 0; i--) {
            Row row = listModel.get(i);
            if (row != null && row.markedForRemoval) {
                listModel.removeElementAt(i);
                System.out.println("Đã xóa card: " + row.client + " - " + row.header);
            }
        }
    }

    private void addNewCardsIfNeeded() {
        // Chỉ thêm card mới nếu chưa có trong list
        for (Row sessionRow : sessions.values()) {
            if (!isRowInList(sessionRow)) {
                // Thêm card mới vào cuối danh sách để tránh nhảy
                listModel.addElement(sessionRow);

                // Log để debug
                System.out.println("Thêm card mới: " + sessionRow.client + " - " + sessionRow.header);
            }
        }
    }

    private Row findRowByIdentity(Row currentRow) {
        for (Row sessionRow : sessions.values()) {
            if (isSameRowIdentity(currentRow, sessionRow)) {
                return sessionRow;
            }
        }
        return null;
    }

    private boolean isRowInList(Row sessionRow) {
        for (int i = 0; i < listModel.size(); i++) {
            Row listRow = listModel.get(i);
            if (isSameRowIdentity(listRow, sessionRow)) {
                return true;
            }
        }
        return false;
    }

    private void updateRowData(Row target, Row source) {
        // Cập nhật tất cả dữ liệu mà không tạo object mới
        target.nameA = source.nameA;
        target.nameB = source.nameB;
        target.scoreA = source.scoreA;
        target.scoreB = source.scoreB;
        target.game = source.game;
        target.bestOf = source.bestOf;
        target.gamesA = source.gamesA;
        target.gamesB = source.gamesB;
        target.doubles = source.doubles;
        target.kind = source.kind;
        target.courtId = source.courtId; // Cập nhật courtId
        target.gameScores = source.gameScores; // Cập nhật điểm các ván đã hoàn thành
        target.updated = source.updated;
    }

    private boolean isSameRow(Row r1, Row r2) {
        if (r1 == null || r2 == null)
            return false;
        // Chỉ so sánh những gì thực sự quan trọng để tránh nhảy liên tục
        // Không so sánh client, host, header vì chúng ít thay đổi
        return r1.scoreA == r2.scoreA &&
                r1.scoreB == r2.scoreB &&
                r1.game == r2.game &&
                r1.gamesA == r2.gamesA &&
                r1.gamesB == r2.gamesB &&
                r1.bestOf == r2.bestOf &&
                r1.doubles == r2.doubles;
    }

    private boolean isSameRowIdentity(Row r1, Row r2) {
        if (r1 == null || r2 == null)
            return false;
        // So sánh identity để xác định có phải cùng một Row không
        return java.util.Objects.equals(r1.client, r2.client) &&
                java.util.Objects.equals(r1.host, r2.host) &&
                java.util.Objects.equals(r1.header, r2.header);
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private static long parseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return 0L;
        }
    }

    private static Map<String, String> parseFlatJson(String json) {
        Map<String, String> m = new ConcurrentHashMap<>();
        if (json == null)
            return m;
        String s = json.trim();
        if (s.startsWith("{"))
            s = s.substring(1);
        if (s.endsWith("}"))
            s = s.substring(0, s.length() - 1);
        String[] parts = s.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String p : parts) {
            int i = p.indexOf(':');
            if (i <= 0)
                continue;
            String k = unquote(p.substring(0, i).trim());
            String v = unquote(p.substring(i + 1).trim());
            m.put(k, v);
        }
        return m;
    }

    private static String unquote(String x) {
        x = x.trim();
        if (x.startsWith("\"") && x.endsWith("\"")) {
            x = x.substring(1, x.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return x;
    }

    // === Bật/tắt cửa sổ nổi ===
    private void toggleFloat(JButton sourceBtn) {
        if (floatFrame == null) {
            // Đang ở trong tab -> tách ra cửa sổ
            JTabbedPane tabs = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
            if (tabs != null) {
                dockTabs = tabs;
                dockIndex = tabs.indexOfComponent(this);
                if (dockIndex >= 0) {
                    dockTitle = tabs.getTitleAt(dockIndex);
                    dockIcon = tabs.getIconAt(dockIndex);
                    tabs.remove(this);
                }
            }

            floatFrame = new JFrame(dockTitle != null ? dockTitle : "Monitor");
            floatFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            floatFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    // Khi user đóng cửa sổ, tự đính ngược về tab (nếu app chưa tắt)
                    if (!shuttingDown) {
                        reattachToTab();
                        sourceBtn.setText("Mở cửa sổ");
                    }
                }
            });

            floatFrame.getContentPane().add(this);
            floatFrame.setMinimumSize(new Dimension(640, 480));
            floatFrame.setSize(900, 600);
            floatFrame.setLocationRelativeTo(null);
            floatFrame.setVisible(true);

            sourceBtn.setText("Thu về tab");
        } else {
            // Đang nổi -> quay về tab
            floatFrame.dispose(); // sẽ kích hoạt windowClosed -> reattachToTab()
            sourceBtn.setText("Mở cửa sổ");
        }
    }

    private void reattachToTab() {
        if (floatFrame != null) {
            floatFrame.getContentPane().remove(this);
            floatFrame = null;
        }
        if (dockTabs != null) {
            int idx = dockIndex >= 0 ? Math.min(dockIndex, dockTabs.getTabCount()) : dockTabs.getTabCount();
            dockTabs.insertTab(dockTitle != null ? dockTitle : "Monitor", dockIcon, this, null, idx);
            dockTabs.setSelectedComponent(this);
        }
    }

    @Override
    public void close() {
        shuttingDown = true; // để windowClosed không tự gắn lại vào tab
        stopRx();
        if (uiTimer != null) {
            uiTimer.stop();
            uiTimer = null;
        }
        if (debounceTimer != null) {
            debounceTimer.stop();
            debounceTimer = null;
        }
        if (floatFrame != null) {
            floatFrame.getContentPane().remove(this);
            floatFrame.dispose();
            floatFrame = null;
        }
    }

    // --- Dữ liệu cho mỗi card ---
    private static class Row {
        String client, host, header, kind, nameA, nameB, courtId; // Thêm courtId
        boolean doubles;
        int game, bestOf, scoreA, scoreB, gamesA, gamesB;
        long updated;
        boolean markedForRemoval = false; // Flag để đánh dấu Row cần xóa
        String gameScores = ""; // Điểm của các ván đã hoàn thành (format: "21:19,19:21")

        String formatUpdated() {
            return new SimpleDateFormat("HH:mm:ss").format(new Date(updated));
        }

        String viewerKey() {
            return (client == null ? "" : client) + "@" + (host == null ? "" : host) + ":"
                    + (header == null ? "" : header);
        }
    }

    // --- Renderer tạo card hiện đại ---
    private static class CardRenderer extends JPanel implements ListCellRenderer<Row> {
        private final JLabel lblHeader = new JLabel();
        private final JLabel lblMeta = new JLabel();
        private final JLabel lblNames = new JLabel();
        private final JLabel lblVS = new JLabel();
        private final JLabel lblNames2 = new JLabel();
        private final JLabel lblScore = new JLabel();
        private final JLabel lblScoreB = new JLabel(); // Thêm label điểm cho đội B
        private final JLabel lblGames = new JLabel();
        private final JLabel lblGameScores = new JLabel(); // Thêm label điểm các ván đã hoàn thành
        private final ChipLabel chipUpdated = new ChipLabel();

        // Thêm các panel để hiển thị ô điểm
        private final JPanel scoreAPanel = new JPanel(); // Không set layout cố định
        private final JPanel scoreBPanel = new JPanel(); // Không set layout cố định
        private final JPanel nameAPanel = new JPanel(new BorderLayout());
        private final JPanel nameBPanel = new JPanel(new BorderLayout());

        CardRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);
            setBorder(new EmptyBorder(12, 12, 12, 12));
            putClientProperty("JComponent.roundRect", true); // FlatLaf hint

            // Header + chip thời gian
            JPanel top = new JPanel(new BorderLayout(8, 0));
            top.setOpaque(false);
            lblHeader.setFont(lblHeader.getFont().deriveFont(Font.BOLD, lblHeader.getFont().getSize2D() + 10f));
            lblHeader.setForeground(Color.WHITE); // Thay đổi màu chữ header thành trắng
            top.add(lblHeader, BorderLayout.CENTER);
            top.add(chipUpdated, BorderLayout.EAST);

            // Meta dòng phụ (HORIZONTAL/vertical, Đơn/Đôi, client@host)
            lblMeta.setForeground(Color.WHITE); // Thay đổi màu chữ meta thành trắng

            // Tên & điểm
            lblNames.setFont(lblNames.getFont().deriveFont(Font.BOLD, 16f)); // Tăng font size tên đội A
            lblNames.setForeground(Color.WHITE); // Thay đổi màu chữ tên thành trắng
            lblNames2.setFont(lblNames2.getFont().deriveFont(Font.BOLD, 16f)); // Tăng font size tên đội B
            lblNames2.setForeground(Color.WHITE); // Thay đổi màu chữ tên đội B thành trắng
            lblScore.setFont(lblScore.getFont().deriveFont(Font.BOLD, lblScore.getFont().getSize2D() + 4f));
            lblScore.setForeground(Color.WHITE); // Thay đổi màu chữ điểm thành trắng
            lblScoreB.setFont(lblScoreB.getFont().deriveFont(Font.BOLD, lblScoreB.getFont().getSize2D() + 4f));
            lblScoreB.setForeground(Color.WHITE); // Thay đổi màu chữ điểm đội B thành trắng
            lblGames.setForeground(Color.WHITE); // Thay đổi màu chữ games thành trắng
            lblGameScores.setForeground(Color.WHITE); // Thay đổi màu chữ game scores thành trắng

            // Thiết lập style cho các panel điểm
            scoreAPanel.setOpaque(false);
            scoreBPanel.setOpaque(false);

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            // center.add(Box.createVerticalStrut(6));
            // center.add(lblMeta);
            // Tạo panel để căn chỉnh lblGames sát mép trái
            // JPanel gamesPanel = new JPanel(new BorderLayout());
            // gamesPanel.setOpaque(false);
            // gamesPanel.add(lblGames, BorderLayout.WEST);
            // center.add(gamesPanel);
            // center.add(Box.createVerticalStrut(6));

            // Tạo panel cho tên và các ô điểm của đội A
            JPanel teamAPanel = new JPanel(new BorderLayout(8, 0));
            teamAPanel.setOpaque(false);

            // Panel tên A (kích thước sẽ cập nhật theo số cột)
            nameAPanel.setOpaque(false);
            nameAPanel.setPreferredSize(new Dimension(250, 50));
            nameAPanel.setMinimumSize(new Dimension(250, 50));
            nameAPanel.setMaximumSize(new Dimension(250, 50));
            nameAPanel.setSize(new Dimension(250, 50));
            nameAPanel.add(lblNames, BorderLayout.CENTER); // Căn giữa tên theo chiều dọc

            teamAPanel.add(nameAPanel, BorderLayout.WEST);

            // Tạo panel chứa các ô điểm cho đội A - căn giữa hoàn hảo theo chiều dọc
            JPanel scoreWrapperA = new JPanel();
            scoreWrapperA.setOpaque(false);
            scoreWrapperA.setLayout(new BoxLayout(scoreWrapperA, BoxLayout.Y_AXIS));
            scoreWrapperA.add(Box.createVerticalGlue()); // Căn trên
            scoreWrapperA.add(scoreAPanel); // Các ô điểm ở giữa
            scoreWrapperA.add(Box.createVerticalGlue()); // Căn dưới
            teamAPanel.add(scoreWrapperA, BorderLayout.CENTER);

            center.add(teamAPanel);
            center.add(Box.createVerticalStrut(0));

            // Tạo panel cho tên và các ô điểm của đội B
            JPanel teamBPanel = new JPanel(new BorderLayout(8, 0));
            teamBPanel.setOpaque(false);

            // Panel tên B (kích thước sẽ cập nhật theo số cột)
            nameBPanel.setOpaque(false);
            nameBPanel.setPreferredSize(new Dimension(250, 50));
            nameBPanel.setMinimumSize(new Dimension(250, 50));
            nameBPanel.setMaximumSize(new Dimension(250, 50));
            nameBPanel.setSize(new Dimension(250, 50));
            nameBPanel.add(lblNames2, BorderLayout.CENTER); // Căn giữa tên theo chiều dọc

            teamBPanel.add(nameBPanel, BorderLayout.WEST);

            // Tạo panel chứa các ô điểm cho đội B - căn giữa hoàn hảo theo chiều dọc
            JPanel scoreWrapperB = new JPanel();
            scoreWrapperB.setOpaque(false);
            scoreWrapperB.setLayout(new BoxLayout(scoreWrapperB, BoxLayout.Y_AXIS));
            scoreWrapperB.add(Box.createVerticalGlue()); // Căn trên
            scoreWrapperB.add(scoreBPanel); // Các ô điểm ở giữa
            scoreWrapperB.add(Box.createVerticalGlue()); // Căn dưới
            teamBPanel.add(scoreWrapperB, BorderLayout.CENTER);

            center.add(teamBPanel);
            center.add(Box.createVerticalStrut(0));

            JSeparator line = new JSeparator(SwingConstants.HORIZONTAL);
            line.setForeground(Color.WHITE); // màu trắng
            line.setBackground(Color.WHITE);
            top.add(line, BorderLayout.SOUTH);

            add(top, BorderLayout.NORTH);
            add(center, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends Row> list, Row r, int index, boolean isSelected, boolean cellHasFocus) {

            // Nội dung
            // Cập nhật kích thước linh hoạt theo số cột hiện tại
            int cellW = Math.max(320, list.getFixedCellWidth());
            int nameW = Math.max(160, (int) Math.round(cellW * 0.5));
            Dimension nameSize = new Dimension(nameW, 50);
            nameAPanel.setPreferredSize(nameSize);
            nameAPanel.setMinimumSize(nameSize);
            nameAPanel.setMaximumSize(new Dimension(nameW, Integer.MAX_VALUE));
            nameBPanel.setPreferredSize(nameSize);
            nameBPanel.setMinimumSize(nameSize);
            nameBPanel.setMaximumSize(new Dimension(nameW, Integer.MAX_VALUE));

            // Đặt kích thước renderer theo ô
            Dimension fixedSize = new Dimension(cellW, 200);
            setPreferredSize(fixedSize);
            setMinimumSize(fixedSize);
            setMaximumSize(new Dimension(cellW, Integer.MAX_VALUE));
            // Hiển thị courtId trong header nếu có
            String headerText = (r.courtId != null && !r.courtId.isEmpty())
                    ? String.format("%s", safe(r.header))
                    : safe(r.header);
            lblHeader.setText(headerText);

            String mode = (r.doubles ? "Đôi" : "Đơn");
            // Hiển thị courtId nếu có, nếu không thì hiển thị thông tin cũ
            String courtInfo = (r.courtId != null && !r.courtId.isEmpty())
                    ? String.format("%s • %s • %s@%s", safe(r.courtId), mode, safe(r.client),
                            safe(r.host))
                    : String.format("%s • %s@%s", mode, safe(r.client), safe(r.host));
            lblMeta.setText(courtInfo);

            // Xử lý hiển thị tên cho đánh đôi
            if (r.doubles) {
                // Tách tên đội A thành 2 phần và hiển thị trên 2 hàng
                String[] namesA = r.nameA.split("\\s*-\\s*"); // Tách theo dấu gạch ngang
                if (namesA.length >= 2) {
                    lblNames.setText("<html><div style='text-align: left; line-height: 1.2;'>" + namesA[0] + "<br>"
                            + namesA[1] + "</div></html>");
                } else {
                    lblNames.setText(safe(r.nameA));
                }

                // Tách tên đội B thành 2 phần và hiển thị trên 2 hàng
                String[] namesB = r.nameB.split("\\s*-\\s*"); // Tách theo dấu gạch ngang
                if (namesB.length >= 2) {
                    lblNames2.setText("<html><div style='text-align: left; line-height: 1.2;'>" + namesB[0] + "<br>"
                            + namesB[1] + "</div></html>");
                } else {
                    lblNames2.setText(safe(r.nameB));
                }
            } else {
                // Nếu đánh đơn, hiển thị tên bình thường
                lblNames.setText(safe(r.nameA));
                lblNames2.setText(safe(r.nameB));
            }

            lblVS.setText(String.format("vs"));

            // Tạo các ô điểm cho đội A
            scoreAPanel.removeAll();
            scoreAPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0)); // Không có khoảng cách
            int maxGames = Math.min(r.bestOf, 3); // Giới hạn tối đa 3 ván
            for (int i = 0; i < maxGames; i++) {
                JLabel scoreLabel = createScoreLabel(i, r, true);
                scoreAPanel.add(scoreLabel);
                // Không có khoảng cách giữa các ô điểm, viền sẽ tự nhiên chia ra
            }

            // Tạo các ô điểm cho đội B
            scoreBPanel.removeAll();
            scoreBPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0)); // Không có khoảng cách
            for (int i = 0; i < maxGames; i++) {
                JLabel scoreLabel = createScoreLabel(i, r, false);
                scoreBPanel.add(scoreLabel);
                // Không có khoảng cách giữa các ô điểm, viền sẽ tự nhiên chia ra
            }

            lblGames.setText(String.format("Ván: %d  •  BO%d  •  Ván thắng: %d - %d",
                    r.game, r.bestOf, r.gamesA, r.gamesB));

            // Hiển thị điểm các ván đã hoàn thành một cách đẹp
            if (r.gameScores != null && !r.gameScores.isEmpty()) {
                String[] gameScores = r.gameScores.split(",");
                StringBuilder displayText = new StringBuilder();
                displayText.append(safe(r.nameA)).append(" - ");

                for (int i = 0; i < r.bestOf; i++) {
                    if (i > 0)
                        displayText.append(" | ");
                    if (i < gameScores.length && gameScores[i].contains(":")) {
                        // Ván đã thi đấu
                        String[] scores = gameScores[i].split(":");
                        if (scores.length == 2) {
                            displayText.append(scores[0]);
                        }
                    } else if (i == r.game - 1) {
                        // Ván hiện tại
                        displayText.append(r.scoreA);
                    } else {
                        // Ván chưa thi đấu
                        displayText.append("-");
                    }
                }

                displayText.append("\n"); // Xuống dòng
                displayText.append(safe(r.nameB)).append(" - ");

                for (int i = 0; i < r.bestOf; i++) {
                    if (i > 0)
                        displayText.append(" | ");
                    if (i < gameScores.length && gameScores[i].contains(":")) {
                        // Ván đã thi đấu
                        String[] scores = gameScores[i].split(":");
                        if (scores.length == 2) {
                            displayText.append(scores[1]);
                        }
                    } else if (i == r.game - 1) {
                        // Ván hiện tại
                        displayText.append(r.scoreB);
                    } else {
                        // Ván chưa thi đấu
                        displayText.append("-");
                    }
                }

                lblGameScores.setText(displayText.toString());
            } else {
                // Nếu chưa có ván nào hoàn thành, hiển thị tất cả các ván
                StringBuilder displayText = new StringBuilder();
                displayText.append(safe(r.nameA)).append(" - ");

                for (int i = 0; i < r.bestOf; i++) {
                    if (i > 0)
                        displayText.append(" | ");
                    if (i == 0) {
                        // Ván đầu tiên (hiện tại)
                        displayText.append(r.scoreA);
                    } else {
                        // Các ván chưa thi đấu
                        displayText.append("-");
                    }
                }

                displayText.append("\n"); // Xuống dòng
                displayText.append(safe(r.nameB)).append(" - ");

                for (int i = 0; i < r.bestOf; i++) {
                    if (i > 0)
                        displayText.append(" | ");
                    if (i == 0) {
                        // Ván đầu tiên (hiện tại)
                        displayText.append(r.scoreB);
                    } else {
                        // Các ván chưa thi đấu
                        displayText.append("-");
                    }
                }

                lblGameScores.setText(displayText.toString());
            }

            // Chip thời gian + màu trạng thái theo tuổi gói tin
            long age = System.currentTimeMillis() - r.updated;
            chipUpdated.setText(r.formatUpdated());
            chipUpdated.setStatus(age < 3000 ? ChipStatus.OK : (age < 7000 ? ChipStatus.WARN : ChipStatus.STALE));

            // Style card
            setBackground(Color.BLACK); // Thay đổi background thành màu đen
            // Viền trắng duy nhất + tiêu đề hiển thị thông tin sân trên viền
            Border margin = new EmptyBorder(6, 6, 6, 6); // khoảng cách giữa các card
            String borderTitle = (r.courtId != null && !r.courtId.isEmpty()) ? (safe(r.courtId)) : "";
            javax.swing.border.TitledBorder titled = BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.WHITE, 3),
                    borderTitle,
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    getFont().deriveFont(Font.BOLD, 18f),
                    Color.WHITE);
            Border padding = new EmptyBorder(16, 16, 16, 16); // khoảng cách nội dung tới viền
            setBorder(BorderFactory.createCompoundBorder(margin,
                    BorderFactory.createCompoundBorder(titled, padding)));

            return this;
        }

        private static String safe(String s) {
            if (s == null)
                return "";
            if (s.trim().isEmpty())
                return "";
            return s.trim();
        }

        private static Color cardBg(boolean selected) {
            Color sel = UIManager.getColor("List.selectionBackground");
            Color bg = UIManager.getColor("Panel.background");
            return selected && sel != null ? sel : bg != null ? bg : Color.WHITE;
        }

        private static Color cardStroke(boolean selected) {
            if (selected) {
                Color c = UIManager.getColor("List.selectionForeground");
                return c != null ? c : new Color(60, 120, 240);
            }
            Color c = UIManager.getColor("Component.borderColor");
            return c != null ? c : new Color(210, 210, 210);
        }

        private JLabel createScoreLabel(int index, Row r, boolean isTeamA) {
            JLabel label = new JLabel();
            label.setFont(label.getFont().deriveFont(Font.BOLD, 22f)); // Tăng font size từ 20f lên 22f để to hơn
            label.setForeground(Color.YELLOW); // Thay đổi chữ thành màu vàng
            label.setOpaque(true); // Bật background
            label.setBackground(Color.GRAY); // Thay đổi background thành màu xám
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // Viền đen dày 2px để chia rõ ràng
            label.setPreferredSize(new Dimension(55, 45)); // Tăng kích thước từ 48x40 lên 55x45 để đảm bảo điểm 21
                                                           // không bị cắt
            label.setMinimumSize(new Dimension(55, 45)); // Kích thước tối thiểu
            label.setMaximumSize(new Dimension(55, 45)); // Kích thước tối đa
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.CENTER); // Căn giữa theo chiều dọc
            label.putClientProperty("JComponent.roundRect", true); // FlatLaf hint

            // Xác định điểm cho ván này
            int score = 0;
            boolean isCurrentGame = (index == r.game - 1);
            boolean isCompleted = false;

            if (r.gameScores != null && !r.gameScores.isEmpty()) {
                String[] gameScores = r.gameScores.split(",");
                if (index < gameScores.length && gameScores[index].contains(":")) {
                    // Ván đã hoàn thành
                    String[] scores = gameScores[index].split(":");
                    if (scores.length == 2) {
                        score = isTeamA ? Integer.parseInt(scores[0]) : Integer.parseInt(scores[1]);
                        isCompleted = true;
                    }
                }
            }

            if (isCurrentGame && !isCompleted) {
                // Ván hiện tại
                score = isTeamA ? r.scoreA : r.scoreB;
                label.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3)); // Viền vàng dày 3px cho ván đang thi
            } else if (isCompleted) {
                // Ván đã hoàn thành
                label.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2)); // Viền xanh dày 2px cho ván đã hoàn
                                                                                 // thành
            } else {
                // Ván chưa thi đấu hoặc không cần thi (BO1)
                if (r.bestOf == 1) {
                    // BO1: chỉ hiển thị 1 ván
                    label.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2)); // Viền xám đậm dày 2px
                    score = -1;
                } else {
                    // BO3: hiển thị tối đa 3 ván
                    label.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2)); // Viền xám đậm dày 2px
                    score = -1;
                }
            }

            if (score >= 0) {
                label.setText(String.valueOf(score));
            } else {
                label.setText("--");
            }

            return label;
        }
    }

    // --- ChipLabel đơn giản với 3 trạng thái ---
    private enum ChipStatus {
        OK, WARN, STALE
    }

    private static class ChipLabel extends JLabel {
        private ChipStatus status = ChipStatus.OK;

        ChipLabel() {
            super("00:00:00");
            setOpaque(true);
            setBorder(new EmptyBorder(2, 8, 2, 8));
            setFont(getFont().deriveFont(Font.BOLD, 14f)); // Tăng font size để dễ nhìn hơn
            putClientProperty("JComponent.roundRect", true); // FlatLaf hint
            apply();
        }

        void setStatus(ChipStatus s) {
            status = s;
            apply();
        }

        private void apply() {
            switch (status) {
                case OK:
                    setBackground(new Color(0, 170, 100, 200)); // Tăng độ đậm
                    setForeground(Color.WHITE); // Chữ trắng để dễ nhìn trên nền đen
                    break;
                case WARN:
                    setBackground(new Color(255, 165, 0, 200)); // Tăng độ đậm
                    setForeground(Color.WHITE); // Chữ trắng để dễ nhìn trên nền đen
                    break;
                case STALE:
                    setBackground(new Color(255, 0, 0, 200)); // Tăng độ đậm
                    setForeground(Color.WHITE); // Chữ trắng để dễ nhìn trên nền đen
                    break;
            }
        }
    }

    // ======= Simple viewer window to display one board =======
    private static class MonitorWindow extends javax.swing.JFrame {
        private final javax.swing.JTextArea area = new javax.swing.JTextArea();

        MonitorWindow(String key, String title) {
            super(title);
            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            area.setEditable(false);
            area.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));
            getContentPane().add(new javax.swing.JScrollPane(area));
            setSize(420, 260);
            setLocationByPlatform(true);
        }

        void update(Row r) {
            String courtInfo = (r.courtId != null && !r.courtId.isEmpty()) ? String.format("Sân %s • ", safe(r.courtId))
                    : "";

            // Tạo thông tin về điểm các ván đã hoàn thành
            String gameScoresInfo = "";
            if (r.gameScores != null && !r.gameScores.isEmpty()) {
                String[] gameScores = r.gameScores.split(",");
                StringBuilder scoresText = new StringBuilder("\nĐiểm các ván đã thi đấu:\n");
                for (int i = 0; i < gameScores.length; i++) {
                    String[] scores = gameScores[i].split(":");
                    if (scores.length == 2) {
                        scoresText.append("Ván ").append(i + 1).append(": ").append(scores[0]).append(" - ")
                                .append(scores[1]).append("\n");
                    }
                }
                gameScoresInfo = scoresText.toString();
            }

            String text = String.format(
                    "%s\n%s%s vs %s\nĐiểm hiện tại: %d - %d\nVán: %d / BO%d  (Thắng: %d - %d)%sCập nhật: %s",
                    r.header,
                    courtInfo,
                    MonitorTab.safe(r.nameA), MonitorTab.safe(r.nameB),
                    r.scoreA, r.scoreB,
                    r.game, r.bestOf,
                    r.gamesA, r.gamesB,
                    gameScoresInfo,
                    r.formatUpdated());
            area.setText(text);
        }
    }

    private void openViewerFor(Row r) {
        String key = r.viewerKey();
        MonitorWindow w = viewers.get(key);
        if (w == null) {
            String courtInfo = (r.courtId != null && !r.courtId.isEmpty()) ? String.format("[Sân %s] ", safe(r.courtId))
                    : "";
            String title = String.format("%s%s — %s@%s",
                    courtInfo,
                    (r.doubles ? "[Đôi] " : "[Đơn] ") + r.header,
                    safe(r.client), safe(r.host));
            w = new MonitorWindow(key, title);
            viewers.put(key, w);
        }
        w.update(r);
        if (!w.isVisible())
            w.setVisible(true);
    }

    // Helper to avoid null strings
    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private void closeViewer(String key) {
        MonitorWindow w = viewers.remove(key);
        if (w != null) {
            try {
                w.setVisible(false);
                w.dispose();
            } catch (Exception ignore) {
            }
        }
    }
}
