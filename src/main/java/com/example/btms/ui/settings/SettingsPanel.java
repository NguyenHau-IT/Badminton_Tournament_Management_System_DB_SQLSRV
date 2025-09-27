package com.example.btms.ui.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.prefs.Preferences;
import com.example.btms.util.sound.SoundLibrary;

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
