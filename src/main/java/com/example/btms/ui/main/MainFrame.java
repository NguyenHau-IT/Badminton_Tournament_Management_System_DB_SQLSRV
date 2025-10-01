package com.example.btms.ui.main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.LinkedHashMap; // still used earlier? (kept for backward compatibility but theme menu removed)
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.example.btms.config.ConnectionConfig;
import com.example.btms.config.Prefs;
import com.example.btms.model.db.SQLSRVConnectionManager;
import com.example.btms.model.tournament.GiaiDau;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.cateoftuornament.ChiTietGiaiDauRepository;
import com.example.btms.repository.club.CauLacBoRepository;
import com.example.btms.repository.player.VanDongVienRepository;
import com.example.btms.service.auth.AuthService;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.cateoftuornament.ChiTietGiaiDauService;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.db.DatabaseService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.ui.auth.LoginTab;
import com.example.btms.ui.auth.LoginTab.Role;
import com.example.btms.ui.bracket.SoDoThiDauPanel;
import com.example.btms.ui.category.NoiDungManagementPanel;
import com.example.btms.ui.cateoftuornament.DangKyNoiDungPanel;
import com.example.btms.ui.club.CauLacBoManagementPanel;
import com.example.btms.ui.control.BadmintonControlPanel;
import com.example.btms.ui.control.MultiCourtControlPanel;
import com.example.btms.ui.log.LogTab;
import com.example.btms.ui.monitor.MonitorTab;
import com.example.btms.ui.net.NetworkConfig;
import com.example.btms.ui.player.VanDongVienManagementPanel;
import com.example.btms.ui.screenshot.ScreenshotTab;
import com.example.btms.ui.settings.SettingsPanel;
import com.example.btms.ui.tournament.TournamentTabPanel;
import com.example.btms.util.report.RegistrationPdfExporter;
import com.example.btms.util.ui.IconUtil;
import com.example.btms.util.ui.Ui;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
// import com.formdev.flatlaf.extras.FlatSVGIcon; // (no longer used after removing icons)

public class MainFrame extends JFrame {
    // Lưu map font gốc để tránh nhân đôi khi đổi scale nhiều lần
    private static java.util.Map<Object, Font> baseUIFontMap;

    // Tạo sau khi có Connection
    private NoiDungService noiDungService;
    private NoiDungManagementPanel noiDungPanel;
    private CauLacBoManagementPanel cauLacBoPanel;
    private DangKyNoiDungPanel dangKyNoiDungPanel;
    private VanDongVienManagementPanel vanDongVienPanel;
    private com.example.btms.ui.team.DangKyDoiPanel dangKyDoiPanel;
    private com.example.btms.ui.player.DangKyCaNhanPanel dangKyCaNhanPanel; // đăng ký cá nhân (đơn)
    private com.example.btms.ui.category.ContentParticipantsPanel contentParticipantsPanel; // xem VDV/Đội theo nội dung
    private com.example.btms.ui.draw.BocThamThiDau bocThamThiDauPanel; // bốc thăm thi đấu (đơn/đôi) 0-based
    private com.example.btms.ui.bracket.SoDoThiDauPanel soDoThiDauPanel; // sơ đồ thi đấu trực quan
    // Cửa sổ nổi cho "Sơ đồ thi đấu"
    private JFrame soDoThiDauFrame;
    // Dạng tab: mỗi nội dung là 1 tab riêng
    private javax.swing.JTabbedPane soDoTabbedPane;
    private final java.util.LinkedHashMap<Integer, com.example.btms.ui.bracket.SoDoThiDauPanel> soDoPanelsByNoiDung = new java.util.LinkedHashMap<>();
    // Cửa sổ nổi cho "Thi đấu"/"Nhiều sân"
    private JFrame thiDauFrame;

    private final NetworkConfig netCfg; // cấu hình interface đã chọn
    private final SQLSRVConnectionManager manager = new SQLSRVConnectionManager();
    private final DatabaseService service = new DatabaseService(manager);
    private final ConnectionConfig dbCfg; // cấu hình kết nối DB (bind từ application.properties)

    private final BadmintonControlPanel controlPanel = new BadmintonControlPanel();
    private final MultiCourtControlPanel multiCourtPanel = new MultiCourtControlPanel();
    private final MonitorTab monitorTab = new MonitorTab();
    private final ScreenshotTab screenshotTab = new ScreenshotTab();
    private final LogTab logTab = new LogTab();
    private final LoginTab loginTab = new LoginTab(); // dùng trong dialog đăng nhập tự tạo
    private final TournamentTabPanel tournamentTabPanel = new TournamentTabPanel(service);
    private GiaiDau selectedGiaiDau; // giải đấu đã chọn sau đăng nhập

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
    private SettingsPanel settingsPanel; // trang cài đặt
    // Left navigation tree
    private JTree navTree;
    private DefaultTreeModel navModel;
    // Lưu lựa chọn gần nhất dưới mục "Danh sách đăng kí" để dùng cho popup của
    // chính mục cha
    private ContentNode lastSelectedRegListContent;

    // Icons
    // (Icons kept if later needed for menu entries – currently omitted to simplify)

    private javax.swing.Timer ramTimer;
    // removed legacy select panel usage; selection is done via dialog now

    public MainFrame() {
        this(null, null);
    }

