package com.example.btms.ui.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.NetworkInterface;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.example.btms.config.Prefs;
import com.example.btms.controller.ScoreboardPinController;
import com.example.btms.model.match.BadmintonMatch;
import com.example.btms.model.player.VanDongVien;
import com.example.btms.model.team.DangKiDoi;
import com.example.btms.repository.category.CategoryRepository;
import com.example.btms.repository.player.VanDongVienRepository;
import com.example.btms.service.scoreboard.ScoreboardRemote;
import com.example.btms.service.scoreboard.ScoreboardService;
import com.example.btms.service.team.DoiService;
import com.example.btms.ui.scoreboard.MiniScorePanel;
import com.example.btms.util.log.Log;
import com.example.btms.util.net.NetworkUtil;
import com.example.btms.util.qr.QRCodeUtil;
import com.example.btms.util.swing.SelectionGuard;
import com.example.btms.util.ui.ButtonFactory;

public class BadmintonControlPanel extends JPanel implements PropertyChangeListener {

    /* ===== Services & model ===== */
    private BadmintonMatch match;
    private final ScoreboardService scoreboardSvc = new ScoreboardService();
    private Connection conn;
    private NetworkInterface selectedIf;
    private int courtPort = -1; // Port của sân tương ứng
    private String courtId = ""; // ID của sân để hiển thị trên monitor

    /* ===== Widgets: Config ===== */
    private final JComboBox<String> cboHeaderSingles = new JComboBox<>();
    private final JComboBox<String> cboHeaderDoubles = new JComboBox<>();
    private final JComboBox<String> cboNameA = new JComboBox<>();
    private final JComboBox<String> cboNameB = new JComboBox<>();
    private final JComboBox<DangKiDoi> cboTeamA = new JComboBox<>();
    private final JComboBox<DangKiDoi> cboTeamB = new JComboBox<>();
    private final JComboBox<String> bestOf = new JComboBox<>(new String[] { "Bo 1", "Bo 3" });
    private final JCheckBox doubles = new JCheckBox("Đánh đôi");
    private final JComboBox<String> initialServer = new JComboBox<>(
            new String[] { "Đội A giao cầu", "Đội B giao cầu" });

    /* ===== Widgets: Controls ===== */
    private final JComboBox<String> cboDisplayKind = new JComboBox<>(
            new String[] { "Dọc (vertical)", "Ngang (horizontal)" });
    private final JComboBox<String> cboScreen = new JComboBox<>();
    private JButton btnStart, btnFinish, btnReset, btnOpenDisplay, btnOpenDisplayH, btnCloseDisplay, btnReloadLists;

    /* ===== Score buttons ===== */
    private JButton aPlus, bPlus, aMinus, bMinus, undo, nextGame, swapEnds, toggleServe;

    /* ===== Status labels ===== */
    private final JLabel lblGame = new JLabel("Ván 1", SwingConstants.LEFT);
    private final JLabel lblGamesWon = new JLabel("Ván: 0 - 0", SwingConstants.LEFT);
    private final JLabel lblServer = new JLabel("Giao cầu: A (R)", SwingConstants.LEFT);
    private final JLabel lblStatus = new JLabel("Sẵn sàng", SwingConstants.LEFT);
    private final JLabel lblWinner = new JLabel("-", SwingConstants.LEFT);

    /* ===== Remote control (URL + QR) ===== */
    private final JLabel lblRemoteUrl = new JLabel("-");
    private final JLabel lblRemoteQr = new JLabel();
    // Trạng thái hiển thị link điều khiển và giá trị URL hiện tại
    private boolean remoteUrlVisible = false;
    private boolean qrCodeVisible = false; // Mặc định hiển thị QR code
    private String currentRemoteUrl = null;
    private JButton btnToggleLinkVisible;
    private JButton btnToggleQrVisible;

    /* ===== Live preview ===== */
    private MiniScorePanel mini;
    private JPanel miniContainer; // Container chứa mini panel

    /* ===== Labels để ẩn/hiện ===== */
    private JLabel labHeaderSingles, labHeaderDoubles, labA1, labB1, labTeamA, labTeamB;

    /* ===== Data maps ===== */
    private final Map<String, Integer> headerKnrSingles = new LinkedHashMap<>();
    private final Map<String, Integer> headerKnrDoubles = new LinkedHashMap<>();
    private final Map<String, Integer> singlesNameToId = new HashMap<>();

    private boolean hasStarted = false;
    private boolean finishScheduled = false;
    private javax.swing.Timer finishTimer = null;

    /* ===== Split panes & prefs ===== */
    private final JSplitPane mainSplit; // Left | CenterRight
    private final JSplitPane centerRightSplit; // Center | Right
    private final JSplitPane leftVert; // (Config | Controls)
    private final JSplitPane midVert; // (Live | Score+QR)
    private final JSplitPane rightVert; // (Status | Log)
    private final Preferences prefs = Preferences.userNodeForPackage(BadmintonControlPanel.class);

    /* ===== UI const ===== */
    private static final String PH_HEADER_S = "— Chọn nội dung đơn —";
    private static final String PH_HEADER_D = "— Chọn nội dung đôi —";
    private static final String PH_PLAYER = "— Chọn VĐV —";
    private static final String PH_TEAM = "— Chọn đội —";

