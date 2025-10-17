package com.example.btms.service.scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.net.NetworkInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.btms.infrastructure.net.ScoreboardBroadcaster;
import com.example.btms.model.match.BadmintonMatch;
import com.example.btms.ui.scoreboard.BadmintonDisplayFrame;
import com.example.btms.ui.scoreboard.BadmintonDisplayHorizontalFrame;
import com.example.btms.util.ui.FullscreenHelper;
import com.example.btms.util.ui.IconUtil;

public class ScoreboardService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreboardService.class);
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
                } catch (RuntimeException ex) {
                    LOGGER.warn("Không thể giải phóng fullscreen khi đóng cửa sổ: {}", ex.getMessage());
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
                } catch (RuntimeException ex) {
                    LOGGER.warn("Không thể giải phóng fullscreen khi đóng cửa sổ: {}", ex.getMessage());
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
                } catch (RuntimeException ex) {
                    LOGGER.warn("Không thể giải phóng fullscreen khi đóng cửa sổ ngang: {}", ex.getMessage());
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
                } catch (RuntimeException ex) {
                    LOGGER.warn("Không thể giải phóng fullscreen khi đóng cửa sổ ngang: {}", ex.getMessage());
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
        } catch (RuntimeException ex) {
            LOGGER.warn("Không thể giải phóng fullscreen: {}", ex.getMessage());
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
        } catch (RuntimeException ex) {
            LOGGER.debug("Bỏ qua lỗi khi khôi phục hiển thị: {}", ex.getMessage());
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
        } catch (RuntimeException ex) {
            LOGGER.debug("Bỏ qua lỗi khi đảm bảo hiển thị và focus: {}", ex.getMessage());
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
        } catch (RuntimeException ex) {
            LOGGER.debug("Bỏ qua lỗi khi đưa frame trở lại fullscreen: {}", ex.getMessage());
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
        } catch (RuntimeException ex) {
            LOGGER.debug("Bỏ qua lỗi khi xác định màn hình chứa frame: {}", ex.getMessage());
        }
        return -1;
    }

    // UDP screenshot sending removed: screenshots are saved to local folder only

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