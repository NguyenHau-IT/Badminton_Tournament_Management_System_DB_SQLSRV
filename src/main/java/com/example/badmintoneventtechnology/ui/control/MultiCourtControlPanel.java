package com.example.badmintoneventtechnology.ui.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

import com.example.badmintoneventtechnology.model.match.CourtSession;
import com.example.badmintoneventtechnology.service.match.CourtManagerService;
import com.example.badmintoneventtechnology.service.match.CourtManagerService.CourtStatus;
import com.example.badmintoneventtechnology.ui.scoreboard.BadmintonDisplayFrame;
import com.example.badmintoneventtechnology.ui.scoreboard.BadmintonDisplayHorizontalFrame;

/**
 * Panel điều khiển nhiều sân cầu lông cùng lúc
 */
public class MultiCourtControlPanel extends JPanel implements PropertyChangeListener {

    private final CourtManagerService courtManager = CourtManagerService.getInstance();
    private final JTabbedPane courtTabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    private final JPanel overviewPanel = new JPanel();

    // Controls
    private final JTextField txtNewCourtId = new JTextField(10);
    private final JTextField txtNewCourtHeader = new JTextField(15);
    private final JComboBox<String> cboDisplayKind = new JComboBox<>(new String[] { "Dọc", "Ngang" });

    // Database connection
    private Connection databaseConnection;

    // Network interface được chọn
    private NetworkInterface selectedIf;

