package com.example.btms.service.scoreboard;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.example.btms.infrastructure.net.ScoreboardBroadcaster;
import com.example.btms.model.match.BadmintonMatch;
import com.example.btms.ui.scoreboard.BadmintonDisplayFrame;
import com.example.btms.ui.scoreboard.BadmintonDisplayHorizontalFrame;
import com.example.btms.util.ui.FullscreenHelper;
import com.example.btms.util.ui.IconUtil;

public class ScoreboardService {
    private ScoreboardBroadcaster broadcaster;
    // Cho phép mở nhiều cửa sổ bảng điểm cùng lúc
    private final List<BadmintonDisplayFrame> verticalDisplays = new ArrayList<>();
    private final List<BadmintonDisplayHorizontalFrame> horizontalDisplays = new ArrayList<>();

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
        BadmintonDisplayFrame frame = new BadmintonDisplayFrame(match);
        IconUtil.applyTo(frame);
        verticalDisplays.add(frame);
        // Xóa khỏi danh sách khi đóng
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                // Giải phóng fullscreen cho frame này để không ảnh hưởng các cửa sổ khác
                try {
                    releaseFullscreen(frame);
                } catch (Throwable ignore) {
                }
                try {
                    frame.detach();
                } catch (Exception ignore) {
                }
                verticalDisplays.remove(frame);
                restoreAndRefocusOtherDisplays();
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    releaseFullscreen(frame);
                } catch (Throwable ignore) {
                }
                try {
                    frame.detach();
                } catch (Exception ignore) {
                }
                verticalDisplays.remove(frame);
                restoreAndRefocusOtherDisplays();
            }
        });
        FullscreenHelper.enterFullscreenOnScreen(frame, screenIndex);
    }

    public void openHorizontal(BadmintonMatch match, int screenIndex, String header) {
        BadmintonDisplayHorizontalFrame frame = new BadmintonDisplayHorizontalFrame(match);
        IconUtil.applyTo(frame);
        frame.setHeader(header == null || header.isBlank() ? "TRẬN ĐẤU" : header);
        frame.setPartners("", "");
        horizontalDisplays.add(frame);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                try {
                    releaseFullscreen(frame);
                } catch (Throwable ignore) {
                }
                try {
                    frame.detach();
                } catch (Exception ignore) {
                }
                horizontalDisplays.remove(frame);
                restoreAndRefocusOtherDisplays();
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    releaseFullscreen(frame);
                } catch (Throwable ignore) {
                }
                try {
                    frame.detach();
                } catch (Exception ignore) {
                }
                horizontalDisplays.remove(frame);
                restoreAndRefocusOtherDisplays();
            }
        });
        FullscreenHelper.enterFullscreenOnScreen(frame, screenIndex);
    }

    public void closeDisplays() {
        for (BadmintonDisplayFrame v : new ArrayList<>(verticalDisplays)) {
            try {
                releaseFullscreen(v);
                v.setVisible(false);
                v.dispose();
            } catch (Exception ignore) {
            }
        }
        verticalDisplays.clear();
        for (BadmintonDisplayHorizontalFrame h : new ArrayList<>(horizontalDisplays)) {
            try {
                releaseFullscreen(h);
                h.setVisible(false);
                h.detach();
                h.dispose();
            } catch (Exception ignore) {
            }
        }
        horizontalDisplays.clear();
    }

    // Giải phóng chế độ fullscreen chỉ cho frame được đóng, tránh ảnh hưởng màn
    // hình khác
    private void releaseFullscreen(java.awt.Frame frame) {
        try {
            java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
            for (java.awt.GraphicsDevice gd : ge.getScreenDevices()) {
                if (gd.getFullScreenWindow() == frame) {
                    gd.setFullScreenWindow(null);
                }
            }
        } catch (Throwable ignore) {
        }
    }

    // Đưa các bảng điểm còn lại ra trước, khôi phục nếu đang bị thu nhỏ/ẩn
    private void restoreAndRefocusOtherDisplays() {
        try {
            for (BadmintonDisplayFrame v : new ArrayList<>(verticalDisplays)) {
                ensureVisibleAndFocused(v);
            }
            for (BadmintonDisplayHorizontalFrame h : new ArrayList<>(horizontalDisplays)) {
                ensureVisibleAndFocused(h);
            }
        } catch (Throwable ignore) {
        }
    }

    private void ensureVisibleAndFocused(java.awt.Frame f) {
        try {
            if (f.getState() == java.awt.Frame.ICONIFIED) {
                f.setState(java.awt.Frame.NORMAL);
            }
            if (!f.isVisible())
                f.setVisible(true);
            // Thử đảm bảo fullscreen đúng màn hình nếu khung bị rơi khỏi fullscreen
            tryReenterFullscreen(f);
            f.toFront();
            f.requestFocus();
        } catch (Throwable ignore) {
        }
    }

    private void tryReenterFullscreen(java.awt.Frame f) {
        if (!(f instanceof javax.swing.JFrame jf))
            return;
        try {
            java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
            java.awt.GraphicsDevice[] devices = ge.getScreenDevices();
            int idx = screenIndexOf(f, devices);
            if (idx >= 0) {
                java.awt.GraphicsDevice gd = devices[idx];
                if (gd.getFullScreenWindow() == null) {
                    // An toàn: đưa frame này trở lại fullscreen trên đúng màn hình
                    FullscreenHelper.enterFullscreenOnScreen(jf, idx);
                }
            }
        } catch (Throwable ignore) {
        }
    }

    private int screenIndexOf(java.awt.Frame f, java.awt.GraphicsDevice[] devices) {
        try {
            java.awt.Rectangle fb = f.getBounds();
            for (int i = 0; i < devices.length; i++) {
                java.awt.Rectangle db = devices[i].getDefaultConfiguration().getBounds();
                if (db.intersects(fb))
                    return i;
            }
        } catch (Throwable ignore) {
        }
        return -1;
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
        for (BadmintonDisplayFrame v : verticalDisplays) {
            try {
                v.setState(java.awt.Frame.ICONIFIED);
            } catch (Exception ignore) {
            }
        }
        for (BadmintonDisplayHorizontalFrame h : horizontalDisplays) {
            try {
                h.setState(java.awt.Frame.ICONIFIED);
            } catch (Exception ignore) {
            }
        }
    }

    public void restoreDisplays() {
        for (BadmintonDisplayFrame v : verticalDisplays) {
            try {
                v.setState(java.awt.Frame.NORMAL);
                v.toFront();
                v.requestFocus();
            } catch (Exception ignore) {
            }
        }
        for (BadmintonDisplayHorizontalFrame h : horizontalDisplays) {
            try {
                h.setState(java.awt.Frame.NORMAL);
                h.toFront();
                h.requestFocus();
            } catch (Exception ignore) {
            }
        }
    }
}