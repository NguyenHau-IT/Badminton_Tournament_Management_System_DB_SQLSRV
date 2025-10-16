package com.example.btms.ui.tools;

import com.example.btms.config.Prefs;
import com.example.btms.service.db.DbBackupService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Backup window: supports manual backup and auto-backup on an interval (up to
 * 60 minutes).
 */
public class DbBackupFrame extends JFrame {

    private final Connection conn;
    private final DbBackupService service = new DbBackupService();

    private final JTextField txtFolder = new JTextField(28);
    private final JButton btnBrowse = new JButton("Chọn...");
    private final JButton btnBackup = new JButton("Sao lưu ngay");
    private final JCheckBox chkAuto = new JCheckBox("Tự sao lưu");
    private final JComboBox<String> cbInterval = new JComboBox<>(new String[] {
            "5 phút", "10 phút", "15 phút", "30 phút", "45 phút", "60 phút"
    });
    private final JLabel lblStatus = new JLabel(" ");

    private Timer timer;

    public DbBackupFrame(Connection conn) {
        super("Sao lưu CSDL");
        this.conn = conn;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(560, 240);
        setLocationByPlatform(true);
        setLayout(new BorderLayout(0, 8));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;
        form.add(new JLabel("Thư mục lưu:"), gc);
        gc.gridx = 1;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        form.add(txtFolder, gc);
        gc.gridx = 3;
        gc.gridwidth = 1;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        form.add(btnBrowse, gc);

        gc.gridx = 0;
        gc.gridy++;
        form.add(new JLabel("Chế độ:"), gc);
        gc.gridx = 1;
        gc.gridwidth = 1;
        form.add(chkAuto, gc);
        gc.gridx = 2;
        form.add(new JLabel("Chu kỳ:"), gc);
        gc.gridx = 3;
        form.add(cbInterval, gc);

        add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(btnBackup);
        south.add(right, BorderLayout.EAST);
        south.add(lblStatus, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        loadPrefs();
        wireEvents();
        updateTimer();
    }

    private void loadPrefs() {
        Prefs p = new Prefs();
        txtFolder.setText(p.get("backup.dir", new File("backup").getAbsolutePath()));
        chkAuto.setSelected(p.getBool("backup.auto", false));
        int idx = p.getInt("backup.interval.index", 1); // default 10 phút
        idx = Math.max(0, Math.min(idx, cbInterval.getItemCount() - 1));
        cbInterval.setSelectedIndex(idx);
    }

    private void savePrefs() {
        Prefs p = new Prefs();
        p.put("backup.dir", txtFolder.getText());
        p.putBool("backup.auto", chkAuto.isSelected());
        p.putInt("backup.interval.index", cbInterval.getSelectedIndex());
    }

    private void wireEvents() {
        btnBrowse.addActionListener(e -> chooseFolder());
        btnBackup.addActionListener(e -> doBackup());
        chkAuto.addActionListener(e -> {
            savePrefs();
            updateTimer();
        });
        cbInterval.addActionListener(e -> {
            savePrefs();
            updateTimer();
        });
    }

    private void chooseFolder() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File base = new File(txtFolder.getText());
        if (base.isDirectory())
            fc.setCurrentDirectory(base);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtFolder.setText(fc.getSelectedFile().getAbsolutePath());
            savePrefs();
        }
    }

    private int getIntervalMinutes() {
        switch (cbInterval.getSelectedIndex()) {
            case 0:
                return 5;
            case 1:
                return 10;
            case 2:
                return 15;
            case 3:
                return 30;
            case 4:
                return 45;
            case 5:
                return 60;
            default:
                return 10;
        }
    }

    private void updateTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (!chkAuto.isSelected()) {
            lblStatus.setText("Tự sao lưu: tắt");
            return;
        }
        int minutes = getIntervalMinutes();
        long period = Math.max(1, Math.min(60, minutes)) * 60_000L; // clamp 1..60
        timer = new Timer("db-backup-timer", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                doBackup();
            }
        }, period, period);
        lblStatus.setText("Tự sao lưu mỗi " + minutes + " phút");
    }

    private void doBackup() {
        File dir = new File(txtFolder.getText());
        if (dir.getPath().isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thư mục lưu.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        btnBackup.setEnabled(false);
        new Thread(() -> {
            try {
                DbBackupService.BackupResult r = service.backupNow(conn, dir);
                SwingUtilities.invokeLater(() -> lblStatus.setText("Đã sao lưu: " + r.targetFile.getName()
                        + " (" + r.millis + " ms)"));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                        "Sao lưu thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE));
            } finally {
                SwingUtilities.invokeLater(() -> btnBackup.setEnabled(true));
            }
        }, "db-backup").start();
    }
}