    private static final Font FONT_LABEL = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_VALUE = new Font("SansSerif", Font.BOLD, 13);
    private static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD, 15);
    private static final Font FONT_BTN = new Font("SansSerif", Font.BOLD, 14);
    // private static final Font FONT_BTN_BIG = new Font("SansSerif", Font.BOLD,
    // 15); // (unused)

    private static final Color COL_PRIMARY = new Color(30, 136, 229);
    private static final Color COL_SUCCESS = new Color(46, 204, 113);
    private static final Color COL_DANGER = new Color(231, 76, 60);
    private static final Color COL_WARNING = new Color(241, 196, 15);
    private static final Color COL_NEUTRAL = new Color(120, 120, 120);

    private static final Dimension BTN_CTRL = new Dimension(200, 30);
    private static final Dimension BTN_CTRL_SMALL = new Dimension(150, 30);
    private static final Dimension BTN_SCORE = new Dimension(120, 46);
    private static final Dimension BTN_UTILITY = new Dimension(140, 46);

    /* ===== Utils ===== */
    private final Log logger = new Log();
    private final SelectionGuard guard = new SelectionGuard();

    // Client name cho broadcast - có thể set từ MainFrame
    private String customClientName = null;

    public BadmintonMatch getMatch() {
        return this.match;
    }

    private void initializeMatch() {
        // Khởi tạo với shared match hoặc match của PIN nếu có
        if (courtPinCode != null && !courtPinCode.equals("0000")) {
            BadmintonMatch pinMatch = ScoreboardPinController.getMatchByPin(courtPinCode);
            if (pinMatch != null) {
                this.match = pinMatch;
                return;
            }
        }
        // Fallback to shared match
        this.match = ScoreboardRemote.get().match();
    }

    private void switchToMatchByPin() {
        BadmintonMatch oldMatch = this.match;

        // Remove listeners from old match
        if (oldMatch != null) {
            oldMatch.removePropertyChangeListener(this);
            if (mini != null) {
                oldMatch.removePropertyChangeListener(mini);
            }
        }

        BadmintonMatch pinMatch = ScoreboardPinController.getMatchByPin(courtPinCode);
        if (pinMatch != null) {
            this.match = pinMatch;
            logger.logTs("Switched to PIN match for PIN: %s", courtPinCode);
        } else {
            this.match = ScoreboardRemote.get().match();
            logger.logTs("Fallback to shared match for PIN: %s", courtPinCode);
        }

        // Add listeners to new match
        match.addPropertyChangeListener(this);

        // Recreate mini panel with new match
        recreateMiniPanel();

        // Cập nhật UI với match mới
        SwingUtilities.invokeLater(() -> {
            if (mini != null) {
                // Force refresh mini panel with new match data
                mini.repaint();
                mini.revalidate();
            }
        });
    }

    private void recreateMiniPanel() {
        if (match != null && miniContainer != null) {
            SwingUtilities.invokeLater(() -> {
                // Remove old mini panel if exists
                if (mini != null) {
                    miniContainer.remove(mini);
                }

                // Create new mini panel with new match
                mini = new MiniScorePanel(match);
                mini.setBorder(new EmptyBorder(6, 6, 6, 6));

                // Add new mini panel to container
                miniContainer.add(mini);

                // Add listener to match for mini panel updates
                match.addPropertyChangeListener(mini);

                // Refresh container
                miniContainer.revalidate();
                miniContainer.repaint();

                logger.logTs("Recreated mini panel with new match for PIN: %s", courtPinCode);
            });
        }
    }

    public BadmintonControlPanel() {
        super(new BorderLayout());
        initializeMatch();
        match.addPropertyChangeListener(this);

        // Debug: Kiểm tra port khi khởi tạo
        logger.logTs("BadmintonControlPanel constructor: courtPort = %d", this.courtPort);

        /* ===== Column LEFT: Cấu hình + Điều khiển ===== */
        JPanel config = buildConfigCard();
        JPanel controls = buildControlsCard();
        leftVert = vSplit(config, controls, 0.30);
        JPanel leftCol = wrapWithSize(leftVert, new Dimension(300, 420), new Dimension(560, 620));

        /* ===== Column CENTER: Live + Score/QR ===== */
        JPanel live = buildLiveCard();

        // Khởi tạo MiniScorePanel sau khi buildLiveCard() đã tạo miniContainer
        mini = new MiniScorePanel(match);
        mini.setBorder(new EmptyBorder(6, 6, 6, 6));
        miniContainer.add(mini);

        JPanel scoreQ = buildScoreAndQrCard();
        midVert = vSplit(live, scoreQ, 0.40);
        JPanel midCol = wrapWithSize(midVert, new Dimension(520, 420), new Dimension(640, 620));

        /* ===== Column RIGHT: Trạng thái + Log ===== */
        JPanel status = buildStatusCard();
        JPanel logs = buildLogCard();
        rightVert = vSplit(status, logs, 0.20);
        JPanel rightCol = wrapWithSize(rightVert, new Dimension(300, 420), new Dimension(460, 620));

        /* ===== HORIZONTAL: Center | Right ===== */
        centerRightSplit = hSplit(midCol, rightCol, 0.80); // ~70% cho cột giữa

        /* ===== HORIZONTAL: Left | (Center|Right) ===== */
        mainSplit = hSplit(leftCol, centerRightSplit, 0.20); // ~20% cho cột trái
        lockRightMin(mainSplit, rightCol, 100);

        add(mainSplit, BorderLayout.CENTER);

        // defaults & bindings
        cboDisplayKind.setSelectedIndex(1);
        populateScreens();
        installKeyBindings();

        SwingUtilities.invokeLater(this::restoreSplitLocations);
    }

    /* =================== PUBLIC APIS =================== */

    public void setConnection(Connection connection) {
        this.conn = connection;
        reloadListsFromDb();
    }

    public void setNetworkInterface(NetworkInterface nif) {
        this.selectedIf = nif;
        updateRemoteLinkUi();
    }

    /** Gọi từ MainFrame để set client name cho broadcast */
    public void setClientName(String clientName) {
        this.customClientName = clientName;
    }

    /** Set port của sân cho bảng điểm */
    public void setCourtPort(int port) {
        logger.logTs("BadmintonControlPanel.setCourtPort: Đang set port từ %d thành %d", this.courtPort, port);
        logger.logTs("BadmintonControlPanel.setCourtPort: Port parameter = %d, Port type = %s", port,
                port > 0 ? "valid" : "invalid");
        this.courtPort = port;
        logger.logTs("BadmintonControlPanel.setCourtPort: Đã set port %d cho sân", port);
        logger.logTs("BadmintonControlPanel.setCourtPort: Kiểm tra sau khi set: courtPort = %d", this.courtPort);
        logger.logTs("BadmintonControlPanel.setCourtPort: courtPort > 0? %s", (this.courtPort > 0) ? "YES" : "NO");
        // Không gọi updateRemoteLinkUi ngay lập tức để tránh lỗi khi selectedIf chưa
        // được set
    }

    /** Set ID của sân để hiển thị trên monitor */
    public void setCourtId(String courtId) {
        this.courtId = courtId != null ? courtId : "";
        logger.logTs("BadmintonControlPanel.setCourtId: Đã set courtId = %s", this.courtId);
    }

    /** Lấy ID của sân */
    public String getCourtId() {
        return courtId;
    }

    /**
     * Bắt đầu broadcast ở trạng thái chờ (chưa bấm Bắt đầu trận) để hiện card trên
     * Monitor ngay khi mở sân
     */
    public void startIdleBroadcast(String header) {
        try {
            String hdr = (header == null || header.isBlank()) ? "TRẬN ĐẤU" : header;
            String displayKind = (cboDisplayKind.getSelectedIndex() == 0) ? "VERTICAL" : "HORIZONTAL";
            String clientName = customClientName != null ? customClientName : System.getProperty("user.name", "client");
            String hostShown = "";

            // Không thay đổi trạng thái match, chỉ phát thông tin rỗng ban đầu
            scoreboardSvc.startBroadcast(match, selectedIf, clientName, hostShown, displayKind,
                    hdr, doubles.isSelected(), "", "", courtId);
            logger.logTs("startIdleBroadcast: court=%s, header=%s", courtId, hdr);
        } catch (Exception ex) {
            logger.logTs("Lỗi startIdleBroadcast: %s", ex.getMessage());
        }
    }

    /** Dừng mọi broadcast (dùng khi đóng/xóa sân) */
    public void stopBroadcast() {
        try {
            scoreboardSvc.stopBroadcast();
        } catch (Exception ignore) {
        }
    }

    /** Force update remote link UI */
    public void forceUpdateRemoteLinkUi() {
        updateRemoteLinkUi();
    }

    /** Lấy port của sân */
    public int getCourtPort() {
        logger.logTs("BadmintonControlPanel.getCourtPort: Trả về port %d", this.courtPort);
        return courtPort;
    }

    public void saveSplitLocations() {
        try {
            if (mainSplit != null)
                prefs.putInt("split.main", mainSplit.getDividerLocation());
            if (centerRightSplit != null)
                prefs.putInt("split.centerRight", centerRightSplit.getDividerLocation());
            if (leftVert != null)
                prefs.putInt("split.leftVert", leftVert.getDividerLocation());
            if (midVert != null)
                prefs.putInt("split.midVert", midVert.getDividerLocation());
            if (rightVert != null)
                prefs.putInt("split.rightVert", rightVert.getDividerLocation());
        } catch (Exception ignore) {
        }
    }

    /* =================== BUILD SECTIONS =================== */

    private JPanel buildConfigCard() {
        JPanel card = section("Cấu hình trận đấu");
        card.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;

        int r = 0;
        labHeaderSingles = addRow(card, c, r++, "Nội dung đơn", cboHeaderSingles);
        labA1 = addRow(card, c, r++, "Đội A (đơn)", cboNameA);
        labB1 = addRow(card, c, r++, "Đội B (đơn)", cboNameB);
        addRow(card, c, r++, "Thể thức", bestOf);

        labHeaderDoubles = addRow(card, c, r++, "Nội dung đôi", cboHeaderDoubles);
        labTeamA = addRow(card, c, r++, "Đội A (đôi)", cboTeamA);
        labTeamB = addRow(card, c, r++, "Đội B (đôi)", cboTeamB);
        addRow(card, c, r++, "Giao cầu trước", initialServer);

        GridBagConstraints cFull = (GridBagConstraints) c.clone();
        cFull.gridx = 0;
        cFull.gridy = r++;
        cFull.gridwidth = 2;
        cFull.weightx = 1.0;
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        doubles.setBorder(new EmptyBorder(0, 8, 0, 0));
        bottom.add(doubles, BorderLayout.WEST);
        btnReloadLists = ButtonFactory.outlined("Tải danh sách", COL_PRIMARY, BTN_CTRL_SMALL, FONT_BTN);
        btnReloadLists.addActionListener(e -> reloadListsFromDb());
        bottom.add(btnReloadLists, BorderLayout.EAST);
        card.add(bottom, cFull);

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = r;
        filler.gridwidth = 2;
        filler.weightx = 1.0;
        filler.weighty = 1.0;
        filler.fill = GridBagConstraints.BOTH;
        card.add(Box.createGlue(), filler);

        // init + listeners
        setPlaceholdersAndVisibility();
        cboHeaderSingles.addActionListener(e -> {
            if (!doubles.isSelected() && !guard.isSuppressed())
                onHeaderSinglesChosen();
        });
        cboHeaderDoubles.addActionListener(e -> {
            if (doubles.isSelected() && !guard.isSuppressed())
                onHeaderDoublesChosen();
        });
        doubles.addActionListener(e -> {
            if (!guard.isSuppressed())
                toggleSinglesOrDoubles();
        });
        cboNameA.addActionListener(e -> ensureDifferentVdvAndUpdate());
        cboNameB.addActionListener(e -> ensureDifferentVdvAndUpdate());
        cboTeamA.addActionListener(e -> ensureDifferentTeamsAndUpdate());
        cboTeamB.addActionListener(e -> ensureDifferentTeamsAndUpdate());
        return card;
    }

    private JPanel buildControlsCard() {
        JPanel card = section("Điều khiển");
        card.setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new GridLayout(1, 2, 10, 0));
        top.setOpaque(false);
        top.add(labeled(cboScreen, "Màn hình hiển thị"));
        top.add(labeled(cboDisplayKind, "Kiểu bảng điểm"));

        JPanel buttons = new JPanel(new GridLayout(4, 2, 10, 0));
        buttons.setOpaque(false);
        btnStart = ButtonFactory.filled("Bắt đầu trận", COL_SUCCESS, Color.WHITE, BTN_CTRL, FONT_BTN);
        btnFinish = ButtonFactory.filled("Kết thúc trận", COL_DANGER, Color.WHITE, BTN_CTRL, FONT_BTN);
        btnFinish.setEnabled(false);
        btnOpenDisplay = ButtonFactory.outlined("Mở bảng điểm (dọc)", COL_PRIMARY, BTN_CTRL, FONT_BTN);
        btnOpenDisplay.setEnabled(false);
        btnOpenDisplayH = ButtonFactory.outlined("Mở bảng điểm (ngang)", COL_PRIMARY, BTN_CTRL, FONT_BTN);
        btnOpenDisplayH.setEnabled(false);
        btnCloseDisplay = ButtonFactory.outlined("Đóng", COL_NEUTRAL, BTN_CTRL, FONT_BTN);
        btnCloseDisplay.setEnabled(false);
        btnReset = ButtonFactory.outlined("Đặt lại", COL_WARNING, BTN_CTRL, FONT_BTN);
        btnReset.setEnabled(false);

        // Nút chụp ảnh bảng điểm
        JButton btnCapture = ButtonFactory.outlined("📸 Chụp ảnh", COL_NEUTRAL, BTN_CTRL, FONT_BTN);
        btnCapture.setToolTipText("Chụp ảnh bảng điểm mini hiện tại");
        btnCapture.addActionListener(e -> captureMiniScoreboard());

        for (JButton b : new JButton[] { btnStart, btnFinish, btnOpenDisplay, btnOpenDisplayH, btnCloseDisplay,
                btnReset }) {
            b.setPreferredSize(BTN_CTRL);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, BTN_CTRL.height));
        }

        btnStart.addActionListener(e -> onStart());
        btnFinish.addActionListener(e -> onFinish());
        btnReset.addActionListener(e -> onReset());
        btnOpenDisplay.addActionListener(e -> openDisplayVertical());
        btnOpenDisplayH.addActionListener(e -> openDisplayHorizontal());
        btnCloseDisplay.addActionListener(e -> closeDisplays());

        buttons.add(btnStart);
        buttons.add(btnFinish);
        buttons.add(btnReset);
        buttons.add(btnCapture);
        buttons.add(btnOpenDisplay);
        buttons.add(btnOpenDisplayH);
        buttons.add(btnCloseDisplay);

        card.add(top, BorderLayout.NORTH);
        card.add(buttons, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildLiveCard() {
        JPanel card = section("Xem trước (live)");
        card.setLayout(new BorderLayout(8, 8));
        miniContainer = new JPanel(new GridLayout(1, 1, 10, 0));
        miniContainer.setOpaque(false);
        miniContainer.setPreferredSize(new Dimension(320, 180));

        if (mini != null) {
            mini.setBorder(new EmptyBorder(6, 6, 6, 6));
            miniContainer.add(mini);
        }

        card.add(miniContainer, BorderLayout.CENTER);
        return card;
    }

    /** Ở giữa: Điểm số + (QR + Link để sau) */
    private JPanel buildScoreAndQrCard() {
        JPanel card = section("Điểm số / QR / Link");
        card.setLayout(new BorderLayout(8, 8));

        JPanel scoreButtons = new JPanel(new GridLayout(2, 4, 10, 10));
        scoreButtons.setOpaque(false);

        aPlus = ButtonFactory.filled("+1 A", COL_SUCCESS, Color.WHITE, BTN_SCORE, FONT_BTN);
        bPlus = ButtonFactory.filled("+1 B", COL_SUCCESS, Color.WHITE, BTN_SCORE, FONT_BTN);
        aMinus = ButtonFactory.filled("-1 A", COL_DANGER, Color.WHITE, BTN_SCORE, FONT_BTN);
        bMinus = ButtonFactory.filled("-1 B", COL_DANGER, Color.WHITE, BTN_SCORE, FONT_BTN);
        undo = ButtonFactory.outlined("Hoàn tác", COL_NEUTRAL, BTN_UTILITY, FONT_BTN);
        nextGame = ButtonFactory.outlined("Ván tiếp theo", COL_PRIMARY, BTN_UTILITY, FONT_BTN);
        swapEnds = ButtonFactory.outlined("Đổi sân", COL_WARNING, BTN_UTILITY, FONT_BTN);
        toggleServe = ButtonFactory.outlined("Đổi giao cầu", COL_WARNING, BTN_UTILITY, FONT_BTN);

        for (JButton b : new JButton[] { aPlus, bPlus, aMinus, bMinus, undo, nextGame, swapEnds, toggleServe }) {
            b.setPreferredSize(BTN_SCORE);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, BTN_SCORE.height));
            scoreButtons.add(b);
        }

        // Trước khi bắt đầu trận: khóa toàn bộ
        setScoreButtonsEnabled(false);
        nextGame.setEnabled(false);

        // === LOGGING SUPPORT ===
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Runnable logScore = () -> {
            var s = match.snapshot();
            logger.log("[%s] Tỉ số: %d - %d  |  Ván %d / BO%d",
                    sdf.format(new Date()), s.score[0], s.score[1], s.gameNumber, s.bestOf);
        };

        // === ACTION LISTENERS ===
        aPlus.addActionListener(e -> {
            synchronized (ScoreboardRemote.get().lock()) {
                match.pointTo(0);
            }
            logger.log("[%s] +1 A", sdf.format(new Date()));
            logScore.run();
        });
        bPlus.addActionListener(e -> {
            synchronized (ScoreboardRemote.get().lock()) {
                match.pointTo(1);
            }
            logger.log("[%s] +1 B", sdf.format(new Date()));
            logScore.run();
        });
        aMinus.addActionListener(e -> {
            synchronized (ScoreboardRemote.get().lock()) {
                match.pointDown(0, -1);
            }
            logger.log("[%s] -1 A", sdf.format(new Date()));
            logScore.run();
        });
        bMinus.addActionListener(e -> {
            synchronized (ScoreboardRemote.get().lock()) {
                match.pointDown(1, -1);
            }
            logger.log("[%s] -1 B", sdf.format(new Date()));
            logScore.run();
        });
        undo.addActionListener(e -> {
            synchronized (ScoreboardRemote.get().lock()) {
                match.undo();
            }
            logger.log("[%s] Hoàn tác", sdf.format(new Date()));
            logScore.run();
        });
        nextGame.addActionListener(e -> {
            synchronized (ScoreboardRemote.get().lock()) {
                match.nextGame();
            }
            var s = match.snapshot();
            logger.log("[%s] Sang ván %d (BO%d) — Ván thắng: %d - %d",
                    sdf.format(new Date()), s.gameNumber, s.bestOf, s.games[0], s.games[1]);
        });
        swapEnds.addActionListener(e -> {
            synchronized (ScoreboardRemote.get().lock()) {
                match.swapEnds();
            }
            logger.log("[%s] Đổi sân", sdf.format(new Date()));
            logScore.run();
        });
        toggleServe.addActionListener(e -> {
            synchronized (ScoreboardRemote.get().lock()) {
                match.toggleServer();
            }
            var s = match.snapshot();
            logger.log("[%s] Đổi giao cầu → %s", sdf.format(new Date()), s.server == 0 ? "A" : "B");
        });

        card.add(scoreButtons, BorderLayout.NORTH);

        // Remote link + QR inline (no popup)
        JPanel remoteBox = new JPanel(new BorderLayout(8, 8));
        remoteBox.setOpaque(false);
        JLabel cap = new JLabel("Điều khiển trên điện thoại (cùng Wi‑Fi)");
        cap.setFont(FONT_LABEL);
        remoteBox.add(cap, BorderLayout.NORTH);

        // Hiển thị mã PIN và hướng dẫn
        JPanel pinPanel = new JPanel(new BorderLayout(8, 0));
        pinPanel.setOpaque(false);
        JLabel pinLabel = new JLabel("Mã PIN: " + getCourtPinCode());
        pinLabel.setFont(FONT_VALUE);
        pinLabel.setForeground(COL_PRIMARY);
        pinPanel.add(pinLabel, BorderLayout.WEST);
        remoteBox.add(pinPanel, BorderLayout.CENTER);

        // Hướng dẫn nhập PIN
        JPanel instructionPanel = new JPanel(new BorderLayout(8, 0));
        instructionPanel.setOpaque(false);
        JLabel instructionLabel = new JLabel("📱 Hướng dẫn: Mở trình duyệt → Nhập mã PIN → Điều khiển điểm số");
        instructionLabel.setFont(FONT_LABEL.deriveFont(Font.PLAIN, 11f));
        instructionLabel.setForeground(COL_NEUTRAL);
        instructionPanel.add(instructionLabel, BorderLayout.CENTER);
        remoteBox.add(instructionPanel, BorderLayout.CENTER);

        JPanel linkAndQr = new JPanel(new BorderLayout(12, 8));
        linkAndQr.setOpaque(false);

        // Panel chứa link và nhóm nút (ẩn/hiện + copy)
        JPanel linkPanel = new JPanel(new BorderLayout(8, 0));
        linkPanel.setOpaque(false);
        lblRemoteUrl.setFont(FONT_VALUE);
        linkPanel.add(lblRemoteUrl, BorderLayout.CENTER);

        // Nhóm nút 2 cột, căn sát phải: [Ẩn/Hiện QR] [Ẩn/Hiện Link]
        // [ trống ] [Copy]
        JPanel rightBtnBox = new JPanel(new GridLayout(0, 2, 6, 4));
        rightBtnBox.setOpaque(false);
        btnToggleLinkVisible = ButtonFactory.outlined(remoteUrlVisible ? "Ẩn link" : "Hiện link", COL_NEUTRAL,
                new Dimension(110, 30), FONT_BTN);
        btnToggleLinkVisible.setToolTipText("Ẩn/hiện đường link bấm điểm");
        btnToggleLinkVisible.addActionListener(e -> {
            remoteUrlVisible = !remoteUrlVisible;
            updateRemoteUrlDisplay();
            btnToggleLinkVisible.setText(remoteUrlVisible ? "Ẩn link" : "Hiện link");
        });

        JButton btnCopyLink = ButtonFactory.outlined("Copy", COL_PRIMARY, new Dimension(100, 30), FONT_BTN);
        btnCopyLink.setToolTipText("Copy link vào clipboard");
        btnCopyLink.addActionListener(e -> copyLinkToClipboard());

        btnToggleQrVisible = ButtonFactory.outlined(qrCodeVisible ? "Ẩn QR" : "Hiện QR", COL_NEUTRAL,
                new Dimension(100, 30), FONT_BTN);
        btnToggleQrVisible.setToolTipText("Ẩn/hiện mã QR code");
        btnToggleQrVisible.addActionListener(e -> {
            qrCodeVisible = !qrCodeVisible;
            updateQrCodeDisplay();
            btnToggleQrVisible.setText(qrCodeVisible ? "Ẩn QR" : "Hiện QR");
        });

        rightBtnBox.add(btnToggleQrVisible);
        rightBtnBox.add(btnToggleLinkVisible);
        rightBtnBox.add(Box.createHorizontalStrut(0)); // filler để Copy nằm cột phải hàng dưới
        rightBtnBox.add(btnCopyLink);
        linkPanel.add(rightBtnBox, BorderLayout.EAST);

        // Thêm link /pin để nhập mã PIN
        JPanel pinLinkPanel = new JPanel(new BorderLayout(8, 0));
        pinLinkPanel.setOpaque(false);
        JLabel pinLinkLabel = new JLabel("🔗 Link nhập PIN: " + getPinEntryUrl());
        pinLinkLabel.setFont(FONT_LABEL.deriveFont(Font.PLAIN, 11f));
        pinLinkLabel.setForeground(COL_PRIMARY);
        pinLinkPanel.add(pinLinkLabel, BorderLayout.CENTER);

        // Nút copy link PIN
        JButton btnCopyPinLink = ButtonFactory.outlined("📋 Copy PIN", COL_PRIMARY, new Dimension(110, 30), FONT_BTN);
        btnCopyPinLink.setToolTipText("Copy link nhập PIN vào clipboard");
        btnCopyPinLink.addActionListener(e -> copyPinLinkToClipboard());
        pinLinkPanel.add(btnCopyPinLink, BorderLayout.EAST);

        linkAndQr.add(linkPanel, BorderLayout.NORTH);
        lblRemoteQr.setHorizontalAlignment(SwingConstants.LEFT);
        linkAndQr.add(lblRemoteQr, BorderLayout.CENTER);
        remoteBox.add(linkAndQr, BorderLayout.CENTER);

        card.add(remoteBox, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStatusCard() {
        JPanel statusCard = section("Trạng thái trận");
        statusCard.setLayout(new GridLayout(0, 1, 10, 6));
        styleInfo(lblGame);
        styleInfo(lblGamesWon);
        styleInfo(lblServer);
        styleInfo(lblStatus);
        styleInfo(lblWinner);
        statusCard.add(kv("Ván", lblGame));
        statusCard.add(kv("Ván thắng", lblGamesWon));
        statusCard.add(kv("Giao cầu", lblServer));
        statusCard.add(kv("Trạng thái", lblStatus));
        statusCard.add(kv("Người thắng", lblWinner));
        return statusCard;
    }

    private JPanel buildLogCard() {
        JPanel logBox = section("Log lựa chọn");
        logBox.setLayout(new BorderLayout());
        JTextArea area = logger.getLogArea(); // đã gom chung Log
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Giới hạn độ rộng tối đa và bật word wrap để tránh cuộn ngang
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMaximumSize(new Dimension(350, Integer.MAX_VALUE));

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        logBox.add(scrollPane, BorderLayout.CENTER);
        return logBox;
    }

    /* =================== LAYOUT HELPERS =================== */

    private JSplitPane hSplit(Component a, Component b, double ratio) {
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, a, b);
        sp.setContinuousLayout(true);
        sp.setOneTouchExpandable(true);
        sp.setDividerSize(8);
        sp.setResizeWeight(ratio);
        sp.setDividerLocation(ratio);
        return sp;
    }

    private JSplitPane vSplit(Component top, Component bottom, double ratio) {
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        sp.setContinuousLayout(true);
        sp.setOneTouchExpandable(true);
        sp.setDividerSize(8);
        sp.setResizeWeight(ratio);
        sp.setDividerLocation(ratio);
        return sp;
    }

    private JPanel wrapWithSize(Component c, Dimension min, Dimension pref) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(c, BorderLayout.CENTER);
        p.setMinimumSize(min);
        p.setPreferredSize(pref);
        return p;
    }

    private void lockRightMin(JSplitPane root, Component rightMost, int minWidth) {
        root.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int total = root.getWidth();
                int curRight = rightMost.getWidth();
                if (curRight < minWidth) {
                    root.setDividerLocation(total - minWidth - root.getDividerSize());
                }
            }
        });
    }

    /* =================== UI helpers =================== */

    private JPanel section(String title) {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(true);
        card.setBackground(new Color(250, 250, 250));
        card.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                new EmptyBorder(6, 10, 6, 10)));
        JLabel lab = new JLabel(title);
        lab.setFont(FONT_SECTION);
        head.add(lab, BorderLayout.WEST);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 10, 10, 10));

        card.add(head, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);
        return body;
    }

    private JLabel addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent comp) {
        JLabel lab = new JLabel(label);
        lab.setFont(FONT_LABEL);
        GridBagConstraints lc = (GridBagConstraints) c.clone();
        lc.gridx = 0;
        lc.gridy = row;
        lc.weightx = 0.0;
        lc.insets = new Insets(6, 8, 0, 8);
        p.add(lab, lc);

        GridBagConstraints vc = (GridBagConstraints) c.clone();
        vc.gridx = 1;
        vc.gridy = row;
        vc.weightx = 1.0;
        vc.insets = new Insets(2, 8, 6, 8);
        comp.setFont(FONT_VALUE);
        p.add(comp, vc);
        return lab;
    }

    private JPanel labeled(JComponent comp, String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lab = new JLabel(title);
        lab.setFont(FONT_LABEL);
        p.add(lab, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JPanel kv(String k, JLabel v) {
        JLabel key = new JLabel(k + ": ");
        key.setFont(FONT_LABEL);
        JPanel line = new JPanel(new BorderLayout());
        line.setOpaque(false);
        line.add(key, BorderLayout.WEST);
        line.add(v, BorderLayout.CENTER);
        return line;
    }

    private void styleInfo(JLabel l) {
        l.setFont(FONT_VALUE.deriveFont(Font.BOLD, 14f));
        l.setBorder(new EmptyBorder(4, 6, 4, 6));
    }

    private void setPlaceholdersAndVisibility() {
        guard.runSilently(() -> {
            setPlaceholder(cboHeaderSingles, PH_HEADER_S, true);
            setPlaceholder(cboHeaderDoubles, PH_HEADER_D, true);
            setPlaceholder(cboNameA, PH_PLAYER, false);
            setPlaceholder(cboNameB, PH_PLAYER, false);
            setTeamPlaceholder(cboTeamA);
            setTeamPlaceholder(cboTeamB);
            cboHeaderDoubles.setEnabled(false);
        });
        setSinglesVisible(true);
        setTeamsVisible(false);
        setHeadersVisibility(true, false);
    }

    private void setPlaceholder(JComboBox<String> combo, String ph, boolean enabled) {
        combo.removeAllItems();
        combo.addItem(ph);
        combo.setSelectedIndex(0);
        combo.setEnabled(enabled);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null && value.toString().equals(ph))
                    setForeground(Color.GRAY);
                return c;
            }
        });
    }

    private void setTeamPlaceholder(JComboBox<DangKiDoi> combo) {
        combo.removeAllItems();
        // tạo đối tượng giả làm placeholder
        DangKiDoi ph = new DangKiDoi();
        ph.setIdTeam(-1);
        ph.setTenTeam(PH_TEAM);
        combo.addItem(ph);
        combo.setSelectedIndex(0);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DangKiDoi dk && PH_TEAM.equals(dk.getTenTeam()))
                    setForeground(Color.GRAY);
                return c;
            }
        });
    }

    private void setSinglesVisible(boolean vis) {
        if (labA1 != null)
            labA1.setVisible(vis);
        if (labB1 != null)
            labB1.setVisible(vis);
        cboNameA.setVisible(vis);
        cboNameB.setVisible(vis);
    }

    private void setTeamsVisible(boolean vis) {
        if (labTeamA != null)
            labTeamA.setVisible(vis);
        if (labTeamB != null)
            labTeamB.setVisible(vis);
        cboTeamA.setVisible(vis);
        cboTeamB.setVisible(vis);
    }

    private void setHeadersVisibility(boolean singlesVisible, boolean doublesVisible) {
        if (labHeaderSingles != null)
            labHeaderSingles.setVisible(singlesVisible);
        cboHeaderSingles.setVisible(singlesVisible);
        if (labHeaderDoubles != null)
            labHeaderDoubles.setVisible(doublesVisible);
        cboHeaderDoubles.setVisible(doublesVisible);
        revalidate();
        repaint();
    }

    private static boolean isPh(String s) {
        return PH_PLAYER.equals(s) || PH_HEADER_S.equals(s) || PH_HEADER_D.equals(s);
    }

    private String sel(JComboBox<String> cb) {
        Object v = cb.getSelectedItem();
        String s = v == null ? "" : v.toString().trim();
        return isPh(s) ? "" : s;
    }

    private String currentHeader() {
        return doubles.isSelected() ? sel(cboHeaderDoubles) : sel(cboHeaderSingles);
    }

    /* =================== DATA LOADS =================== */

    private void reloadListsFromDb() {
        guard.runSilently(() -> {
            setPlaceholder(cboHeaderSingles, PH_HEADER_S, true);
            setPlaceholder(cboHeaderDoubles, PH_HEADER_D, true);
        });
        headerKnrSingles.clear();
        headerKnrDoubles.clear();

        if (conn != null) {
            var repo = new CategoryRepository(conn);
            Map<String, Integer>[] maps = repo.loadCategories();
            maps[0].forEach((k, v) -> {
                cboHeaderSingles.addItem(k);
                headerKnrSingles.put(k, v);
            });
            maps[1].forEach((k, v) -> {
                cboHeaderDoubles.addItem(k);
                headerKnrDoubles.put(k, v);
            });
        }

        guard.runSilently(() -> {
            setPlaceholder(cboNameA, PH_PLAYER, false);
            setPlaceholder(cboNameB, PH_PLAYER, false);
            setTeamPlaceholder(cboTeamA);
            cboTeamA.setEnabled(false);
            setTeamPlaceholder(cboTeamB);
            cboTeamB.setEnabled(false);
        });
    }

    private void onHeaderSinglesChosen() {
        String header = sel(cboHeaderSingles);
        if (header.isBlank() || conn == null) {
            guard.runSilently(() -> {
                setPlaceholder(cboNameA, PH_PLAYER, false);
                setPlaceholder(cboNameB, PH_PLAYER, false);
            });
            mini.setHeader("TRẬN ĐẤU");
            setSinglesVisible(true);
            setTeamsVisible(false);
            return;
        }
        Integer knr = headerKnrSingles.get(header);
        // ID giải lưu trong Prefs dưới key 'selectedGiaiDauId' (đồng bộ với
        // CategoryRepository)
        Integer vernr = new Prefs().getInt("selectedGiaiDauId", -1);
        if (knr == null || vernr == null)
            return;

        var repo = new VanDongVienRepository(conn);
        singlesNameToId.clear();
        singlesNameToId.putAll(repo.loadSinglesNames(knr, vernr));

        guard.runSilently(() -> {
            cboNameA.removeAllItems();
            cboNameB.removeAllItems();
            for (String nm : singlesNameToId.keySet()) {
                cboNameA.addItem(nm);
                cboNameB.addItem(nm);
            }
            if (cboNameA.getItemCount() >= 2) {
                guard.runSilently(() -> {
                    cboNameA.setSelectedIndex(0);
                    cboNameB.setSelectedIndex(1);
                });
            } else if (cboNameA.getItemCount() == 1) {
                guard.runSilently(() -> {
                    cboNameA.setSelectedIndex(0);
                    cboNameB.setSelectedIndex(0);
                });
            }
            cboNameA.setEnabled(true);
            cboNameB.setEnabled(true);
            cboTeamA.setEnabled(false);
            cboTeamB.setEnabled(false);
        });

        mini.setHeader(header);
        logger.chooseSinglesHeader(header, knr);
        updateFromVdv();
    }

    private void onHeaderDoublesChosen() {
        String header = sel(cboHeaderDoubles);
        if (header.isBlank() || conn == null) {
            guard.runSilently(() -> {
                setTeamPlaceholder(cboTeamA);
                setTeamPlaceholder(cboTeamB);
            });
            mini.setHeader("TRẬN ĐẤU");
            setSinglesVisible(false);
            setTeamsVisible(true);
            return;
        }
        Integer knr = headerKnrDoubles.get(header);
        Integer vernr = new Prefs().getInt("selectedGiaiDauId", -1);
        if (knr == null || vernr == null)
            return;

        // Dùng DoiService mới thay vì TeamAndPlayerRepository cũ
        DoiService doiService = new DoiService(conn);
        List<DangKiDoi> teams = doiService.getTeamsByNoiDungVaGiai(knr, vernr);

        guard.runSilently(() -> {
            setTeamPlaceholder(cboTeamA);
            setTeamPlaceholder(cboTeamB);
            for (DangKiDoi t : teams) {
                cboTeamA.addItem(t);
                cboTeamB.addItem(t);
            }
            cboTeamA.setEnabled(true);
            cboTeamB.setEnabled(true);
            if (cboTeamA.getItemCount() > 1)
                guard.runSilently(() -> cboTeamA.setSelectedIndex(1));
            if (cboTeamB.getItemCount() > 2)
                guard.runSilently(() -> cboTeamB.setSelectedIndex(2));
        });

        mini.setHeader(header);
        logger.chooseDoublesHeader(header, knr);
        updateFromTeams();
    }

    private void updateFromTeams() {
        if (!doubles.isSelected())
            return;
        DangKiDoi ta = (DangKiDoi) cboTeamA.getSelectedItem();
        DangKiDoi tb = (DangKiDoi) cboTeamB.getSelectedItem();
        if (ta == null || tb == null || ta.getIdTeam() == null || tb.getIdTeam() == null || ta.getIdTeam() < 0
                || tb.getIdTeam() < 0)
            return;
        match.setNames(ta.getTenTeam(), tb.getTenTeam());
        logger.chooseTeamA(ta.getTenTeam(), ta.getIdTeam());
        logger.chooseTeamB(tb.getTenTeam(), tb.getIdTeam());
    }

    /** cập nhật tên khi đấu đơn, dùng String + map id */
    private void updateFromVdv() {
        if (doubles.isSelected())
            return; // chỉ áp dụng cho ĐƠN
        String nameA = sel(cboNameA);
        String nameB = sel(cboNameB);
        if (nameA.isBlank() || nameB.isBlank())
            return;
        match.setNames(nameA, nameB);
        Integer idA = singlesNameToId.getOrDefault(nameA, -1);
        Integer idB = singlesNameToId.getOrDefault(nameB, -1);
        logger.choosePlayerA(nameA, idA);
        logger.choosePlayerB(nameB, idB);
    }

    private void ensureDifferentTeamsAndUpdate() {
        if (!doubles.isSelected() || guard.isSuppressed())
            return;
        DangKiDoi ta = (DangKiDoi) cboTeamA.getSelectedItem();
        DangKiDoi tb = (DangKiDoi) cboTeamB.getSelectedItem();
        if (ta != null && tb != null && ta.getIdTeam() != null && tb.getIdTeam() != null
                && ta.getIdTeam() >= 0 && tb.getIdTeam() >= 0 && ta.getIdTeam().equals(tb.getIdTeam())) {
            guard.runSilently(() -> {
                if (cboTeamB.getItemCount() > 2) {
                    int alt = (cboTeamA.getSelectedIndex() == 1) ? 2 : 1;
                    if (alt < cboTeamB.getItemCount())
                        cboTeamB.setSelectedIndex(alt);
                }
            });
            JOptionPane.showMessageDialog(this, "Đội A và Đội B không được trùng nhau.", "Chọn đội",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        updateFromTeams();
    }

    // vdv a và vdv b ko trùng nhau (đơn)
    private void ensureDifferentVdvAndUpdate() {
        if (doubles.isSelected() || guard.isSuppressed())
            return;
        String nameA = (String) cboNameA.getSelectedItem();
        String nameB = (String) cboNameB.getSelectedItem();
        if (nameA != null && nameB != null && !isPh(nameA) && !isPh(nameB) && nameA.equals(nameB)) {
            guard.runSilently(() -> {
                if (cboNameB.getItemCount() > 1) {
                    int alt = (cboNameA.getSelectedIndex() == 0) ? 1 : 0;
                    if (alt < cboNameB.getItemCount())
                        cboNameB.setSelectedIndex(alt);
                }
            });
            JOptionPane.showMessageDialog(this, "VĐV A và VĐV B không được trùng nhau.", "Chọn VĐV",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        updateFromVdv();
    }

    private void toggleSinglesOrDoubles() {
        boolean isD = doubles.isSelected();

        setHeadersVisibility(!isD, isD);
        setSinglesVisible(!isD);
        setTeamsVisible(isD);

        cboHeaderSingles.setEnabled(!isD);
        cboHeaderDoubles.setEnabled(isD);

        cboNameA.setEnabled(!isD);
        cboNameB.setEnabled(!isD);
        cboTeamA.setEnabled(isD);
        cboTeamB.setEnabled(isD);

        if (isD)
            onHeaderDoublesChosen();
        else
            onHeaderSinglesChosen();
    }

    /* =================== MATCH LIFECYCLE =================== */

    private void onStart() {
        cancelFinishTimer();
        String header = currentHeader();
        if (header.isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Nội dung.", "Thiếu nội dung",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int bo = switch (bestOf.getSelectedIndex()) {
            case 0 -> 1;
            case 1 -> 3;
            default -> 5;
        };
        match.setBestOf(bo);

        String displayKind = (cboDisplayKind.getSelectedIndex() == 0) ? "VERTICAL" : "HORIZONTAL";
        String clientName = customClientName != null ? customClientName : System.getProperty("user.name", "client");
        String hostShown = "";

        if (doubles.isSelected()) {
            DangKiDoi ta = (DangKiDoi) cboTeamA.getSelectedItem();
            DangKiDoi tb = (DangKiDoi) cboTeamB.getSelectedItem();
            if (ta == null || tb == null || ta.getIdTeam() == null || tb.getIdTeam() == null || ta.getIdTeam() < 0
                    || tb.getIdTeam() < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn Đội A/B.", "Thiếu đội", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DoiService doiService = new DoiService(conn);
            VanDongVien[] pa = doiService.getTeamPlayers(ta.getIdTeam());
            VanDongVien[] pb = doiService.getTeamPlayers(tb.getIdTeam());
            if (pa == null || pa.length == 0 || pb == null || pb.length == 0) {
                JOptionPane.showMessageDialog(this, "Đội chưa có đủ VĐV.", "Thiếu dữ liệu đội",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String fullNameA = buildFullTeamName(pa);
            String fullNameB = buildFullTeamName(pb);
            match.setDoubles(true);
            match.setNames(fullNameA, fullNameB);
            mini.setHeader(header);
            match.startMatch(initialServer.getSelectedIndex());
            com.example.btms.util.sound.SoundPlayer.playStartIfEnabled();
            hasStarted = true;
            afterStartUi();
            scoreboardSvc.startBroadcast(match, selectedIf, clientName, hostShown, displayKind,
                    header, true, fullNameA, fullNameB, courtId);
            logger.startDoubles(header, ta.getTenTeam(), ta.getIdTeam(), tb.getTenTeam(), tb.getIdTeam(), bo);
            updateRemoteLinkUi();
        } else {
            String nameA = sel(cboNameA);
            String nameB = sel(cboNameB);
            if (nameA.isBlank() || nameB.isBlank()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn VĐV cho Đội A và Đội B.", "Thiếu VĐV",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            match.setDoubles(false);
            match.setNames(nameA, nameB);
            mini.setHeader(header);
            match.startMatch(initialServer.getSelectedIndex());
            com.example.btms.util.sound.SoundPlayer.playStartIfEnabled();

            hasStarted = true;
            afterStartUi();
            // openDisplayAuto();
            scoreboardSvc.startBroadcast(match, selectedIf, clientName, hostShown, displayKind,
                    header, false, nameA, nameB, courtId);
            // Gắn match lên HTTP server theo port của sân
            Integer idA = singlesNameToId.getOrDefault(nameA, -1);
            Integer idB = singlesNameToId.getOrDefault(nameB, -1);
            logger.startSingles(header, nameA, idA, nameB, idB, bo);
            updateRemoteLinkUi();
        }
    }

    private void afterStartUi() {
        btnStart.setEnabled(false);
        btnFinish.setEnabled(true);
        btnOpenDisplay.setEnabled(true);
        btnOpenDisplayH.setEnabled(true);
        btnCloseDisplay.setEnabled(true);
        btnReset.setEnabled(true);
        setScoreButtonsEnabled(true);
        nextGame.setEnabled(false);

        // Disable các controls liên quan đến việc chọn nội dung và VĐV
        disableConfigControls();
    }

    /**
     * Disable các controls liên quan đến việc chọn nội dung và VĐV khi trận đấu đã
     * bắt đầu
     */
    private void disableConfigControls() {
        // Disable combobox nội dung
        cboHeaderSingles.setEnabled(false);
        cboHeaderDoubles.setEnabled(false);

        // Disable combobox VĐV/đội
        cboNameA.setEnabled(false);
        cboNameB.setEnabled(false);
        cboTeamA.setEnabled(false);
        cboTeamB.setEnabled(false);

        // Disable các options khác
        bestOf.setEnabled(false);
        doubles.setEnabled(false);
        initialServer.setEnabled(false);

        // Disable nút reload
        btnReloadLists.setEnabled(false);
    }

    /**
     * Enable lại các controls liên quan đến việc chọn nội dung và VĐV khi kết thúc
     * trận đấu
     */
    private void enableConfigControls() {
        // Enable combobox nội dung
        cboHeaderSingles.setEnabled(true);
        cboHeaderDoubles.setEnabled(true);

        // Enable combobox VĐV/đội
        cboNameA.setEnabled(true);
        cboNameB.setEnabled(true);
        cboTeamA.setEnabled(true);
        cboTeamB.setEnabled(true);

        // Enable các options khác
        bestOf.setEnabled(true);
        doubles.setEnabled(true);
        initialServer.setEnabled(true);

        // Enable nút reload
        btnReloadLists.setEnabled(true);
    }

    private void onFinish() {
        int ans = JOptionPane.showConfirmDialog(this,
                "Kết thúc trận hiện tại và sẵn sàng cho trận mới?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ans != JOptionPane.YES_OPTION) {
            btnFinish.setEnabled(true);
            return;
        }

        closeDisplays();
        // Không dừng broadcast để card Monitor vẫn còn. Chỉ dừng khi xoá sân.

        // Cập nhật trạng thái sân: đặt trận về trạng thái sẵn sàng (không thi đấu)
        try {
            synchronized (ScoreboardRemote.get().lock()) {
                match.resetAll();
            }
        } catch (Exception ignore) {
        }
        // Báo cho listeners (overview/tổng quan) cập nhật ngay
        try {
            firePropertyChange("matchFinishedManual", false, true);
        } catch (Exception ignore) {
        }

        hasStarted = false;
        btnStart.setEnabled(true);
        btnFinish.setEnabled(false);
        btnOpenDisplay.setEnabled(false);
        btnOpenDisplayH.setEnabled(false);
        btnCloseDisplay.setEnabled(false);
        btnReset.setEnabled(false);

        setScoreButtonsEnabled(false);
        nextGame.setEnabled(false);

        // Enable lại các controls liên quan đến việc chọn nội dung và VĐV
        enableConfigControls();

        lblStatus.setText("Sẵn sàng");
        lblGame.setText("Ván 1");
        lblGamesWon.setText("Ván: 0 - 0");
        lblServer.setText("Giao cầu: A (R)");

        logger.finishMatch();
    }

    private void onReset() {
        if (!hasStarted) {
            onStart();
            return;
        }
        cancelFinishTimer();
        String header = currentHeader();
        if (header.isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Nội dung.", "Thiếu nội dung",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bo = switch (bestOf.getSelectedIndex()) {
            case 0 -> 1;
            case 1 -> 3;
            default -> 5;
        };
        match.setBestOf(bo);

        String displayKind = (cboDisplayKind.getSelectedIndex() == 0) ? "VERTICAL" : "HORIZONTAL";
        String clientName = customClientName != null ? customClientName : System.getProperty("user.name", "client");
        String hostShown = "";

        if (doubles.isSelected()) {
            DangKiDoi ta = (DangKiDoi) cboTeamA.getSelectedItem();
            DangKiDoi tb = (DangKiDoi) cboTeamB.getSelectedItem();
            if (ta == null || tb == null || ta.getIdTeam() == null || tb.getIdTeam() == null || ta.getIdTeam() < 0
                    || tb.getIdTeam() < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn Đội A/B.", "Thiếu đội",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            DoiService doiService = new DoiService(conn);
            VanDongVien[] pa = doiService.getTeamPlayers(ta.getIdTeam());
            VanDongVien[] pb = doiService.getTeamPlayers(tb.getIdTeam());
            if (pa == null || pa.length == 0 || pb == null || pb.length == 0) {
                JOptionPane.showMessageDialog(this, "Đội chưa có đủ VĐV.", "Thiếu dữ liệu đội",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String fullNameA = buildFullTeamName(pa);
            String fullNameB = buildFullTeamName(pb);
            match.setDoubles(true);
            match.setNames(fullNameA, fullNameB);
            mini.setHeader(header);
            match.startMatch(initialServer.getSelectedIndex());
            hasStarted = true;
            afterStartUi();
            openDisplayAuto();
            scoreboardSvc.startBroadcast(
                    match, selectedIf, clientName, hostShown, displayKind,
                    header, true, fullNameA, fullNameB, courtId);
            logger.logTs("ĐẶT LẠI ĐÔI: TEAM A=%s (TEAMID=%d) vs TEAM B=%s (TEAMID=%d)",
                    ta.getTenTeam(), ta.getIdTeam(), tb.getTenTeam(), tb.getIdTeam());
            updateRemoteLinkUi();
        } else {
            String nameA = sel(cboNameA);
            String nameB = sel(cboNameB);
            if (nameA.isBlank() || nameB.isBlank()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn VĐV cho Đội A và Đội B.", "Thiếu VĐV",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            match.setDoubles(false);
            match.setNames(nameA, nameB);
            mini.setHeader(header);
            match.startMatch(initialServer.getSelectedIndex());

            hasStarted = true;
            afterStartUi();
            openDisplayAuto();
            scoreboardSvc.startBroadcast(
                    match, selectedIf, clientName, hostShown, displayKind,
                    header, false, nameA, nameB, courtId);

            Integer idA = singlesNameToId.getOrDefault(nameA, -1);
            Integer idB = singlesNameToId.getOrDefault(nameB, -1);
            logger.logTs("ĐẶT LẠI ĐƠN: A=%s (NNR=%d) vs B=%s (NNR=%d)", nameA, idA, nameB, idB);
            updateRemoteLinkUi();
        }
    }

    private void openDisplayVertical() {
        if (!hasStarted) {
            JOptionPane.showMessageDialog(this, "Hãy bấm \"Bắt đầu trận\" trước khi mở bảng điểm.", "Chưa bắt đầu",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        logger.openDisplayVertical();
        scoreboardSvc.openVertical(match, Math.max(0, cboScreen.getSelectedIndex()));
        btnCloseDisplay.setEnabled(true);
    }

    private void openDisplayHorizontal() {
        if (!hasStarted) {
            JOptionPane.showMessageDialog(this, "Hãy bấm \"Bắt đầu trận\" trước khi mở bảng điểm.", "Chưa bắt đầu",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        logger.openDisplayHorizontal();
        scoreboardSvc.openHorizontal(match, Math.max(0, cboScreen.getSelectedIndex()), currentHeader());
        btnCloseDisplay.setEnabled(true);
    }

    private void closeDisplays() {
        logger.closeDisplays();
        scoreboardSvc.closeDisplays();
    }

    private void openDisplayAuto() {
        if (cboDisplayKind.getSelectedIndex() == 0)
            openDisplayVertical();
        else
            openDisplayHorizontal();
    }

    private void updateRemoteLinkUi() {
        try {
            String ip = NetworkUtil.getLocalIpv4(selectedIf);

            // Kiểm tra IP có hợp lệ không
            if (ip == null || ip.isEmpty()) {
                lblRemoteUrl.setText("<html><b style='color:red;'>LỖI: Interface '" +
                        (selectedIf != null ? selectedIf.getDisplayName() : "null") +
                        "' không có IPv4 address. Vui lòng chọn interface khác.</b></html>");
                lblRemoteQr.setIcon(null);
                logger.logTs("LỖI: Không thể lấy IP từ interface '%s'. Cần chọn interface khác.",
                        selectedIf != null ? selectedIf.getDisplayName() : "null");
                return;
            }

            // Sử dụng port của sân nếu đã set, nếu không thì dùng port mặc định
            int port = (courtPort > 0) ? courtPort : 2345;

            // Tạo URL với mã PIN
            String pinCode = getCourtPinCode();
            String url = "http://" + ip + ":" + port + "/scoreboard/" + pinCode;

            logger.logTs("Điều khiển trên điện thoại: %s (port %d, IP: %s)", url, port, ip);
            // Lưu URL và cập nhật hiển thị theo trạng thái ẩn/hiện
            currentRemoteUrl = url;
            updateRemoteUrlDisplay();

            // Chỉ tạo và hiển thị QR code khi qrCodeVisible = true
            if (qrCodeVisible) {
                var img = QRCodeUtil.generate(url, 100);
                lblRemoteQr.setIcon(new ImageIcon(img));
            } else {
                lblRemoteQr.setIcon(null);
                lblRemoteQr.setText("");
            }

            // Cập nhật link PIN entry nếu có
            SwingUtilities.invokeLater(() -> {
                try {
                    // Tìm và cập nhật label link PIN
                    for (java.awt.Component comp : getComponents()) {
                        if (comp instanceof JPanel) {
                            updatePinLinkInPanel((JPanel) comp);
                        }
                    }
                } catch (Exception ex) {
                    logger.logTs("Lỗi khi cập nhật link PIN: %s", ex.getMessage());
                }
            });
        } catch (Exception ex) {
            lblRemoteUrl.setText("<html><b style='color:red;'>LỖI: " + ex.getMessage() + "</b></html>");
            lblRemoteQr.setIcon(null);
            lblRemoteQr.setText("");
            logger.logTs("Lỗi khi cập nhật remote link UI: %s", ex.getMessage());
        }
    }

    private void copyLinkToClipboard() {
        try {
            // Ưu tiên dùng URL hiện tại nếu đã có
            String url = currentRemoteUrl;
            if (url == null || url.isBlank()) {
                String ip = NetworkUtil.getLocalIpv4(selectedIf);
                if (ip == null || ip.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Không thể copy link - Interface '" +
                                    (selectedIf != null ? selectedIf.getDisplayName() : "null") +
                                    "' không có IPv4 address.\nVui lòng chọn interface khác.",
                            "Lỗi copy",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int port = (courtPort > 0) ? courtPort : 2345;
                url = "http://" + ip + ":" + port + "/scoreboard/" + getCourtPinCode();
            }

            java.awt.datatransfer.StringSelection stringSelection = new java.awt.datatransfer.StringSelection(url);
            java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            logger.logTs("Đã copy link vào clipboard: %s", url);

            // Hiển thị thông báo ngắn
            JOptionPane.showMessageDialog(this,
                    "Đã copy link vào clipboard!\n" + url,
                    "Copy thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            logger.logTs("Lỗi khi copy link: %s", ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi copy link: " + ex.getMessage(),
                    "Lỗi copy",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Cập nhật phần hiển thị link theo trạng thái ẩn/hiện
    private void updateRemoteUrlDisplay() {
        try {
            if (currentRemoteUrl == null || currentRemoteUrl.isBlank()) {
                lblRemoteUrl.setText("-");
                if (btnToggleLinkVisible != null)
                    btnToggleLinkVisible.setEnabled(false);
                return;
            }
            if (btnToggleLinkVisible != null)
                btnToggleLinkVisible.setEnabled(true);
            if (remoteUrlVisible) {
                lblRemoteUrl.setText("<html><b>" + currentRemoteUrl + "</b></html>");
            } else {
                lblRemoteUrl.setText("<html><b>••••••••••</b></html>");
            }
        } catch (Exception ignore) {
        }
    }

    /**
     * Cập nhật hiển thị QR code theo trạng thái ẩn/hiện
     */
    private void updateQrCodeDisplay() {
        try {
            if (qrCodeVisible) {
                // Hiển thị QR code bình thường
                updateRemoteLinkUi(); // Gọi lại để tạo QR code
            } else {
                // Ẩn QR code bằng cách xóa nội dung
                lblRemoteQr.setIcon(null);
                lblRemoteQr.setText("");
            }
        } catch (Exception ignore) {
        }
    }

    /**
     * Chụp ảnh bảng điểm mini và lưu vào folder
     */
    private void captureMiniScoreboard() {
        try {
            // Sử dụng thư mục screenshots trong project
            File projectDir = new File(System.getProperty("user.dir"));
            File screenshotDir = new File(projectDir, "screenshots");
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }

            // Tạo tên file với timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String timestamp = sdf.format(new Date());
            String fileName = String.format("scoreboard_%s.png", timestamp);
            File outputFile = new File(screenshotDir, fileName);

            // Chụp ảnh bảng điểm mini
            BufferedImage image = new BufferedImage(
                    mini.getWidth(), mini.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            mini.paint(g2d);
            g2d.dispose();

            // Lưu ảnh
            ImageIO.write(image, "PNG", outputFile);

            logger.logTs("Đã chụp ảnh bảng điểm: %s", outputFile.getAbsolutePath());

            // Gửi ảnh về admin nếu đang broadcast
            if (hasStarted && selectedIf != null) {
                sendScreenshotToAdmin(image, fileName);
            }

            // Hiển thị thông báo thành công
            JOptionPane.showMessageDialog(this,
                    "Đã chụp ảnh bảng điểm!\n" + outputFile.getName(),
                    "Chụp ảnh thành công",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            logger.logTs("Lỗi khi chụp ảnh bảng điểm: %s", ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi chụp ảnh: " + ex.getMessage(),
                    "Lỗi chụp ảnh",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Gửi ảnh screenshot về admin qua network
     */
    private void sendScreenshotToAdmin(BufferedImage image, String fileName) {
        try {
            // Tạo thông tin trận đấu để gửi kèm
            var snapshot = match.snapshot();
            String matchInfo = String.format("Trận: %s vs %s | Ván %d/%d | Tỉ số: %d-%d | Thời gian: %s",
                    snapshot.names[0], snapshot.names[1], snapshot.gameNumber, snapshot.bestOf,
                    snapshot.score[0], snapshot.score[1], new SimpleDateFormat("HH:mm:ss").format(new Date()));

            // Gửi ảnh qua ScoreboardService để broadcast về admin
            scoreboardSvc.sendScreenshotToAdmin(image, fileName, matchInfo, selectedIf);

            logger.logTs("Đã gửi ảnh screenshot về admin: %s", fileName);
        } catch (Exception ex) {
            logger.logTs("Lỗi khi gửi ảnh về admin: %s", ex.getMessage());
        }
    }

    private void cancelFinishTimer() {
        if (finishTimer != null) {
            finishTimer.stop();
            finishTimer = null;
        }
        finishScheduled = false;
    }

    /* =================== MATCH -> UI =================== */

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        BadmintonMatch.Snapshot s = match.snapshot();
        lblGame.setText("Ván " + s.gameNumber + " / BO" + s.bestOf);
        lblGamesWon.setText("Ván: " + s.games[0] + " - " + s.games[1]);
        String court = " (" + (s.score[s.server] % 2 == 0 ? "R" : "L") + ")";
        lblServer.setText("Giao cầu: " + (s.server == 0 ? "A" : "B") + court);

        // Điều khiển enable/disable theo trạng thái
        if (!hasStarted) {
            setScoreButtonsEnabled(false);
            nextGame.setEnabled(false);
        } else if (s.matchFinished) {
            lblStatus.setText("Trận đấu đã kết thúc");
            lblWinner.setText((s.games[0] > s.games[1] ? s.names[0] : s.names[1]));

            setScoreButtonsEnabled(false);
            nextGame.setEnabled(false);

            if (!finishScheduled) {
                finishScheduled = true;
                try {
                    btnFinish.setEnabled(false);
                } catch (Exception ignored) {
                }

                // Chụp ảnh bảng điểm mini khi trận đấu kết thúc
                SwingUtilities.invokeLater(() -> captureMiniScoreboard());

                // Phát âm kết thúc (1 lần)
                com.example.btms.util.sound.SoundPlayer.playEndIfEnabled();

                cancelFinishTimer();
                finishTimer = new javax.swing.Timer(3000, e -> {
                    logger.logWinner(s.games[0] > s.games[1] ? s.names[0] : s.names[1]);
                    cancelFinishTimer();
                    onFinish();
                });
                finishTimer.setRepeats(false);
                finishTimer.start();
            }
        } else if (s.betweenGamesInterval) {
            lblStatus
                    .setText(
                            "Nghỉ giữa ván - bấm \"Ván tiếp theo\" hoặc sau 3 giây sẽ tự động chuyển sang ván kế tiếp");
            setScoreButtonsEnabled(false);
            nextGame.setEnabled(true);
            finishScheduled = false;
            // đếm ngược 20 giây r chuyển sang trận kế tiếp
            new javax.swing.Timer(20000, e -> {
                match.nextGame();
            }).start();
        } else {
            lblStatus.setText("Đang thi đấu");
            setScoreButtonsEnabled(true);
            nextGame.setEnabled(false);
            finishScheduled = false;
        }

        // Xử lý sự kiện swap để cập nhật mini panel
        if ("swap".equals(evt.getPropertyName()) && mini != null) {
            logger.logTs("=== BADMINTON CONTROL PANEL - SWAP EVENT DETECTED ===");
            logger.logTs("Forcing mini panel refresh for swap event");

            // Log chi tiết trạng thái sau khi đổi sân
            BadmintonMatch.Snapshot swapSnapshot = match.snapshot();
            logger.logTs("=== TRẠNG THÁI SAU KHI ĐỔI SÂN ===");
            logger.logTs("VĐV A: '%s' - Điểm hiện tại: %d, Ván thắng: %d", swapSnapshot.names[0], swapSnapshot.score[0],
                    swapSnapshot.games[0]);
            logger.logTs("VĐV B: '%s' - Điểm hiện tại: %d, Ván thắng: %d", swapSnapshot.names[1], swapSnapshot.score[1],
                    swapSnapshot.games[1]);
            logger.logTs("Server hiện tại: %s (VĐV %s)", swapSnapshot.server == 0 ? "A" : "B",
                    swapSnapshot.server == 0 ? swapSnapshot.names[0] : swapSnapshot.names[1]);
            logger.logTs("Ván hiện tại: %d/%d", swapSnapshot.gameNumber, swapSnapshot.bestOf);

            // Log điểm các ván đã hoàn thành
            logger.logTs("Điểm các ván đã hoàn thành:");
            for (int i = 0; i < swapSnapshot.gameScores.length; i++) {
                if (swapSnapshot.gameScores[i][0] >= 0 && swapSnapshot.gameScores[i][1] >= 0) {
                    logger.logTs("  Ván %d: %s=%d, %s=%d",
                            i + 1,
                            swapSnapshot.names[0], swapSnapshot.gameScores[i][0],
                            swapSnapshot.names[1], swapSnapshot.gameScores[i][1]);
                }
            }
            logger.logTs("================================================");

            mini.forceRefresh();
        }
    }

    /* =================== Misc =================== */

    private void setScoreButtonsEnabled(boolean on) {
        if (aPlus != null)
            aPlus.setEnabled(on);
        if (bPlus != null)
            bPlus.setEnabled(on);
        if (aMinus != null)
            aMinus.setEnabled(on);
        if (bMinus != null)
            bMinus.setEnabled(on);
        if (undo != null)
            undo.setEnabled(on);
        if (swapEnds != null)
            swapEnds.setEnabled(on);
        if (toggleServe != null)
            toggleServe.setEnabled(on);
    }

    private void installKeyBindings() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "aPlus");
        getActionMap().put("aPlus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (aPlus != null && aPlus.isEnabled())
                    aPlus.doClick();
            }
        });
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl ENTER"), "bPlus");
        getActionMap().put("bPlus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (bPlus != null && bPlus.isEnabled())
                    bPlus.doClick();
            }
        });
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("BACK_SPACE"), "undo");
        getActionMap().put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undo != null && undo.isEnabled())
                    undo.doClick();
            }
        });
    }

    private void populateScreens() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();
        cboScreen.removeAllItems();
        if (screens == null || screens.length == 0) {
            cboScreen.addItem("Màn hình 1 (mặc định)");
        } else {
            for (int i = 0; i < screens.length; i++) {
                Rectangle b = screens[i].getDefaultConfiguration().getBounds();
                String item = String.format("Màn hình %d — %dx%d @ (%d,%d)", i + 1, b.width, b.height, b.x, b.y);
                cboScreen.addItem(item);
            }
            cboScreen.setSelectedIndex(0);
        }
    }

    private void restoreSplitLocations() {
        try {
            int v;
            v = prefs.getInt("split.leftVert", -1);
            if (v >= 0 && leftVert != null)
                leftVert.setDividerLocation(v);
            v = prefs.getInt("split.midVert", -1);
            if (v >= 0 && midVert != null)
                midVert.setDividerLocation(v);
            v = prefs.getInt("split.rightVert", -1);
            if (v >= 0 && rightVert != null)
                rightVert.setDividerLocation(v);
            v = prefs.getInt("split.centerRight", -1);
            if (v >= 0 && centerRightSplit != null)
                centerRightSplit.setDividerLocation(v);
            v = prefs.getInt("split.main", -1);
            if (v >= 0 && mainSplit != null)
                mainSplit.setDividerLocation(v);
        } catch (Exception ignore) {
        }
    }

    /** Thu nhỏ (iconify) các cửa sổ scoreboard thuộc sân này */
    public void minimizeDisplays() {
        try {
            scoreboardSvc.minimizeDisplays();
        } catch (Throwable t) {
            // Fallback nhẹ nhàng
            for (java.awt.Window w : java.awt.Window.getWindows()) {
                if (w.isShowing() && w instanceof java.awt.Frame f) {
                    f.setState(java.awt.Frame.ICONIFIED);
                }
            }
        }
    }

    /**
     * Lấy mã PIN của sân hiện tại
     * Cần được set từ MultiCourtControlPanel khi tạo sân
     */
    private String courtPinCode = "0000"; // Mặc định

    public void setCourtPinCode(String pinCode) {
        this.courtPinCode = pinCode;
        // Cập nhật match để sử dụng match của PIN này
        switchToMatchByPin();
    }

    private String getCourtPinCode() {
        return courtPinCode;
    }

    /**
     * Lấy URL để nhập mã PIN
     */
    private String getPinEntryUrl() {
        try {
            String ip = NetworkUtil.getLocalIpv4(selectedIf);
            if (ip == null || ip.isEmpty()) {
                return "LỖI: Interface không có IPv4";
            }
            int port = (courtPort > 0) ? courtPort : 2345;
            return "http://" + ip + ":" + port + "/pin";
        } catch (Exception ex) {
            return "LỖI: " + ex.getMessage();
        }
    }

    /**
     * Copy link nhập PIN vào clipboard
     */
    private void copyPinLinkToClipboard() {
        try {
            String pinUrl = getPinEntryUrl();
            java.awt.datatransfer.StringSelection stringSelection = new java.awt.datatransfer.StringSelection(pinUrl);
            java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            logger.logTs("Đã copy link nhập PIN vào clipboard: %s", pinUrl);

            // Hiển thị thông báo ngắn
            JOptionPane.showMessageDialog(this,
                    "Đã copy link nhập PIN vào clipboard!\n" + pinUrl,
                    "Copy thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            logger.logTs("Lỗi khi copy link PIN: %s", ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi copy link PIN: " + ex.getMessage(),
                    "Lỗi copy",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cập nhật link PIN trong panel
     */
    private void updatePinLinkInPanel(JPanel panel) {
        for (java.awt.Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getText() != null && label.getText().contains("Link nhập PIN:")) {
                    label.setText("🔗 Link nhập PIN: " + getPinEntryUrl());
                    return;
                }
            } else if (comp instanceof JPanel) {
                updatePinLinkInPanel((JPanel) comp);
            }
        }
    }

    /** Khôi phục & đưa các cửa sổ scoreboard ra trước */
    public void restoreDisplays() {
        try {
            scoreboardSvc.restoreDisplays();
        } catch (Throwable t) {
            for (java.awt.Window w : java.awt.Window.getWindows()) {
                if (w.isShowing() && w instanceof java.awt.Frame f) {
                    f.setState(java.awt.Frame.NORMAL);
                    f.toFront();
                    f.requestFocus();
                }
            }
        }
    }

    /**
     * Tạo tên hiển thị từ danh sách VĐV của đội
     * Chỉ trả về tên VĐV đầu tiên vì bảng điểm sẽ hiển thị mỗi tên 1 hàng
     */
    // (Đã bỏ createPlayerDisplayName vì không còn cần riêng hiển thị VĐV đầu tiên)

    /**
     * Tạo tên đầy đủ cho mỗi đội (mỗi VĐV 1 hàng)
     */
    private String buildFullTeamName(VanDongVien[] players) {
        if (players == null || players.length == 0) {
            return "";
        }
        StringBuilder fullName = new StringBuilder();
        for (int i = 0; i < players.length; i++) {
            VanDongVien v = players[i];
            fullName.append(v != null && v.getHoTen() != null ? v.getHoTen() : ("#" + i));
            if (i < players.length - 1) {
                fullName.append(" - ");
            }
        }
        return fullName.toString();
    }

    // Không cần convert nữa: dùng trực tiếp VanDongVien[]
}