    public MainFrame(NetworkConfig cfg, ConnectionConfig dbCfg) {
        super("BADMINTON TUORNAMENT MANAGEMENT SYSTEM");
        this.netCfg = cfg;
        this.dbCfg = dbCfg;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Áp dụng theme từ Prefs trước khi build UI để tránh nháy
        boolean darkPref = new com.example.btms.config.Prefs().getBool("ui.darkTheme", false);
        if (darkPref) {
            try {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
            } catch (Exception ignored) {
            }
        } else {
            try {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            } catch (Exception ignored) {
            }
        }
        Ui.installModernUi();

        JComponent root = (JComponent) getContentPane();
        root.setLayout(new BorderLayout());
        root.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel header = buildHeaderBar();
        JPanel status = buildStatusBar();

        // (Old tab configuration removed)

        // Không dùng Login như một trang trong main nữa; đăng nhập qua dialog

        // Khởi tạo trang cài đặt sớm để luôn truy cập được
        settingsPanel = new SettingsPanel(this);
        ensureViewPresent("Cài đặt", settingsPanel);
        // Chưa hiển thị view nào cho đến khi đăng nhập và chọn giải

        root.add(header, BorderLayout.NORTH);
        // Left navigation tree
        JScrollPane nav = buildNavigationTreePanel();
        root.add(nav, BorderLayout.WEST);
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
                    if (soDoThiDauFrame != null) {
                        soDoThiDauFrame.dispose();
                        soDoThiDauFrame = null;
                    }
                } catch (Exception ignored) {
                }
                try {
                    if (thiDauFrame != null) {
                        thiDauFrame.dispose();
                        thiDauFrame = null;
                    }
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

        // Áp dụng font scale nếu người dùng đã lưu khác 100%
        int pct = new com.example.btms.config.Prefs().getInt("ui.fontScalePercent", 100);
        if (pct != 100) {
            SwingUtilities.invokeLater(this::applyGlobalFontScale);
        }
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
                        // Panel Nội dung của giải theo giải chọn trong Prefs
                        ChiTietGiaiDauService chiTietService = new ChiTietGiaiDauService(
                                new ChiTietGiaiDauRepository(conn));
                        dangKyNoiDungPanel = new DangKyNoiDungPanel(
                                noiDungService,
                                chiTietService,
                                new com.example.btms.config.Prefs(),
                                tournamentTabPanel.getGiaiDauService());
                        // Đăng ký đội (đôi)
                        dangKyDoiPanel = new com.example.btms.ui.team.DangKyDoiPanel(conn);
                        // Đăng ký cá nhân (đơn)
                        dangKyCaNhanPanel = new com.example.btms.ui.player.DangKyCaNhanPanel(conn);
                        // Panel xem người Danh sách đăng kí
                        contentParticipantsPanel = new com.example.btms.ui.category.ContentParticipantsPanel(conn);
                        // Bốc thăm thi đấu (0-based order)
                        bocThamThiDauPanel = new com.example.btms.ui.draw.BocThamThiDau(conn);
                        soDoThiDauPanel = new com.example.btms.ui.bracket.SoDoThiDauPanel(conn);
                        // Tổng sắp huy chương (kết quả toàn đoàn)
                        try {
                            com.example.btms.ui.result.TongSapHuyChuongPanel tongSapHuyChuongPanel = new com.example.btms.ui.result.TongSapHuyChuongPanel(
                                    conn, clbService);
                            ensureViewPresent("Tổng sắp huy chương", tongSapHuyChuongPanel);
                        } catch (Throwable ignore) {
                        }
                        // Tournament selection now uses modal dialog, no panel needed here

                        updateAuthService(conn);

                        statusConn.setText("Đã kết nối");
                        statusConn.setForeground(new Color(46, 204, 113));

                        // Sau khi có connection: buộc đăng nhập và chọn giải qua dialog
                        buildMenuBar();
                        forceLoginAndTournamentSelection();

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
        // Update nav root with tournament name if available
        if (navModel != null) {
            updateNavigationRootTitleFromSelection();
            navModel.nodeChanged((DefaultMutableTreeNode) navModel.getRoot());
            for (int i = 0; i < navTree.getRowCount(); i++)
                navTree.expandRow(i);
        }
    }

