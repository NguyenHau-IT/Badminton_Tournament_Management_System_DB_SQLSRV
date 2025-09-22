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

import javax.swing.Box;
import javax.swing.BoxLayout;
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

import com.example.badmintoneventtechnology.config.DatabaseConfig;
import com.example.badmintoneventtechnology.config.Prefs;
import com.example.badmintoneventtechnology.model.db.ConnectionConfig;
import com.example.badmintoneventtechnology.model.db.SQLSRVConnectionManager;
import com.example.badmintoneventtechnology.repository.category.NoiDungRepository;
import com.example.badmintoneventtechnology.service.auth.AuthService;
import com.example.badmintoneventtechnology.service.category.NoiDungService;
import com.example.badmintoneventtechnology.service.db.DatabaseService;
import com.example.badmintoneventtechnology.ui.auth.LoginTab;
import com.example.badmintoneventtechnology.ui.auth.LoginTab.Role;
import com.example.badmintoneventtechnology.ui.category.NoiDungManagementPanel;
import com.example.badmintoneventtechnology.ui.connect.ConnectPanel;
import com.example.badmintoneventtechnology.ui.control.BadmintonControlPanel;
import com.example.badmintoneventtechnology.ui.control.MultiCourtControlPanel;
import com.example.badmintoneventtechnology.ui.log.LogTab;
import com.example.badmintoneventtechnology.ui.monitor.MonitorTab;
import com.example.badmintoneventtechnology.ui.monitor.ScreenshotTab;
import com.example.badmintoneventtechnology.ui.net.NetworkConfig;
import com.example.badmintoneventtechnology.ui.tournament.GiaiDauSelectPanel;
import com.example.badmintoneventtechnology.ui.tournament.TournamentTabPanel;
import com.example.badmintoneventtechnology.util.ui.IconUtil;
import com.example.badmintoneventtechnology.util.ui.Ui;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGIcon;

public class MainFrame extends JFrame {

    // KHÔNG final: sẽ được tạo sau khi có Connection
    private NoiDungService noiDungService;
    private NoiDungManagementPanel noiDungPanel;

    private final Prefs prefs = new Prefs();
    private final NetworkConfig netCfg; // cấu hình interface đã chọn
    private final SQLSRVConnectionManager manager = new SQLSRVConnectionManager();
    private final DatabaseService service = new DatabaseService(manager);
    private final DatabaseConfig dbConfig;

    private final BadmintonControlPanel controlPanel = new BadmintonControlPanel();
    private final MultiCourtControlPanel multiCourtPanel = new MultiCourtControlPanel();
    private final MonitorTab monitorTab = new MonitorTab();
    private final ScreenshotTab screenshotTab = new ScreenshotTab();
    private final LogTab logTab = new LogTab();
    private final LoginTab loginTab = new LoginTab();
    private final TournamentTabPanel tournamentTabPanel = new TournamentTabPanel(service);

    private AuthService authService;

    // UI fields
    private final JLabel lblAppTitle = new JLabel("Badminton Event Technology");
    private final JLabel lblVersion = new JLabel();
    private final JToggleButton themeToggle = new JToggleButton("Dark");
    private final JLabel statusConn = new JLabel("Chưa kết nối");
    private final JLabel statusHost = new JLabel("-");
    private final JLabel statusMem = new JLabel();

    private final DecimalFormat df = new DecimalFormat("#,##0");

    private final JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    private ConnectPanel connectPanel;

    // Icons
    private final Icon icConnect = loadIcon("/icons/plug.svg", 20);
    private final Icon icScore = loadIcon("/icons/scoreboard.svg", 20);
    private final Icon icConns = loadIcon("/icons/link-2.svg", 20);
    private final Icon icMonitor = loadIcon("/icons/monitor.svg", 20);
    private final Icon icScreenshot = loadIcon("/icons/camera.svg", 20);
    private final Icon icLog = loadIcon("/icons/file-text.svg", 20);
    private final Icon icLogin = loadIcon("/icons/login.svg", 20);
    private final Icon icMultiCourt = loadIcon("/icons/management.svg", 20);
    private final Icon icTournament = loadIcon("/icons/trophy.svg", 20);

    private javax.swing.Timer ramTimer;

    private GiaiDauSelectPanel giaiDauSelectPanel;

    public MainFrame() {
        this(null, null);
    }

