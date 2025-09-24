package com.example.btms.ui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.text.DecimalFormat;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
// import javax.swing.JToggleButton; // removed direct toggle from header
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.btms.config.ConnectionConfig;
import com.example.btms.model.db.SQLSRVConnectionManager;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.cateoftuornament.ChiTietGiaiDauRepository;
import com.example.btms.repository.club.CauLacBoRepository;
import com.example.btms.service.auth.AuthService;
import com.example.btms.repository.player.VanDongVienRepository;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.ui.player.VanDongVienManagementPanel;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.cateoftuornament.ChiTietGiaiDauService;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.db.DatabaseService;
import com.example.btms.ui.auth.LoginTab;
import com.example.btms.ui.auth.LoginTab.Role;
import com.example.btms.ui.category.NoiDungManagementPanel;
import com.example.btms.ui.cateoftuornament.DangKyNoiDungPanel;
import com.example.btms.ui.club.CauLacBoManagementPanel;
import com.example.btms.ui.control.BadmintonControlPanel;
import com.example.btms.ui.control.MultiCourtControlPanel;
import com.example.btms.ui.log.LogTab;
import com.example.btms.ui.monitor.MonitorTab;
import com.example.btms.ui.net.NetworkConfig;
import com.example.btms.ui.screenshot.ScreenshotTab;
import com.example.btms.ui.tournament.GiaiDauSelectPanel;
import com.example.btms.ui.tournament.TournamentTabPanel;
import com.example.btms.util.ui.IconUtil;
import com.example.btms.util.ui.Ui;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
// import com.formdev.flatlaf.extras.FlatSVGIcon; // (no longer used after removing icons)

public class MainFrame extends JFrame {

    // Tạo sau khi có Connection
    private NoiDungService noiDungService;
    private NoiDungManagementPanel noiDungPanel;
    private CauLacBoManagementPanel cauLacBoPanel;
    private DangKyNoiDungPanel dangKyNoiDungPanel;
    private VanDongVienManagementPanel vanDongVienPanel;
    private com.example.btms.ui.team.DangKyDoiPanel dangKyDoiPanel;

    private final NetworkConfig netCfg; // cấu hình interface đã chọn
    private final SQLSRVConnectionManager manager = new SQLSRVConnectionManager();
    private final DatabaseService service = new DatabaseService(manager);
    private final ConnectionConfig dbCfg; // cấu hình kết nối DB (bind từ application.properties)

    private final BadmintonControlPanel controlPanel = new BadmintonControlPanel();
    private final MultiCourtControlPanel multiCourtPanel = new MultiCourtControlPanel();
    private final MonitorTab monitorTab = new MonitorTab();
    private final ScreenshotTab screenshotTab = new ScreenshotTab();
    private final LogTab logTab = new LogTab();
    private final LoginTab loginTab = new LoginTab();
    private final TournamentTabPanel tournamentTabPanel = new TournamentTabPanel(service);

    private AuthService authService;

    // UI fields
    private final JLabel lblAppTitle = new JLabel("BADMINTON TUORNAMENT MANAGEMENT SYSTEM");
    private final JLabel lblVersion = new JLabel();
    // Bỏ toggle ở header; chuyển qua menu. (Giữ method switchTheme)
    private final JLabel statusConn = new JLabel("Chưa kết nối");
    private final JLabel statusHost = new JLabel("-");
    private final JLabel statusMem = new JLabel();

    private final DecimalFormat df = new DecimalFormat("#,##0");
    // (Removed old JTabbedPane navigation)
    // New navigation components
    private final JPanel cardPanel = new JPanel(new CardLayout());
    private final Map<String, Component> views = new LinkedHashMap<>();
    private Role currentRole = null;
    private JMenuBar appMenuBar; // keep reference if needed

    // Icons
    // (Icons kept if later needed for menu entries – currently omitted to simplify)

