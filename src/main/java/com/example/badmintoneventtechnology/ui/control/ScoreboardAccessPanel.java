package com.example.badmintoneventtechnology.ui.control;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.net.NetworkInterface;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.example.badmintoneventtechnology.util.net.NetworkUtil;
import com.example.badmintoneventtechnology.util.qr.QRCodeUtil;

public class ScoreboardAccessPanel extends JPanel {
    private final JLabel qrLabel = new JLabel();
    private final JLabel linkLabel = new JLabel();

    public ScoreboardAccessPanel(NetworkInterface selectedIf) {
        super(new FlowLayout(FlowLayout.LEFT));
        add(qrLabel);
        add(linkLabel);
        refresh(selectedIf);
    }

    public void refresh(NetworkInterface selectedIf) {
        String ip = NetworkUtil.getLocalIpv4(selectedIf);
        if (ip == null || ip.isEmpty()) {
            linkLabel.setText("LỖI: Interface không có IPv4 address");
            qrLabel.setText("Không thể tạo QR code");
            return;
        }

        String link = "http://" + ip + ":2345/scoreboard";
        linkLabel.setText("Link bấm điểm: " + link);
        try {
            BufferedImage qr = QRCodeUtil.generate(link, 100);
            qrLabel.setIcon(new ImageIcon(qr));
        } catch (Exception e) {
            qrLabel.setText("Không tạo được QR code");
        }
    }
}