    public MultiCourtControlPanel() {
        setupUI();
        courtManager.addPropertyChangeListener(this);
        refreshOverview();
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
            if (component instanceof BadmintonControlPanel) {
                BadmintonControlPanel panel = (BadmintonControlPanel) component;
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
        JComboBox<String> courtCombo = new JComboBox<>(
                new String[] { "Sân 1", "Sân 2", "Sân 3", "Sân 4", "Sân 5", "Sân 6", "Sân 7", "Sân 8", "Sân 9",
                        "Sân 10" });
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

    private void addNewCourt(ActionEvent e) {
        String courtId = txtNewCourtId.getText().trim();
        String header = txtNewCourtHeader.getText().trim();

        if (courtId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập ID sân", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (courtManager.getCourt(courtId) != null) {
            JOptionPane.showMessageDialog(this, "Sân với ID này đã tồn tại", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        CourtSession session = courtManager.createCourt(courtId, header);

        // Tạo tab điều khiển cho sân mới
        createCourtControlTab(session);

        // Clear input fields
        txtNewCourtId.setText("");
        txtNewCourtHeader.setText("");

        refreshOverview();
    }

    private void createCourtControlTab(CourtSession session) {
        String tabTitle = session.courtId;

        // Tạo BadmintonControlPanel với kích thước responsive
        BadmintonControlPanel courtControlPanel = createResponsiveControlPanel();

        // Truyền database connection để load dữ liệu
        if (databaseConnection != null) {
            courtControlPanel.setConnection(databaseConnection);
            // Reload dữ liệu từ database sau khi set connection
            try {
                Method reloadMethod = BadmintonControlPanel.class.getDeclaredMethod("reloadListsFromDb");
                reloadMethod.setAccessible(true);
                reloadMethod.invoke(courtControlPanel);
            } catch (Exception e) {
                System.err.println("Không thể reload dữ liệu từ DB: " + e.getMessage());
            }
        }

        // Lưu controlPanel vào session
        session.controlPanel = courtControlPanel;

        // Set mã PIN cho BadmintonControlPanel
        try {
            Method setPinMethod = courtControlPanel.getClass().getDeclaredMethod("setCourtPinCode", String.class);
            setPinMethod.setAccessible(true);
            setPinMethod.invoke(courtControlPanel, session.pinCode);
        } catch (Exception e) {
            System.err.println("Không thể set mã PIN cho BadmintonControlPanel: " + e.getMessage());
        }

        // Set courtId cho BadmintonControlPanel để hiển thị trên monitor
        try {
            Method setCourtIdMethod = courtControlPanel.getClass().getDeclaredMethod("setCourtId", String.class);
            setCourtIdMethod.setAccessible(true);
            setCourtIdMethod.invoke(courtControlPanel, session.courtId);
        } catch (Exception e) {
            System.err.println("Không thể set courtId cho BadmintonControlPanel: " + e.getMessage());
        }

        // Thêm listener để cập nhật overview khi có thay đổi
        courtControlPanel.addPropertyChangeListener(evt -> SwingUtilities.invokeLater(this::refreshOverview));

        // Thêm listener cho các sự kiện thay đổi trong BadmintonControlPanel
        addControlPanelListeners(courtControlPanel);

        // Thêm listener cho thay đổi điểm số từ BadmintonMatch
        addScoreChangeListener(courtControlPanel);

        courtTabs.addTab(tabTitle, courtControlPanel);
        courtTabs.setSelectedComponent(courtControlPanel);

        // Phát broadcast ở trạng thái chờ để MonitorTab hiện card ngay khi mở sân
        try {
            java.lang.reflect.Method idle = courtControlPanel.getClass().getDeclaredMethod("startIdleBroadcast",
                    String.class);
            idle.setAccessible(true);
            idle.invoke(courtControlPanel, session.header);
        } catch (Exception ex) {
            System.err.println("Không thể start idle broadcast khi tạo sân: " + ex.getMessage());
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
                if (comp instanceof BadmintonControlPanel) {
                    try {
                        ((BadmintonControlPanel) comp).stopBroadcast();
                    } catch (Exception ignore) {
                    }
                }
            }

            // 2) Đóng tất cả cửa sổ hiển thị nếu đang mở
            try {
                Map<String, CourtStatus> all2 = courtManager.getAllCourtStatus();
                for (String cid : all2.keySet()) {
                    com.example.badmintoneventtechnology.model.match.CourtSession session = courtManager.getCourt(cid);
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

        // Header
        JLabel headerLabel = new JLabel(status.courtId + " - " + status.header);
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD));
        headerLabel.setBorder(new EmptyBorder(0, 0, 4, 0));
        card.add(headerLabel, BorderLayout.NORTH);

        // Info grid (gọn): dùng GridBagLayout thay vì GridLayout để không giãn dọc
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(2, 0, 2, 8); // hàng sít: 2px
        gc.fill = GridBagConstraints.NONE;

        addInfoRow(infoPanel, gc, "Trạng thái:", (status.isDisplayOpen ? "Đang mở" : "Đã đóng"));
        addInfoRow(infoPanel, gc, "Thi đấu:", (status.isPlaying ? "Đang thi đấu" : "Chưa thi đấu"));
        addInfoRow(infoPanel, gc, "Đội A:", status.names[0]);
        addInfoRow(infoPanel, gc, "Đội B:", status.names[1]);
        addInfoRow(infoPanel, gc, "Điểm:", status.score[0] + " - " + status.score[1]);
        addInfoRow(infoPanel, gc, "Ván:", status.games[0] + " - " + status.games[1]);
        addInfoRow(infoPanel, gc, "Mã PIN:", status.pinCode);

        card.add(infoPanel, BorderLayout.CENTER);

        // Buttons (gọn)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        buttonPanel.setOpaque(false);
        JButton btnRemove = new JButton("Đóng sân");
        btnRemove.setMargin(new Insets(4, 12, 4, 12));
        btnRemove.addActionListener(e -> removeCourt(status.courtId));
        buttonPanel.add(btnRemove);
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

    private void openCourtDisplay(String courtId) {
        CourtSession session = courtManager.getCourt(courtId);
        if (session == null)
            return;

        if (session.display != null) {
            session.display.toFront();
            return;
        }

        if (session.display == null) {
            if (cboDisplayKind.getSelectedIndex() == 0) { // Dọc
                session.display = new BadmintonDisplayFrame(session.match);
            } else { // Ngang
                session.display = new BadmintonDisplayHorizontalFrame(session.match);
            }
        }

        session.horizontal = (cboDisplayKind.getSelectedIndex() == 1);
        session.display.setVisible(true);

        refreshOverview();
    }

    /** Bắt đầu trận đấu cho sân - hiển thị trong khung điều khiển trận đấu */
    private void startCourtMatch(String courtId) {
        CourtSession session = courtManager.getCourt(courtId);
        if (session == null)
            return;

        if (session.controlPanel != null) {
            for (int i = 0; i < courtTabs.getTabCount(); i++) {
                if (courtTabs.getTitleAt(i).equals(courtId)) {
                    courtTabs.setSelectedIndex(i);
                    return;
                }
            }
        }

        BadmintonControlPanel courtControlPanel = createResponsiveControlPanel();

        if (databaseConnection != null) {
            courtControlPanel.setConnection(databaseConnection);
            try {
                Method reloadMethod = BadmintonControlPanel.class.getDeclaredMethod("reloadListsFromDb");
                reloadMethod.setAccessible(true);
                reloadMethod.invoke(courtControlPanel);
            } catch (Exception e) {
                System.err.println("Không thể reload dữ liệu từ DB: " + e.getMessage());
            }
        }

        for (int i = 0; i < courtTabs.getTabCount(); i++) {
            if (courtTabs.getTitleAt(i).equals(courtId)) {
                courtTabs.setComponentAt(i, courtControlPanel);
                courtTabs.setSelectedIndex(i);

                session.controlPanel = courtControlPanel;

                try {
                    Method setPinMethod = courtControlPanel.getClass()
                            .getDeclaredMethod("setCourtPinCode", String.class);
                    setPinMethod.setAccessible(true);
                    setPinMethod.invoke(courtControlPanel, session.pinCode);
                } catch (Exception e) {
                    System.err.println("Không thể set mã PIN cho BadmintonControlPanel: " + e.getMessage());
                }

                // Set courtId cho BadmintonControlPanel để hiển thị trên monitor
                try {
                    Method setCourtIdMethod = courtControlPanel.getClass().getDeclaredMethod("setCourtId",
                            String.class);
                    setCourtIdMethod.setAccessible(true);
                    setCourtIdMethod.invoke(courtControlPanel, session.courtId);
                } catch (Exception e) {
                    System.err.println("Không thể set courtId cho BadmintonControlPanel: " + e.getMessage());
                }

                courtControlPanel.addPropertyChangeListener(evt -> SwingUtilities.invokeLater(this::refreshOverview));
                addControlPanelListeners(courtControlPanel);
                addScoreChangeListener(courtControlPanel);

                refreshOverview();
                return;
            }
        }
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

        } catch (Exception e) {
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
                            "matchFinished".equals(propertyName) || "betweenGamesInterval".equals(propertyName)) {
                        SwingUtilities.invokeLater(this::refreshOverview);
                    }
                };
                addPropertyChangeListenerMethod.invoke(match, scoreListener);
            }
        } catch (Exception e) {
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

        } catch (Exception e) {
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
     * Chọn courtId kế tiếp có sẵn trong combo box
     */
    private void selectNextAvailableCourt(JComboBox<String> courtCombo) {
        String[] allCourts = { "Sân 1", "Sân 2", "Sân 3", "Sân 4", "Sân 5", "Sân 6", "Sân 7", "Sân 8", "Sân 9",
                "Sân 10" };

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
