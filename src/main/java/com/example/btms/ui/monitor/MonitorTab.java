package com.example.btms.ui.monitor;

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
import javax.swing.JComponent;
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

    // === Field phục vụ tách/ghim (tab hoặc card) ===
    private JFrame floatFrame; // cửa sổ nổi
    private JTabbedPane dockTabs; // nếu môi trường cũ dùng JTabbedPane
    private int dockIndex = -1; // vị trí tab cũ
    private String dockTitle; // tiêu đề tab cũ
    private Icon dockIcon; // icon tab cũ
    private boolean shuttingDown = false; // tránh gắn lại khi app đóng
    // Hỗ trợ CardLayout (phiên bản mới không còn JTabbedPane)
    private java.awt.Container originalParent; // parent sử dụng CardLayout
    private String originalCardName; // tên card (được gắn qua clientProperty "cardName")
    private JPanel floatingPlaceholder; // placeholder khi panel đang được tách ra (CardLayout)
    // Tạm thời luôn dùng mirror-mode (không reparent) để tránh lỗi cửa sổ trắng
    // trên một số máy
    private boolean forceMirrorMode = true; // Set false nếu muốn thử lại cơ chế tách thực sự

    // Setting: số cột hiển thị (mặc định 3, cho phép 1..4) – cấu hình qua
    // SettingsPanel chung
    private int columns = 3; // (Đã bỏ combo nội bộ)
    private final Preferences prefs = Preferences.userNodeForPackage(MonitorTab.class);

    public MonitorTab() {
        this(false, null); // Default to client mode
    }

    public MonitorTab(boolean adminMode, String clientId) {
        super(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        // Đặt background rõ ràng để tránh trắng toàn bộ khi tách ra frame mới
        try {
            Color bg = UIManager.getColor("Panel.background");
            if (bg != null)
                setBackground(bg);
            else
                setBackground(Color.DARK_GRAY);
        } catch (Exception ignore) {
            setBackground(Color.DARK_GRAY);
        }

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
        add(scroll, BorderLayout.CENTER); // chỉ còn danh sách; cài đặt chuyển sang trang Settings

        // Tải số cột đã lưu
        try {
            int savedCols = Math.max(1, Math.min(4, prefs.getInt("monitor.columns", columns)));
            columns = savedCols;
            // không còn combo nội bộ để setSelected
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
            // Tăng min width khi giảm số cột để card rộng hơn rõ rệt
            int minWidth;
            switch (Math.max(1, Math.min(6, columns))) {
                case 1 -> minWidth = 600;
                case 2 -> minWidth = 420;
                case 3 -> minWidth = 320;
                case 4 -> minWidth = 280;
                case 5 -> minWidth = 240;
                default -> minWidth = 220;
            }
            int cellWidth = Math.max(minWidth, (vw - totalGap) / columns);
            list.setFixedCellWidth(cellWidth);
            // Điều chỉnh chiều cao card theo số cột (cao hơn trước)
            int cellH;
            switch (Math.max(1, Math.min(6, columns))) {
                case 1 -> cellH = 340;
                case 2 -> cellH = 300;
                case 3 -> cellH = 260;
                default -> cellH = 230;
            }
            list.setFixedCellHeight(cellH);
            // Lưu số cột hiện tại để renderer có thể điều chỉnh tỉ lệ ô tên VĐV
            list.putClientProperty("monitor.columns", columns);
            list.revalidate();
            list.repaint();
        } catch (Exception ignore) {
        }
    }

    /** Cho phép trang Cài đặt chỉnh số cột. */
    public void setColumns(int cols) {
        int newCols = Math.max(1, Math.min(4, cols));
        if (newCols == this.columns)
            return;
        this.columns = newCols;
        try {
            prefs.putInt("monitor.columns", newCols);
        } catch (Exception ignore) {
        }
        SwingUtilities.invokeLater(() -> {
            updateCellWidthForColumns();
            list.putClientProperty("monitor.columns", this.columns);
            list.revalidate();
            list.repaint();
        });
    }

    private void rxLoop() {
        MulticastSocket ms = null;
        try {
            ms = new MulticastSocket(PORT);
            InetAddress group = InetAddress.getByName(GROUP);

            if (nif != null) {
                // Dùng interface do MainFrame cung cấp (ưu tiên cấu hình người dùng)
                ms.setNetworkInterface(nif);
                ms.joinGroup(new InetSocketAddress(group, PORT), nif);
            } else {
                // Tự tìm một network interface multicast thay vì gọi API cũ (deprecated)
                NetworkInterface chosen = null;
                try {
                    chosen = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
                } catch (Exception ignore) {
                }
                try {
                    if (chosen == null || !chosen.isUp() || chosen.isLoopback() || !chosen.supportsMulticast()) {
                        var en = NetworkInterface.getNetworkInterfaces();
                        while (en.hasMoreElements()) {
                            NetworkInterface ni = en.nextElement();
                            if (ni != null && ni.isUp() && !ni.isLoopback() && ni.supportsMulticast()) {
                                chosen = ni;
                                break;
                            }
                        }
                    }
                } catch (Exception ignore) {
                }
                if (chosen != null) {
                    try {
                        ms.setNetworkInterface(chosen);
                        ms.joinGroup(new InetSocketAddress(group, PORT), chosen);
                    } catch (Exception ex) {
                        // Fallback cuối cùng – thử API cũ (đã deprecated) nếu tất cả đều thất bại
                        try {
                            @SuppressWarnings("deprecation")
                            var __unused = 0; // chỉ để gắn suppress trong block
                            ms.joinGroup(group);
                        } catch (Exception ignore2) {
                        }
                    }
                } else {
                    // Không tìm được interface phù hợp – thử loopback hoặc fallback deprecated
                    try {
                        NetworkInterface loop = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
                        if (loop != null) {
                            ms.setNetworkInterface(loop);
                            ms.joinGroup(new InetSocketAddress(group, PORT), loop);
                        } else {
                            @SuppressWarnings("deprecation")
                            var __unused = 0;
                            ms.joinGroup(group);
                        }
                    } catch (Exception ex) {
                        try {
                            @SuppressWarnings("deprecation")
                            var __unused = 0;
                            ms.joinGroup(group);
                        } catch (Exception ignore3) {
                        }
                    }
                }
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
            // Xoá tất cả entries có cùng sid (có thể có nhiều sân dùng chung sid)
            java.util.List<String> keysToRemove = new java.util.ArrayList<>();
            for (var e : sessions.entrySet()) {
                Row row = e.getValue();
                if (row != null && java.util.Objects.equals(row.sid, sid)) {
                    keysToRemove.add(e.getKey());
                }
            }
            for (String k : keysToRemove) {
                Row removed = sessions.remove(k);
                if (removed != null) {
                    // Đóng viewer tương ứng
                    closeViewer(removed.viewerKey());
                }
            }

            // Đồng bộ xoá khỏi listModel ngay lập tức theo sid để cập nhật UI tức thời
            if (sid != null && !sid.isEmpty()) {
                for (int i = listModel.size() - 1; i >= 0; i--) {
                    Row r = listModel.getElementAt(i);
                    if (r != null && java.util.Objects.equals(r.sid, sid)) {
                        try {
                            listModel.removeElementAt(i);
                        } catch (Exception ignore) {
                        }
                    }
                }
            }

            // Yêu cầu refresh UI ngay để phản ánh việc xoá
            SwingUtilities.invokeLater(this::refreshCards);
            return;
        }
        if (!"UPSERT".equalsIgnoreCase(op)) {
            return;
        }

        // Lọc dữ liệu dựa trên mode
        String clientId = m.getOrDefault("client", "");

        // Dùng key kết hợp để tránh đè khi nhiều sân dùng chung sid
        String courtIdFromMsg = m.getOrDefault("courtId", "");
        String key = sid + "|" + (courtIdFromMsg == null ? "" : courtIdFromMsg);
        Row r = sessions.computeIfAbsent(key, k -> {
            return new Row();
        });

        // Gắn sid để đảm bảo identity duy nhất theo phiên
        r.sid = sid;

        r.client = clientId;
        r.host = m.getOrDefault("host", "");
        r.courtId = m.getOrDefault("courtId", ""); // Parse courtId từ broadcast
        r.header = m.getOrDefault("header", "TRẬN ĐẤU");
        r.kind = m.getOrDefault("kind", "HORIZONTAL");
        r.doubles = "true".equalsIgnoreCase(m.getOrDefault("doubles", "false"));
        r.nameA = m.getOrDefault("nameA", "");
        r.nameB = m.getOrDefault("nameB", "");
        // Nhận tên CLB (nếu có)
        r.clubA = m.getOrDefault("clubA", "");
        r.clubB = m.getOrDefault("clubB", "");
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

        // Nếu viewer đang mở cho phiên này, cập nhật ngay nội dung
        SwingUtilities.invokeLater(() -> updateViewerIfOpen(r));

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
                    // Phát sự kiện thay đổi để tất cả JList (bao gồm cửa sổ mirror) vẽ lại
                    // Ngay cả khi object tham chiếu không đổi, set() sẽ fire contentsChanged
                    listModel.set(i, currentRow);
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
        row.clubA = "";
        row.clubB = "";
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
                // Đóng viewer nếu đang mở cho row này
                try {
                    closeViewer(row.viewerKey());
                } catch (Exception ignore) {
                }
                listModel.removeElementAt(i);
                System.out.println("Đã xóa card: " + row.client + " - " + row.header);
            }
        }
    }

    /**
     * Cập nhật nội dung của cửa sổ viewer nếu đang mở ứng với Row r
     */
    private void updateViewerIfOpen(Row r) {
        try {
            MonitorWindow w = viewers.get(r.viewerKey());
            if (w != null) {
                w.update(r);
            }
        } catch (Exception ignore) {
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
        target.clubA = source.clubA;
        target.clubB = source.clubB;
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

    private boolean isSameRowIdentity(Row r1, Row r2) {
        if (r1 == null || r2 == null)
            return false;
        // Ưu tiên so sánh theo sid nếu có
        if (r1.sid != null && r2.sid != null) {
            return java.util.Objects.equals(r1.sid, r2.sid);
        }
        // Sau đó so sánh theo courtId nếu có, để phân biệt nhiều sân cùng
        // client/host/header
        if (r1.courtId != null && !r1.courtId.isEmpty() && r2.courtId != null && !r2.courtId.isEmpty()) {
            return java.util.Objects.equals(r1.client, r2.client) &&
                    java.util.Objects.equals(r1.host, r2.host) &&
                    java.util.Objects.equals(r1.header, r2.header) &&
                    java.util.Objects.equals(r1.courtId, r2.courtId);
        }
        // Fallback cũ
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
        // Bọc trong EDT (an toàn khi sau này gọi từ thread khác)
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> toggleFloat(sourceBtn));
            return;
        }

        // --- Workaround: luôn dùng chế độ mirror thay vì reparent ---
        if (forceMirrorMode) {
            if (floatFrame == null) {
                Dimension sz = getSize();
                if (sz == null || sz.width < 200 || sz.height < 150) {
                    sz = new Dimension(900, 600);
                }
                createMirrorFloating(sourceBtn, sz);
                System.out.println("[MonitorTab] Opened mirror floating window (forceMirrorMode=true)");
            } else {
                try {
                    floatFrame.dispose();
                } catch (Exception ignore) {
                }
                floatFrame = null;
                sourceBtn.setText("Mở cửa sổ");
                System.out.println("[MonitorTab] Closed mirror floating window");
            }
            return; // Bỏ qua logic reparent phía dưới
        }

        if (floatFrame == null) {
            // Lưu lại kích thước để dùng lại nếu người dùng đã resize trước đó
            final Dimension initialSize = getSize();

            // Thử môi trường JTabbedPane trước (tương thích cũ)
            JTabbedPane tabs = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
            if (tabs != null) {
                dockTabs = tabs;
                dockIndex = tabs.indexOfComponent(this);
                if (dockIndex >= 0) {
                    dockTitle = tabs.getTitleAt(dockIndex);
                    dockIcon = tabs.getIconAt(dockIndex);
                    tabs.remove(this);
                }
            } else {
                // CardLayout path
                originalParent = getParent();
                if (originalParent != null) {
                    Object cn = null;
                    try {
                        cn = ((JComponent) this).getClientProperty("cardName");
                    } catch (Exception ignore) {
                    }
                    originalCardName = (cn != null) ? cn.toString() : ("Monitor-" + System.identityHashCode(this));
                    try {
                        // Thêm placeholder để tránh khoảng trắng trên MainFrame khi panel bị remove
                        if (floatingPlaceholder == null) {
                            floatingPlaceholder = new JPanel(new BorderLayout());
                            floatingPlaceholder.add(new JLabel(
                                    "Giám sát đang mở ở cửa sổ riêng - bấm 'Thu về' để quay lại", JLabel.CENTER),
                                    BorderLayout.CENTER);
                            floatingPlaceholder.putClientProperty("cardName", originalCardName);
                        }
                        originalParent.remove(this); // tách ra
                        try {
                            originalParent.add(floatingPlaceholder, originalCardName);
                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    originalParent.revalidate();
                    originalParent.repaint();
                }
            }

            // Khởi tạo frame nổi
            floatFrame = new JFrame(
                    dockTitle != null ? dockTitle : (originalCardName != null ? originalCardName : "Monitor"));
            floatFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            floatFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (!shuttingDown) {
                        reattachToContainer();
                        sourceBtn.setText("Mở cửa sổ");
                    }
                }
            });

            // Đảm bảo contentPane có BorderLayout
            if (!(floatFrame.getContentPane().getLayout() instanceof BorderLayout)) {
                floatFrame.getContentPane().setLayout(new BorderLayout());
            }

            floatFrame.getContentPane().add(this, BorderLayout.CENTER);
            // Ép layout & vẽ lại để tránh trắng; không dùng pack để giữ layout hiện tại
            floatFrame.getContentPane().revalidate();
            floatFrame.getContentPane().repaint();
            this.revalidate();
            this.repaint();

            // Gán kích thước trực tiếp (tránh 0x0 rồi bị trắng do chưa layout xong)
            Dimension effectiveSize = initialSize;
            if (effectiveSize == null || effectiveSize.width < 300 || effectiveSize.height < 200) {
                effectiveSize = new Dimension(900, 600);
            }
            floatFrame.setMinimumSize(
                    new Dimension(Math.max(640, effectiveSize.width), Math.max(480, effectiveSize.height)));
            floatFrame.setSize(Math.max(900, effectiveSize.width), Math.max(600, effectiveSize.height));

            final Dimension mirrorSize = effectiveSize;
            SwingUtilities.invokeLater(() -> {
                updateCellWidthForColumns(); // cập nhật lại width cell sau khi tách
                list.repaint();
            });

            floatFrame.setLocationRelativeTo(null);
            floatFrame.setVisible(true);
            floatFrame.toFront();
            floatFrame.requestFocus();
            sourceBtn.setText("Thu về");

            System.out.println("[MonitorTab] Đã tách ra cửa sổ nổi. Root children=" + getComponentCount() +
                    ", listModel=" + listModel.getSize());

            // Fallback: nếu vì lý do nào đó không có component con -> tạo dummy label để
            // người dùng thấy
            if (getComponentCount() == 0) {
                add(new JLabel("(MonitorTab rỗng - fallback)"), BorderLayout.CENTER);
                revalidate();
                repaint();
                System.out.println("[MonitorTab] Fallback label added vì componentCount==0");
            }

            // Fallback mới: nếu cửa sổ trắng dù listModel > 0 thì tạo mirror frame không
            // reparent
            SwingUtilities.invokeLater(() -> {
                boolean blank = isFloatingBlank();
                if (blank && listModel.getSize() > 0) {
                    System.out.println("[MonitorTab] Phát hiện cửa sổ nổi trắng -> dùng chế độ mirror.");
                    // Khôi phục panel về parent cũ (nếu placeholder tồn tại sẽ bị thay)
                    reattachToContainer();
                    // Tạo mirror frame dùng model chung nhưng JList khác
                    createMirrorFloating(sourceBtn, mirrorSize);
                }
            });
        } else {
            // Đang nổi -> quay về
            floatFrame.dispose(); // windowClosed sẽ reattach
        }
    }

    // Kiểm tra xem trong frame nổi hiện tại có JList hoặc ScrollPane hợp lệ không
    private boolean isFloatingBlank() {
        if (floatFrame == null)
            return false;
        java.util.List<Component> stack = new java.util.ArrayList<>();
        stack.add(floatFrame.getContentPane());
        boolean hasList = false;
        while (!stack.isEmpty()) {
            Component c = stack.remove(stack.size() - 1);
            if (c instanceof JList) {
                hasList = true;
                break;
            }
            if (c instanceof java.awt.Container cont) {
                for (Component ch : cont.getComponents())
                    stack.add(ch);
            }
        }
        if (!hasList)
            return true;
        return false;
    }

    // Tạo frame mirror: không remove MonitorTab; build UI nhẹ để hiển thị danh sách
    private void createMirrorFloating(JButton sourceBtn, Dimension preferred) {
        JFrame mirror = new JFrame(originalCardName != null ? originalCardName : "Monitor");
        mirror.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(8, 8));
        JLabel title = new JLabel(isAdminMode ? "Giám sát (Admin) - Mirror" : "Giám sát - Mirror");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> mirror.dispose());
        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.WEST);
        top.add(btnClose, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);
        // JList mirror chia sẻ model
        JList<Row> mirrorList = new JList<>(listModel);
        mirrorList.setCellRenderer(new CardRenderer());
        mirrorList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        mirrorList.setVisibleRowCount(-1);
        mirrorList.setFixedCellWidth(list.getFixedCellWidth());
        mirrorList.setFixedCellHeight(list.getFixedCellHeight());
        root.add(new JScrollPane(mirrorList), BorderLayout.CENTER);
        mirror.setContentPane(root);
        if (preferred == null)
            preferred = new Dimension(900, 600);
        mirror.setSize(Math.max(900, preferred.width), Math.max(600, preferred.height));
        mirror.setLocationRelativeTo(null);
        mirror.setVisible(true);
        mirror.toFront();
        mirror.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                sourceBtn.setText("Mở cửa sổ");
            }
        });
        // Gán floatFrame sang mirror để logic đóng vẫn dùng
        this.floatFrame = mirror;
        sourceBtn.setText("Thu về");
    }

    private void reattachToContainer() {
        if (floatFrame != null) {
            try {
                floatFrame.getContentPane().remove(this);
            } catch (Exception ignore) {
            }
            floatFrame = null;
        }
        if (dockTabs != null) { // legacy JTabbedPane
            int idx = dockIndex >= 0 ? Math.min(dockIndex, dockTabs.getTabCount()) : dockTabs.getTabCount();
            dockTabs.insertTab(dockTitle != null ? dockTitle : "Monitor", dockIcon, this, null, idx);
            dockTabs.setSelectedComponent(this);
        } else if (originalParent != null) { // CardLayout
            try {
                // Loại bỏ placeholder nếu có
                if (floatingPlaceholder != null) {
                    try {
                        originalParent.remove(floatingPlaceholder);
                    } catch (Exception ignore) {
                    }
                }
                originalParent.add(this, originalCardName);
                originalParent.revalidate();
                originalParent.repaint();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void close() {
        shuttingDown = true; // để windowClosed không tự gắn lại khi đóng app
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
        String client, host, header, kind, nameA, nameB, clubA, clubB, courtId; // Thêm courtId và tên CLB
        boolean doubles;
        int game, bestOf, scoreA, scoreB, gamesA, gamesB;
        long updated;
        boolean markedForRemoval = false; // Flag để đánh dấu Row cần xóa
        String gameScores = ""; // Điểm của các ván đã hoàn thành (format: "21:19,19:21")
        String sid; // định danh phiên duy nhất để phân biệt nhiều sân

        String formatUpdated() {
            return new SimpleDateFormat("HH:mm:ss").format(new Date(updated));
        }

        String viewerKey() {
            // Bao gồm courtId và sid để đảm bảo key duy nhất cho mỗi sân/phiên
            String cid = (courtId == null ? "" : courtId);
            String s = (sid == null ? "" : sid);
            return cid + "|" + s + "|" + (client == null ? "" : client) + "@" + (host == null ? "" : host)
                    + ":" + (header == null ? "" : header);
        }
    }

    // --- Renderer tạo card hiện đại ---
    private static class CardRenderer extends JPanel implements ListCellRenderer<Row> {
        private final JLabel lblHeader = new JLabel();
        private final JLabel lblMeta = new JLabel();
        private final JLabel lblClubA = new JLabel();
        private final JLabel lblNames = new JLabel();
        private final JLabel lblVS = new JLabel();
        private final JLabel lblClubB = new JLabel();
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
            lblClubA.setFont(lblClubA.getFont().deriveFont(Font.ITALIC, 13f));
            lblClubA.setForeground(Color.LIGHT_GRAY);
            lblNames.setFont(lblNames.getFont().deriveFont(Font.BOLD, 16f)); // Tăng font size tên đội A
            lblNames.setForeground(Color.WHITE); // Thay đổi màu chữ tên thành trắng
            lblClubB.setFont(lblClubB.getFont().deriveFont(Font.ITALIC, 13f));
            lblClubB.setForeground(Color.LIGHT_GRAY);
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
            nameAPanel.setPreferredSize(new Dimension(250, 60));
            nameAPanel.setMinimumSize(new Dimension(250, 60));
            nameAPanel.setMaximumSize(new Dimension(250, 60));
            nameAPanel.setSize(new Dimension(250, 60));
            nameAPanel.add(lblClubA, BorderLayout.NORTH);
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
            nameBPanel.setPreferredSize(new Dimension(250, 60));
            nameBPanel.setMinimumSize(new Dimension(250, 60));
            nameBPanel.setMaximumSize(new Dimension(250, 60));
            nameBPanel.setSize(new Dimension(250, 60));
            nameBPanel.add(lblClubB, BorderLayout.NORTH);
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
            // Lấy số cột hiện tại từ client property (do MonitorTab set)
            Object colProp = list.getClientProperty("monitor.columns");
            int cols = 3;
            if (colProp instanceof Integer i)
                cols = i;
            // Tăng tỉ lệ chiều rộng ô tên VĐV khi số cột ít đi
            double nameRatio;
            int minName;
            switch (Math.max(1, Math.min(6, cols))) {
                case 1 -> {
                    nameRatio = 0.70;
                    minName = 240;
                }
                case 2 -> {
                    nameRatio = 0.60;
                    minName = 200;
                }
                case 3 -> {
                    nameRatio = 0.50;
                    minName = 160;
                }
                default -> {
                    nameRatio = 0.45;
                    minName = 150;
                }
            }
            int nameW = Math.max(minName, (int) Math.round(cellW * nameRatio));
            // Cao hơn khi ít cột để hiển thị thêm dòng CLB
            int nameH;
            switch (Math.max(1, Math.min(6, cols))) {
                case 1 -> nameH = 90;
                case 2 -> nameH = 75;
                case 3 -> nameH = 65;
                default -> nameH = 60;
            }
            Dimension nameSize = new Dimension(nameW, nameH);
            nameAPanel.setPreferredSize(nameSize);
            nameAPanel.setMinimumSize(nameSize);
            nameAPanel.setMaximumSize(new Dimension(nameW, Integer.MAX_VALUE));
            nameBPanel.setPreferredSize(nameSize);
            nameBPanel.setMinimumSize(nameSize);
            nameBPanel.setMaximumSize(new Dimension(nameW, Integer.MAX_VALUE));

            // Đặt kích thước renderer theo ô – tăng chiều cao khi giảm số cột
            int cellH;
            switch (Math.max(1, Math.min(6, cols))) {
                case 1 -> cellH = 340;
                case 2 -> cellH = 300;
                case 3 -> cellH = 260;
                default -> cellH = 230;
            }
            Dimension fixedSize = new Dimension(cellW, cellH);
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

            // Cập nhật dòng CLB nếu có
            lblClubA.setText(safe(r.clubA));
            lblClubB.setText(safe(r.clubB));

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
            // Kích thước ô điểm theo số cột (tăng chiều cao như yêu cầu)
            Dimension scoreBoxSize;
            float scoreFont;
            switch (Math.max(1, Math.min(6, cols))) {
                case 1 -> {
                    scoreBoxSize = new Dimension(80, 70); // giảm chiều rộng một chút
                    scoreFont = 32f;
                }
                case 2 -> {
                    scoreBoxSize = new Dimension(72, 64); // giảm chiều rộng một chút
                    scoreFont = 28f;
                }
                case 3 -> {
                    scoreBoxSize = new Dimension(60, 56); // giảm chiều rộng một chút
                    scoreFont = 26f;
                }
                default -> {
                    scoreBoxSize = new Dimension(58, 52); // giảm chiều rộng một chút
                    scoreFont = 24f;
                }
            }
            int maxGames = Math.min(r.bestOf, 3); // Giới hạn tối đa 3 ván
            for (int i = 0; i < maxGames; i++) {
                JLabel scoreLabel = createScoreLabel(i, r, true, scoreBoxSize, scoreFont);
                scoreAPanel.add(scoreLabel);
                // Không có khoảng cách giữa các ô điểm, viền sẽ tự nhiên chia ra
            }

            // Tạo các ô điểm cho đội B
            scoreBPanel.removeAll();
            scoreBPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0)); // Không có khoảng cách
            for (int i = 0; i < maxGames; i++) {
                JLabel scoreLabel = createScoreLabel(i, r, false, scoreBoxSize, scoreFont);
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

        private JLabel createScoreLabel(int index, Row r, boolean isTeamA, Dimension boxSize, float fontSize) {
            JLabel label = new JLabel();
            label.setFont(label.getFont().deriveFont(Font.BOLD, fontSize));
            label.setForeground(Color.YELLOW); // Thay đổi chữ thành màu vàng
            label.setOpaque(true); // Bật background
            label.setBackground(Color.GRAY); // Thay đổi background thành màu xám
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // Viền đen dày 2px để chia rõ ràng
            label.setPreferredSize(boxSize);
            label.setMinimumSize(boxSize);
            label.setMaximumSize(boxSize);
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
            applyViewerPreferences(w); // áp dụng cài đặt ngay khi tạo
        }
        w.update(r);
        if (!w.isVisible())
            w.setVisible(true);
    }

    /** Áp dụng các cài đặt (always-on-top, font scale) cho cửa sổ viewer. */
    private void applyViewerPreferences(MonitorWindow w) {
        try {
            java.util.prefs.Preferences p = java.util.prefs.Preferences.userRoot().node("demo.h2client");
            boolean onTop = p.getBoolean("ui.alwaysOnTop", false);
            int scalePct = p.getInt("ui.fontScalePercent", 100);
            w.setAlwaysOnTop(onTop);
            if (scalePct != 100) {
                float mul = scalePct / 100f;
                java.awt.Font f = w.getFont();
                if (f != null) {
                    java.awt.Font nf = f.deriveFont(f.getSize2D() * mul);
                    w.setFont(nf);
                }
                // scale text area if exists
                for (java.awt.Component c : w.getContentPane().getComponents()) {
                    scaleFontsRecursively(c, mul);
                }
            }
        } catch (Exception ignore) {
        }
    }

    /**
     * Được gọi từ MainFrame nếu cần cập nhật tất cả viewer sau khi đổi setting
     * runtime.
     */
    public void refreshAllViewerSettings() {
        for (MonitorWindow w : viewers.values()) {
            applyViewerPreferences(w);
        }
    }

    private void scaleFontsRecursively(java.awt.Component c, float mul) {
        try {
            java.awt.Font f = c.getFont();
            if (f != null)
                c.setFont(f.deriveFont(f.getSize2D() * mul));
        } catch (Exception ignore) {
        }
        if (c instanceof java.awt.Container cont) {
            for (java.awt.Component ch : cont.getComponents()) {
                scaleFontsRecursively(ch, mul);
            }
        }
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
