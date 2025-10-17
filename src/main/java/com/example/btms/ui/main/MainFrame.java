package com.example.btms.ui.main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;

import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.LinkedHashMap; // still used earlier? (kept for backward compatibility but theme menu removed)
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.example.btms.config.ConnectionConfig;
import com.example.btms.config.NetworkConfig;
import com.example.btms.config.Prefs;
import com.example.btms.model.bracket.SoDoCaNhan;
import com.example.btms.model.bracket.SoDoDoi;
import com.example.btms.model.db.SQLSRVConnectionManager;
import com.example.btms.model.tournament.GiaiDau;
import com.example.btms.repository.bracket.SoDoCaNhanRepository;
import com.example.btms.repository.bracket.SoDoDoiRepository;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.cateoftuornament.ChiTietGiaiDauRepository;
import com.example.btms.repository.club.CauLacBoRepository;
import com.example.btms.repository.player.VanDongVienRepository;
import com.example.btms.repository.result.KetQuaCaNhanRepository;
import com.example.btms.repository.result.KetQuaDoiRepository;
import com.example.btms.service.auth.AuthService;
import com.example.btms.service.bracket.SoDoCaNhanService;
import com.example.btms.service.bracket.SoDoDoiService;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.cateoftuornament.ChiTietGiaiDauService;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.db.DatabaseService;
import com.example.btms.service.player.VanDongVienService;
import com.example.btms.service.result.KetQuaCaNhanService;
import com.example.btms.service.result.KetQuaDoiService;
import com.example.btms.ui.auth.LoginFrame;
import com.example.btms.ui.auth.LoginFrame.Role;
import com.example.btms.ui.bracket.SoDoThiDauPanel;
import com.example.btms.ui.category.NoiDungManagementPanel;
import com.example.btms.ui.cateoftuornament.DangKyNoiDungPanel;
import com.example.btms.ui.club.CauLacBoManagementPanel;
import com.example.btms.ui.control.BadmintonControlPanel;
import com.example.btms.ui.control.MultiCourtControlPanel;
import com.example.btms.ui.log.LogTab;
import com.example.btms.ui.manager.UnifiedWindowManager;
import com.example.btms.ui.manager.UnifiedWindowManager.WindowType;
import com.example.btms.ui.monitor.MonitorTab;
import com.example.btms.ui.player.VanDongVienManagementPanel;
import com.example.btms.ui.report.BaoCaoPdfPanel;
import com.example.btms.ui.screenshot.ScreenshotTab;
import com.example.btms.ui.settings.SettingsPanel;
import com.example.btms.ui.tournament.TournamentManagementComponent;
import com.example.btms.util.report.RegistrationPdfExporter;
import com.example.btms.util.ui.IconUtil;
import com.example.btms.util.ui.Ui;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
// import com.formdev.flatlaf.extras.FlatSVGIcon; // (no longer used after removing icons)

