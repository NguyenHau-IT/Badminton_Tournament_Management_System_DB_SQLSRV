package com.example.badmintoneventtechnology.ui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.text.DecimalFormat;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.example.badmintoneventtechnology.config.AppProps;
import com.example.badmintoneventtechnology.model.db.MySqlConnectionManager;
import com.example.badmintoneventtechnology.service.db.DatabaseService;
import com.example.badmintoneventtechnology.ui.control.BadmintonControlPanel;
import com.example.badmintoneventtechnology.ui.control.MultiCourtControlPanel; // <-- NEW
import com.example.badmintoneventtechnology.ui.log.LogTab;
import com.example.badmintoneventtechnology.ui.monitor.MonitorTab;
import com.example.badmintoneventtechnology.ui.monitor.ScreenshotTab;
import com.example.badmintoneventtechnology.ui.net.NetworkConfig;
import com.example.badmintoneventtechnology.ui.tool.ConnectionsManagerPanel;
import com.example.badmintoneventtechnology.util.ui.IconUtil;
import com.example.badmintoneventtechnology.util.ui.Ui;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGIcon;

public class MainFrame extends JFrame {
    private final NetworkConfig netCfg; // cấu hình interface đã chọn
    private final MySqlConnectionManager manager = new MySqlConnectionManager();
    private final DatabaseService service = new DatabaseService(manager);
    // Service lấy nội dung và VĐV theo giải
    private final com.example.badmintoneventtechnology.service.db.GiaiRelatedService giaiRelatedService = new com.example.badmintoneventtechnology.service.db.GiaiRelatedService(
            service);
    // Panel hiển thị nội dung và VĐV
    private final com.example.badmintoneventtechnology.ui.tournament.GiaiContentPanel giaiContentPanel = new com.example.badmintoneventtechnology.ui.tournament.GiaiContentPanel(
            giaiRelatedService);

    private final BadmintonControlPanel controlPanel = new BadmintonControlPanel();
    private final MultiCourtControlPanel multiCourtPanel = new MultiCourtControlPanel();
    private final ConnectionsManagerPanel connMgrPanel = new ConnectionsManagerPanel();
    private final MonitorTab monitorTab = new MonitorTab();
    private final ScreenshotTab screenshotTab = new ScreenshotTab();
    private final LogTab logTab = new LogTab();
    // Tab quản lý giải đấu
    private final com.example.badmintoneventtechnology.ui.tournament.GiaiTab giaiTab = new com.example.badmintoneventtechnology.ui.tournament.GiaiTab(
            service);
    // Panel chọn giải đấu
    private final com.example.badmintoneventtechnology.ui.tournament.GiaiChooserPanel giaiChooserPanel = new com.example.badmintoneventtechnology.ui.tournament.GiaiChooserPanel(
            service);

    // UI fields
    private final JLabel lblAppTitle = new JLabel("Badminton Event Technology");
    private final JLabel lblVersion = new JLabel();
    private final JToggleButton themeToggle = new JToggleButton("Dark");
    private final JLabel statusConn = new JLabel("Chưa kết nối");
    private final JLabel statusHost = new JLabel("-"); // hiển thị IF/host:port
    private final JLabel statusMem = new JLabel();

    private final DecimalFormat df = new DecimalFormat("#,##0");

    // Tabs
    private final JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

    // Icons
    private final Icon icScore = loadIcon("/icons/scoreboard.svg", 20);
    private final Icon icConns = loadIcon("/icons/link-2.svg", 20);
    private final Icon icMonitor = loadIcon("/icons/monitor.svg", 20);
    private final Icon icScreenshot = loadIcon("/icons/camera.svg", 20);
    private final Icon icLog = loadIcon("/icons/file-text.svg", 20);
    private final Icon icMultiCourt = loadIcon("/icons/management.svg", 20);

    // RAM ticker
    private javax.swing.Timer ramTimer;

    public MainFrame() {
        this(null);
    }