    public MainFrame(NetworkConfig cfg, DatabaseConfig dbConfig) {
        super("Badminton Event Technology");
        this.netCfg = cfg;
        this.dbConfig = dbConfig;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Ui.installModernUi();

        JComponent root = (JComponent) getContentPane();
        root.setLayout(new BorderLayout());
        root.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel header = buildHeaderBar();
        JPanel status = buildStatusBar();

        tabs.putClientProperty("JTabbedPane.tabType", "card");
        tabs.putClientProperty("JTabbedPane.showTabSeparators", true);
        tabs.putClientProperty("JTabbedPane.minimumTabWidth", 120);
        tabs.putClientProperty("JTabbedPane.tabWidthMode", "equal");
        tabs.putClientProperty("JTabbedPane.tabAreaAlignment", "leading");
        tabs.putClientProperty("JTabbedPane.tabsPopupPolicy", "asNeeded");
        tabs.putClientProperty("JTabbedPane.tabSelectionHeight", 3);
        tabs.putClientProperty("JTabbedPane.tabClosable", false);
        tabs.setPreferredSize(new Dimension(1200, 800));

        connectPanel = new ConnectPanel(service, prefs, new ConnectPanel.ConnectionConsumer() {
            @Override
            public void onConnected(Connection c, String host, String port) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        controlPanel.setConnection(c);
                    } catch (Exception ignored) {
                    }
                    try {
                        multiCourtPanel.setConnection(c);
                    } catch (Exception ignored) {
                    }
                    try {
                        tournamentTabPanel.updateConnection();
                    } catch (Exception ignored) {
                    }

                    updateAuthService(c);
                    statusConn.setText("Đã kết nối");

                    ensureTabPresent("Login", loginTab, icLogin);
                    ensureTabPresent("Control", controlPanel, icScore);
                    ensureTabPresent("Multi Court", multiCourtPanel, icMultiCourt);
                    ensureTabPresent("Tournament", tournamentTabPanel, icTournament);
                    ensureTabPresent("Monitor", monitorTab, icMonitor);
                    ensureTabPresent("Screenshot", screenshotTab, icScreenshot);
                    ensureTabPresent("Log", logTab, icLog);
                    tabs.setSelectedComponent(loginTab);
                });
            }

            @Override
            public void onDisconnected() {
                SwingUtilities.invokeLater(() -> {
                    try {
                        controlPanel.setConnection(null);
                    } catch (Exception ignored) {
                    }
                    try {
                        multiCourtPanel.setConnection(null);
                    } catch (Exception ignored) {
                    }
                    try {
                        tournamentTabPanel.updateConnection();
                    } catch (Exception ignored) {
                    }

                    try {
                        tournamentTabPanel.unlockSelection();
                    } catch (Exception ignored) {
                    }

                    updateAuthService(null);
                    statusConn.setText("Chưa kết nối");
                    statusHost.setText("-");

                    ensureTabPresent("Login", loginTab, icLogin);
                    ensureTabAbsent(controlPanel);
                    ensureTabAbsent(monitorTab);
                    tabs.setSelectedComponent(loginTab);
                });
            }
        });

        if (netCfg != null) {
            connectPanel.setOutboundBind(netCfg);
        }

        loginTab.setListener((username, role) -> SwingUtilities.invokeLater(() -> {
            ensureTabAbsent(loginTab);

            if (role == Role.ADMIN) {
                monitorTab.setAdminMode(true, null);
                controlPanel.setClientName("ADMIN-" + username);
                ensureTabPresent("Chọn giải đấu", giaiDauSelectPanel, null);
                ensureTabPresent("Giải đấu", tournamentTabPanel, icTournament);
                ensureTabPresent("Nội dung", noiDungPanel, null);
                ensureTabPresent("Thi đấu", multiCourtPanel, icMultiCourt);
                ensureTabPresent("Giám sát", monitorTab, icMonitor);
                ensureTabPresent("Kết quả đã thi đấu", screenshotTab, icScreenshot);
                ensureTabPresent("Logs", logTab, icLog);
                tabs.setSelectedComponent(giaiDauSelectPanel);
            } else {
                monitorTab.setAdminMode(false, username);
                controlPanel.setClientName("CLIENT-" + username);
                ensureTabPresent("Connect", connectPanel, icConnect);
                ensureTabPresent("Giải đấu", tournamentTabPanel, icTournament);
                ensureTabPresent("Nhiều sân", multiCourtPanel, icMultiCourt);
                ensureTabPresent("Giám sát", monitorTab, icMonitor);
                tabs.setSelectedComponent(tournamentTabPanel);
            }

            try {
                tournamentTabPanel.unlockSelection();
            } catch (Exception ignored) {
            }
        }));

        // Ban đầu chỉ có Login
        ensureTabPresent("Login", loginTab, icLogin);

        root.add(header, BorderLayout.NORTH);
        root.add(wrapCard(tabs), BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);

        if (netCfg != null && netCfg.ifName() != null && !netCfg.ifName().isBlank()) {
            try {
                NetworkInterface ni = NetworkInterface.getByName(netCfg.ifName());
                if (ni != null) {
                    monitorTab.setNetworkInterface(ni);
                    controlPanel.setNetworkInterface(ni);
                    multiCourtPanel.setNetworkInterface(ni);
                }
                statusHost.setText("IF: " + netCfg.ifName());
            } catch (SocketException ignored) {
                statusHost.setText("IF: " + netCfg.ifName());
            }
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = javax.swing.JOptionPane.showConfirmDialog(
                        MainFrame.this,
                        "Bạn có chắc muốn tắt ứng dụng?\n\nTất cả dữ liệu chưa lưu sẽ bị mất.",
                        "Xác nhận tắt ứng dụng",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE);
                if (result == javax.swing.JOptionPane.YES_OPTION)
                    System.exit(0);
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
        setAlwaysOnTop(false);
        IconUtil.applyTo(this);

        String implTitle = getClass().getPackage().getImplementationTitle();
        String implVer = getClass().getPackage().getImplementationVersion();
        if (implTitle != null)
            lblAppTitle.setText(implTitle);
        lblVersion.setText(implVer != null ? "v" + implVer : "");

        ramTimer = new javax.swing.Timer(1000, ae -> updateMemory());
        ramTimer.start();

        autoConnectDatabase();
    }

    /* -------------------- UI Builders -------------------- */

    private JPanel buildHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(new EmptyBorder(8, 8, 8, 8));
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
        bar.setBorder(new EmptyBorder(6, 8, 6, 8));

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
        if (comp == null)
            return; // NULL-SAFE: không add tab khi panel chưa sẵn sàng
        int idx = indexOf(comp);
        if (idx == -1)
            tabs.addTab(title, icon, comp);
        else {
            tabs.setTitleAt(idx, title);
            tabs.setIconAt(idx, icon);
        }
    }

    private void ensureTabAbsent(Component comp) {
        int idx = indexOf(comp);
        if (idx != -1)
            tabs.removeTabAt(idx);
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
        p.setBorder(new EmptyBorder(3, 3, 3, 3));
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
        String cp = path.startsWith("/") ? path.substring(1) : path;
        try {
            return new FlatSVGIcon(cp, size, size);
        } catch (Exception ex) {
            String pngPath = cp.replace(".svg", ".png");
            var url = getClass().getClassLoader().getResource(pngPath);
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
            System.err.println("Icon not found on classpath: " + cp + " or " + pngPath);
            return null;
        }
    }

    public DatabaseService service() {
        return service;
    }

    /* -------------------- Auth wiring -------------------- */
    private void updateAuthService(Connection c) {
        this.authService = (c != null) ? new AuthService(c) : null;
        loginTab.setAuthService(this.authService);
    }

    /* -------------------- AUTO CONNECT DATABASE -------------------- */
    private void autoConnectDatabase() {
        new Thread(() -> {
            try {
                String host = (dbConfig != null) ? dbConfig.getServer() : "GODZILLA\\SQLDEV";
                String port = (dbConfig != null) ? dbConfig.getPort() : "1433";
                String database = (dbConfig != null) ? dbConfig.getDatabaseName() : "badminton";
                String user = (dbConfig != null) ? dbConfig.getUsername() : "hau2";
                String password = (dbConfig != null) ? dbConfig.getPassword() : "hau123";

                ConnectionConfig config = new ConnectionConfig()
                        .host(host).port(port).databaseInput(database)
                        .user(user).password(password).mode(ConnectionConfig.Mode.NAME);

                service.setConfig(config);
                Connection conn = service.connect();

                SwingUtilities.invokeLater(() -> {
                    try {
                        controlPanel.setConnection(conn);
                        multiCourtPanel.setConnection(conn);
                        tournamentTabPanel.updateConnection();

                        // tạo service & panel “Nội dung”
                        noiDungService = new NoiDungService(new NoiDungRepository(conn));
                        noiDungPanel = new NoiDungManagementPanel(noiDungService);
                        giaiDauSelectPanel = new GiaiDauSelectPanel(tournamentTabPanel.getGiaiDauService());

                        updateAuthService(conn);

                        statusConn.setText("Đã kết nối");
                        statusConn.setForeground(new Color(46, 204, 113));

                        ensureTabPresent("Login", loginTab, icLogin);
                        ensureTabPresent("Tournament", tournamentTabPanel, icTournament);
                        ensureTabPresent("Nội dung", noiDungPanel, null); // giờ đã có panel
                        ensureTabPresent("Multi Court", multiCourtPanel, icMultiCourt);
                        ensureTabPresent("Monitor", monitorTab, icMonitor);
                        ensureTabPresent("Screenshot", screenshotTab, icScreenshot);
                        ensureTabPresent("Log", logTab, icLog);

                        tabs.setSelectedComponent(loginTab);
                        System.out.println("✓ Tự động kết nối SQL Server thành công!");
                    } catch (Exception e) {
                        System.err.println("Lỗi cập nhật UI sau kết nối: " + e.getMessage());
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusConn.setText("Lỗi kết nối");
                    statusConn.setForeground(new Color(231, 76, 60));
                    statusHost.setText("Không thể kết nối SQL Server");

                    System.err.println("✗ Không thể tự động kết nối SQL Server: " + e.getMessage());
                    e.printStackTrace();

                    javax.swing.JOptionPane.showMessageDialog(
                            this,
                            "Không thể tự động kết nối đến SQL Server.\n\n" +
                                    "Vui lòng kiểm tra:\n" +
                                    "- SQL Server đang chạy\n" +
                                    "- Database 'badminton' đã tồn tại\n" +
                                    "- Thông tin kết nối đúng\n\n" +
                                    "Chi tiết lỗi: " + e.getMessage(),
                            "Lỗi kết nối Database",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
}
