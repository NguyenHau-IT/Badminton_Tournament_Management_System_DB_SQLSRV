package com.example.badmintoneventtechnology.service.scoreboard;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import com.example.badmintoneventtechnology.infrastructure.net.ScoreboardBroadcaster;
import com.example.badmintoneventtechnology.model.match.BadmintonMatch;
import com.example.badmintoneventtechnology.ui.scoreboard.BadmintonDisplayFrame;
import com.example.badmintoneventtechnology.ui.scoreboard.BadmintonDisplayHorizontalFrame;
import com.example.badmintoneventtechnology.util.ui.FullscreenHelper;
import com.example.badmintoneventtechnology.util.ui.IconUtil;

public class ScoreboardService {
    private ScoreboardBroadcaster broadcaster;
    private BadmintonDisplayFrame displayV;
    private BadmintonDisplayHorizontalFrame displayH;
    private javax.swing.JFrame verticalFrame;
    private javax.swing.JFrame horizontalFrame;

    public void startBroadcast(BadmintonMatch match, NetworkInterface nif, String clientName, String hostShown,
            String displayKind, String header, boolean isDoubles, String aLabel, String bLabel, String courtId) {
        stopBroadcast();
        try {
            broadcaster = new ScoreboardBroadcaster(match, nif, clientName, hostShown, displayKind, courtId);
        } catch (Exception ignored) {
        }
        if (broadcaster != null) {
            broadcaster.setMeta(header, isDoubles, aLabel, bLabel);
            broadcaster.start();
        }
    }

    public void stopBroadcast() {
        if (broadcaster != null) {
            try {
                broadcaster.stop();
            } catch (Exception ignored) {
            }
            broadcaster = null;
        }
    }

    public void openVertical(BadmintonMatch match, int screenIndex) {
        if (displayV == null) {
            displayV = new BadmintonDisplayFrame(match);
            IconUtil.applyTo(displayV);
        }
        FullscreenHelper.enterFullscreenOnScreen(displayV, screenIndex);
    }

    public void openHorizontal(BadmintonMatch match, int screenIndex, String header) {
        if (displayH == null) {
            displayH = new BadmintonDisplayHorizontalFrame(match);
            IconUtil.applyTo(displayH);
        }
        displayH.setHeader(header == null || header.isBlank() ? "TRẬN ĐẤU" : header);
        displayH.setPartners("", "");
        FullscreenHelper.enterFullscreenOnScreen(displayH, screenIndex);
    }

    public void closeDisplays() {
        if (displayV != null) {
            displayV.setVisible(false);
            displayV.dispose();
            displayV = null;
        }
        if (displayH != null) {
            displayH.setVisible(false);
            displayH.detach();
            displayH.dispose();
            displayH = null;
        }
    }

    /**
     * Gửi ảnh screenshot về admin qua UDP broadcast
     */
    public void sendScreenshotToAdmin(BufferedImage image, String fileName, String matchInfo, NetworkInterface nif) {
        try {
            // Chuyển ảnh thành byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "PNG", baos);
            byte[] imageData = baos.toByteArray();

            // Tạo header chứa thông tin
            String header = String.format("SCREENSHOT|%s|%s|%d", fileName, matchInfo, imageData.length);
            byte[] headerBytes = header.getBytes("UTF-8");

            // Tạo packet hoàn chỉnh: header + ảnh
            byte[] fullPacket = new byte[headerBytes.length + imageData.length];
            System.arraycopy(headerBytes, 0, fullPacket, 0, headerBytes.length);
            System.arraycopy(imageData, 0, fullPacket, headerBytes.length, imageData.length);

            // Gửi qua UDP broadcast
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);

                // Tìm địa chỉ broadcast của network interface
                Enumeration<InetAddress> addresses = nif.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().contains(".")) {
                        // Tạo địa chỉ broadcast
                        String ip = addr.getHostAddress();
                        String[] parts = ip.split("\\.");
                        String broadcastIP = parts[0] + "." + parts[1] + "." + parts[2] + ".255";

                        InetAddress broadcastAddr = InetAddress.getByName(broadcastIP);
                        DatagramPacket packet = new DatagramPacket(fullPacket, fullPacket.length, broadcastAddr, 2346);
                        socket.send(packet);

                        // Đã gửi screenshot thành công
                        break;
                    }
                }
            }

        } catch (Exception ex) {
            System.err.println("Lỗi khi gửi screenshot: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void minimizeDisplays() {
        if (verticalFrame != null)
            verticalFrame.setState(java.awt.Frame.ICONIFIED);
        if (horizontalFrame != null)
            horizontalFrame.setState(java.awt.Frame.ICONIFIED);
    }

    public void restoreDisplays() {
        if (verticalFrame != null) {
            verticalFrame.setState(java.awt.Frame.NORMAL);
            verticalFrame.toFront();
            verticalFrame.requestFocus();
        }
        if (horizontalFrame != null) {
            horizontalFrame.setState(java.awt.Frame.NORMAL);
            horizontalFrame.toFront();
            horizontalFrame.requestFocus();
        }
    }
}