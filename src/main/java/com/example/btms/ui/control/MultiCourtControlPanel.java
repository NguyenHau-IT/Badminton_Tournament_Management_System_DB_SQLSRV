package com.example.btms.ui.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.sql.Connection;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.example.btms.model.match.CourtSession;
import com.example.btms.service.match.CourtManagerService;
import com.example.btms.service.match.CourtManagerService.CourtStatus;

/**
 * Panel điều khiển nhiều sân cầu lông cùng lúc
 */
public class MultiCourtControlPanel extends JPanel implements PropertyChangeListener {

    private final CourtManagerService courtManager = CourtManagerService.getInstance();
    private final JTabbedPane courtTabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    // Khi true, lần tạo tab kế tiếp sẽ KHÔNG phát idle broadcast (dùng cho hydrate
    // an toàn)
    private volatile boolean suppressIdleBroadcastOnce = false;
    // Tránh gắn trùng listeners lên cùng một BadmintonControlPanel khi tái gắn
    // (hydrate)
    private final java.util.Set<BadmintonControlPanel> wiredPanels = java.util.Collections
            .newSetFromMap(new java.util.WeakHashMap<>());

    /** Gọi trước khi hydrate để bỏ qua việc phát idle broadcast lần kế tiếp. */
    public void suppressIdleBroadcastOnce() {
        this.suppressIdleBroadcastOnce = true;
    }

    private final JPanel overviewPanel = new JPanel();
    // Lưu trạng thái hiển/ẩn PIN cho từng sân
    private final java.util.Map<String, Boolean> pinVisibleMap = new java.util.concurrent.ConcurrentHashMap<>();

    private final JTextField txtNewCourtHeader = new JTextField(15);
    // Database connection
    private Connection databaseConnection;

    // Network interface được chọn
    private NetworkInterface selectedIf;

    public MultiCourtControlPanel() {
        setupUI();
        // Đăng ký listener sau khi đối tượng đã khởi tạo hoàn chỉnh để tránh "leaking
        // this" trong constructor
        SwingUtilities.invokeLater(() -> courtManager.addPropertyChangeListener(MultiCourtControlPanel.this));
        refreshOverview();
        // Đảm bảo khi mở cửa sổ, các sân hiện có sẽ có tab điều khiển ngay
        SwingUtilities.invokeLater(this::ensureTabsForExistingCourts);
    }

    /** Set database connection for loading data */
    public void setConnection(Connection connection) {
        this.databaseConnection = connection;
    }

    /** Set network interface để hiển thị IP đúng cho web scoreboard */
    public void setNetworkInterface(NetworkInterface nif) {
        this.selectedIf = nif;

        // Cập nhật network interface cho tất cả các BadmintonControlPanel hiện có
        updateAllControlPanelsNetworkInterface();

        // Cập nhật UI nếu cần
        SwingUtilities.invokeLater(this::refreshOverview);
    }

    /** Cập nhật network interface cho tất cả các BadmintonControlPanel hiện có */
    private void updateAllControlPanelsNetworkInterface() {
        if (selectedIf == null)
            return;

        // Cập nhật cho tất cả các tab
        for (int i = 0; i < courtTabs.getTabCount(); i++) {
            var component = courtTabs.getComponentAt(i);
            if (component instanceof BadmintonControlPanel panel) {
                panel.setNetworkInterface(selectedIf);
            }
        }
    }

    /** Lấy network interface hiện tại */
    public NetworkInterface getNetworkInterface() {
        return selectedIf;
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createOverviewPanel());
        splitPane.setRightComponent(createCourtTabsPanel());
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Quản lý sân mới"));

        // Combo box chọn sân
        // Tạo danh sách 50 sân
        String[] courts = new String[50];
        for (int i = 0; i < courts.length; i++) {
            courts[i] = "Sân " + (i + 1);
        }

        // Tạo combo box
        JComboBox<String> courtCombo = new JComboBox<>(courts);

        courtCombo.setPreferredSize(new Dimension(100, 30));
        courtCombo.setFont(new Font("SansSerif", Font.BOLD, 13));

        panel.add(new JLabel("Chọn sân:"));
        panel.add(courtCombo);

        panel.add(new JLabel("Tiêu đề:"));
        panel.add(txtNewCourtHeader);