    private javax.swing.Timer ramTimer;
    private GiaiDauSelectPanel giaiDauSelectPanel;

    public MainFrame() {
        this(null, null);
    }

    public MainFrame(NetworkConfig cfg, ConnectionConfig dbCfg) {
        super("BADMINTON TUORNAMENT MANAGEMENT SYSTEM");
        this.netCfg = cfg;
        this.dbCfg = dbCfg;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Ui.installModernUi();

        JComponent root = (JComponent) getContentPane();
        root.setLayout(new BorderLayout());
        root.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel header = buildHeaderBar();
        JPanel status = buildStatusBar();

        // (Old tab configuration removed)

        // Listener login -> mở các tab phù hợp sau khi đăng nhập
        loginTab.setListener((username, role) -> SwingUtilities.invokeLater(() -> {
            currentRole = role;
            if (role == Role.ADMIN) {
                monitorTab.setAdminMode(true, null);
                controlPanel.setClientName("ADMIN-" + username);
                ensureViewPresent("Chọn giải đấu", giaiDauSelectPanel);
                ensureViewPresent("Giải đấu", tournamentTabPanel);
                ensureViewPresent("Nội dung", noiDungPanel);
                ensureViewPresent("Câu lạc bộ", cauLacBoPanel);
                ensureViewPresent("Vận động viên", vanDongVienPanel);
                ensureViewPresent("Đăng ký nội dung", dangKyNoiDungPanel);
                ensureViewPresent("Đăng ký đội", dangKyDoiPanel);
                ensureViewPresent("Thi đấu", multiCourtPanel);
                ensureViewPresent("Giám sát", monitorTab);
                ensureViewPresent("Kết quả đã thi đấu", screenshotTab);
                ensureViewPresent("Logs", logTab);
                if (giaiDauSelectPanel != null)
                    showView("Chọn giải đấu");
            } else {
                monitorTab.setAdminMode(false, username);
                controlPanel.setClientName("CLIENT-" + username);
                ensureViewPresent("Giải đấu", tournamentTabPanel);
                ensureViewPresent("Nhiều sân", multiCourtPanel);
                ensureViewPresent("Giám sát", monitorTab);
                showView("Giải đấu");
            }
            buildMenuBar();
            try {
                tournamentTabPanel.unlockSelection();
            } catch (Exception ignored) {
            }
        }));

        // Ban đầu chỉ có Login (CardLayout)
        ensureViewPresent("Login", loginTab);
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, "Login");

        root.add(header, BorderLayout.NORTH);
        // Use cardPanel instead of tabs in CENTER (tabs kept temporarily for reference)
        root.add(wrapCard(cardPanel), BorderLayout.CENTER);
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
        bar.setBorder(new EmptyBorder(6, 10, 6, 10));
        JLabel avatar = new JLabel(IconUtil.loadRoundAvatar(36));