    public MainFrame(NetworkConfig cfg) {
        super("Badminton Event Technology");
        // Lắng nghe chọn giải để load nội dung và VĐV (sau khi super đã gọi)
        giaiChooserPanel.addPropertyChangeListener(evt -> {
            if ("selectedGiai".equals(evt.getPropertyName())) {
                com.example.badmintoneventtechnology.model.tournament.Giai giai = (com.example.badmintoneventtechnology.model.tournament.Giai) evt
                        .getNewValue();
                giaiContentPanel.loadByGiai(giai);
            }
        });
        // Khi tab chọn giải được hiển thị, cũng hiển thị panel nội dung/VĐV
        ensureTabPresent("Nội dung & VĐV", giaiContentPanel, icScore);
        this.netCfg = cfg;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Ui.installModernUi();

        // Root
        JComponent root = (JComponent) getContentPane();
        root.setLayout(new BorderLayout());
        root.setBorder(new EmptyBorder(5, 5, 5, 5)); // Giảm border để tận dụng không gian màn hình

        // Header + Status
        JPanel header = buildHeaderBar();
        JPanel status = buildStatusBar();

        // Tab style - tối ưu cho toàn màn hình
        tabs.putClientProperty("JTabbedPane.tabType", "card");
        tabs.putClientProperty("JTabbedPane.showTabSeparators", true);
        tabs.putClientProperty("JTabbedPane.minimumTabWidth", 120); // Giảm để tận dụng không gian
        tabs.putClientProperty("JTabbedPane.tabWidthMode", "equal");
        tabs.putClientProperty("JTabbedPane.tabAreaAlignment", "leading");
        tabs.putClientProperty("JTabbedPane.tabsPopupPolicy", "asNeeded");
        tabs.putClientProperty("JTabbedPane.tabSelectionHeight", 3);
        tabs.putClientProperty("JTabbedPane.tabClosable", false); // Không cho phép đóng tab

        // Đảm bảo các tab chiếm toàn bộ không gian có sẵn
        tabs.setPreferredSize(new Dimension(1200, 800)); // Kích thước mặc định lớn hơn

        // Tự động kết nối và đăng nhập
        autoConnectAndLogin();

        // Lắp vào frame - để các tab chiếm toàn màn hình
        // Loại bỏ wrapCenter để các tab có thể mở rộng tối đa
        root.add(header, BorderLayout.NORTH);
        root.add(wrapCard(tabs), BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);

        // Nếu đã chọn interface từ bước trước, cấu hình MonitorTab nghe đúng IF đó
        if (netCfg != null && netCfg.ifName() != null && !netCfg.ifName().isBlank()) {
            try {
                NetworkInterface ni = NetworkInterface.getByName(netCfg.ifName());
                if (ni != null) {
                    monitorTab.setNetworkInterface(ni); // quan trọng
                    controlPanel.setNetworkInterface(ni); // dùng để hiển thị link điều khiển
                    multiCourtPanel.setNetworkInterface(ni); // dùng để hiển thị IP đúng cho web scoreboard
                }
                statusHost.setText("IF: " + netCfg.ifName()); // hiển thị tham khảo
            } catch (SocketException ignored) {
                statusHost.setText("IF: " + netCfg.ifName());
            }
        }

        // Window events
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Hiện thông báo xác nhận trước khi đóng
                int result = javax.swing.JOptionPane.showConfirmDialog(
                        MainFrame.this,
                        "Bạn có chắc muốn tắt ứng dụng?\n\nTất cả dữ liệu chưa lưu sẽ bị mất.",
                        "Xác nhận tắt ứng dụng",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE);

                ensureTabPresent("Giải đấu", giaiTab, icScore);
                ensureTabPresent("Chọn giải", giaiChooserPanel, icScore);
                if (result == javax.swing.JOptionPane.YES_OPTION) {
                    // Người dùng xác nhận, đóng ứng dụng
                    dispose();
                }
                // Nếu chọn NO hoặc Cancel, không làm gì cả - cửa sổ sẽ không đóng
            }

            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    controlPanel.saveSplitLocations();
                } catch (Exception ignored) {
                }

                try {
                    monitorTab.close();
                } catch (Exception ignored) {
                }
                try {
                    screenshotTab.cleanup();
                } catch (Exception ignored) {
                }
                try {
                    logTab.cleanup();
                } catch (Exception ignored) {
                }
                if (ramTimer != null)
                    ramTimer.stop();
                service.disconnect();
            }
        });

        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true); // Luôn hiển thị trên cùng
        IconUtil.applyTo(this);

        // Hiển thị version từ MANIFEST (nếu có)
        String implTitle = getClass().getPackage().getImplementationTitle();
        String implVer = getClass().getPackage().getImplementationVersion();
        if (implTitle != null)
            lblAppTitle.setText(implTitle);
        lblVersion.setText(implVer != null ? "v" + implVer : "");

        // RAM ticker
        ramTimer = new javax.swing.Timer(1000, ae -> updateMemory());
        ramTimer.start();

        // Auto-connect if configured
        // autoConnectIfConfigured(); // Commented out vì đã thay bằng
        // autoConnectAndLogin
    }

    private void autoConnectAndLogin() {
        // Tự động kết nối database
        String host = AppProps.get("db.host", "127.0.0.1");
        String port = AppProps.get("db.port", "3306");
        String name = AppProps.get("db.name", "bet_db");
        String user = AppProps.get("db.user", "root");
        String pass = AppProps.get("db.password", "");

        var cfg = new com.example.badmintoneventtechnology.model.db.ConnectionConfig()
                .host(host)
                .port(port)
                .databaseInput(name)
                .user(user)
                .password(pass)
                .mode(com.example.badmintoneventtechnology.model.db.ConnectionConfig.Mode.NAME);

        service.setConfig(cfg);
        try {
            Connection c = service.connect();
            // Propagate to panels
            try {
                connMgrPanel.setConnection(c);
            } catch (Exception ignored) {
            }
            try {
                controlPanel.setConnection(c);
            } catch (Exception ignored) {
            }
            try {
                multiCourtPanel.setConnection(c);
            } catch (Exception ignored) {
            }

            statusConn.setText("Đã kết nối");
            statusHost.setText(host + ":" + port);

            // Refresh data cho các tab giải đấu
            try {
                giaiTab.refreshData();
            } catch (Exception e) {
                System.err.println("Lỗi khi refresh giải tab: " + e.getMessage());
            }

            try {
                giaiChooserPanel.refreshData();
            } catch (Exception e) {
                System.err.println("Lỗi khi refresh giải chooser: " + e.getMessage());
            }

            // Tự động đăng nhập với quyền Admin
            monitorTab.setAdminMode(true, null);
            controlPanel.setClientName("ADMIN-auto");

            // Hiển thị tất cả các tab (Admin mode)
            ensureTabPresent("Quản lý giải", giaiTab, icScore);
            ensureTabPresent("Chọn giải", giaiChooserPanel, icScore);
            ensureTabPresent("Nội dung & VĐV", giaiContentPanel, icScore);
            ensureTabPresent("Nhiều sân", multiCourtPanel, icMultiCourt);
            ensureTabPresent("Connections", connMgrPanel, icConns);
            ensureTabPresent("Giám sát", monitorTab, icMonitor);
            ensureTabPresent("Screenshots", screenshotTab, icScreenshot);
            ensureTabPresent("Logs", logTab, icLog);

            // Bắt đầu từ tab chọn giải
            tabs.setSelectedComponent(giaiChooserPanel);

        } catch (Exception e) {
            // Nếu không kết nối được, hiển thị thông báo lỗi
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Không thể kết nối database!\n" +
                            "Host: " + host + ":" + port + "\n" +
                            "Database: " + name + "\n" +
                            "Lỗi: " + e.getMessage(),
                    "Lỗi kết nối",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /* -------------------- UI Builders -------------------- */

    private JPanel buildHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(new EmptyBorder(8, 8, 8, 8)); // Giảm border để tận dụng không gian màn hình

        JLabel avatar = new JLabel(IconUtil.loadRoundAvatar(36));

        lblAppTitle.setFont(lblAppTitle.getFont().deriveFont(Font.BOLD, 18f));
        lblVersion.setFont(lblAppTitle.getFont().deriveFont(Font.PLAIN, 12f));
        lblVersion.setForeground(new Color(110, 110, 110));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 0));
        titleBox.setOpaque(false);
        titleBox.add(lblAppTitle);
        titleBox.add(lblVersion);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(avatar);
        left.add(titleBox);

        themeToggle.setFocusable(false);
        themeToggle.setSelected(UIManager.getLookAndFeel() instanceof FlatDarkLaf);
        themeToggle.addActionListener(e -> switchTheme(themeToggle.isSelected()));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(themeToggle);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(new EmptyBorder(6, 8, 6, 8)); // Giảm border để tận dụng không gian màn hình

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(new JLabel("Trạng thái:"));
        left.add(statusConn);
        left.add(new JLabel(" | "));
        left.add(new JLabel("Máy chủ:"));
        left.add(statusHost);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(new JLabel("RAM:"));
        right.add(statusMem);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    /* -------------------- Tab helpers -------------------- */

    private void ensureTabPresent(String title, Component comp, Icon icon) {
        int idx = indexOf(comp);
        if (idx == -1)
            tabs.addTab(title, icon, comp);
        else {
            tabs.setTitleAt(idx, title);
            tabs.setIconAt(idx, icon);
        }
    }

    private int indexOf(Component comp) {
        for (int i = 0; i < tabs.getTabCount(); i++)
            if (tabs.getComponentAt(i) == comp)
                return i;
        return -1;
    }

    /* -------------------- Helpers -------------------- */

    private JComponent wrapCard(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(3, 3, 3, 3)); // Giảm border để tận dụng không gian màn hình
        p.add(c, BorderLayout.CENTER);
        p.setOpaque(false);
        return p;
    }

    private void switchTheme(boolean dark) {
        FlatAnimatedLafChange.showSnapshot();
        try {
            if (dark)
                UIManager.setLookAndFeel(new FlatDarkLaf());
            else
                UIManager.setLookAndFeel(new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ignored) {
        } finally {
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        }
    }

    private void updateMemory() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long max = rt.maxMemory() / (1024 * 1024);
        statusMem.setText(df.format(used) + " / " + df.format(max) + " MB");
    }

    private Icon loadIcon(String path, int size) {
        // FlatSVGIcon mong muốn đường dẫn classpath (không leading '/')
        String cp = path.startsWith("/") ? path.substring(1) : path;

        try {
            // Ưu tiên SVG từ classpath
            return new FlatSVGIcon(cp, size, size);
        } catch (Exception ex) {
            // fallback: PNG cùng tên (nếu có)
            String pngPath = cp.replace(".svg", ".png");
            var url = getClass().getClassLoader().getResource(pngPath);
            if (url != null) {
                Image img = new ImageIcon(url).getImage()
                        .getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }

            // log nhẹ để biết đường dẫn nào không tìm thấy
            System.err.println("Icon not found on classpath: " + cp + " or " + pngPath);
            return null;
        }
    }

    public DatabaseService service() {
        return service;
    }
}