    private void buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu mSystem = new JMenu("Hệ thống");
        JMenuItem miLogin = new JMenuItem("Đăng nhập...");
        miLogin.addActionListener(e -> forceLoginAndTournamentSelection());
        mSystem.add(miLogin);
        // Làm mới dữ liệu ứng dụng mà không cần đăng nhập lại/chọn giải lại
        JMenuItem miRefresh = new JMenuItem("Làm mới dữ liệu");
        miRefresh.addActionListener(e -> refreshApplicationData());
        mSystem.add(miRefresh);
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
            // Nếu chưa chọn giải -> chỉ hiện chọn giải và cài đặt
            if (selectedGiaiDau == null) {
                JMenu mManage = new JMenu("Quản lý");
                if (currentRole == Role.ADMIN)
                    mManage.add(menuSelectTournament());
                mb.add(mManage);

                JMenu mOther = new JMenu("Khác");
                mOther.add(menuItem("Cài đặt"));
                mb.add(mOther);
            } else {
                JMenu mManage = new JMenu("Quản lý");
                if (currentRole == Role.ADMIN)
                    mManage.add(menuSelectTournament());
                mManage.add(menuItem("Giải đấu"));
                if (currentRole == Role.ADMIN) {
                    mManage.add(menuItem("Nội dung"));
                    mManage.add(menuItem("Câu lạc bộ"));
                    mManage.add(menuItem("Vận động viên"));
                    mManage.add(menuItem("Nội dung của giải"));
                    mManage.add(menuItem("Đăng ký đội"));
                    mManage.add(menuItem("Đăng ký cá nhân"));
                    mManage.add(menuItem("Danh sách đăng kí"));
                    mManage.add(menuItem("Bốc thăm thi đấu"));
                    // Mặc định mở "Sơ đồ thi đấu" ở cửa sổ riêng khi chọn từ menu
                    JMenuItem miSoDo = new JMenuItem("Sơ đồ thi đấu");
                    miSoDo.addActionListener(e -> openSoDoThiDauWindow());
                    if (!views.containsKey("Sơ đồ thi đấu"))
                        miSoDo.setEnabled(false);
                    mManage.add(miSoDo);
                }
                mb.add(mManage);

                JMenu mPlay = new JMenu("Thi đấu");
                if (currentRole == Role.ADMIN) {
                    JMenuItem miThiDau = new JMenuItem("Thi đấu");
                    miThiDau.addActionListener(e -> openThiDauWindow());
                    if (!views.containsKey("Thi đấu"))
                        miThiDau.setEnabled(false);
                    mPlay.add(miThiDau);
                } else {
                    JMenuItem miNhieuSan = new JMenuItem("Nhiều sân");
                    miNhieuSan.addActionListener(e -> openThiDauWindow());
                    if (!views.containsKey("Nhiều sân"))
                        miNhieuSan.setEnabled(false);
                    mPlay.add(miNhieuSan);
                }
                mPlay.add(menuItem("Giám sát"));
                if (currentRole == Role.ADMIN)
                    mPlay.add(menuItem("Kết quả đã thi đấu"));
                mb.add(mPlay);

                JMenu mOther = new JMenu("Khác");
                if (currentRole == Role.ADMIN)
                    mOther.add(menuItem("Logs"));
                mOther.add(menuItem("Cài đặt"));
                mb.add(mOther);
            }
        }
        // Nếu chưa đăng nhập vẫn có truy cập Cài đặt (ví dụ đổi theme trước khi login)
        if (currentRole == null) {
            JMenu mOther = new JMenu("Khác");
            mOther.add(menuItem("Cài đặt"));
            mb.add(mOther);
        }

        setJMenuBar(mb);
        revalidate();
        repaint();
    }

    /**
     * Làm mới dữ liệu toàn ứng dụng dựa trên kết nối và giải hiện tại, không yêu
     * cầu login/chọn lại.
     * - Cập nhật TournamentTabPanel connection.
     * - Yêu cầu các panel tự reload.
     * - Làm mới cây điều hướng và tiêu đề.
     */
    private void refreshApplicationData() {
        try {
            // Cập nhật tiêu đề root theo giải đang chọn
            updateNavigationRootTitleFromSelection();
            // Cập nhật connection cho panel giải đấu
            try {
                tournamentTabPanel.updateConnection();
            } catch (Throwable ignore) {
            }
            // Gọi reload cho các panel quản lý
            try {
                if (noiDungPanel != null)
                    noiDungPanel.requestFocus();
            } catch (Throwable ignore) {
            }
            try {
                if (cauLacBoPanel != null) {
                    /* panel tự quản lý reload khi mở */ }
            } catch (Throwable ignore) {
            }
            try {
                if (vanDongVienPanel != null) {
                    /* có nút làm mới riêng */ }
            } catch (Throwable ignore) {
            }
            try {
                if (dangKyNoiDungPanel != null) {
                    /* có reloadAsync trong panel */ }
            } catch (Throwable ignore) {
            }
            try {
                if (dangKyDoiPanel != null) {
                    /* có reload() internal, sẽ reload khi mở lại */ }
            } catch (Throwable ignore) {
            }
            try {
                if (dangKyCaNhanPanel != null) {
                    /* có reload() internal */ }
            } catch (Throwable ignore) {
            }
            // Làm mới Nội dung đăng ký hiển thị theo nội dung (nếu đang mở)
            try {
                if (contentParticipantsPanel != null)
                    contentParticipantsPanel.selectNoiDungById(null);
            } catch (Throwable ignore) {
            }
            // Làm mới Sơ đồ thi đấu: tab đang mở reloadData(); danh sách trong cây cũng
            // được rebuild
            try {
                if (soDoTabbedPane != null) {
                    int count = soDoTabbedPane.getTabCount();
                    for (int i = 0; i < count; i++) {
                        java.awt.Component c = soDoTabbedPane.getComponentAt(i);
                        if (c instanceof com.example.btms.ui.bracket.SoDoThiDauPanel p) {
                            p.reloadData();
                        }
                    }
                }
            } catch (Throwable ignore) {
            }
            // Làm mới cây điều hướng (bao gồm lọc Sơ đồ theo bốc thăm đã có)
            rebuildNavigationTree();
            JOptionPane.showMessageDialog(this, "Đã làm mới dữ liệu.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Throwable ex) {
            JOptionPane.showMessageDialog(this, "Lỗi làm mới dữ liệu: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Force the user to login and then select a tournament before entering the main
     * UI.
     */
    private void forceLoginAndTournamentSelection() {
        // 1) Đăng nhập bằng dialog nội bộ
        if (!showLoginDialog()) {
            return; // huỷ
        }

        // 2) Chọn giải đấu
        GiaiDau gd = showTournamentSelectDialog();
        if (gd == null) {
            // quay về trạng thái chưa đăng nhập
            currentRole = null;
            selectedGiaiDau = null;
            buildMenuBar();
            rebuildNavigationTree();
            return;
        }
        selectedGiaiDau = gd;
        try {
            new com.example.btms.config.Prefs().putInt("selectedGiaiDauId", gd.getId());
        } catch (Exception ignore) {
        }

        // 3) Hiển thị đầy đủ chức năng
        registerViewsForCurrentRole();
        buildMenuBar();
        rebuildNavigationTree();
        showView("Giải đấu");
    }

    private void registerViewsForCurrentRole() {
        if (currentRole == Role.ADMIN) {
            ensureViewPresent("Giải đấu", tournamentTabPanel);
            ensureViewPresent("Nội dung", noiDungPanel);
            ensureViewPresent("Câu lạc bộ", cauLacBoPanel);
            ensureViewPresent("Vận động viên", vanDongVienPanel);
            ensureViewPresent("Nội dung của giải", dangKyNoiDungPanel);
            ensureViewPresent("Đăng ký đội", dangKyDoiPanel);
            ensureViewPresent("Đăng ký cá nhân", dangKyCaNhanPanel);
            ensureViewPresent("Danh sách đăng kí", contentParticipantsPanel);
            ensureViewPresent("Bốc thăm thi đấu", bocThamThiDauPanel);
            ensureViewPresent("Sơ đồ thi đấu", soDoThiDauPanel);
            ensureViewPresent("Thi đấu", multiCourtPanel);
            ensureViewPresent("Giám sát", monitorTab);
            ensureViewPresent("Kết quả đã thi đấu", screenshotTab);
            ensureViewPresent("Logs", logTab);
            ensureViewPresent("Cài đặt", settingsPanel);
        } else {
            ensureViewPresent("Giải đấu", tournamentTabPanel);
            ensureViewPresent("Nhiều sân", multiCourtPanel);
            ensureViewPresent("Giám sát", monitorTab);
            ensureViewPresent("Cài đặt", settingsPanel);
        }
    }

    private JMenuItem menuSelectTournament() {
        JMenuItem mi = new JMenuItem("Chọn giải đấu...");
        mi.addActionListener(e -> {
            GiaiDau gd = showTournamentSelectDialog();
            if (gd != null) {
                selectedGiaiDau = gd;
                try {
                    new com.example.btms.config.Prefs().putInt("selectedGiaiDauId", gd.getId());
                } catch (Exception ignore) {
                }
                // Đóng cửa sổ Sơ đồ thi đấu (nếu đang mở) để lần mở sau build tabs theo giải
                // mới
                if (soDoThiDauFrame != null) {
                    try {
                        soDoThiDauFrame.dispose();
                    } catch (Exception ignored) {
                    }
                    soDoThiDauFrame = null;
                    if (soDoTabbedPane != null)
                        soDoTabbedPane = null;
                    soDoPanelsByNoiDung.clear();
                }
                updateNavigationRootTitleFromSelection();
                if (navModel != null)
                    navModel.nodeChanged((DefaultMutableTreeNode) navModel.getRoot());
                rebuildNavigationTree();
            }
        });
        return mi;
    }

    /**
     * Build the left navigation tree with high-level sections and map leaf nodes to
     * views.
     */
    private JScrollPane buildNavigationTreePanel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Giải đấu hiện tại");
        navModel = new DefaultTreeModel(root);
        navTree = new JTree(navModel);
        navTree.setRootVisible(true);
        navTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        navTree.addTreeSelectionListener((TreeSelectionEvent e) -> {
            Object comp = navTree.getLastSelectedPathComponent();
            if (!(comp instanceof DefaultMutableTreeNode node))
                return;
            activateNavNode(node);
            // Ghi nhớ nội dung đang chọn nếu nó nằm dưới "Danh sách đăng kí"
            try {
                Object uo = node.getUserObject();
                if (uo instanceof ContentNode cn) {
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                    String parentLabel = (parent != null && parent.getUserObject() instanceof String s) ? s : "";
                    if ("Danh sách đăng kí".equals(parentLabel)) {
                        lastSelectedRegListContent = cn;
                    }
                }
            } catch (Exception ignore) {
            }
        });

        // Cho phép click lại cùng một mục vẫn kích hoạt (khi selection không đổi)
        navTree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getButton() != java.awt.event.MouseEvent.BUTTON1)
                    return;
                int row = navTree.getRowForLocation(e.getX(), e.getY());
                if (row < 0)
                    return;
                javax.swing.tree.TreePath path = navTree.getPathForRow(row);
                if (path == null)
                    return;
                // Chỉ kích hoạt khi click lại đúng node đang được chọn để tránh kích hoạt 2 lần
                javax.swing.tree.TreePath selected = navTree.getSelectionPath();
                if (selected == null || !selected.equals(path))
                    return;
                Object comp = path.getLastPathComponent();
                if (comp instanceof DefaultMutableTreeNode node) {
                    activateNavNode(node);
                }
            }
        });

        // Context menu động theo node được click
        navTree.addMouseListener(new java.awt.event.MouseAdapter() {
            private void selectNodeAt(java.awt.event.MouseEvent e) {
                int row = navTree.getRowForLocation(e.getX(), e.getY());
                if (row >= 0) {
                    javax.swing.tree.TreePath path = navTree.getPathForRow(row);
                    if (path != null)
                        navTree.setSelectionPath(path);
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger() || javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    selectNodeAt(e);
                    showTreeContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    selectNodeAt(e);
                    showTreeContextMenu(e);
                }
            }
        });

        JScrollPane sp = new JScrollPane(navTree);
        // Header với nút Mở rộng/Thu gọn tất cả
        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 2));
        header.setOpaque(false);
        javax.swing.JButton btnExpandAll = new javax.swing.JButton("Mở rộng");
        javax.swing.JButton btnCollapseAll = new javax.swing.JButton("Thu gọn");
        btnExpandAll.setFocusable(false);
        btnCollapseAll.setFocusable(false);
        btnExpandAll.setToolTipText("Mở rộng tất cả các mục");
        btnCollapseAll.setToolTipText("Thu gọn tất cả các mục");
        btnExpandAll.addActionListener(e -> {
            try {
                int i = 0;
                while (i < navTree.getRowCount()) {
                    navTree.expandRow(i);
                    i++;
                }
            } catch (Exception ignore) {
            }
        });
        btnCollapseAll.addActionListener(e -> {
            try {
                // Thu gọn từ dưới lên, giữ root
                for (int i = navTree.getRowCount() - 1; i >= 1; i--) {
                    navTree.collapseRow(i);
                }
            } catch (Exception ignore) {
            }
        });
        header.add(btnExpandAll);
        header.add(btnCollapseAll);
        sp.setColumnHeaderView(header);
        sp.setPreferredSize(new java.awt.Dimension(240, 10));
        rebuildNavigationTree();
        return sp;
    }

    /** Hiển thị menu chuột phải theo node hiện tại trong cây điều hướng. */
    private void showTreeContextMenu(java.awt.event.MouseEvent e) {
        try {
            javax.swing.tree.TreePath selPath = navTree.getSelectionPath();
            if (selPath == null)
                return;
            Object last = selPath.getLastPathComponent();
            if (!(last instanceof DefaultMutableTreeNode node))
                return;
            Object uo = node.getUserObject();
            javax.swing.JPopupMenu pm = new javax.swing.JPopupMenu();

            if (uo instanceof ContentNode cn) {
                // Với mỗi nội dung: "Bốc thăm, lưu và mở sơ đồ"
                javax.swing.JMenuItem mi = new javax.swing.JMenuItem("Bốc thăm, lưu và mở sơ đồ");
                mi.addActionListener(ev -> doAutoDrawSaveAndOpenForContent(cn));
                pm.add(mi);
            } else if (uo instanceof String label) {
                if ("Danh sách đăng kí".equals(label)) {
                    javax.swing.JMenuItem mi1 = new javax.swing.JMenuItem("Bốc thăm theo nội dung đang chọn");
                    mi1.setEnabled(lastSelectedRegListContent != null);
                    mi1.addActionListener(ev -> {
                        if (lastSelectedRegListContent == null)
                            return;
                        try {
                            boolean ok = doAutoDrawSave(lastSelectedRegListContent.idNoiDung);
                            if (ok) {
                                rebuildNavigationTree();
                                JOptionPane.showMessageDialog(this, "Đã bốc thăm: " + lastSelectedRegListContent.label,
                                        "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    pm.add(mi1);

                    javax.swing.JMenuItem mi2 = new javax.swing.JMenuItem("Bốc thăm tất cả");
                    mi2.addActionListener(ev -> {
                        try {
                            int okCount = 0;
                            for (int i = 0; i < node.getChildCount(); i++) {
                                Object ch = node.getChildAt(i);
                                if (ch instanceof DefaultMutableTreeNode cnode) {
                                    Object cuo = cnode.getUserObject();
                                    if (cuo instanceof ContentNode ccn) {
                                        if (doAutoDrawSave(ccn.idNoiDung))
                                            okCount++;
                                    }
                                }
                            }
                            rebuildNavigationTree();
                            JOptionPane.showMessageDialog(this, "Đã bốc thăm " + okCount + " nội dung.", "Hoàn tất",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    pm.add(mi2);
                }
                // Thêm menu cho nút phải tại mục "Đăng ký đội" để xuất PDF
                if ("Đăng ký thi đấu".equals(label)) {
                    pm.addSeparator();
                    javax.swing.JMenu mExport = new javax.swing.JMenu("Xuất danh sách đăng ký (PDF)");
                    javax.swing.JMenuItem miAll = new javax.swing.JMenuItem("Tất cả");
                    miAll.addActionListener(ev -> doExportTeamRegistrationsPdf(ExportMode.ALL));
                    javax.swing.JMenuItem miByClub = new javax.swing.JMenuItem("Theo CLB");
                    miByClub.addActionListener(ev -> doExportTeamRegistrationsPdf(ExportMode.BY_CLUB));
                    javax.swing.JMenuItem miByContent = new javax.swing.JMenuItem("Theo nội dung");
                    miByContent.addActionListener(ev -> doExportTeamRegistrationsPdf(ExportMode.BY_CONTENT));
                    mExport.add(miAll);
                    mExport.add(miByClub);
                    mExport.add(miByContent);
                    pm.add(mExport);
                }
                if ("Sơ đồ thi đấu".equals(label)) {
                    pm.addSeparator();
                    javax.swing.JMenu mExport = new javax.swing.JMenu("Xuất danh sách sơ đồ (PDF)");
                    javax.swing.JMenuItem miOneFile = new javax.swing.JMenuItem("Xuất 1 file (tất cả nội dung)");
                    miOneFile.addActionListener(ev -> {
                        try {
                            Connection conn = (service != null) ? service.current() : null;
                            if (conn == null) {
                                JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            // Create a temporary bracket panel to render all nội dung
                            SoDoThiDauPanel p = new SoDoThiDauPanel(conn);
                            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
                            fc.setDialogTitle("Xuất PDF sơ đồ (tất cả nội dung)");
                            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF", "pdf"));
                            String tour = (selectedGiaiDau != null && selectedGiaiDau.getTenGiai() != null
                                    && !selectedGiaiDau.getTenGiai().isBlank())
                                            ? selectedGiaiDau.getTenGiai()
                                            : new com.example.btms.config.Prefs().get("selectedGiaiDauName",
                                                    "giai-dau");
                            String safe = normalizeFileNameUnderscore(tour);
                            fc.setSelectedFile(new java.io.File(safe + "_so_do_thi_dau.pdf"));
                            int r = fc.showSaveDialog(this);
                            if (r != javax.swing.JFileChooser.APPROVE_OPTION)
                                return;
                            java.io.File f = fc.getSelectedFile();
                            if (f == null)
                                return;
                            if (!f.getName().toLowerCase().endsWith(".pdf")) {
                                f = new java.io.File(f.getAbsolutePath() + ".pdf");
                            }
                            boolean ok = p.exportAllBracketsToSinglePdf(f);
                            if (ok)
                                JOptionPane.showMessageDialog(this, "Đã xuất: " + f.getAbsolutePath(), "Thành công",
                                        JOptionPane.INFORMATION_MESSAGE);
                            else
                                JOptionPane.showMessageDialog(this, "Không thể xuất PDF.", "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    javax.swing.JMenuItem miEach = new javax.swing.JMenuItem("Xuất mỗi nội dung 1 file");
                    miEach.addActionListener(ev -> {
                        try {
                            Connection conn = (service != null) ? service.current() : null;
                            if (conn == null) {
                                JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            SoDoThiDauPanel p = new SoDoThiDauPanel(conn);
                            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
                            fc.setDialogTitle("Chọn thư mục để lưu các PDF sơ đồ");
                            fc.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
                            fc.setAcceptAllFileFilterUsed(false);
                            fc.setSelectedFile(new java.io.File(System.getProperty("user.home", ".")));
                            int r = fc.showSaveDialog(this);
                            if (r != javax.swing.JFileChooser.APPROVE_OPTION)
                                return;
                            java.io.File dir = fc.getSelectedFile();
                            if (dir == null)
                                return;
                            int created = p.exportEachBracketToDirectory(dir);
                            if (created > 0)
                                JOptionPane.showMessageDialog(this,
                                        "Đã xuất " + created + " file vào: " + dir.getAbsolutePath(),
                                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                            else
                                JOptionPane.showMessageDialog(this, "Không có nội dung để xuất.", "Thông báo",
                                        JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    mExport.add(miOneFile);
                    mExport.add(miEach);
                    pm.add(mExport);
                }
            }

            if (pm.getComponentCount() > 0)
                pm.show(navTree, e.getX(), e.getY());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể hiển thị menu: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Bốc thăm + lưu (không mở sơ đồ). Trả về true nếu thành công. */
    private boolean doAutoDrawSave(int idNoiDung) throws Exception {
        Connection conn = (service != null) ? service.current() : null;
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        int idGiai = (selectedGiaiDau != null) ? selectedGiaiDau.getId()
                : new com.example.btms.config.Prefs().getInt("selectedGiaiDauId", -1);
        if (idGiai <= 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        var ndOpt = new NoiDungService(new NoiDungRepository(conn)).getNoiDungById(idNoiDung);
        boolean isTeam = ndOpt.isPresent() && Boolean.TRUE.equals(ndOpt.get().getTeam());

        if (isTeam) {
            var doiSvc = new com.example.btms.service.team.DangKiDoiService(
                    new com.example.btms.repository.team.DangKiDoiRepository(conn));
            var bocSvc = new com.example.btms.service.draw.BocThamDoiService(
                    conn, new com.example.btms.repository.draw.BocThamDoiRepository(conn));
            java.util.List<com.example.btms.model.team.DangKiDoi> teams = doiSvc.listTeams(idGiai, idNoiDung);
            java.util.Collections.shuffle(teams);
            java.util.List<com.example.btms.model.draw.BocThamDoi> rows = new java.util.ArrayList<>();
            for (int i = 0; i < teams.size(); i++) {
                var t = teams.get(i);
                Integer idClb = t.getIdCauLacBo();
                rows.add(new com.example.btms.model.draw.BocThamDoi(idGiai, idNoiDung,
                        idClb == null ? 0 : idClb, t.getTenTeam(), i, 1));
            }
            bocSvc.resetWithOrder(idGiai, idNoiDung, rows);
        } else {
            var dkSvc = new com.example.btms.service.player.DangKiCaNhanService(
                    new com.example.btms.repository.player.DangKiCaNhanRepository(conn));
            var bocSvc = new com.example.btms.service.draw.BocThamCaNhanService(
                    new com.example.btms.repository.draw.BocThamCaNhanRepository(conn));
            var regs = dkSvc.listByGiaiAndNoiDung(idGiai, idNoiDung);
            java.util.Collections.shuffle(regs);
            var existed = bocSvc.list(idGiai, idNoiDung);
            for (var r : existed)
                bocSvc.delete(idGiai, idNoiDung, r.getIdVdv());
            for (int i = 0; i < regs.size(); i++) {
                var r = regs.get(i);
                bocSvc.create(idGiai, idNoiDung, r.getIdVdv(), i, 1);
            }
        }
        return true;
    }

    /** Bốc thăm + lưu và mở sơ đồ, auto seed + lưu sơ đồ. */
    private void doAutoDrawSaveAndOpenForContent(ContentNode cn) {
        try {
            if (!doAutoDrawSave(cn.idNoiDung))
                return;
            openSoDoThiDauWindow();
            ensureSoDoTab(cn.idNoiDung, cn.label);
            com.example.btms.ui.bracket.SoDoThiDauPanel p = soDoPanelsByNoiDung.get(cn.idNoiDung);
            if (p != null) {
                p.selectNoiDungById(cn.idNoiDung);
                p.autoSeedFromDrawAndSave();
            }
            rebuildNavigationTree();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể thực hiện: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Recreate the tree nodes according to current role and available views. */
    private void rebuildNavigationTree() {
        if (navModel == null)
            return;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) navModel.getRoot();
        if (root == null)
            return;
        // Root title = tournament name if available
        updateNavigationRootTitleFromSelection();
        root.removeAllChildren();

        if (currentRole == null) {
            root.add(new DefaultMutableTreeNode("Vui lòng đăng nhập"));
        } else if (selectedGiaiDau == null) {
            root.add(new DefaultMutableTreeNode("Vui lòng chọn giải đấu"));
        } else {
            // 1) Nội dung của giải -> expand to all registered categories
            DefaultMutableTreeNode ndg = new DefaultMutableTreeNode("Nội dung của giải");
            try {
                if (service != null && service.current() != null) {
                    var repo = new com.example.btms.repository.category.NoiDungRepository(service.current());
                    java.util.Map<String, Integer>[] maps = repo.loadCategories();
                    java.util.Map<String, Integer> singles = maps[0];
                    java.util.Map<String, Integer> doubles = maps[1];
                    for (var entry : singles.entrySet()) {
                        ndg.add(new DefaultMutableTreeNode(new ContentNode(entry.getKey(), entry.getValue())));
                    }
                    for (var entry : doubles.entrySet()) {
                        ndg.add(new DefaultMutableTreeNode(new ContentNode(entry.getKey(), entry.getValue())));
                    }
                }
            } catch (Exception ignored) {
            }
            root.add(ndg);

            // 2) Đăng ký thi đấu
            DefaultMutableTreeNode reg = new DefaultMutableTreeNode("Đăng ký thi đấu");
            reg.add(new DefaultMutableTreeNode("Đăng ký đội"));
            reg.add(new DefaultMutableTreeNode("Đăng ký cá nhân"));
            DefaultMutableTreeNode regList = new DefaultMutableTreeNode("Danh sách đăng kí");
            try {
                if (service != null && service.current() != null) {
                    var repo = new com.example.btms.repository.category.NoiDungRepository(service.current());
                    java.util.Map<String, Integer>[] maps = repo.loadCategories();
                    java.util.Map<String, Integer> singles = maps[0];
                    java.util.Map<String, Integer> doubles = maps[1];
                    for (var entry : singles.entrySet()) {
                        regList.add(new DefaultMutableTreeNode(new ContentNode(entry.getKey(), entry.getValue())));
                    }
                    for (var entry : doubles.entrySet()) {
                        regList.add(new DefaultMutableTreeNode(new ContentNode(entry.getKey(), entry.getValue())));
                    }
                }
            } catch (Exception ignored) {
            }
            reg.add(regList);
            root.add(reg);

            // 3) Bốc thăm
            DefaultMutableTreeNode draw = new DefaultMutableTreeNode("Bốc thăm");
            // Sơ đồ thi đấu + xổ danh sách nội dung của giải
            DefaultMutableTreeNode soDoNode = new DefaultMutableTreeNode("Sơ đồ thi đấu");
            try {
                if (service != null && service.current() != null) {
                    var repo = new com.example.btms.repository.category.NoiDungRepository(service.current());
                    java.util.Map<String, Integer>[] maps = repo.loadCategories();
                    java.util.Map<String, Integer> singles = maps[0];
                    java.util.Map<String, Integer> doubles = maps[1];

                    // Chỉ hiển thị các nội dung đã có bốc thăm rồi
                    int idGiai = (selectedGiaiDau != null) ? selectedGiaiDau.getId() : -1;
                    var bocThamDoiSvc = new com.example.btms.service.draw.BocThamDoiService(
                            service.current(),
                            new com.example.btms.repository.draw.BocThamDoiRepository(service.current()));
                    var bocThamCaNhanSvc = new com.example.btms.service.draw.BocThamCaNhanService(
                            new com.example.btms.repository.draw.BocThamCaNhanRepository(service.current()));

                    // Cá nhân
                    for (var entry : singles.entrySet()) {
                        try {
                            var list = bocThamCaNhanSvc.list(idGiai, entry.getValue());
                            if (list != null && !list.isEmpty()) {
                                soDoNode.add(
                                        new DefaultMutableTreeNode(new ContentNode(entry.getKey(), entry.getValue())));
                            }
                        } catch (Exception ignore2) {
                        }
                    }
                    // Đội/Đôi
                    for (var entry : doubles.entrySet()) {
                        try {
                            var list = bocThamDoiSvc.list(idGiai, entry.getValue());
                            if (list != null && !list.isEmpty()) {
                                soDoNode.add(
                                        new DefaultMutableTreeNode(new ContentNode(entry.getKey(), entry.getValue())));
                            }
                        } catch (Exception ignore2) {
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            draw.add(soDoNode);
            root.add(draw);

            // 4) Kết quả
            DefaultMutableTreeNode result = new DefaultMutableTreeNode("Kết quả");
            result.add(new DefaultMutableTreeNode("Kết quả đã thi đấu"));
            result.add(new DefaultMutableTreeNode("Tổng sắp huy chương"));
            root.add(result);
        }

        navModel.reload();
        for (int i = 0; i < navTree.getRowCount(); i++) {
            navTree.expandRow(i);
        }
    }

    /**
     * Kích hoạt hành động cho một node trong cây điều hướng (dùng chung cho
     * listener & mouse).
     */
    private void activateNavNode(DefaultMutableTreeNode node) {
        if (node == null)
            return;
        if (node.getChildCount() > 0)
            return; // chỉ xử lý leaf items
        Object uo = node.getUserObject();
        if (uo instanceof ContentNode cn) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
            String parentLabel = (parent != null && parent.getUserObject() instanceof String s) ? s : "";
            if ("Danh sách đăng kí".equals(parentLabel)) {
                if (contentParticipantsPanel != null) {
                    ensureViewPresent("Danh sách đăng kí", contentParticipantsPanel);
                    contentParticipantsPanel.selectNoiDungById(cn.idNoiDung);
                    showView("Danh sách đăng kí");
                }
            } else if ("Sơ đồ thi đấu".equals(parentLabel)) {
                openSoDoThiDauWindow();
                ensureSoDoTab(cn.idNoiDung, cn.label);
            } else if ("Nội dung của giải".equals(parentLabel)) {
                if (dangKyNoiDungPanel != null) {
                    ensureViewPresent("Nội dung của giải", dangKyNoiDungPanel);
                    try {
                        dangKyNoiDungPanel.selectNoiDungById(cn.idNoiDung);
                    } catch (Throwable ignore) {
                    }
                    showView("Nội dung của giải");
                }
            }
            return;
        }
        if (uo instanceof String label) {
            if ("Sơ đồ thi đấu".equals(label)) {
                openSoDoThiDauWindow();
                return;
            }
            if ("Bốc thăm thi đấu".equals(label)) {
                if (bocThamThiDauPanel != null)
                    ensureViewPresent("Bốc thăm thi đấu", bocThamThiDauPanel);
            }
            if (views.containsKey(label))
                showView(label);
        }
    }

    // Tree leaf model for "Danh sách đăng kí" -> Nội dung items
    private static final class ContentNode {
        final String label;
        final Integer idNoiDung;

        ContentNode(String label, Integer idNoiDung) {
            this.label = label;
            this.idNoiDung = idNoiDung;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private void updateNavigationRootTitleFromSelection() {
        try {
            String title = "Giải đấu";
            if (selectedGiaiDau != null && selectedGiaiDau.getTenGiai() != null
                    && !selectedGiaiDau.getTenGiai().isBlank()) {
                title = selectedGiaiDau.getTenGiai();
            }
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) navModel.getRoot();
            if (root != null) {
                root.setUserObject(title);
            }
        } catch (Exception ignored) {
        }
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
        try {
            if (soDoThiDauFrame != null) {
                soDoThiDauFrame.dispose();
                soDoThiDauFrame = null;
            }
        } catch (Exception ignore) {
        }
        try {
            if (thiDauFrame != null) {
                thiDauFrame.dispose();
                thiDauFrame = null;
            }
        } catch (Exception ignore) {
        }
        currentRole = null;
        selectedGiaiDau = null;
        showView("Cài đặt");
        buildMenuBar();
        rebuildNavigationTree();
    }

    /** Public wrapper cho SettingsPanel gọi đổi theme và lưu Prefs. */
    public void applyTheme(boolean dark) {
        // lưu Prefs ở SettingsPanel rồi, đây chỉ thực thi
        switchTheme(dark);
        try {
            monitorTab.refreshAllViewerSettings();
        } catch (Exception ignore) {
        }
    }

    /** Được SettingsPanel gọi để đổi số cột monitor. */
    public void updateMonitorColumns(int cols) {
        try {
            java.lang.reflect.Method m = monitorTab.getClass().getMethod("setColumns", int.class);
            m.invoke(monitorTab, cols);
        } catch (Exception ignore) {
        }
    }

    /**
     * Được SettingsPanel gọi để áp dụng always-on-top cho các viewer nổi (monitor).
     */
    public void applyAlwaysOnTopFloating(boolean onTop) {
        try {
            // MonitorTab hiện tạo các MonitorWindow (JFrame). Ta thử gọi method công khai
            // nếu có trong tương lai.
            // Tạm thời: set alwaysOnTop cho frame chính (giới hạn) – có thể cải tiến nếu
            // expose danh sách windows.
            setAlwaysOnTop(onTop);
            try {
                monitorTab.refreshAllViewerSettings();
            } catch (Exception ignore) {
            }
        } catch (Exception ignore) {
        }
    }

    /**
     * Áp dụng font scale toàn cục (gọi sau khi đổi ui.fontScalePercent nếu muốn
     * realtime).
     */
    public void applyGlobalFontScale() {
        int pct = new com.example.btms.config.Prefs().getInt("ui.fontScalePercent", 100);
        float mul = Math.max(0.5f, pct / 100f);
        javax.swing.UIDefaults defs = UIManager.getDefaults();
        if (baseUIFontMap == null) {
            baseUIFontMap = new java.util.HashMap<>();
            for (java.util.Enumeration<Object> e = defs.keys(); e.hasMoreElements();) {
                Object key = e.nextElement();
                Object val = defs.get(key);
                if (val instanceof Font f) {
                    baseUIFontMap.put(key, f); // lưu bản gốc
                }
            }
        }
        for (var entry : baseUIFontMap.entrySet()) {
            Font base = entry.getValue();
            if (base != null) {
                float newSize = Math.max(9f, base.getSize2D() * mul);
                Font scaled = base.deriveFont(newSize);
                defs.put(entry.getKey(), new javax.swing.plaf.FontUIResource(scaled));
            }
        }
        // Cập nhật toàn bộ cây UI cho tất cả cửa sổ hiện có
        for (java.awt.Window w : java.awt.Window.getWindows()) {
            try {
                SwingUtilities.updateComponentTreeUI(w);
            } catch (Exception ignore) {
            }
        }
        // Refresh monitor viewer settings (always-on-top, etc.) – font đã được update
        // qua UIManager
        try {
            monitorTab.refreshAllViewerSettings();
        } catch (Exception ignore) {
        }
    }

    /**
     * Mở "Sơ đồ thi đấu" ở một cửa sổ riêng, tái sử dụng kết nối hiện tại.
     * Nếu cửa sổ đã mở, sẽ đưa ra phía trước.
     */
    private void openSoDoThiDauWindow() {
        try {
            if (soDoThiDauFrame != null) {
                soDoThiDauFrame.setVisible(true);
                soDoThiDauFrame.toFront();
                soDoThiDauFrame.requestFocus();
                return;
            }
            Connection conn = (service != null) ? service.current() : null;
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Cửa sổ tabs – tạo JTabbedPane, chưa thêm tab nào (chỉ thêm theo yêu cầu)
            JTabbedPane tabs = new JTabbedPane();
            // Khi chuyển tab, tự reload data của tab đang chọn
            tabs.addChangeListener(e -> {
                try {
                    int sel = tabs.getSelectedIndex();
                    if (sel >= 0) {
                        java.awt.Component c = tabs.getComponentAt(sel);
                        if (c instanceof SoDoThiDauPanel p) {
                            p.reloadData();
                        }
                    }
                } catch (Exception ignore) {
                }
            });
            this.soDoTabbedPane = tabs;
            JFrame f = new JFrame();
            String title = "Sơ đồ thi đấu";
            try {
                if (selectedGiaiDau != null && selectedGiaiDau.getTenGiai() != null
                        && !selectedGiaiDau.getTenGiai().isBlank())
                    title = title + " - " + selectedGiaiDau.getTenGiai();
            } catch (Exception ignore) {
            }
            f.setTitle(title);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setLayout(new BorderLayout());
            f.add(tabs, BorderLayout.CENTER);
            IconUtil.applyTo(f);
            f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            f.setLocationRelativeTo(this);
            f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    soDoThiDauFrame = null;
                    soDoPanelsByNoiDung.clear();
                    soDoTabbedPane = null;
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    soDoThiDauFrame = null;
                    soDoPanelsByNoiDung.clear();
                    soDoTabbedPane = null;
                }
            });
            soDoThiDauFrame = f;
            f.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể mở Sơ đồ thi đấu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Đảm bảo có tab cho idNoiDung, nếu chưa có thì tạo; sau đó chọn tab đó. */
    private void ensureSoDoTab(Integer idNoiDung, String title) {
        try {
            if (soDoThiDauFrame == null)
                return;
            if (soDoTabbedPane == null)
                return;
            SoDoThiDauPanel existing = soDoPanelsByNoiDung.get(idNoiDung);
            if (existing != null) {
                // đã có -> focus
                int count = soDoTabbedPane.getTabCount();
                for (int i = 0; i < count; i++) {
                    if (soDoTabbedPane.getComponentAt(i) == existing) {
                        soDoTabbedPane.setSelectedIndex(i);
                        break;
                    }
                }
            } else {
                Connection conn = (service != null) ? service.current() : null;
                if (conn == null) {
                    JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                SoDoThiDauPanel p = new SoDoThiDauPanel(conn);
                try {
                    p.selectNoiDungById(idNoiDung);
                    p.setNoiDungLabelMode(true);
                    p.reloadData(); // tự động load khi tab vừa được tạo
                } catch (Throwable ignore) {
                }
                soDoPanelsByNoiDung.put(idNoiDung, p);
                soDoTabbedPane.addTab(title, p);
                int idx = soDoTabbedPane.indexOfComponent(p);
                makeTabClosable(soDoTabbedPane, idx, title, () -> {
                    // remove mapping and tab when closing
                    soDoPanelsByNoiDung.remove(idNoiDung);
                    int i = soDoTabbedPane.indexOfComponent(p);
                    if (i >= 0)
                        soDoTabbedPane.removeTabAt(i);
                    // Nếu sau khi đóng tab mà không còn tab nào nữa, đóng luôn cửa sổ chung
                    try {
                        if (soDoTabbedPane.getTabCount() == 0 && soDoThiDauFrame != null) {
                            soDoThiDauFrame.dispose();
                            // Các listener windowClosing/windowClosed sẽ dọn dẹp map & refs
                        }
                    } catch (Exception ignore) {
                    }
                });
                soDoTabbedPane.setSelectedComponent(p);
            }
            // bring window to front
            soDoThiDauFrame.setVisible(true);
            soDoThiDauFrame.toFront();
            soDoThiDauFrame.requestFocus();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể mở tab Sơ đồ: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Đặt tab header có nút đóng. */
    private void makeTabClosable(JTabbedPane tabs, int index, String title, Runnable onClose) {
        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tabHeader.setOpaque(false);
        JLabel lbl = new JLabel(title);
        JButton btn = new JButton("x");
        btn.setMargin(new Insets(0, 4, 0, 4));
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setFocusable(false);
        btn.addActionListener(e -> {
            try {
                if (onClose != null)
                    onClose.run();
            } catch (Exception ignore) {
            }
        });
        tabHeader.add(lbl);
        tabHeader.add(btn);
        tabs.setTabComponentAt(index, tabHeader);
    }

    /** Đưa cửa sổ ra trước, và focus tab tương ứng idNoiDung nếu tồn tại. */
    private void showSoDoTabForNoiDung(Integer idNoiDung) {
        if (soDoThiDauFrame == null)
            return;
        if (soDoTabbedPane == null)
            return;
        if (idNoiDung == null) {
            soDoThiDauFrame.setVisible(true);
            soDoThiDauFrame.toFront();
            soDoThiDauFrame.requestFocus();
            return;
        }
        // Tìm index tab theo panel map
        com.example.btms.ui.bracket.SoDoThiDauPanel target = soDoPanelsByNoiDung.get(idNoiDung);
        if (target != null) {
            int count = soDoTabbedPane.getTabCount();
            for (int i = 0; i < count; i++) {
                if (soDoTabbedPane.getComponentAt(i) == target) {
                    soDoTabbedPane.setSelectedIndex(i);
                    break;
                }
            }
        }
        // đảm bảo cửa sổ hiện lên
        soDoThiDauFrame.setVisible(true);
        soDoThiDauFrame.toFront();
        soDoThiDauFrame.requestFocus();
    }

    /* -------------------- Export đăng ký đội (PDF) -------------------- */
    private enum ExportMode {
        ALL, BY_CLUB, BY_CONTENT
    }

    private void doExportTeamRegistrationsPdf(ExportMode mode) {
        try {
            Connection conn = (service != null) ? service.current() : null;
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int idGiai;
            idGiai = (selectedGiaiDau != null) ? selectedGiaiDau.getId()
                    : new Prefs().getInt("selectedGiaiDauId", -1);
            if (idGiai <= 0) {
                JOptionPane.showMessageDialog(this, "Chưa chọn giải.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String giaiName = (selectedGiaiDau != null && selectedGiaiDau.getTenGiai() != null)
                    ? selectedGiaiDau.getTenGiai()
                    : "Giải đấu";

            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Xuất PDF danh sách đăng ký đội");
            fc.setFileFilter(new FileNameExtensionFilter("PDF", "pdf"));
            String baseName = switch (mode) {
                case ALL -> "dangky-doi_all";
                case BY_CLUB -> "dangky-doi_theo-clb";
                case BY_CONTENT -> "dangky-doi_theo-noidung";
            };
            fc.setSelectedFile(new java.io.File(baseName + ".pdf"));
            int r = fc.showSaveDialog(this);
            if (r != JFileChooser.APPROVE_OPTION)
                return;
            java.io.File f = fc.getSelectedFile();
            if (f == null)
                return;
            if (!f.getName().toLowerCase().endsWith(".pdf")) {
                f = new java.io.File(f.getAbsolutePath() + ".pdf");
            }

            switch (mode) {
                case ALL ->
                    RegistrationPdfExporter.exportAll(conn, idGiai, f, giaiName);
                case BY_CLUB ->
                    RegistrationPdfExporter.exportByClub(conn, idGiai, f, giaiName);
                case BY_CONTENT ->
                    RegistrationPdfExporter.exportByNoiDung(conn, idGiai, f, giaiName);
            }
            JOptionPane.showMessageDialog(this, "Đã xuất: " + f.getAbsolutePath(), "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Xuất PDF thất bại: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Mở trang "Thi đấu" (điều khiển nhiều sân) ở cửa sổ riêng.
     * Dùng chung CourtManagerService nên có thể mở song song với tab nếu cần.
     */
    private void openThiDauWindow() {
        try {
            if (thiDauFrame != null) {
                thiDauFrame.setVisible(true);
                thiDauFrame.toFront();
                thiDauFrame.requestFocus();
                return;
            }
            Connection conn = (service != null) ? service.current() : null;
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Tạo panel điều khiển mới và gắn kết nối + network interface hiện tại
            MultiCourtControlPanel panel = new MultiCourtControlPanel();
            panel.setConnection(conn);
            try {
                if (multiCourtPanel != null && multiCourtPanel.getNetworkInterface() != null) {
                    panel.setNetworkInterface(multiCourtPanel.getNetworkInterface());
                }
            } catch (Throwable ignore) {
            }

            JFrame f = new JFrame();
            String title = (currentRole == Role.ADMIN) ? "Thi đấu" : "Nhiều sân";
            try {
                if (selectedGiaiDau != null && selectedGiaiDau.getTenGiai() != null
                        && !selectedGiaiDau.getTenGiai().isBlank())
                    title = title + " - " + selectedGiaiDau.getTenGiai();
            } catch (Exception ignore) {
            }
            f.setTitle(title);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setLayout(new BorderLayout());
            f.add(panel, BorderLayout.CENTER);
            IconUtil.applyTo(f);
            f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            f.setLocationRelativeTo(this);
            f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    thiDauFrame = null;
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    thiDauFrame = null;
                }
            });
            thiDauFrame = f;
            f.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể mở trang Thi đấu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* -------------------- Inline dialogs -------------------- */
    private boolean showLoginDialog() {
        final JDialog dlg = new JDialog(this, "Đăng nhập", true);
        dlg.setLayout(new BorderLayout());
        try {
            if (loginTab.getParent() != null) {
                ((java.awt.Container) loginTab.getParent()).remove(loginTab);
            }
        } catch (Exception ignore) {
        }
        loginTab.setListener((username, role) -> {
            this.currentRole = role;
            try {
                if (role == Role.ADMIN) {
                    monitorTab.setAdminMode(true, null);
                    controlPanel.setClientName("ADMIN-" + username);
                } else {
                    monitorTab.setAdminMode(false, username);
                    controlPanel.setClientName("CLIENT-" + username);
                }
            } catch (Exception ignore) {
            }
            dlg.dispose();
        });
        dlg.add(loginTab, BorderLayout.CENTER);
        JPanel south = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> {
            this.currentRole = null;
            dlg.dispose();
        });
        south.add(btnCancel);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        return currentRole != null;
    }

    private GiaiDau showTournamentSelectDialog() {
        final JDialog dlg = new JDialog(this, "Chọn giải đấu", true);
        dlg.setLayout(new BorderLayout());
        TournamentTabPanel chooser = new TournamentTabPanel(service);
        chooser.updateConnection();
        dlg.add(chooser, BorderLayout.CENTER);
        JPanel south = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        JButton btnOk = new JButton("Chọn");
        JButton btnCancel = new JButton("Hủy");
        final GiaiDau[] result = new GiaiDau[1];
        btnOk.addActionListener(e -> {
            GiaiDau pick = chooser.getSelectedGiaiDau();
            if (pick == null) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng chọn một giải đấu.", "Chưa chọn",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            result[0] = pick;
            dlg.dispose();
        });
        btnCancel.addActionListener(e -> {
            result[0] = null;
            dlg.dispose();
        });
        south.add(btnCancel);
        south.add(btnOk);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.setSize(900, 600);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        return result[0];
    }

    // Normalize a string to a safe ASCII filename using underscores as separator
    private static String normalizeFileNameUnderscore(String s) {
        if (s == null)
            return "giai-dau";
        String x = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-zA-Z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("(^_|_$)", "")
                .toLowerCase();
        if (x.isBlank())
            return "giai-dau";
        return x;
    }
}