        lblAppTitle.setFont(lblAppTitle.getFont().deriveFont(Font.BOLD, 18f));
        lblVersion.setFont(lblAppTitle.getFont().deriveFont(Font.PLAIN, 12f));
        lblVersion.setForeground(new Color(110, 110, 110));
        lblAppTitle.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, lblAppTitle.getPreferredSize().height));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 0));
        titleBox.setOpaque(false);
        titleBox.add(lblAppTitle);
        titleBox.add(lblVersion);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new javax.swing.BoxLayout(center, javax.swing.BoxLayout.X_AXIS));
        center.add(avatar);
        center.add(javax.swing.Box.createHorizontalStrut(10));
        center.add(titleBox);
        center.add(javax.swing.Box.createHorizontalGlue());

        bar.add(center, BorderLayout.CENTER);
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

    /* (Legacy tab helper methods removed after migration to CardLayout) */

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

    // (Removed loadIcon method – not needed for text-only menu navigation)

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
                // Đọc từ ConnectionConfig (bind application.properties), có fallback:
                String host = (dbCfg != null && dbCfg.host() != null && !dbCfg.host().isBlank())
                        ? dbCfg.host()
                        : "GODZILLA\\SQLDEV";
                String port = (dbCfg != null && dbCfg.port() != null && !dbCfg.port().isBlank())
                        ? dbCfg.port()
                        : "1433";
                String database = (dbCfg != null && dbCfg.databaseInput() != null && !dbCfg.databaseInput().isBlank())
                        ? dbCfg.databaseInput()
                        : "badminton";
                String user = (dbCfg != null && dbCfg.user() != null && !dbCfg.user().isBlank())
                        ? dbCfg.user()
                        : "hau2";
                String password = (dbCfg != null && dbCfg.password() != null)
                        ? dbCfg.password()
                        : "hau123";

                boolean encrypt = (dbCfg != null) ? dbCfg.effectiveEncrypt() : true;
                boolean trust = (dbCfg != null) ? dbCfg.effectiveTrustServerCertificate() : true;
                int loginTimeout = (dbCfg != null) ? dbCfg.effectiveLoginTimeoutSeconds() : 30;
                boolean integrated = (dbCfg != null) ? dbCfg.effectiveIntegratedSecurity() : false;

                // Dựng runtime config & URL
                ConnectionConfig runtimeCfg = new ConnectionConfig().mode(ConnectionConfig.Mode.NAME);

                String url;
                if (dbCfg != null && dbCfg.buildJdbcUrl() != null) {
                    url = dbCfg.buildJdbcUrl();
                } else {
                    StringBuilder sb = new StringBuilder("jdbc:sqlserver://")
                            .append(host).append(":").append(port)
                            .append(";databaseName=").append(database)
                            .append(";encrypt=").append(encrypt)
                            .append(";trustServerCertificate=").append(trust)
                            .append(";loginTimeout=").append(loginTimeout);
                    if (integrated)
                        sb.append(";integratedSecurity=true");
                    url = sb.toString();
                }

                runtimeCfg.setUrl(url);
                runtimeCfg.setUsername(user);
                runtimeCfg.setPassword(password);

                service.setConfig(runtimeCfg);
                Connection conn = service.connect();

                SwingUtilities.invokeLater(() -> {
                    try {
                        controlPanel.setConnection(conn);
                        multiCourtPanel.setConnection(conn);
                        tournamentTabPanel.updateConnection();

                        noiDungService = new NoiDungService(new NoiDungRepository(conn));
                        noiDungPanel = new NoiDungManagementPanel(noiDungService);
                        // CLB
                        CauLacBoService clbService = new CauLacBoService(new CauLacBoRepository(conn));
                        cauLacBoPanel = new CauLacBoManagementPanel(clbService);
                        // Vận động viên
                        VanDongVienService vdvService = new VanDongVienService(new VanDongVienRepository(conn));
                        vanDongVienPanel = new VanDongVienManagementPanel(vdvService, clbService);
                        // Panel đăng ký nội dung theo giải chọn trong Prefs
                        ChiTietGiaiDauService chiTietService = new ChiTietGiaiDauService(
                                new ChiTietGiaiDauRepository(conn));
                        dangKyNoiDungPanel = new DangKyNoiDungPanel(
                                noiDungService,
                                chiTietService,
                                new com.example.btms.config.Prefs(),
                                tournamentTabPanel.getGiaiDauService());
                        // Đăng ký đội (đôi)
                        dangKyDoiPanel = new com.example.btms.ui.team.DangKyDoiPanel(conn);
                        giaiDauSelectPanel = new GiaiDauSelectPanel(tournamentTabPanel.getGiaiDauService());

                        updateAuthService(conn);

                        statusConn.setText("Đã kết nối");
                        statusConn.setForeground(new Color(46, 204, 113));

                        // Sau khi có connection đảm bảo Login view tồn tại
                        ensureViewPresent("Login", loginTab);
                        showView("Login");
                        buildMenuBar();

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
                                    "- Database '" +
                                    ((dbCfg != null && dbCfg.databaseInput() != null
                                            && !dbCfg.databaseInput().isBlank())
                                                    ? dbCfg.databaseInput()
                                                    : "badminton")
                                    +
                                    "' đã tồn tại\n" +
                                    "- Thông tin kết nối đúng\n\n" +
                                    "Chi tiết lỗi: " + e.getMessage(),
                            "Lỗi kết nối Database",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    /*
     * -------------------- CardLayout View Helpers (new navigation)
     * --------------------
     */
    private void ensureViewPresent(String name, Component comp) {
        if (name == null || comp == null)
            return;
        if (!views.containsKey(name)) {
            try { // gắn tên card để component tự biết khi floating
                if (comp instanceof JComponent jc)
                    jc.putClientProperty("cardName", name);
            } catch (Exception ignore) {
            }
            cardPanel.add(comp, name);
            views.put(name, comp);
        }
    }

    private void showView(String name) {
        if (!views.containsKey(name))
            return;
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, name);
        setTitle("Badminton Event Technology - " + name);
    }

    private void buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu mSystem = new JMenu("Hệ thống");
        JMenuItem miLogin = new JMenuItem("Đăng nhập");
        miLogin.addActionListener(e -> showView("Login"));
        mSystem.add(miLogin);
        if (currentRole != null) {
            JMenuItem miLogout = new JMenuItem("Đăng xuất");
            miLogout.addActionListener(e -> doLogout());
            mSystem.add(miLogout);
        }
        mSystem.addSeparator();
        JMenuItem miExit = new JMenuItem("Thoát");
        miExit.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        mSystem.add(miExit);
        mb.add(mSystem);

        if (currentRole != null) {
            JMenu mManage = new JMenu("Quản lý");
            if (currentRole == Role.ADMIN && giaiDauSelectPanel != null)
                mManage.add(menuItem("Chọn giải đấu"));
            mManage.add(menuItem("Giải đấu"));
            if (currentRole == Role.ADMIN) {
                mManage.add(menuItem("Nội dung"));
                mManage.add(menuItem("Câu lạc bộ"));
                mManage.add(menuItem("Vận động viên"));
                mManage.add(menuItem("Đăng ký nội dung"));
                mManage.add(menuItem("Đăng ký đội"));
            }
            mb.add(mManage);

            JMenu mPlay = new JMenu("Thi đấu");
            if (currentRole == Role.ADMIN)
                mPlay.add(menuItem("Thi đấu"));
            else
                mPlay.add(menuItem("Nhiều sân"));
            mPlay.add(menuItem("Giám sát"));
            if (currentRole == Role.ADMIN)
                mPlay.add(menuItem("Kết quả đã thi đấu"));
            mb.add(mPlay);

            // Menu giao diện luôn hiển thị để đổi theme cả khi chưa đăng nhập
            JMenu mTheme = new JMenu("Giao diện");
            JCheckBoxMenuItem miDark = new JCheckBoxMenuItem("Chế độ tối");
            miDark.setSelected(UIManager.getLookAndFeel() instanceof FlatDarkLaf);
            miDark.addActionListener(e -> switchTheme(miDark.isSelected()));
            mTheme.add(miDark);
            mb.add(mTheme);

            JMenu mOther = new JMenu("Khác");
            if (currentRole == Role.ADMIN)
                mOther.add(menuItem("Logs"));
            mb.add(mOther);
        }

        setJMenuBar(mb);
        this.appMenuBar = mb;
        revalidate();
        repaint();
    }

    private JMenuItem menuItem(String viewName) {
        JMenuItem mi = new JMenuItem(viewName);
        mi.addActionListener(e -> showView(viewName));
        if (!views.containsKey(viewName))
            mi.setEnabled(false);
        return mi;
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;
        currentRole = null;
        showView("Login");
        buildMenuBar();
    }
}
