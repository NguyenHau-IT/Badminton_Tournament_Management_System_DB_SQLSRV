package com.example.btms.ui.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import com.example.btms.config.Prefs;
import com.example.btms.ui.main.MainFrame;
import com.example.btms.util.sound.SoundLibrary;

/** Trang cài đặt chung của ứng dụng. */
public class SettingsPanel extends JPanel {
    private final Prefs prefs = new Prefs();
    private final MainFrame mainFrame; // để gọi applyTheme

    private JCheckBox chkDark;
    private JComboBox<String> cboMonitorCols;
    private JSpinner spFontScale;
    private JCheckBox chkAlwaysOnTop;
    private JCheckBox chkSoundEnabled;
    private JLabel lblStartSound;
    private JLabel lblEndSound;
    private Preferences soundPrefs;
    private JLabel lblReportLogo;
    private JLabel lblSponsorLogo;
    private JComboBox<String> cboBracketNameFont;

    public SettingsPanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private Component buildHeader() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel title = new JLabel("CÀI ĐẶT HỆ THỐNG");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        panel.add(title);
        return panel;
    }

    private Component buildContent() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 4, 8, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;

        // Theme
        chkDark = new JCheckBox("Chế độ tối (Dark Mode)");
        chkDark.setSelected(prefs.getBool("ui.darkTheme", false));
        chkDark.addActionListener(e -> {
            boolean dark = chkDark.isSelected();
            prefs.putBool("ui.darkTheme", dark);
            // đổi theme sau một tick để tránh flicker focus
            SwingUtilities.invokeLater(() -> mainFrame.applyTheme(dark));
        });
        panel.add(chkDark, gc);

        gc.gridy++;
        JLabel note = new JLabel("Thay đổi giao diện áp dụng ngay lập tức và được lưu cho lần mở sau.");
        note.setFont(note.getFont().deriveFont(Font.ITALIC, 12f));
        panel.add(note, gc);

        // Monitor columns
        gc.gridy++;
        cboMonitorCols = new JComboBox<>(new String[] { "1", "2", "3", "4" });
        int savedCols = prefs.getInt("monitor.columns", 3);
        if (savedCols < 1 || savedCols > 4)
            savedCols = 3;
        cboMonitorCols.setSelectedItem(String.valueOf(savedCols));
        cboMonitorCols.addActionListener(e -> {
            Object sel = cboMonitorCols.getSelectedItem();
            if (sel instanceof String s && s.chars().allMatch(Character::isDigit)) {
                int cols = Integer.parseInt(s);
                if (cols >= 1 && cols <= 4) {
                    prefs.putInt("monitor.columns", cols);
                    // gọi sang monitorTab nếu có
                    mainFrame.updateMonitorColumns(cols);
                }
            }
        });
        panel.add(rowLabelWithComp("Số cột giám sát:", cboMonitorCols), gc);

        // Font scale
        gc.gridy++;
        double fs = prefs.getInt("ui.fontScalePercent", 100);
        spFontScale = new JSpinner(new SpinnerNumberModel(fs, 80, 160, 5));
        spFontScale.addChangeListener(e -> {
            int pct = ((Number) spFontScale.getValue()).intValue();
            prefs.putInt("ui.fontScalePercent", pct);
            // Áp dụng realtime
            mainFrame.applyGlobalFontScale();
        });
        panel.add(rowLabelWithComp("Font scale (%):", spFontScale), gc);

        // Bracket name font size (SoDoThiDau)
        gc.gridy++;
        String[] sizes = new String[15]; // 10..24
        for (int i = 0; i < sizes.length; i++)
            sizes[i] = String.valueOf(10 + i);
        cboBracketNameFont = new JComboBox<>(sizes);
        int savedSize = prefs.getInt("bracket.nameFontSize", 12);
        if (savedSize < 10 || savedSize > 24)
            savedSize = 12;
        cboBracketNameFont.setSelectedItem(String.valueOf(savedSize));
        cboBracketNameFont.addActionListener(e -> {
            Object sel = cboBracketNameFont.getSelectedItem();
            if (sel instanceof String s && s.chars().allMatch(Character::isDigit)) {
                int v = Integer.parseInt(s);
                if (v >= 10 && v <= 24) {
                    prefs.putInt("bracket.nameFontSize", v);
                    // Repaint UI to reflect font change where applicable
                    SwingUtilities.invokeLater(() -> mainFrame.repaint());
                }
            }
        });
        panel.add(rowLabelWithComp("Cỡ chữ tên VĐV/Đội (Sơ đồ thi đấu):", cboBracketNameFont), gc);

        // Bracket seeding settings
        gc.gridy++;
        JComboBox<String> cboSeedMode = new JComboBox<>(new String[] {
                "Mode 1: [1,5,3,7;2,6,4,8]",
                "Mode 2: [8,4,6,2;7,3,5,1]",
                "Mode 3: [1,8,5,4;3,6,7,2]",
                "Mode 4: [1,2,3,4,5,6,7,8]",
                "Mode 5: [8,7,6,5,4,3,2,1]",
                "Mode 6: [8,3,6,2;7,4,5,1]",
                "Mode 7: [1,8,4,5;2,7,3,6]"
        });
        int seedMode = prefs.getInt("bracket.seed.mode", 2);
        if (seedMode < 1 || seedMode > 7)
            seedMode = 2;
        cboSeedMode.setSelectedIndex(seedMode - 1);
        cboSeedMode.setToolTipText("Chọn cách đặt cặp ở vòng 1 khi gán sơ đồ từ bốc thăm");
        cboSeedMode.addActionListener(e -> {
            int idx = cboSeedMode.getSelectedIndex();
            prefs.putInt("bracket.seed.mode", idx + 1);
        });
        panel.add(rowLabelWithComp("Chế độ seed (Sơ đồ thi đấu):", cboSeedMode), gc);

        gc.gridy++;
        JCheckBox chkAvoidSameClub = new JCheckBox("Tránh để cùng CLB gặp nhau ở vòng 1");
        chkAvoidSameClub.setSelected(prefs.getBool("bracket.seed.avoidSameClub", true));
        chkAvoidSameClub.setToolTipText("Cố gắng hoán đổi vị trí để giảm tối đa cặp cùng CLB ở vòng 1");
        chkAvoidSameClub.addActionListener(e -> {
            prefs.putBool("bracket.seed.avoidSameClub", chkAvoidSameClub.isSelected());
        });
        panel.add(chkAvoidSameClub, gc);

        // Always on top for floating windows (monitor viewers / control?)
        gc.gridy++;
        chkAlwaysOnTop = new JCheckBox("Cửa sổ nổi luôn trên cùng");
        chkAlwaysOnTop.setSelected(prefs.getBool("ui.alwaysOnTop", false));
        chkAlwaysOnTop.addActionListener(e -> {
            prefs.putBool("ui.alwaysOnTop", chkAlwaysOnTop.isSelected());
            mainFrame.applyAlwaysOnTopFloating(chkAlwaysOnTop.isSelected());
        });
        panel.add(chkAlwaysOnTop, gc);

        // ================== Logo báo cáo (PDF) ==================
        gc.gridy++;
        lblReportLogo = new JLabel(getShortPath(prefs.get("report.logo.path", "(chưa chọn)")));
        JButton btnPickLogo = new JButton("Chọn...");
        btnPickLogo.addActionListener(e -> chooseLogoFile());
        JButton btnClearLogo = new JButton("Xóa");
        btnClearLogo.addActionListener(e -> {
            prefs.remove("report.logo.path");
            lblReportLogo.setText("(chưa chọn)");
        });
        panel.add(rowLabelWithComp("Logo báo cáo:", wrapTriple(lblReportLogo, btnPickLogo, btnClearLogo)), gc);

        // ================== Header image (top bar) ==================
        gc.gridy++;
        JLabel lblHeaderLogo = new JLabel(getShortPath(prefs.get("ui.header.logo.path", "(chưa chọn)")));
        JButton btnPickHeader = new JButton("Chọn...");
        btnPickHeader.addActionListener(e -> chooseHeaderLogo(lblHeaderLogo));
        JButton btnClearHeader = new JButton("Xóa");
        btnClearHeader.addActionListener(e -> {
            prefs.remove("ui.header.logo.path");
            lblHeaderLogo.setText("(chưa chọn)");
            // refresh main frame header
            SwingUtilities.invokeLater(() -> mainFrame.refreshHeader());
        });
        panel.add(rowLabelWithComp("Ảnh header (Top):", wrapTriple(lblHeaderLogo, btnPickHeader, btnClearHeader)), gc);

        // ================== Logo nhà tài trợ (PDF) ==================
        gc.gridy++;
        lblSponsorLogo = new JLabel(getShortPath(prefs.get("report.sponsor.logo.path", "(chưa chọn)")));
        JButton btnPickSponsorLogo = new JButton("Chọn...");
        btnPickSponsorLogo.addActionListener(e -> chooseSponsorLogoFile());
        JButton btnClearSponsorLogo = new JButton("Xóa");
        btnClearSponsorLogo.addActionListener(e -> {
            prefs.remove("report.sponsor.logo.path");
            lblSponsorLogo.setText("(chưa chọn)");
        });
        panel.add(rowLabelWithComp("Logo nhà tài trợ:",
                wrapTriple(lblSponsorLogo, btnPickSponsorLogo, btnClearSponsorLogo)), gc);

        // === Âm thanh trận đấu ===
        gc.gridy++;
        chkSoundEnabled = new JCheckBox("Bật âm báo trận (bắt đầu / kết thúc)");
        soundPrefs = Preferences.userRoot().node("btms.sound");
        chkSoundEnabled.setSelected(soundPrefs.getBoolean("sound.enabled", false));
        chkSoundEnabled.addActionListener(e -> {
            soundPrefs.putBoolean("sound.enabled", chkSoundEnabled.isSelected());
        });
        panel.add(chkSoundEnabled, gc);

        // Row chọn file âm bắt đầu
        gc.gridy++;
        lblStartSound = new JLabel(getShortPath(soundPrefs.get("sound.start.path", "(chưa chọn)")));
        JButton btnPickStart = new JButton("Chọn...");
        btnPickStart.addActionListener(e -> chooseSoundFile("sound.start.path", lblStartSound));
        JButton btnBuiltinStart = new JButton("Built-in");
        btnBuiltinStart.addActionListener(e -> chooseBuiltIn("sound.start.path", lblStartSound));
        panel.add(rowLabelWithComp("Âm bắt đầu:", wrapTriple(lblStartSound, btnPickStart, btnBuiltinStart)), gc);

        // Row chọn file âm kết thúc
        gc.gridy++;
        lblEndSound = new JLabel(getShortPath(soundPrefs.get("sound.end.path", "(chưa chọn)")));
        JButton btnPickEnd = new JButton("Chọn...");
        btnPickEnd.addActionListener(e -> chooseSoundFile("sound.end.path", lblEndSound));
        JButton btnBuiltinEnd = new JButton("Built-in");
        btnBuiltinEnd.addActionListener(e -> chooseBuiltIn("sound.end.path", lblEndSound));
        panel.add(rowLabelWithComp("Âm kết thúc:", wrapTriple(lblEndSound, btnPickEnd, btnBuiltinEnd)), gc);

        // Test buttons
        gc.gridy++;
        JButton btnTestStart = new JButton("Test bắt đầu");
        btnTestStart.addActionListener(e -> com.example.btms.util.sound.SoundPlayer.play(
                soundPrefs.get("sound.start.path", "")));
        JButton btnTestEnd = new JButton("Test kết thúc");
        btnTestEnd.addActionListener(e -> com.example.btms.util.sound.SoundPlayer.play(
                soundPrefs.get("sound.end.path", "")));
        panel.add(rowLabelWithComp("Thử âm:", wrapPair(btnTestStart, btnTestEnd)), gc);

        // Reset layout button
        gc.gridy++;
        JButton btnResetLayout = new JButton("Reset bố cục điều khiển");
        btnResetLayout.addActionListener(e -> {
            prefs.remove("split.main");
            prefs.remove("split.centerRight");
            prefs.remove("split.leftVert");
            prefs.remove("split.midVert");
            prefs.remove("split.rightVert");
            javax.swing.JOptionPane.showMessageDialog(this, "Đã xóa thông số chia bố cục. Khởi động lại để áp dụng.");
        });
        panel.add(btnResetLayout, gc);

        return panel;
    }

    private Component rowLabelWithComp(String label, Component comp) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.add(new JLabel(label));
        row.add(comp);
        return row;
    }

    private Component wrapPair(Component a, Component b) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.add(a);
        p.add(b);
        return p;
    }

    private Component wrapTriple(Component a, Component b, Component c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.add(a);
        p.add(b);
        p.add(c);
        return p;
    }

    private void chooseSoundFile(String key, JLabel target) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn file WAV");
        // If user has previously set a path, open its directory
        String current = soundPrefs.get(key, "");
        if (current != null && !current.isBlank() && !current.startsWith("builtin:")) {
            java.io.File cur = new java.io.File(current);
            if (cur.getParentFile() != null && cur.getParentFile().exists()) {
                fc.setCurrentDirectory(cur.getParentFile());
            }
        } else {
            java.io.File soundsDir = new java.io.File("sounds");
            if (soundsDir.exists())
                fc.setCurrentDirectory(soundsDir);
        }
        int r = fc.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            var f = fc.getSelectedFile();
            if (f != null && f.isFile()) {
                if (!f.getName().toLowerCase().endsWith(".wav")) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Chỉ hỗ trợ file WAV.", "Âm thanh",
                            javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }
                soundPrefs.put(key, f.getAbsolutePath());
                target.setText(getShortPath(f.getAbsolutePath()));
            }
        }
    }

    private void chooseBuiltIn(String key, JLabel target) {
        java.util.List<String> wavs = SoundLibrary.listBuiltInWavFiles();
        if (wavs.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Không tìm thấy file WAV nào trong thư mục sounds/",
                    "Built-in", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String selected = (String) javax.swing.JOptionPane.showInputDialog(this,
                "Chọn âm thanh built-in:",
                "Built-in Sounds",
                javax.swing.JOptionPane.PLAIN_MESSAGE,
                null,
                wavs.toArray(),
                wavs.get(0));
        if (selected != null) {
            String val = "builtin:" + selected;
            soundPrefs.put(key, val);
            target.setText(getShortPath("[builtin] " + selected));
        }
    }

    private void chooseLogoFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn ảnh logo (PNG/JPG)");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Ảnh (PNG, JPG)", "png", "jpg", "jpeg"));
        String current = prefs.get("report.logo.path", "");
        if (current != null && !current.isBlank()) {
            java.io.File cur = new java.io.File(current);
            if (cur.getParentFile() != null && cur.getParentFile().exists()) {
                fc.setCurrentDirectory(cur.getParentFile());
            }
        }
        int r = fc.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            var f = fc.getSelectedFile();
            if (f != null && f.isFile()) {
                String lower = f.getName().toLowerCase();
                if (!(lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg"))) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Chỉ hỗ trợ PNG/JPG.", "Logo",
                            javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }
                prefs.put("report.logo.path", f.getAbsolutePath());
                lblReportLogo.setText(getShortPath(f.getAbsolutePath()));
            }
        }
    }

    private void chooseSponsorLogoFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn ảnh logo nhà tài trợ (PNG/JPG)");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Ảnh (PNG, JPG)", "png", "jpg", "jpeg"));
        String current = prefs.get("report.sponsor.logo.path", "");
        if (current != null && !current.isBlank()) {
            java.io.File cur = new java.io.File(current);
            if (cur.getParentFile() != null && cur.getParentFile().exists()) {
                fc.setCurrentDirectory(cur.getParentFile());
            }
        }
        int r = fc.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            var f = fc.getSelectedFile();
            if (f != null && f.isFile()) {
                String lower = f.getName().toLowerCase();
                if (!(lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg"))) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Chỉ hỗ trợ PNG/JPG.", "Logo nhà tài trợ",
                            javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }
                prefs.put("report.sponsor.logo.path", f.getAbsolutePath());
                lblSponsorLogo.setText(getShortPath(f.getAbsolutePath()));
            }
        }
    }

    private void chooseHeaderLogo(JLabel target) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn ảnh header (PNG/JPG)");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Ảnh (PNG, JPG)", "png", "jpg", "jpeg"));
        String current = prefs.get("ui.header.logo.path", "");
        if (current != null && !current.isBlank()) {
            java.io.File cur = new java.io.File(current);
            if (cur.getParentFile() != null && cur.getParentFile().exists()) {
                fc.setCurrentDirectory(cur.getParentFile());
            }
        }
        int r = fc.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            var f = fc.getSelectedFile();
            if (f != null && f.isFile()) {
                String lower = f.getName().toLowerCase();
                if (!(lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg"))) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Chỉ hỗ trợ PNG/JPG.", "Ảnh header",
                            javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }
                prefs.put("ui.header.logo.path", f.getAbsolutePath());
                target.setText(getShortPath(f.getAbsolutePath()));
                SwingUtilities.invokeLater(() -> mainFrame.refreshHeader());
            }
        }
    }

    private String getShortPath(String path) {
        if (path == null || path.isBlank())
            return "(chưa chọn)";
        if (path.startsWith("builtin:")) {
            return "[builtin] " + path.substring("builtin:".length());
        }
        if (path.length() > 38)
            return "..." + path.substring(path.length() - 35);
        return path;
    }
}