@SuppressWarnings("all")
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
    // Trang báo cáo PDF tổng hợp
    private BaoCaoPdfPanel baoCaoPdfPanel;
    // Trang biên bản (kết quả dạng log set/điểm)
    private com.example.btms.ui.result.BienBanPanel bienBanPanel;
    // Unified window manager thay thế BracketWindowManager và PlayWindowManager
    private final UnifiedWindowManager windowManager = new UnifiedWindowManager();

    private final NetworkConfig netCfg; // cấu hình interface đã chọn
    private final SQLSRVConnectionManager manager = new SQLSRVConnectionManager();
    private final DatabaseService service = new DatabaseService(manager);
    @SuppressWarnings("unused")
    private final ConnectionConfig dbCfg; // cấu hình kết nối DB (bind từ application.properties)

    private final BadmintonControlPanel controlPanel = new BadmintonControlPanel();
    private final MultiCourtControlPanel multiCourtPanel = new MultiCourtControlPanel();
    private final MonitorTab monitorTab = new MonitorTab();
    private final ScreenshotTab screenshotTab = new ScreenshotTab();
    private final LogTab logTab = new LogTab();
    // Login UI is created on demand via LoginFrame
    // Dùng JPanel thay cho JFrame để nhúng vào MainFrame
    private final TournamentManagementComponent tournamentTabPanel = new TournamentManagementComponent(service, false); // full
                                                                                                                        // management
                                                                                                                        // mode
    private GiaiDau selectedGiaiDau; // giải đấu đã chọn sau đăng nhập

    private AuthService authService;

    // UI fields (đã xóa lblAppTitle và lblVersion vì không còn header)
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

    // Đã xóa refreshHeader() vì không còn header

    public MainFrame(NetworkConfig cfg, ConnectionConfig dbCfg) {
        super("BADMINTON TUORNAMENT MANAGEMENT SYSTEM");
        this.netCfg = cfg;
        this.dbCfg = dbCfg;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Startup hygiene: do NOT carry over last selected tournament between app runs.
        // Only clears transient tournament selection keys; user Settings (theme, font,
        // etc.) remain intact.
        try {
            clearTournamentSelectionPrefs();
        } catch (Exception ignore) {
        }

        // Áp dụng theme từ Prefs trước khi build UI để tránh nháy
        boolean darkPref = new com.example.btms.config.Prefs().getBool("ui.darkTheme", false);
        if (darkPref) {
            try {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
            } catch (UnsupportedLookAndFeelException ignored) {
            }
        } else {
            try {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            } catch (UnsupportedLookAndFeelException ignored) {
            }
        }
        Ui.installModernUi();

        JComponent root = (JComponent) getContentPane();
        root.setLayout(new BorderLayout());
        root.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel status = buildStatusBar();

        // Khởi tạo trang cài đặt sớm để luôn truy cập được
        settingsPanel = new SettingsPanel(this);
        ensureViewPresent("Cài đặt", settingsPanel);
        // Chưa hiển thị view nào cho đến khi đăng nhập và chọn giải

        // Không add headerBar nữa
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
                statusHost.setText("IF: " + netCfg.ifName() + ", IP: " + netCfg.ipv4Address());
            } catch (SocketException ignored) {
                statusHost.setText("IF: " + netCfg.ifName() + ", IP: " + netCfg.ipv4Address());
            }
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    var cm = com.example.btms.service.match.CourtManagerService.getInstance();
                    if (cm.hasAnyOpenCourt()) {
                        int choice = javax.swing.JOptionPane.showConfirmDialog(
                                MainFrame.this,
                                "Hiện còn sân đang mở. Bạn có muốn đóng tất cả sân và thoát?",
                                "Đóng tất cả sân trước khi thoát",
                                javax.swing.JOptionPane.YES_NO_OPTION,
                                javax.swing.JOptionPane.WARNING_MESSAGE);
                        if (choice == javax.swing.JOptionPane.YES_OPTION) {
                            try {
                                cm.closeAllCourts();
                            } catch (Exception ignore) {
                            }
                            // Sau khi đóng tất cả sân, cho phép thoát
                            int result = javax.swing.JOptionPane.showConfirmDialog(
                                    MainFrame.this,
                                    "Bạn có chắc muốn tắt ứng dụng?\n\nTất cả dữ liệu chưa lưu sẽ bị mất.",
                                    "Xác nhận tắt ứng dụng",
                                    javax.swing.JOptionPane.YES_NO_OPTION,
                                    javax.swing.JOptionPane.QUESTION_MESSAGE);
                            if (result == javax.swing.JOptionPane.YES_OPTION)
                                try {
                                    clearTournamentSelectionPrefs();
                                } catch (Exception ignore) {
                                }
                            System.exit(0);
                            return;
                        }
                        return; // nếu chọn NO thì giữ ứng dụng mở
                    }
                } catch (HeadlessException ignore) {
                }
                // Nếu không còn sân nào mở thì cho phép dispose
                try {
                    clearTournamentSelectionPrefs();
                } catch (Exception ignore) {
                }
                try {
                    windowManager.reset(); // Đóng tất cả windows
                } catch (Exception ignore) {
                }
                System.exit(0);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    controlPanel.saveSplitLocations();
                } catch (Exception ignored) {
                }
                try {
                    windowManager.reset(); // Đóng tất cả windows
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
                try {
                    clearTournamentSelectionPrefs();
                } catch (Exception ignore) {
                }
            }
        });

        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setAlwaysOnTop(false);
        // Defer icon application until after constructor to avoid 'leaking this in
        // constructor'
        SwingUtilities.invokeLater(() -> IconUtil.applyTo(this));

        // Đã xóa việc set title và version cho header vì không còn header

        ramTimer = new javax.swing.Timer(1000, ae -> updateMemory());
        ramTimer.start();

        // Không tự động kết nối DB nữa.
        // Thay vào đó: hiển thị hộp thoại thiết lập DB, sau đó mới đăng nhập và chọn
        // giải.
        SwingUtilities.invokeLater(this::startDatabaseSetupFlow);

        // Áp dụng font scale nếu người dùng đã lưu khác 100%
        int pct = new com.example.btms.config.Prefs().getInt("ui.fontScalePercent", 100);
        if (pct != 100) {
            SwingUtilities.invokeLater(this::applyGlobalFontScale);
        }
    }

    /* -------------------- UI Builders -------------------- */

    // Đã xóa buildHeaderBar() vì không còn cần header

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
        } catch (UnsupportedLookAndFeelException ignored) {
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
    }

    /*
     * -------------------- SETUP DATABASE FLOW (manual, after network)
     * --------------------
     */
    private boolean showLoginDialog() {
        // Create the standalone login window
        LoginFrame win = new LoginFrame();
        win.setAuthService(this.authService);

        AtomicBoolean accepted = new AtomicBoolean(false);
        SecondaryLoop loop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();

        win.setListener((username, role) -> {
            try {
                currentRole = role;
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
                accepted.set(true);
            } finally {
                try {
                    win.dispose();
                } catch (Exception ignore) {
                }
                loop.exit();
            }
        });

        win.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                // If closed without login, treat as cancel
                if (!accepted.get())
                    loop.exit();
            }
        });

        win.open();
        loop.enter();
        return accepted.get();
    }

    /**
     * Bắt đầu flow cấu hình và kết nối CSDL: mở DbConnectionFrame, sau khi kết nối
     * thành công sẽ khởi tạo các panel và buộc đăng nhập + chọn giải.
     */
    private void startDatabaseSetupFlow() {
        com.example.btms.ui.db.DbConnectionFrame frame = new com.example.btms.ui.db.DbConnectionFrame();

        frame.setOnOk(sel -> {
            if (sel == null) {
                statusConn.setText("Chưa kết nối");
                statusConn.setForeground(new Color(231, 76, 60));
                if (!isVisible()) {
                    dispose();
                    System.exit(0);
                }
                return;
            }

            try {
                String url = nz(sel.getJdbcUrl());
                ConnectionConfig runtimeCfg = new ConnectionConfig().mode(ConnectionConfig.Mode.NAME);
                runtimeCfg.setUrl(url);
                if (sel.getUsername() != null && !sel.getUsername().isBlank())
                    runtimeCfg.setUsername(sel.getUsername());
                if (sel.getPassword() != null)
                    runtimeCfg.setPassword(new String(sel.getPassword()));

                service.setConfig(runtimeCfg);
                Connection conn = service.connect();
                // Ẩn/đóng cửa sổ kết nối trước khi chuyển bước chọn giải
                try {
                    frame.setVisible(false);
                } catch (Exception ignore) {
                }
                try {
                    frame.dispose();
                } catch (Exception ignore) {
                }
                onDatabaseConnected(conn);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Kết nối CSDL thất bại: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                statusConn.setText("Lỗi kết nối");
                statusConn.setForeground(new Color(231, 76, 60));
                if (!isVisible()) {
                    dispose();
                    System.exit(1);
                }
            }
        });

        frame.setOnCancel(() -> {
            statusConn.setText("Chưa kết nối");
            statusConn.setForeground(new Color(231, 76, 60));
            try {
                frame.setVisible(false);
            } catch (Exception ignore) {
            }
            try {
                frame.dispose();
            } catch (Exception ignore) {
            }
            if (!isVisible()) {
                dispose();
                System.exit(0);
            }
        });

        frame.open();
    }

    // helper
    private static String nz(String s) {
        return (s == null) ? "" : s;
    }

    /**
     * Được gọi khi kết nối DB thành công. Khởi tạo các service/panel phụ thuộc DB,
     * cập nhật trạng thái, dựng menu và ép người dùng đăng nhập + chọn giải.
     */
    private void onDatabaseConnected(Connection conn) {
        try {
            controlPanel.setConnection(conn);
            multiCourtPanel.setConnection(conn);
            // Ensure tournament panel binds to the new DB connection before using its
            // service
            try {
                tournamentTabPanel.updateConnection();
            } catch (Throwable ignore) {
            }

            noiDungService = new NoiDungService(new NoiDungRepository(conn));
            noiDungPanel = new NoiDungManagementPanel(noiDungService);

            // CLB
            CauLacBoService clbService = new CauLacBoService(new CauLacBoRepository(conn));
            cauLacBoPanel = new CauLacBoManagementPanel(clbService);
            // Vận động viên
            VanDongVienService vdvService = new VanDongVienService(new VanDongVienRepository(conn));
            vanDongVienPanel = new VanDongVienManagementPanel(vdvService, clbService);
            // Panel Nội dung của giải theo giải chọn trong Prefs
            ChiTietGiaiDauService chiTietService = new ChiTietGiaiDauService(new ChiTietGiaiDauRepository(conn));
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
            // Set network interface cho soDoThiDauPanel từ multiCourtPanel đã được set
            // trước đó
            try {
                NetworkInterface ni = multiCourtPanel.getNetworkInterface();
                if (ni != null) {
                    soDoThiDauPanel.setNetworkInterface(ni);
                }
            } catch (Exception ignored) {
            }
            // Báo cáo PDF – tổng hợp eksport
            try {
                baoCaoPdfPanel = new BaoCaoPdfPanel(conn);
            } catch (Exception ignore) {
            }
            // Tổng sắp huy chương (kết quả toàn đoàn)
            try {
                com.example.btms.ui.result.TongSapHuyChuongPanel tongSapHuyChuongPanel = new com.example.btms.ui.result.TongSapHuyChuongPanel(
                        conn, clbService);
                ensureViewPresent("Tổng sắp huy chương", tongSapHuyChuongPanel);
            } catch (Throwable ignore) {
            }
            // Trang biên bản
            try {
                bienBanPanel = new com.example.btms.ui.result.BienBanPanel(conn);
                ensureViewPresent("Trang biên bản", bienBanPanel);
            } catch (Throwable ignore) {
            }

            updateAuthService(conn);

            statusConn.setText("Đã kết nối");
            statusConn.setForeground(new Color(46, 204, 113));

            // Flow theo yêu cầu: Chỉ chọn giải, ẩn dialog, rồi mở trang chủ.
            // Mặc định coi như ADMIN để hiển thị đầy đủ tính năng (bỏ bước đăng nhập).
            try {
                monitorTab.setAdminMode(true, null);
                controlPanel.setClientName("ADMIN");
            } catch (Exception ignore) {
            }
            currentRole = Role.ADMIN;
            buildMenuBar();
            startTournamentOnlyFlow();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật UI: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Chỉ chọn giải đấu (không đăng nhập), sau đó mở trang chủ và hiển thị
     * MainFrame.
     */
    private void startTournamentOnlyFlow() {
        // 1) Chọn giải đấu
        GiaiDau gd = showTournamentSelectDialog();
        if (gd == null) {
            // Không chọn giải thì thoát nếu frame chưa hiển thị
            if (!isVisible()) {
                dispose();
                System.exit(0);
            }
            return;
        }
        selectedGiaiDau = gd;
        try {
            new com.example.btms.config.Prefs().putInt("selectedGiaiDauId", gd.getId());
            if (gd.getTenGiai() != null) {
                new com.example.btms.config.Prefs().put("selectedGiaiDauName", gd.getTenGiai());
            }
        } catch (Exception ignore) {
        }

        // 2) Hiển thị đầy đủ chức năng theo vai trò hiện tại (ADMIN)
        registerViewsForCurrentRole();
        buildMenuBar();
        rebuildNavigationTree();
        showView("Giải đấu");
        // 3) Làm mới toàn bộ để các panel nạp lại theo giải vừa chọn
        refreshApplicationData();

        // 4) Đảm bảo MainFrame hiển thị
        try {
            if (!isVisible()) {
                try {
                    if (getWidth() == 0 || getHeight() == 0)
                        pack();
                } catch (Throwable ignore) {
                }
                setLocationRelativeTo(null);
                setVisible(true);
            }
            if ((getExtendedState() & java.awt.Frame.ICONIFIED) != 0) {
                setExtendedState(java.awt.Frame.NORMAL);
            }
            toFront();
            requestFocus();
        } catch (Throwable ignore) {
        }
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
                    // Báo cáo PDF tổng hợp
                    mManage.add(menuItem("Báo cáo (PDF)"));
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
                // Backup DB tool
                JMenuItem miBackup = new JMenuItem("Sao lưu CSDL...");
                miBackup.addActionListener(e -> {
                    Connection c = null;
                    try {
                        c = (service != null) ? service.current() : null;
                    } catch (Throwable ignore) {
                    }
                    if (c == null) {
                        JOptionPane.showMessageDialog(this, "Chưa kết nối CSDL.", "Thông báo",
                                JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    try {
                        com.example.btms.ui.tools.DbBackupFrame f = new com.example.btms.ui.tools.DbBackupFrame(c);
                        f.setVisible(true);
                    } catch (Throwable ex) {
                        JOptionPane.showMessageDialog(this, "Không mở được cửa sổ sao lưu: " + ex.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                });
                mOther.add(miBackup);
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
            // Gọi refreshAll cho các panel quản lý (nếu có)
            try {
                if (noiDungPanel != null)
                    noiDungPanel.refreshAll();
            } catch (Throwable ignore) {
            }
            try {
                if (cauLacBoPanel != null)
                    cauLacBoPanel.refreshAll();
            } catch (Throwable ignore) {
            }
            try {
                if (vanDongVienPanel != null)
                    vanDongVienPanel.refreshAll();
            } catch (Throwable ignore) {
            }
            try {
                if (dangKyNoiDungPanel != null)
                    dangKyNoiDungPanel.refreshAll();
            } catch (Throwable ignore) {
            }
            try {
                if (dangKyDoiPanel != null)
                    dangKyDoiPanel.refreshAll();
            } catch (Throwable ignore) {
            }
            try {
                if (dangKyCaNhanPanel != null)
                    dangKyCaNhanPanel.refreshAll();
            } catch (Throwable ignore) {
            }
            // Làm mới Nội dung đăng ký hiển thị theo nội dung (nếu đang mở)
            try {
                if (contentParticipantsPanel != null)
                    contentParticipantsPanel.refreshAll();
            } catch (Throwable ignore) {
            }
            // Làm mới Sơ đồ thi đấu: tab đang mở reloadData(); danh sách trong cây cũng
            // được rebuild
            try {
                windowManager.reloadOpenTabs(WindowType.BRACKET);
            } catch (Throwable ignore) {
            }
            // Làm mới cây điều hướng (bao gồm lọc Sơ đồ theo bốc thăm đã có)
            rebuildNavigationTree();
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
            if (!isVisible()) {
                dispose();
                System.exit(0);
            }
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
            if (!isVisible()) {
                dispose();
                System.exit(0);
            }
            return;
        }
        selectedGiaiDau = gd;
        try {
            new com.example.btms.config.Prefs().putInt("selectedGiaiDauId", gd.getId());
            if (gd.getTenGiai() != null) {
                new com.example.btms.config.Prefs().put("selectedGiaiDauName", gd.getTenGiai());
            }
        } catch (Exception ignore) {
        }

        // 3) Hiển thị đầy đủ chức năng
        registerViewsForCurrentRole();
        buildMenuBar();
        rebuildNavigationTree();
        showView("Giải đấu");
        // Làm mới toàn bộ để các panel nạp lại theo giải vừa chọn
        refreshApplicationData();

        // Nếu frame đang bị thu nhỏ (ICONIFIED), khôi phục và đưa lên trước
        try {
            if ((getExtendedState() & java.awt.Frame.ICONIFIED) != 0) {
                setExtendedState(java.awt.Frame.NORMAL);
            }
            toFront();
            requestFocus();
        } catch (Throwable ignore) {
        }
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
            if (bienBanPanel != null)
                ensureViewPresent("Trang biên bản", bienBanPanel);
            ensureViewPresent("Logs", logTab);
            ensureViewPresent("Cài đặt", settingsPanel);
            if (baoCaoPdfPanel != null)
                ensureViewPresent("Báo cáo (PDF)", baoCaoPdfPanel);
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
                    if (gd.getTenGiai() != null) {
                        new com.example.btms.config.Prefs().put("selectedGiaiDauName", gd.getTenGiai());
                    }
                } catch (Exception ignore) {
                }
                // Đóng cửa sổ Sơ đồ thi đấu (nếu đang mở) để lần mở sau build tabs theo giải
                // mới
                windowManager.reset();
                updateNavigationRootTitleFromSelection();
                if (navModel != null)
                    navModel.nodeChanged((DefaultMutableTreeNode) navModel.getRoot());
                rebuildNavigationTree();
                // Làm mới toàn bộ để áp dụng giải mới
                refreshApplicationData();
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
                javax.swing.JMenuItem mi2 = new javax.swing.JMenuItem("Xoá bốc thăm và sơ đồ");
                mi2.addActionListener(ev -> doClearDrawAndBracketForContent(cn));

                pm.add(mi);
                pm.add(mi2);
                pm.addSeparator();
                javax.swing.JMenuItem miRefreshNode = new javax.swing.JMenuItem("Làm mới mục này");
                miRefreshNode.addActionListener(ev -> {
                    try {
                        rebuildNavigationTree();
                    } catch (Exception ex) {
                    }
                });
                pm.add(miRefreshNode);
                // Xóa đăng ký theo nội dung (của giải hiện tại)
                pm.addSeparator();
                javax.swing.JMenu mDelThisContent = new javax.swing.JMenu("Xóa đăng ký cho nội dung này");
                javax.swing.JMenuItem miDelSingC = new javax.swing.JMenuItem("Chỉ cá nhân");
                miDelSingC.addActionListener(ev -> {
                    try {
                        Connection conn = (service != null) ? service.current() : null;
                        if (conn == null) {
                            JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        int idGiai = new com.example.btms.config.Prefs().getInt("selectedGiaiDauId", -1);
                        String tenGiai = new com.example.btms.config.Prefs().get("selectedGiaiDauName", "");
                        if (idGiai <= 0) {
                            JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
                            return;
                        }
                        int c = JOptionPane.showConfirmDialog(this,
                                "Xóa TẤT CẢ đăng ký cá nhân của nội dung này trong giải\n" +
                                        (tenGiai != null && !tenGiai.isBlank() ? ("- " + tenGiai + "\n") : "") +
                                        "Hành động này không thể hoàn tác.",
                                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (c != JOptionPane.YES_OPTION)
                            return;
                        var svc = new com.example.btms.service.player.DangKiCaNhanService(
                                new com.example.btms.repository.player.DangKiCaNhanRepository(conn));
                        int deleted = svc.deleteAllByGiaiAndNoiDung(idGiai, cn.idNoiDung);
                        if (dangKyCaNhanPanel != null)
                            dangKyCaNhanPanel.refreshAll();
                        if (contentParticipantsPanel != null)
                            contentParticipantsPanel.refreshAll();
                        JOptionPane.showMessageDialog(this, "Đã xóa " + deleted + " đăng ký cá nhân của nội dung.",
                                "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                    } catch (HeadlessException ex) {
                        JOptionPane.showMessageDialog(this, "Xóa tất cả thất bại: " + ex.getMessage(), "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
                javax.swing.JMenuItem miDelTeamC = new javax.swing.JMenuItem("Chỉ đội");
                miDelTeamC.addActionListener(ev -> {
                    try {
                        Connection conn = (service != null) ? service.current() : null;
                        if (conn == null) {
                            JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        int idGiai = new com.example.btms.config.Prefs().getInt("selectedGiaiDauId", -1);
                        String tenGiai = new com.example.btms.config.Prefs().get("selectedGiaiDauName", "");
                        if (idGiai <= 0) {
                            JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
                            return;
                        }
                        int c = JOptionPane.showConfirmDialog(this,
                                "Xóa TẤT CẢ đăng ký đội của nội dung này trong giải\n" +
                                        (tenGiai != null && !tenGiai.isBlank() ? ("- " + tenGiai + "\n") : "") +
                                        "Lưu ý: Nếu CHI_TIET_DOI không có FK ON DELETE CASCADE, thành viên đội có thể còn lại.\n"
                                        +
                                        "Hành động này không thể hoàn tác.",
                                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (c != JOptionPane.YES_OPTION)
                            return;
                        var svc = new com.example.btms.service.team.DangKiDoiService(
                                new com.example.btms.repository.team.DangKiDoiRepository(conn));
                        int deleted = svc.deleteAllByGiaiAndNoiDung(idGiai, cn.idNoiDung);
                        if (dangKyDoiPanel != null)
                            dangKyDoiPanel.refreshAll();
                        if (contentParticipantsPanel != null)
                            contentParticipantsPanel.refreshAll();
                        JOptionPane.showMessageDialog(this, "Đã xóa " + deleted + " đội của nội dung.",
                                "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                    } catch (HeadlessException ex) {
                        JOptionPane.showMessageDialog(this, "Xóa tất cả thất bại: " + ex.getMessage(), "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
                javax.swing.JMenuItem miDelBothC = new javax.swing.JMenuItem("Cả cá nhân và đội");
                miDelBothC.addActionListener(ev -> {
                    try {
                        Connection conn = (service != null) ? service.current() : null;
                        if (conn == null) {
                            JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        int idGiai = new com.example.btms.config.Prefs().getInt("selectedGiaiDauId", -1);
                        String tenGiai = new com.example.btms.config.Prefs().get("selectedGiaiDauName", "");
                        if (idGiai <= 0) {
                            JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
                            return;
                        }
                        int c = JOptionPane.showConfirmDialog(this,
                                "Xóa TẤT CẢ đăng ký (cá nhân + đội) của nội dung này trong giải\n" +
                                        (tenGiai != null && !tenGiai.isBlank() ? ("- " + tenGiai + "\n") : "") +
                                        "Lưu ý: Nếu CHI_TIET_DOI không có FK ON DELETE CASCADE, thành viên đội có thể còn lại.\n"
                                        +
                                        "Hành động này không thể hoàn tác.",
                                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (c != JOptionPane.YES_OPTION)
                            return;
                        var teamSvc = new com.example.btms.service.team.DangKiDoiService(
                                new com.example.btms.repository.team.DangKiDoiRepository(conn));
                        var singSvc = new com.example.btms.service.player.DangKiCaNhanService(
                                new com.example.btms.repository.player.DangKiCaNhanRepository(conn));
                        int delTeams = teamSvc.deleteAllByGiaiAndNoiDung(idGiai, cn.idNoiDung);
                        int delSingles = singSvc.deleteAllByGiaiAndNoiDung(idGiai, cn.idNoiDung);
                        if (dangKyDoiPanel != null)
                            dangKyDoiPanel.refreshAll();
                        if (dangKyCaNhanPanel != null)
                            dangKyCaNhanPanel.refreshAll();
                        if (contentParticipantsPanel != null)
                            contentParticipantsPanel.refreshAll();
                        JOptionPane.showMessageDialog(this,
                                "Đã xóa " + delSingles + " đăng ký cá nhân và " + delTeams + " đội của nội dung.",
                                "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                    } catch (HeadlessException ex) {
                        JOptionPane.showMessageDialog(this, "Xóa tất cả thất bại: " + ex.getMessage(), "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
                mDelThisContent.add(miDelSingC);
                mDelThisContent.add(miDelTeamC);
                mDelThisContent.add(miDelBothC);
                pm.add(mDelThisContent);
            } else if (uo instanceof String label) {
                // Root node (tournament name) -> global refresh
                if (node.getParent() == null) {
                    javax.swing.JMenuItem miRootRefresh = new javax.swing.JMenuItem("Làm mới dữ liệu");
                    miRootRefresh.addActionListener(ev -> refreshApplicationData());
                    pm.add(miRootRefresh);
                }
                if ("Danh sách đăng kí".equals(label)) {
                    javax.swing.JMenuItem miRefresh = new javax.swing.JMenuItem("Làm mới danh sách");
                    miRefresh.addActionListener(ev -> {
                        try {
                            if (contentParticipantsPanel != null)
                                contentParticipantsPanel.refreshAll();
                        } catch (Exception ex) {
                        }
                    });
                    pm.add(miRefresh);
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

                    // Xóa theo nội dung đã chọn (ghi nhớ ở lastSelectedRegListContent)
                    if (lastSelectedRegListContent != null) {
                        pm.addSeparator();
                        javax.swing.JMenu mDelByContent = new javax.swing.JMenu(
                                "Xóa đăng ký cho nội dung đang chọn");
                        javax.swing.JMenuItem miDelSing = new javax.swing.JMenuItem("Chỉ cá nhân");
                        miDelSing.addActionListener(ev -> {
                            try {
                                Connection conn = (service != null) ? service.current() : null;
                                if (conn == null) {
                                    JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi",
                                            JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                int idGiai = new com.example.btms.config.Prefs().getInt("selectedGiaiDauId", -1);
                                String tenGiai = new com.example.btms.config.Prefs().get("selectedGiaiDauName", "");
                                if (idGiai <= 0) {
                                    JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
                                    return;
                                }
                                int c = JOptionPane.showConfirmDialog(this,
                                        "Xóa TẤT CẢ đăng ký cá nhân của nội dung này trong giải\n" +
                                                (tenGiai != null && !tenGiai.isBlank() ? ("- " + tenGiai + "\n") : "") +
                                                "Hành động này không thể hoàn tác.",
                                        "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                if (c != JOptionPane.YES_OPTION)
                                    return;
                                var svc = new com.example.btms.service.player.DangKiCaNhanService(
                                        new com.example.btms.repository.player.DangKiCaNhanRepository(conn));
                                int deleted = svc.deleteAllByGiaiAndNoiDung(idGiai,
                                        lastSelectedRegListContent.idNoiDung);
                                if (dangKyCaNhanPanel != null)
                                    dangKyCaNhanPanel.refreshAll();
                                if (contentParticipantsPanel != null)
                                    contentParticipantsPanel.refreshAll();
                                JOptionPane.showMessageDialog(this,
                                        "Đã xóa " + deleted + " đăng ký cá nhân của nội dung.",
                                        "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                            } catch (HeadlessException ex) {
                                JOptionPane.showMessageDialog(this, "Xóa tất cả thất bại: " + ex.getMessage(), "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        });
                        javax.swing.JMenuItem miDelTeam = new javax.swing.JMenuItem("Chỉ đội");
                        miDelTeam.addActionListener(ev -> {
                            try {
                                Connection conn = (service != null) ? service.current() : null;
                                if (conn == null) {
                                    JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi",
                                            JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                int idGiai = new com.example.btms.config.Prefs().getInt("selectedGiaiDauId", -1);
                                String tenGiai = new com.example.btms.config.Prefs().get("selectedGiaiDauName", "");
                                if (idGiai <= 0) {
                                    JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
                                    return;
                                }
                                int c = JOptionPane.showConfirmDialog(this,
                                        "Xóa TẤT CẢ đăng ký đội của nội dung này trong giải\n" +
                                                (tenGiai != null && !tenGiai.isBlank() ? ("- " + tenGiai + "\n") : "") +
                                                "Lưu ý: Nếu CHI_TIET_DOI không có FK ON DELETE CASCADE, thành viên đội có thể còn lại.\n"
                                                +
                                                "Hành động này không thể hoàn tác.",
                                        "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                if (c != JOptionPane.YES_OPTION)
                                    return;
                                var svc = new com.example.btms.service.team.DangKiDoiService(
                                        new com.example.btms.repository.team.DangKiDoiRepository(conn));
                                int deleted = svc.deleteAllByGiaiAndNoiDung(idGiai,
                                        lastSelectedRegListContent.idNoiDung);
                                if (dangKyDoiPanel != null)
                                    dangKyDoiPanel.refreshAll();
                                if (contentParticipantsPanel != null)
                                    contentParticipantsPanel.refreshAll();
                                JOptionPane.showMessageDialog(this, "Đã xóa " + deleted + " đội của nội dung.",
                                        "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                            } catch (HeadlessException ex) {
                                JOptionPane.showMessageDialog(this, "Xóa tất cả thất bại: " + ex.getMessage(), "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        });
                        javax.swing.JMenuItem miDelBoth = new javax.swing.JMenuItem("Cả cá nhân và đội");
                        miDelBoth.addActionListener(ev -> {
                            try {
                                Connection conn = (service != null) ? service.current() : null;
                                if (conn == null) {
                                    JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi",
                                            JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                int idGiai = new com.example.btms.config.Prefs().getInt("selectedGiaiDauId", -1);
                                String tenGiai = new com.example.btms.config.Prefs().get("selectedGiaiDauName", "");
                                if (idGiai <= 0) {
                                    JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
                                    return;
                                }
                                int c = JOptionPane.showConfirmDialog(this,
                                        "Xóa TẤT CẢ đăng ký (cá nhân + đội) của nội dung này trong giải\n" +
                                                (tenGiai != null && !tenGiai.isBlank() ? ("- " + tenGiai + "\n") : "") +
                                                "Lưu ý: Nếu CHI_TIET_DOI không có FK ON DELETE CASCADE, thành viên đội có thể còn lại.\n"
                                                +
                                                "Hành động này không thể hoàn tác.",
                                        "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                if (c != JOptionPane.YES_OPTION)
                                    return;
                                var teamSvc = new com.example.btms.service.team.DangKiDoiService(
                                        new com.example.btms.repository.team.DangKiDoiRepository(conn));
                                var singSvc = new com.example.btms.service.player.DangKiCaNhanService(
                                        new com.example.btms.repository.player.DangKiCaNhanRepository(conn));
                                int delTeams = teamSvc.deleteAllByGiaiAndNoiDung(idGiai,
                                        lastSelectedRegListContent.idNoiDung);
                                int delSingles = singSvc.deleteAllByGiaiAndNoiDung(idGiai,
                                        lastSelectedRegListContent.idNoiDung);
                                if (dangKyDoiPanel != null)
                                    dangKyDoiPanel.refreshAll();
                                if (dangKyCaNhanPanel != null)
                                    dangKyCaNhanPanel.refreshAll();
                                if (contentParticipantsPanel != null)
                                    contentParticipantsPanel.refreshAll();
                                JOptionPane.showMessageDialog(this,
                                        "Đã xóa " + delSingles + " đăng ký cá nhân và " + delTeams
                                                + " đội của nội dung.",
                                        "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                            } catch (HeadlessException ex) {
                                JOptionPane.showMessageDialog(this, "Xóa tất cả thất bại: " + ex.getMessage(), "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        });
                        mDelByContent.add(miDelSing);
                        mDelByContent.add(miDelTeam);
                        mDelByContent.add(miDelBoth);
                        pm.add(mDelByContent);
                    }
                }
                if ("Nội dung của giải".equals(label)) {
                    javax.swing.JMenuItem miRefreshNdg = new javax.swing.JMenuItem("Làm mới danh sách nội dung");
                    miRefreshNdg.addActionListener(ev -> {
                        try {
                            if (dangKyNoiDungPanel != null)
                                dangKyNoiDungPanel.refreshAll();
                        } catch (Exception ex) {
                        }
                        try {
                            rebuildNavigationTree();
                        } catch (Exception ex) {
                        }
                    });
                    pm.add(miRefreshNdg);
                }
                // Thêm menu cho nút phải tại mục "Đăng ký đội" để xuất PDF
                if ("Đăng ký thi đấu".equals(label)) {
                    javax.swing.JMenuItem miRefreshRegs = new javax.swing.JMenuItem("Làm mới các bảng đăng ký");
                    miRefreshRegs.addActionListener(ev -> {
                        try {
                            if (dangKyDoiPanel != null)
                                dangKyDoiPanel.refreshAll();
                        } catch (Exception ex) {
                        }
                        try {
                            if (dangKyCaNhanPanel != null)
                                dangKyCaNhanPanel.refreshAll();
                        } catch (Exception ex) {
                        }
                    });
                    pm.add(miRefreshRegs);
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
                    pm.addSeparator();
                    javax.swing.JMenu mDeleteAll = new javax.swing.JMenu("Xóa tất cả đăng ký");
                    javax.swing.JMenuItem miDelSingles = new javax.swing.JMenuItem("Chỉ cá nhân");
                    miDelSingles.addActionListener(ev -> {
                        try {
                            Connection conn = (service != null) ? service.current() : null;
                            if (conn == null) {
                                JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            int idGiai = new com.example.btms.config.Prefs().getInt("selectedGiaiDauId", -1);
                            String tenGiai = new com.example.btms.config.Prefs().get("selectedGiaiDauName", "");
                            if (idGiai <= 0) {
                                JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
                                return;
                            }
                            int c = JOptionPane.showConfirmDialog(this,
                                    "Bạn có chắc muốn xóa TẤT CẢ đăng ký cá nhân của giải\n" +
                                            (tenGiai != null && !tenGiai.isBlank() ? ("- " + tenGiai + "\n") : "") +
                                            "Hành động này không thể hoàn tác.",
                                    "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (c != JOptionPane.YES_OPTION)
                                return;
                            var svc = new com.example.btms.service.player.DangKiCaNhanService(
                                    new com.example.btms.repository.player.DangKiCaNhanRepository(conn));
                            int deleted = svc.deleteAllByGiai(idGiai);
                            if (dangKyCaNhanPanel != null)
                                dangKyCaNhanPanel.refreshAll();
                            JOptionPane.showMessageDialog(this, "Đã xóa " + deleted + " đăng ký cá nhân.",
                                    "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                        } catch (HeadlessException ex) {
                            JOptionPane.showMessageDialog(this, "Xóa tất cả thất bại: " + ex.getMessage(), "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    javax.swing.JMenuItem miDelTeams = new javax.swing.JMenuItem("Chỉ đội");
                    miDelTeams.addActionListener(ev -> {
                        try {
                            Connection conn = (service != null) ? service.current() : null;
                            if (conn == null) {
                                JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            int idGiai = new com.example.btms.config.Prefs().getInt("selectedGiaiDauId", -1);
                            String tenGiai = new com.example.btms.config.Prefs().get("selectedGiaiDauName", "");
                            if (idGiai <= 0) {
                                JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
                                return;
                            }
                            int c = JOptionPane.showConfirmDialog(this,
                                    "Bạn có chắc muốn xóa TẤT CẢ đăng ký đội của giải\n" +
                                            (tenGiai != null && !tenGiai.isBlank() ? ("- " + tenGiai + "\n") : "") +
                                            "Lưu ý: Nếu CHI_TIET_DOI không có FK ON DELETE CASCADE, thành viên đội có thể còn lại.\n"
                                            +
                                            "Hành động này không thể hoàn tác.",
                                    "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (c != JOptionPane.YES_OPTION)
                                return;
                            var svc = new com.example.btms.service.team.DangKiDoiService(
                                    new com.example.btms.repository.team.DangKiDoiRepository(conn));
                            int deleted = svc.deleteAllByGiai(idGiai);
                            if (dangKyDoiPanel != null)
                                dangKyDoiPanel.refreshAll();
                            JOptionPane.showMessageDialog(this, "Đã xóa " + deleted + " đội.",
                                    "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                        } catch (HeadlessException ex) {
                            JOptionPane.showMessageDialog(this, "Xóa tất cả thất bại: " + ex.getMessage(), "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    javax.swing.JMenuItem miDelBoth = new javax.swing.JMenuItem("Cả cá nhân và đội");
                    miDelBoth.addActionListener(ev -> {
                        try {
                            Connection conn = (service != null) ? service.current() : null;
                            if (conn == null) {
                                JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            int idGiai = new com.example.btms.config.Prefs().getInt("selectedGiaiDauId", -1);
                            String tenGiai = new com.example.btms.config.Prefs().get("selectedGiaiDauName", "");
                            if (idGiai <= 0) {
                                JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
                                return;
                            }
                            int c = JOptionPane.showConfirmDialog(this,
                                    "Bạn có chắc muốn xóa TẤT CẢ đăng ký (cá nhân + đội) của giải\n" +
                                            (tenGiai != null && !tenGiai.isBlank() ? ("- " + tenGiai + "\n") : "") +
                                            "Lưu ý: Nếu CHI_TIET_DOI không có FK ON DELETE CASCADE, thành viên đội có thể còn lại.\n"
                                            +
                                            "Hành động này không thể hoàn tác.",
                                    "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (c != JOptionPane.YES_OPTION)
                                return;
                            var teamSvc = new com.example.btms.service.team.DangKiDoiService(
                                    new com.example.btms.repository.team.DangKiDoiRepository(conn));
                            var singSvc = new com.example.btms.service.player.DangKiCaNhanService(
                                    new com.example.btms.repository.player.DangKiCaNhanRepository(conn));
                            int delTeams = teamSvc.deleteAllByGiai(idGiai);
                            int delSingles = singSvc.deleteAllByGiai(idGiai);
                            if (dangKyDoiPanel != null)
                                dangKyDoiPanel.refreshAll();
                            if (dangKyCaNhanPanel != null)
                                dangKyCaNhanPanel.refreshAll();
                            JOptionPane.showMessageDialog(this,
                                    "Đã xóa " + delSingles + " đăng ký cá nhân và " + delTeams + " đội.",
                                    "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                        } catch (HeadlessException ex) {
                            JOptionPane.showMessageDialog(this, "Xóa tất cả thất bại: " + ex.getMessage(), "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    mDeleteAll.add(miDelSingles);
                    mDeleteAll.add(miDelTeams);
                    mDeleteAll.add(miDelBoth);
                    pm.add(mDeleteAll);
                }
                if ("Đăng ký đội".equals(label)) {
                    javax.swing.JMenuItem miRefreshDoi = new javax.swing.JMenuItem("Làm mới danh sách đội");
                    miRefreshDoi.addActionListener(ev -> {
                        try {
                            if (dangKyDoiPanel != null)
                                dangKyDoiPanel.refreshAll();
                        } catch (Exception ex) {
                        }
                    });
                    pm.add(miRefreshDoi);
                    javax.swing.JMenuItem miDelAllTeams = new javax.swing.JMenuItem("Xóa tất cả đăng ký đội");
                    miDelAllTeams.addActionListener(ev -> {
                        try {
                            Connection conn = (service != null) ? service.current() : null;
                            if (conn == null) {
                                JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            int idGiai = new com.example.btms.config.Prefs().getInt("selectedGiaiDauId", -1);
                            String tenGiai = new com.example.btms.config.Prefs().get("selectedGiaiDauName", "");
                            if (idGiai <= 0) {
                                JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
                                return;
                            }
                            int c = JOptionPane.showConfirmDialog(this,
                                    "Bạn có chắc muốn xóa TẤT CẢ đăng ký đội của giải\n" +
                                            (tenGiai != null && !tenGiai.isBlank() ? ("- " + tenGiai + "\n") : "") +
                                            "Lưu ý: Nếu CHI_TIET_DOI không có FK ON DELETE CASCADE, thành viên đội có thể còn lại.\n"
                                            +
                                            "Hành động này không thể hoàn tác.",
                                    "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (c != JOptionPane.YES_OPTION)
                                return;
                            var svc = new com.example.btms.service.team.DangKiDoiService(
                                    new com.example.btms.repository.team.DangKiDoiRepository(conn));
                            int deleted = svc.deleteAllByGiai(idGiai);
                            if (dangKyDoiPanel != null)
                                dangKyDoiPanel.refreshAll();
                            JOptionPane.showMessageDialog(this, "Đã xóa " + deleted + " đội.",
                                    "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                        } catch (HeadlessException ex) {
                            JOptionPane.showMessageDialog(this, "Xóa tất cả thất bại: " + ex.getMessage(), "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    pm.add(miDelAllTeams);
                }
                if ("Đăng ký cá nhân".equals(label)) {
                    javax.swing.JMenuItem miRefreshCaNhan = new javax.swing.JMenuItem("Làm mới danh sách cá nhân");
                    miRefreshCaNhan.addActionListener(ev -> {
                        try {
                            if (dangKyCaNhanPanel != null)
                                dangKyCaNhanPanel.refreshAll();
                        } catch (Exception ex) {
                        }
                    });
                    pm.add(miRefreshCaNhan);
                    javax.swing.JMenuItem miDelAllSingles = new javax.swing.JMenuItem("Xóa tất cả đăng ký cá nhân");
                    miDelAllSingles.addActionListener(ev -> {
                        try {
                            Connection conn = (service != null) ? service.current() : null;
                            if (conn == null) {
                                JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            int idGiai = new com.example.btms.config.Prefs().getInt("selectedGiaiDauId", -1);
                            String tenGiai = new com.example.btms.config.Prefs().get("selectedGiaiDauName", "");
                            if (idGiai <= 0) {
                                JOptionPane.showMessageDialog(this, "Chưa chọn giải.");
                                return;
                            }
                            int c = JOptionPane.showConfirmDialog(this,
                                    "Bạn có chắc muốn xóa TẤT CẢ đăng ký cá nhân của giải\n" +
                                            (tenGiai != null && !tenGiai.isBlank() ? ("- " + tenGiai + "\n") : "") +
                                            "Hành động này không thể hoàn tác.",
                                    "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (c != JOptionPane.YES_OPTION)
                                return;
                            var svc = new com.example.btms.service.player.DangKiCaNhanService(
                                    new com.example.btms.repository.player.DangKiCaNhanRepository(conn));
                            int deleted = svc.deleteAllByGiai(idGiai);
                            if (dangKyCaNhanPanel != null)
                                dangKyCaNhanPanel.refreshAll();
                            JOptionPane.showMessageDialog(this, "Đã xóa " + deleted + " đăng ký cá nhân.",
                                    "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                        } catch (HeadlessException ex) {
                            JOptionPane.showMessageDialog(this, "Xóa tất cả thất bại: " + ex.getMessage(), "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    pm.add(miDelAllSingles);
                }
                if ("Sơ đồ thi đấu".equals(label)) {
                    javax.swing.JMenuItem miRefreshSoDo = new javax.swing.JMenuItem("Làm mới tất cả sơ đồ đang mở");
                    miRefreshSoDo.addActionListener(ev -> {
                        try {
                            windowManager.reloadOpenTabs(WindowType.BRACKET);
                        } catch (Exception ex) {
                        }
                    });
                    pm.add(miRefreshSoDo);
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
                        } catch (HeadlessException ex) {
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
                        } catch (HeadlessException ex) {
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
        Integer __idTmp = (selectedGiaiDau != null) ? selectedGiaiDau.getId() : null;
        int idGiai = (__idTmp != null)
                ? __idTmp
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
            var regs = dkSvc.listByGiaiAndNoiDung(idGiai, idNoiDung, null);
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

    private void doClearDrawAndBracketForContent(ContentNode cn) {
        Connection conn = (service != null) ? service.current() : null;
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Chưa có kết nối CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SoDoCaNhanService soDoCaNhanService = new SoDoCaNhanService(new SoDoCaNhanRepository(conn));
        SoDoDoiService soDoDoiService = new SoDoDoiService(new SoDoDoiRepository(conn));
        KetQuaCaNhanService ketQuaCaNhanService = new KetQuaCaNhanService(new KetQuaCaNhanRepository(conn));
        KetQuaDoiService ketQuaDoiService = new KetQuaDoiService(new KetQuaDoiRepository(conn));
        int idGiai = new com.example.btms.config.Prefs().getInt("selectedGiaiDauId", -1);
        if (idGiai <= 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải hoặc nội dung", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xoá ?\n\n" +
                        "Hành động này sẽ xoá dữ liệu và không thể hoàn tác.",
                "Xác nhận xoá", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Xác định nội dung là đội/đôi hay cá nhân từ repository (an toàn, không ném
        // SQLException ra ngoài)
        boolean isTeam;
        try {
            var ndOpt = new NoiDungService(new NoiDungRepository(conn)).getNoiDungById(cn.idNoiDung);
            isTeam = ndOpt.isPresent() && Boolean.TRUE.equals(ndOpt.get().getTeam());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không xác định được loại nội dung: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int idNoiDung = cn.idNoiDung;

        try {
            // 1) Xoá toàn bộ bản ghi sơ đồ theo loại nội dung
            if (isTeam) {
                List<SoDoDoi> olds = soDoDoiService.list(idGiai, idNoiDung);
                for (SoDoDoi r : olds) {
                    try {
                        soDoDoiService.delete(idGiai, idNoiDung, r.getViTri());
                    } catch (Exception ignore) {
                    }
                }
            } else {
                List<SoDoCaNhan> olds = soDoCaNhanService.list(idGiai, idNoiDung);
                for (SoDoCaNhan r : olds) {
                    try {
                        soDoCaNhanService.delete(idGiai, idNoiDung, r.getViTri());
                    } catch (Exception ignore) {
                    }
                }
            }

            // 2) Xoá kết quả huy chương (ranks 1,2,3)
            for (int rank : new int[] { 1, 2, 3 }) {
                try {
                    if (isTeam) {
                        ketQuaDoiService.delete(idGiai, idNoiDung, rank);
                    } else {
                        ketQuaCaNhanService.delete(idGiai, idNoiDung, rank);
                    }
                } catch (Exception ignore) {
                }
            }

            // 3) Xoá danh sách bốc thăm (draws)
            try {
                if (isTeam) {
                    var bocThamDoiSvc = new com.example.btms.service.draw.BocThamDoiService(
                            conn, new com.example.btms.repository.draw.BocThamDoiRepository(conn));
                    var drawList = bocThamDoiSvc.list(idGiai, idNoiDung);
                    for (var r : drawList) {
                        try {
                            bocThamDoiSvc.delete(idGiai, idNoiDung, r.getThuTu());
                        } catch (Exception ignore) {
                        }
                    }
                } else {
                    var bocThamCaNhanSvc = new com.example.btms.service.draw.BocThamCaNhanService(
                            new com.example.btms.repository.draw.BocThamCaNhanRepository(conn));
                    var drawList = bocThamCaNhanSvc.list(idGiai, idNoiDung);
                    for (var r : drawList) {
                        try {
                            bocThamCaNhanSvc.delete(idGiai, idNoiDung, r.getIdVdv());
                        } catch (Exception ignore) {
                        }
                    }
                }
            } catch (Exception ignore) {
            }

            JOptionPane.showMessageDialog(this, "Đã xoá sơ đồ, kết quả và bốc thăm (nếu có)", "Hoàn tất",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xoá: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Bốc thăm + lưu và mở sơ đồ, auto seed + lưu sơ đồ. */
    private void doAutoDrawSaveAndOpenForContent(ContentNode cn) {
        try {
            NetworkInterface ni = (netCfg != null && netCfg.ifName() != null)
                    ? NetworkInterface.getByName(netCfg.ifName())
                    : null;
            if (!doAutoDrawSave(cn.idNoiDung))
                return;
            windowManager.openBracketWindow(service, this,
                    (selectedGiaiDau != null ? selectedGiaiDau.getTenGiai() : null), ni);
            windowManager.ensureBracketTab(service, cn.idNoiDung, cn.label, this);
            com.example.btms.ui.bracket.SoDoThiDauPanel p = windowManager.getBracketPanelByNoiDungId(cn.idNoiDung);
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
                    Integer _tmpId = (selectedGiaiDau != null) ? selectedGiaiDau.getId() : null;
                    int idGiai = (_tmpId != null) ? _tmpId : -1;
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
            result.add(new DefaultMutableTreeNode("Trang biên bản"));
            result.add(new DefaultMutableTreeNode("Tổng sắp huy chương"));
            result.add(new DefaultMutableTreeNode("Báo cáo (PDF)"));
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
        try {
            NetworkInterface ni = (netCfg != null && netCfg.ifName() != null)
                    ? NetworkInterface.getByName(netCfg.ifName())
                    : null;
            if (node == null)
                return;
            if (node.getChildCount() > 0)
                return; // chỉ xử lý leaf items
            Object uo = node.getUserObject();
            if (uo instanceof ContentNode cn) {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                String parentLabel = (parent != null && parent.getUserObject() instanceof String s) ? s : "";
                if (null != parentLabel)
                    switch (parentLabel) {
                        case "Danh sách đăng kí" -> {
                            if (contentParticipantsPanel != null) {
                                ensureViewPresent("Danh sách đăng kí", contentParticipantsPanel);
                                contentParticipantsPanel.selectNoiDungById(cn.idNoiDung);
                                showView("Danh sách đăng kí");
                            }
                        }
                        case "Sơ đồ thi đấu" -> {
                            windowManager.openBracketWindow(service, this,
                                    (selectedGiaiDau != null ? selectedGiaiDau.getTenGiai() : null), ni);
                            windowManager.ensureBracketTab(service, cn.idNoiDung, cn.label, this);
                        }
                        case "Nội dung của giải" -> {
                            if (dangKyNoiDungPanel != null) {
                                ensureViewPresent("Nội dung của giải", dangKyNoiDungPanel);
                                try {
                                    dangKyNoiDungPanel.selectNoiDungById(cn.idNoiDung);
                                } catch (Throwable ignore) {
                                }
                                showView("Nội dung của giải");
                            }
                        }
                        default -> {
                        }
                    }
                return;
            }
            if (uo instanceof String label) {
                if ("Sơ đồ thi đấu".equals(label)) {
                    windowManager.openBracketWindow(service, this,
                            (selectedGiaiDau != null ? selectedGiaiDau.getTenGiai() : null), ni);
                    return;
                }
                if ("Bốc thăm thi đấu".equals(label)) {
                    if (bocThamThiDauPanel != null)
                        ensureViewPresent("Bốc thăm thi đấu", bocThamThiDauPanel);
                }
                if (views.containsKey(label))
                    showView(label);
            }
        } catch (SocketException ignored) {
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
            windowManager.closeWindow(WindowType.BRACKET);
        } catch (Exception ignore) {
        }
        try {
            windowManager.closeWindow(WindowType.PLAY);
        } catch (Exception ignore) {
        }
        try {
            clearTournamentSelectionPrefs();
        } catch (Exception ignore) {
        }
        currentRole = null;
        selectedGiaiDau = null;
        showView("Cài đặt");
        buildMenuBar();
        rebuildNavigationTree();
    }

    /** Clear only transient tournament selection preferences on exit/logout. */
    private void clearTournamentSelectionPrefs() {
        try {
            com.example.btms.config.Prefs p = new com.example.btms.config.Prefs();
            p.remove("selectedGiaiDauId");
            p.remove("selectedGiaiDauName");
        } catch (Exception ignore) {
        }
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
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException
                | InvocationTargetException ignore) {
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
        } catch (SecurityException ignore) {
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
    // Đã chuyển toàn bộ logic Sơ đồ thi đấu sang BracketWindowManager

    /** Đưa cửa sổ ra trước, và focus tab tương ứng idNoiDung nếu tồn tại. */
    @SuppressWarnings("unused")
    private void showSoDoTabForNoiDung(Integer idNoiDung) {
        NetworkInterface ni;
        try {
            ni = (netCfg != null && netCfg.ifName() != null)
                    ? NetworkInterface.getByName(netCfg.ifName())
                    : null;
            // Method kept for compatibility; delegate to unified window manager
            if (ni != null) {
                windowManager.openBracketWindow(service, this,
                        (selectedGiaiDau != null ? selectedGiaiDau.getTenGiai() : null), ni);
            } else {
                // Open without network interface if none available
                windowManager.openBracketWindow(service, this,
                        (selectedGiaiDau != null ? selectedGiaiDau.getTenGiai() : null), null);
            }
            if (idNoiDung != null) {
                // Focus if already exists; creation requires a title which we don't have here
                var p = windowManager.getBracketPanelByNoiDungId(idNoiDung);
                // no-op: window is brought to front by openWindow; focusing is handled by user
            }
        } catch (SocketException ignored) {
            // If we cannot resolve the network interface, open window without it.
            try {
                windowManager.openBracketWindow(service, this,
                        (selectedGiaiDau != null ? selectedGiaiDau.getTenGiai() : null), null);
            } catch (Exception ignore) {
            }
        }
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
            if (selectedGiaiDau != null && selectedGiaiDau.getId() != null) {
                idGiai = selectedGiaiDau.getId();
            } else {
                idGiai = new Prefs().getInt("selectedGiaiDauId", -1);
            }
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
        windowManager.openPlayWindow(
                service,
                this,
                currentRole,
                (selectedGiaiDau != null ? selectedGiaiDau.getTenGiai() : null),
                multiCourtPanel != null ? multiCourtPanel.getNetworkInterface() : null);
    }

    /* -------------------- Inline dialogs -------------------- */

    private GiaiDau showTournamentSelectDialog() {
        // Lấy userId từ Preferences nếu có
        Integer currentUserId = null;
        try {
            com.example.btms.config.Prefs prefs = new com.example.btms.config.Prefs();
            int userId = prefs.getInt("userId", -1);
            if (userId != -1) {
                currentUserId = userId;
            }
        } catch (Exception e) {
            // Không có user ID hoặc lỗi, hiển thị tất cả giải đấu
        }

        return com.example.btms.ui.tournament.TournamentManagementComponent.showSelectionDialog(this, service,
                currentUserId);
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