        JButton btnAddCourt = new JButton("Thêm sân");
        btnAddCourt.addActionListener(e -> addNewCourtFromCombo(courtCombo));
        panel.add(btnAddCourt);

        JButton btnCloseAll = new JButton("Đóng tất cả sân");
        btnCloseAll.addActionListener(e -> closeAllCourts());
        panel.add(btnCloseAll);

        // Nút mở bảng điều khiển (chỉ hiện/đưa tab điều khiển sân ra trước)
        JButton btnShowControl = new JButton("Hiện bảng điều khiển");
        btnShowControl.addActionListener(e -> showControlPanelFromCombo(courtCombo));
        panel.add(btnShowControl);

        return panel;
    }

    private JPanel createOverviewPanel() {
        overviewPanel.setLayout(new BoxLayout(overviewPanel, BoxLayout.Y_AXIS));
        overviewPanel.setBorder(BorderFactory.createTitledBorder("Tổng quan các sân"));

        JScrollPane scrollPane = new JScrollPane(overviewPanel);
        scrollPane.setPreferredSize(new Dimension(150, 400)); // hoặc 180/160 tùy bạn

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createCourtTabsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Điều khiển trận đấu"));
        panel.add(courtTabs, BorderLayout.CENTER);
        return panel;
    }

    private void createCourtControlTab(CourtSession session) {
        String tabTitle = session.courtId;

        // Nếu đã có control panel trong session thì tái sử dụng (hydrate an toàn)
        BadmintonControlPanel courtControlPanel;
        boolean createdNewPanel = false;
        if (session.controlPanel instanceof BadmintonControlPanel existing) {
            courtControlPanel = existing;
            // Nếu panel đang ở container khác, tháo ra trước khi add vào tab mới
            try {
                java.awt.Container parent = courtControlPanel.getParent();
                // Nếu parent hiện tại không phải là chính JTabbedPane của chúng ta,
                // mới cần remove để tránh component thuộc nhiều container.
                if (parent != null && parent != courtTabs) {
                    parent.remove(courtControlPanel);
                }
            } catch (Exception ignore) {
            }
        } else {
            // Tạo BadmintonControlPanel với kích thước responsive
            courtControlPanel = createResponsiveControlPanel();
            createdNewPanel = true;
            // Truyền database connection để load dữ liệu chỉ với panel mới
            if (databaseConnection != null) {
                courtControlPanel.setConnection(databaseConnection);
                // Reload dữ liệu từ database sau khi set connection
                try {
                    Method reloadMethod = BadmintonControlPanel.class.getDeclaredMethod("reloadListsFromDb");
                    reloadMethod.setAccessible(true);
                    reloadMethod.invoke(courtControlPanel);
                } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException
                        | InvocationTargetException e) {
                    System.err.println("Không thể reload dữ liệu từ DB: " + e.getMessage());
                }
            }
            // Lưu controlPanel vào session (ghi đè panel cũ nếu có)
            session.controlPanel = courtControlPanel;
        }

        // Set network interface nếu đã chọn
        if (selectedIf != null) {
            try {
                courtControlPanel.setNetworkInterface(selectedIf);
            } catch (Exception ignore) {
            }
        }

        // Set mã PIN cho BadmintonControlPanel (không ảnh hưởng state trận)
        try {
            Method setPinMethod = courtControlPanel.getClass().getDeclaredMethod("setCourtPinCode", String.class);
            setPinMethod.setAccessible(true);
            setPinMethod.invoke(courtControlPanel, session.pinCode);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException
                | InvocationTargetException e) {
            System.err.println("Không thể set mã PIN cho BadmintonControlPanel: " + e.getMessage());
        }

        // Set courtId cho BadmintonControlPanel để hiển thị trên monitor
        try {
            Method setCourtIdMethod = courtControlPanel.getClass().getDeclaredMethod("setCourtId", String.class);
            setCourtIdMethod.setAccessible(true);
            setCourtIdMethod.invoke(courtControlPanel, session.courtId);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException
                | InvocationTargetException e) {
            System.err.println("Không thể set courtId cho BadmintonControlPanel: " + e.getMessage());
        }

        // Chỉ gắn listeners một lần cho mỗi panel để tránh trùng lặp khi hydrate
        if (!wiredPanels.contains(courtControlPanel)) {
            // Thêm listener để cập nhật overview khi có thay đổi
            courtControlPanel.addPropertyChangeListener(evt -> SwingUtilities.invokeLater(this::refreshOverview));
            // Thêm listener cho các sự kiện thay đổi trong BadmintonControlPanel
            addControlPanelListeners(courtControlPanel);
            // Thêm listener cho thay đổi điểm số từ BadmintonMatch
            addScoreChangeListener(courtControlPanel);
            wiredPanels.add(courtControlPanel);
        }

        // Nếu tab đã tồn tại, thay thế component tại tab đó; nếu chưa, thêm tab mới
        int existingIdx = -1;
        for (int i = 0; i < courtTabs.getTabCount(); i++) {
            if (tabTitle.equals(courtTabs.getTitleAt(i))) {
                existingIdx = i;
                break;
            }
        }
        if (existingIdx >= 0) {
            courtTabs.setComponentAt(existingIdx, courtControlPanel);
            courtTabs.setSelectedIndex(existingIdx);
        } else {
            courtTabs.addTab(tabTitle, courtControlPanel);
            courtTabs.setSelectedComponent(courtControlPanel);
        }
        // Đảm bảo cập nhật UI ngay
        courtTabs.revalidate();
        courtTabs.repaint();

        // Phát broadcast ở trạng thái chờ để MonitorTab hiện card ngay khi mở sân
        // Chỉ thực hiện khi:
        // - KHÔNG bật suppressIdleBroadcastOnce, VÀ
        // - Panel mới được tạo (không phải hydrate), VÀ
        // - Trạng thái sân đang idle (không playing/paused/finished)
        boolean shouldStartIdle = false;
        try {
            Map<String, CourtStatus> all = courtManager.getAllCourtStatus();
            CourtStatus st = (all != null) ? all.get(session.courtId) : null;
            shouldStartIdle = (st == null) || (!st.isPlaying && !st.isPaused && !st.isFinished);
        } catch (Exception ignore) {
        }

        if (!suppressIdleBroadcastOnce && createdNewPanel && shouldStartIdle) {
            try {
                java.lang.reflect.Method idle = courtControlPanel.getClass().getDeclaredMethod("startIdleBroadcast",
                        String.class);
                idle.setAccessible(true);
                idle.invoke(courtControlPanel, session.header);
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException
                    | InvocationTargetException ex) {
                System.err.println("Không thể start idle broadcast khi tạo sân: " + ex.getMessage());
            }
        } else {
            // reset cờ sau khi bỏ qua một lần
            if (suppressIdleBroadcastOnce)
                suppressIdleBroadcastOnce = false;
        }
    }

    /**
     * Tạo tab điều khiển cho tất cả các sân đã tồn tại trong CourtManagerService
     * (nếu chưa có).
     */
    private void ensureTabsForExistingCourts() {
        try {
            Map<String, CourtStatus> all = courtManager.getAllCourtStatus();
            if (all == null || all.isEmpty())
                return;
            for (String courtId : all.keySet()) {
                // Bỏ qua nếu tab đã tồn tại
                boolean exists = false;
                for (int i = 0; i < courtTabs.getTabCount(); i++) {
                    if (courtId.equals(courtTabs.getTitleAt(i))) {
                        exists = true;
                        break;
                    }
                }
                if (exists)
                    continue;

                CourtSession session = courtManager.getCourt(courtId);
                if (session != null) {
                    createCourtControlTab(session);
                }
            }
        } catch (Exception ignore) {
        }
    }

    private void closeAllCourts() {
        // Chặn đóng nếu có sân đang thi đấu
        Map<String, CourtStatus> all = courtManager.getAllCourtStatus();
        java.util.List<String> playingCourts = new java.util.ArrayList<>();
        for (CourtStatus st : all.values()) {
            if (st.isPlaying) {
                playingCourts.add(st.courtId);
            }
        }

        if (!playingCourts.isEmpty()) {
            String msg = "Các sân sau đang thi đấu: " + String.join(", ", playingCourts)
                    + "\nVui lòng kết thúc các trận trước khi đóng tất cả sân.";
            JOptionPane.showMessageDialog(this, msg, "Đang thi đấu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn đóng tất cả các sân?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            // 1) Dừng broadcast cho tất cả các tab hiện có
            for (int i = 0; i < courtTabs.getTabCount(); i++) {
                java.awt.Component comp = courtTabs.getComponentAt(i);
                if (comp instanceof BadmintonControlPanel badmintonControlPanel) {
                    try {
                        badmintonControlPanel.stopBroadcast();
                    } catch (Exception ignore) {
                    }
                }
            }

            // 2) Đóng tất cả cửa sổ hiển thị nếu đang mở
            try {
                Map<String, CourtStatus> all2 = courtManager.getAllCourtStatus();
                for (String cid : all2.keySet()) {
                    com.example.btms.model.match.CourtSession session = courtManager.getCourt(cid);
                    if (session != null && session.display != null) {
                        try {
                            session.display.dispose();
                        } catch (Exception ignore) {
                        }
                    }
                }
            } catch (Exception ignore) {
            }

            // 3) Xoá tất cả sân trong service (tương đương removeCourt cho từng sân)
            courtManager.closeAllCourts();

            // 4) Xoá toàn bộ tab điều khiển
            courtTabs.removeAll();

            // 5) Cập nhật lại overview
            refreshOverview();
        }
    }

    private void refreshOverview() {
        overviewPanel.removeAll();

        Map<String, CourtStatus> allStatus = courtManager.getAllCourtStatus();
        if (allStatus.isEmpty()) {
            JLabel noCourtsLabel = new JLabel("Chưa có sân nào");
            noCourtsLabel.setAlignmentX(CENTER_ALIGNMENT);
            overviewPanel.add(noCourtsLabel);
        } else {
            // Sắp xếp các sân theo courtId
            allStatus.values().stream()
                    .sorted((s1, s2) -> {
                        // Tách số từ courtId (ví dụ: "Sân 1" -> 1, "Sân 10" -> 10)
                        int num1 = extractCourtNumber(s1.courtId);
                        int num2 = extractCourtNumber(s2.courtId);
                        return Integer.compare(num1, num2);
                    })
                    .forEach(status -> {
                        overviewPanel.add(createCourtStatusCard(status));
                        overviewPanel.add(Box.createVerticalStrut(6)); // hẹp hơn: 6px
                    });
        }

        overviewPanel.revalidate();
        overviewPanel.repaint();
    }

    /**
     * Trích xuất số từ courtId để sắp xếp
     * Ví dụ: "Sân 1" -> 1, "Sân 10" -> 10, "Court A" -> 0
     */
    private int extractCourtNumber(String courtId) {
        if (courtId == null)
            return 0;

        // Tìm số cuối cùng trong chuỗi
        String[] parts = courtId.split("\\s+");
        for (int i = parts.length - 1; i >= 0; i--) {
            try {
                return Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                // Bỏ qua nếu không phải số
            }
        }

        // Nếu không tìm thấy số, sắp xếp theo thứ tự alphabet
        return 0;
    }

    /**
     * Card trạng thái gọn: lưới bằng GridBagLayout, không kéo dãn theo chiều dọc
     */
    private JPanel createCourtStatusCard(CourtStatus status) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(6, 6, 6, 6)));
        card.setAlignmentX(LEFT_ALIGNMENT); // để BoxLayout không kéo full width khi không cần

        // Header + nút Đóng sân bên phải
        JPanel headerBar = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel(status.courtId + " - " + status.header);
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD));
        headerLabel.setBorder(new EmptyBorder(0, 0, 4, 0));
        headerBar.add(headerLabel, BorderLayout.WEST);
        JButton btnRemoveTop = new JButton("Đóng sân");
        btnRemoveTop.setMargin(new Insets(4, 12, 4, 12));
        btnRemoveTop.addActionListener(e -> removeCourt(status.courtId));
        JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightWrap.setOpaque(false);
        rightWrap.add(btnRemoveTop);
        headerBar.add(rightWrap, BorderLayout.EAST);
        headerBar.setOpaque(false);
        card.add(headerBar, BorderLayout.NORTH);

        // Info grid (gọn): dùng GridBagLayout thay vì GridLayout để không giãn dọc
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(2, 0, 2, 8); // hàng sít: 2px
        gc.fill = GridBagConstraints.NONE;

        // Trạng thái sân: Mở, Đang sử dụng, Đóng
        String courtState;
        if (!status.isDisplayOpen) {
            courtState = "Đóng";
        } else if (status.hasNames || status.isPlaying || status.isPaused || status.isFinished) {
            courtState = "Đang sử dụng"; // đã có cấu hình/tên hoặc có hoạt động
        } else {
            courtState = "Mở";
        }
        addInfoRow(infoPanel, gc, "Trạng thái sân:", courtState);

        // Trạng thái trận đấu: Chưa thi đấu, Đang thi đấu, Tạm dừng, Kết thúc
        String matchState;
        if (status.isFinished)
            matchState = "Kết thúc";
        else if (status.isPaused)
            matchState = "Tạm dừng";
        else if (status.isPlaying)
            matchState = "Đang thi đấu";
        else
            matchState = "Chưa thi đấu";
        addInfoRow(infoPanel, gc, "Trạng thái trận:", matchState);
        addInfoRow(infoPanel, gc, "Đội A:", status.names[0]);
        addInfoRow(infoPanel, gc, "Đội B:", status.names[1]);
        addInfoRow(infoPanel, gc, "Điểm:", status.score[0] + " - " + status.score[1]);
        addInfoRow(infoPanel, gc, "Ván:", status.games[0] + " - " + status.games[1]);
        // Hàng riêng cho Mã PIN để hỗ trợ ẩn/hiện
        boolean pinVisible = Boolean.TRUE.equals(pinVisibleMap.get(status.courtId));
        String pinText = pinVisible ? safePin(status.pinCode) : maskPin(status.pinCode);

        JLabel lk = new JLabel("Mã PIN:");
        lk.setFont(lk.getFont().deriveFont(Font.PLAIN, 12f));
        JLabel lv = new JLabel(pinText);
        lv.setFont(lv.getFont().deriveFont(Font.BOLD, 12f));

        gc.gridx = 0;
        gc.weightx = 0.0;
        infoPanel.add(lk, gc);
        gc.gridx = 1;
        gc.weightx = 1.0;
        infoPanel.add(lv, gc);
        gc.gridy++;

        card.add(infoPanel, BorderLayout.CENTER);

        // Buttons (gọn)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        buttonPanel.setOpaque(false);
        JButton btnTogglePin = new JButton(pinVisible ? "Ẩn PIN" : "Hiện PIN");
        btnTogglePin.setMargin(new Insets(4, 12, 4, 12));
        btnTogglePin.addActionListener(e -> {
            boolean current = Boolean.TRUE.equals(pinVisibleMap.get(status.courtId));
            boolean next = !current;
            pinVisibleMap.put(status.courtId, next);
            lv.setText(next ? safePin(status.pinCode) : maskPin(status.pinCode));
            btnTogglePin.setText(next ? "Ẩn PIN" : "Hiện PIN");
        });
        buttonPanel.add(btnTogglePin);
        // Nút hiện bảng điều khiển cho sân này
        JButton btnShowCtl = new JButton("Hiện điều khiển");
        btnShowCtl.setMargin(new Insets(4, 12, 4, 12));
        btnShowCtl.addActionListener(e -> showControlPanelForCourtId(status.courtId));
        buttonPanel.add(btnShowCtl);

        // Nút Đóng sân đã chuyển lên header
        card.add(buttonPanel, BorderLayout.SOUTH);

        // Chặn card bị kéo cao khi nằm trong BoxLayout Y_AXIS
        SwingUtilities.invokeLater(
                () -> card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height)));

        return card;
    }

    /** Thêm một hàng (key, value) cho infoPanel */
    private void addInfoRow(JPanel panel, GridBagConstraints gc, String key, String val) {
        JLabel lk = new JLabel(key);
        lk.setFont(lk.getFont().deriveFont(Font.PLAIN, 12f));

        JLabel lv = new JLabel(val);
        lv.setFont(lv.getFont().deriveFont(Font.BOLD, 12f));

        // cột trái
        gc.gridx = 0;
        gc.weightx = 0.0;
        panel.add(lk, gc);

        // cột phải
        gc.gridx = 1;
        gc.weightx = 1.0;
        panel.add(lv, gc);

        gc.gridy++;
    }

    /** Ẩn PIN bằng dấu chấm, giữ độ dài tương đương */
    private static String maskPin(String pin) {
        if (pin == null || pin.isBlank())
            return "-";
        int len = pin.trim().length();
        return "•".repeat(Math.max(1, len));
    }

    /** Chuẩn hóa hiển thị PIN (tránh null/empty) */
    private static String safePin(String pin) {
        return (pin == null || pin.isBlank()) ? "-" : pin;
    }

    private void removeCourt(String courtId) {
        // Chặn đóng sân nếu đang thi đấu
        CourtStatus st = courtManager.getAllCourtStatus().get(courtId);
        if (st != null && st.isPlaying) {
            JOptionPane.showMessageDialog(this,
                    courtId + " đang thi đấu. Vui lòng kết thúc trận trước khi đóng sân.",
                    "Đang thi đấu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn đóng sân " + courtId + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            CourtSession session = courtManager.getCourt(courtId);
            if (session != null && session.display != null) {
                try {
                    session.display.dispose();
                } catch (Exception ignore) {
                }
            }

            // Dừng broadcast nếu có
            if (session != null && session.controlPanel instanceof BadmintonControlPanel) {
                try {
                    BadmintonControlPanel cp = (BadmintonControlPanel) session.controlPanel;
                    cp.stopBroadcast();
                } catch (Exception ignore) {
                }
            }

            courtManager.removeCourt(courtId);

            for (int i = 0; i < courtTabs.getTabCount(); i++) {
                if (courtTabs.getTitleAt(i).equals(courtId)) {
                    courtTabs.removeTabAt(i);
                    break;
                }
            }
            refreshOverview();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(this::refreshOverview);
    }

    /** Tạo BadmintonControlPanel với kích thước responsive theo hiển thị */
    private BadmintonControlPanel createResponsiveControlPanel() {
        BadmintonControlPanel panel = new BadmintonControlPanel();

        // Set network interface để hiển thị IP đúng cho web scoreboard
        if (selectedIf != null) {
            panel.setNetworkInterface(selectedIf);
        }

        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        int optimalWidth = Math.min(screenWidth * 3 / 4, 800);
        int optimalHeight = Math.min(screenHeight * 2 / 3, 600);

        int minWidth = Math.max(500, screenWidth / 4);
        int minHeight = Math.max(300, screenHeight / 4);

        int maxWidth = Math.min(screenWidth * 4 / 5, 1000);
        int maxHeight = Math.min(screenHeight * 3 / 4, 800);

        panel.setPreferredSize(new Dimension(optimalWidth, optimalHeight));
        panel.setMaximumSize(new Dimension(maxWidth, maxHeight));
        panel.setMinimumSize(new Dimension(minWidth, minHeight));
        panel.setSize(new Dimension(optimalWidth, optimalHeight));

        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int currentWidth = panel.getWidth();
                int currentHeight = panel.getHeight();

                if (currentWidth > maxWidth || currentHeight > maxHeight) {
                    panel.setSize(Math.min(currentWidth, maxWidth), Math.min(currentHeight, maxHeight));
                }
                if (currentWidth < minWidth || currentHeight < minHeight) {
                    panel.setSize(Math.max(currentWidth, minWidth), Math.max(currentHeight, minHeight));
                }
            }
        });

        return panel;
    }

    /** Thêm các listener để theo dõi thay đổi trong BadmintonControlPanel */
    private void addControlPanelListeners(BadmintonControlPanel panel) {
        try {
            java.lang.reflect.Field cboNameAField = panel.getClass().getDeclaredField("cboNameA");
            java.lang.reflect.Field cboNameBField = panel.getClass().getDeclaredField("cboNameB");
            java.lang.reflect.Field cboTeamAField = panel.getClass().getDeclaredField("cboTeamA");
            java.lang.reflect.Field cboTeamBField = panel.getClass().getDeclaredField("cboTeamB");
            java.lang.reflect.Field cboHeaderSinglesField = panel.getClass().getDeclaredField("cboHeaderSingles");
            java.lang.reflect.Field cboHeaderDoublesField = panel.getClass().getDeclaredField("cboHeaderDoubles");
            java.lang.reflect.Field bestOfField = panel.getClass().getDeclaredField("bestOf");
            java.lang.reflect.Field doublesField = panel.getClass().getDeclaredField("doubles");

            cboNameAField.setAccessible(true);
            cboNameBField.setAccessible(true);
            cboTeamAField.setAccessible(true);
            cboTeamBField.setAccessible(true);
            cboHeaderSinglesField.setAccessible(true);
            cboHeaderDoublesField.setAccessible(true);
            bestOfField.setAccessible(true);
            doublesField.setAccessible(true);

            javax.swing.JComboBox<?> cboNameA = (javax.swing.JComboBox<?>) cboNameAField.get(panel);
            javax.swing.JComboBox<?> cboNameB = (javax.swing.JComboBox<?>) cboNameBField.get(panel);
            javax.swing.JComboBox<?> cboTeamA = (javax.swing.JComboBox<?>) cboTeamAField.get(panel);
            javax.swing.JComboBox<?> cboTeamB = (javax.swing.JComboBox<?>) cboTeamBField.get(panel);
            javax.swing.JComboBox<?> cboHeaderSingles = (javax.swing.JComboBox<?>) cboHeaderSinglesField.get(panel);
            javax.swing.JComboBox<?> cboHeaderDoubles = (javax.swing.JComboBox<?>) cboHeaderDoublesField.get(panel);
            javax.swing.JComboBox<?> bestOfCombo = (javax.swing.JComboBox<?>) bestOfField.get(panel);
            javax.swing.JCheckBox doublesCheck = (javax.swing.JCheckBox) doublesField.get(panel);

            java.awt.event.ActionListener refreshListener = e -> SwingUtilities.invokeLater(this::refreshOverview);
            java.awt.event.ItemListener itemRefreshListener = e -> SwingUtilities.invokeLater(this::refreshOverview);

            cboNameA.addActionListener(refreshListener);
            cboNameB.addActionListener(refreshListener);
            cboTeamA.addActionListener(refreshListener);
            cboTeamB.addActionListener(refreshListener);
            cboHeaderSingles.addActionListener(refreshListener);
            cboHeaderDoubles.addActionListener(refreshListener);
            bestOfCombo.addActionListener(refreshListener);
            doublesCheck.addItemListener(itemRefreshListener);

        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            System.err.println("Không thể thêm listener cho BadmintonControlPanel: " + e.getMessage());
        }
    }

    /** Thêm listener để theo dõi thay đổi điểm số từ BadmintonMatch */
    private void addScoreChangeListener(BadmintonControlPanel panel) {
        try {
            Method getMatchMethod = panel.getClass().getDeclaredMethod("getMatch");
            getMatchMethod.setAccessible(true);
            Object match = getMatchMethod.invoke(panel);

            if (match != null) {
                Method addPropertyChangeListenerMethod = match.getClass().getDeclaredMethod("addPropertyChangeListener",
                        java.beans.PropertyChangeListener.class);
                addPropertyChangeListenerMethod.setAccessible(true);

                java.beans.PropertyChangeListener scoreListener = evt -> {
                    String propertyName = evt.getPropertyName();
                    if ("score".equals(propertyName) || "games".equals(propertyName) ||
                            "gameNumber".equals(propertyName) || "server".equals(propertyName) ||
                            "matchFinished".equals(propertyName) || "betweenGamesInterval".equals(propertyName)
                            || "manualPaused".equals(propertyName) || "status".equals(propertyName)) {
                        SwingUtilities.invokeLater(this::refreshOverview);
                    }
                };
                addPropertyChangeListenerMethod.invoke(match, scoreListener);
            }
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException
                | InvocationTargetException e) {
            System.err.println("Không thể thêm listener cho thay đổi điểm số: " + e.getMessage());
        }

        // Fallback timer
        javax.swing.Timer scoreCheckTimer = new javax.swing.Timer(500,
                evt -> SwingUtilities.invokeLater(this::refreshOverview));
        scoreCheckTimer.start();

        addScoreButtonListeners(panel);
    }

    /** Thêm listener trực tiếp cho các nút điểm số */
    private void addScoreButtonListeners(BadmintonControlPanel panel) {
        try {
            java.lang.reflect.Field aPlusField = panel.getClass().getDeclaredField("aPlus");
            java.lang.reflect.Field bPlusField = panel.getClass().getDeclaredField("bPlus");
            java.lang.reflect.Field aMinusField = panel.getClass().getDeclaredField("aMinus");
            java.lang.reflect.Field bMinusField = panel.getClass().getDeclaredField("bMinus");
            java.lang.reflect.Field undoField = panel.getClass().getDeclaredField("undo");
            java.lang.reflect.Field nextGameField = panel.getClass().getDeclaredField("nextGame");

            aPlusField.setAccessible(true);
            bPlusField.setAccessible(true);
            aMinusField.setAccessible(true);
            bMinusField.setAccessible(true);
            undoField.setAccessible(true);
            nextGameField.setAccessible(true);

            javax.swing.JButton aPlus = (javax.swing.JButton) aPlusField.get(panel);
            javax.swing.JButton bPlus = (javax.swing.JButton) bPlusField.get(panel);
            javax.swing.JButton aMinus = (javax.swing.JButton) aMinusField.get(panel);
            javax.swing.JButton bMinus = (javax.swing.JButton) bMinusField.get(panel);
            javax.swing.JButton undo = (javax.swing.JButton) undoField.get(panel);
            javax.swing.JButton nextGame = (javax.swing.JButton) nextGameField.get(panel);

            java.awt.event.ActionListener scoreButtonListener = createDebouncedRefreshListener();

            aPlus.addActionListener(scoreButtonListener);
            bPlus.addActionListener(scoreButtonListener);
            aMinus.addActionListener(scoreButtonListener);
            bMinus.addActionListener(scoreButtonListener);
            undo.addActionListener(scoreButtonListener);
            nextGame.addActionListener(scoreButtonListener);

        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            System.err.println("Không thể thêm listener cho nút điểm số: " + e.getMessage());
        }
    }

    /** Debounce refresh để tránh gọi quá dày */
    private java.awt.event.ActionListener createDebouncedRefreshListener() {
        return new java.awt.event.ActionListener() {
            private javax.swing.Timer timer;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (timer != null) {
                    timer.restart();
                } else {
                    timer = new javax.swing.Timer(100, evt -> {
                        SwingUtilities.invokeLater(MultiCourtControlPanel.this::refreshOverview);
                        timer = null;
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            }
        };
    }

    /** Thêm sân mới từ combo box chọn sân */
    private void addNewCourtFromCombo(JComboBox<String> courtCombo) {
        String courtId = (String) courtCombo.getSelectedItem();
        String header = txtNewCourtHeader.getText().trim();

        if (courtId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sân", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (header.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tiêu đề", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (courtManager.getCourt(courtId) != null) {
            JOptionPane.showMessageDialog(this, "Sân với ID này đã tồn tại", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        CourtSession session = courtManager.createCourt(courtId, header);
        createCourtControlTab(session);
        txtNewCourtHeader.setText("");

        // Tự động chọn courtId kế tiếp
        selectNextAvailableCourt(courtCombo);

        refreshOverview();
    }

    /**
     * Hiện (mở/đưa ra trước) tab bảng điều khiển cho sân được chọn trong combo box.
     * Không tạo sân mới; nếu sân chưa được tạo, yêu cầu người dùng thêm sân trước.
     */
    private void showControlPanelFromCombo(JComboBox<String> courtCombo) {
        String courtId = (String) courtCombo.getSelectedItem();
        showControlPanelForCourtId(courtId);
    }

    /** Hiện (mở/đưa ra trước) tab bảng điều khiển cho sân theo courtId. */
    private void showControlPanelForCourtId(String courtId) {
        if (courtId == null || courtId.isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sân", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        CourtSession session = courtManager.getCourt(courtId);
        if (session == null) {
            JOptionPane.showMessageDialog(this, "Sân này chưa được tạo. Vui lòng thêm sân trước.",
                    "Chưa có sân", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idx = -1;
        for (int i = 0; i < courtTabs.getTabCount(); i++) {
            if (courtId.equals(courtTabs.getTitleAt(i))) {
                idx = i;
                break;
            }
        }
        if (idx >= 0) {
            courtTabs.setSelectedIndex(idx);
        } else {
            createCourtControlTab(session);
        }
        try {
            java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (win != null) {
                win.setVisible(true);
                win.toFront();
                win.requestFocus();
            }
        } catch (Exception ignore) {
        }
    }

    /**
     * Chọn courtId kế tiếp có sẵn trong combo box
     */
    private void selectNextAvailableCourt(JComboBox<String> courtCombo) {
        // Tạo danh sách 50 sân: "Sân 1" -> "Sân 50"
        String[] allCourts = new String[50];
        for (int i = 0; i < 50; i++) {
            allCourts[i] = "Sân " + (i + 1);
        }

        for (String court : allCourts) {
            if (courtManager.getCourt(court) == null) {
                // Tìm thấy sân trống, chọn nó
                courtCombo.setSelectedItem(court);
                return;
            }
        }

        // Nếu tất cả sân đều đã được tạo, chọn sân đầu tiên
        if (allCourts.length > 0) {
            courtCombo.setSelectedItem(allCourts[0]);
        }
    }

}
