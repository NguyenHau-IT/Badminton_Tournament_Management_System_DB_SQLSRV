package com.example.btms.ui.manager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.NetworkInterface;
import java.sql.Connection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.example.btms.service.db.DatabaseService;
import com.example.btms.ui.auth.LoginFrame.Role;
import com.example.btms.ui.bracket.SoDoThiDauPanel;
import com.example.btms.ui.control.MultiCourtControlPanel;
import com.example.btms.util.ui.IconUtil;

/**
 * Unified window manager cho tất cả floating windows trong ứng dụng.
 * Thay thế BracketWindowManager và PlayWindowManager bằng một component thống
 * nhất.
 */
public class UnifiedWindowManager {

    public enum WindowType {
        BRACKET("Sơ đồ thi đấu", true), // có tabs
        PLAY("Thi đấu", false), // single panel
        MONITOR("Giám sát", false), // future use
        REPORT("Báo cáo", false); // future use

        private final String defaultTitle;
        private final boolean supportsTabs;

        WindowType(String defaultTitle, boolean supportsTabs) {
            this.defaultTitle = defaultTitle;
            this.supportsTabs = supportsTabs;
        }

        public String getDefaultTitle() {
            return defaultTitle;
        }

        public boolean supportsTabs() {
            return supportsTabs;
        }
    }

    // Cấu hình cho mỗi loại window
    public static class WindowConfig {
        private String title;
        private boolean maximized = true;
        private boolean alwaysOnTop = false;
        private Supplier<JPanel> contentSupplier;
        private Runnable onClose;

        public WindowConfig title(String title) {
            this.title = title;
            return this;
        }

        public WindowConfig maximized(boolean maximized) {
            this.maximized = maximized;
            return this;
        }

        public WindowConfig alwaysOnTop(boolean alwaysOnTop) {
            this.alwaysOnTop = alwaysOnTop;
            return this;
        }

        public WindowConfig content(Supplier<JPanel> supplier) {
            this.contentSupplier = supplier;
            return this;
        }

        public WindowConfig onClose(Runnable onClose) {
            this.onClose = onClose;
            return this;
        }
    }

    // Storage for windows and their content
    private final EnumMap<WindowType, JFrame> windows = new EnumMap<>(WindowType.class);
    private final EnumMap<WindowType, JTabbedPane> tabPanes = new EnumMap<>(WindowType.class);

    // Specific storage for bracket tabs
    private final LinkedHashMap<Integer, SoDoThiDauPanel> bracketPanelsByNoiDung = new LinkedHashMap<>();

    /**
     * Mở window theo loại với cấu hình tùy chỉnh
     */
    public void openWindow(WindowType type, DatabaseService service, JFrame parent,
            String tournamentTitle, WindowConfig config) {
        try {
            JFrame existingFrame = windows.get(type);
            if (existingFrame != null) {
                existingFrame.setVisible(true);
                existingFrame.toFront();
                existingFrame.requestFocus();
                return;
            }

            Connection conn = (service != null) ? service.current() : null;
            if (conn == null) {
                JOptionPane.showMessageDialog(parent, "Chưa có kết nối CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFrame frame = createWindow(type, config, tournamentTitle, parent);
            setupWindowContent(type, frame, config);
            windows.put(type, frame);

            frame.setVisible(true);
        } catch (HeadlessException ex) {
            JOptionPane.showMessageDialog(parent,
                    "Không thể mở " + type.getDefaultTitle() + ": " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Store NetworkInterface cho BRACKET window
    private NetworkInterface bracketNetworkInterface;

    /**
     * Phương thức thuận tiện cho Bracket window
     */
    public void openBracketWindow(DatabaseService service, JFrame parent, String tournamentTitle) {
        openBracketWindow(service, parent, tournamentTitle, null);
    }

    /**
     * Phương thức thuận tiện cho Bracket window với NetworkInterface
     */
    public void openBracketWindow(DatabaseService service, JFrame parent, String tournamentTitle,
            NetworkInterface nic) {
        // Lưu NetworkInterface để sử dụng khi tạo tabs
        this.bracketNetworkInterface = nic;
        WindowConfig config = new WindowConfig()
                .title(WindowType.BRACKET.getDefaultTitle() +
                        (tournamentTitle != null ? " - " + tournamentTitle : ""));
        openWindow(WindowType.BRACKET, service, parent, tournamentTitle, config);
    }

    /**
     * Phương thức thuận tiện cho Play window
     */
    public void openPlayWindow(DatabaseService service, JFrame parent, Role role,
            String tournamentTitle, NetworkInterface nic) {
        WindowConfig config = new WindowConfig()
                .title((role == Role.ADMIN ? "Thi đấu" : "Nhiều sân") +
                        (tournamentTitle != null ? " - " + tournamentTitle : ""))
                .content(() -> {
                    MultiCourtControlPanel panel = new MultiCourtControlPanel();
                    try {
                        panel.setConnection(service.current());
                        if (nic != null)
                            panel.setNetworkInterface(nic);
                    } catch (Exception ignored) {
                    }
                    return panel;
                });
        openWindow(WindowType.PLAY, service, parent, tournamentTitle, config);
    }

    /**
     * Thêm tab cho Bracket window
     */
    public void ensureBracketTab(DatabaseService service, int idNoiDung, String title, JFrame parent) {
        try {
            JFrame bracketFrame = windows.get(WindowType.BRACKET);
            JTabbedPane tabs = tabPanes.get(WindowType.BRACKET);

            if (bracketFrame == null || tabs == null)
                return;

            SoDoThiDauPanel existing = bracketPanelsByNoiDung.get(idNoiDung);
            if (existing != null) {
                // Focus existing tab
                focusExistingTab(tabs, existing);
            } else {
                // Create new tab
                createBracketTab(service, idNoiDung, title, tabs, parent);
            }

            bracketFrame.setVisible(true);
            bracketFrame.toFront();
            bracketFrame.requestFocus();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Không thể mở tab Sơ đồ: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JFrame createWindow(WindowType type, WindowConfig config, String tournamentTitle, JFrame parent) {
        JFrame frame = new JFrame();

        String title = config.title != null ? config.title
                : type.getDefaultTitle() + (tournamentTitle != null ? " - " + tournamentTitle : "");
        frame.setTitle(title);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        IconUtil.applyTo(frame);

        if (config.maximized) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        frame.setLocationRelativeTo(parent);
        frame.setAlwaysOnTop(config.alwaysOnTop);

        // Window cleanup listener
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                cleanup(type);
                if (config.onClose != null)
                    config.onClose.run();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                cleanup(type);
                if (config.onClose != null)
                    config.onClose.run();
            }
        });

        return frame;
    }

    private void setupWindowContent(WindowType type, JFrame frame, WindowConfig config) {
        if (type.supportsTabs()) {
            // Setup tabbed interface (for BRACKET)
            JTabbedPane tabs = new JTabbedPane();
            tabs.addChangeListener(e -> reloadActiveTab(tabs));
            tabPanes.put(type, tabs);
            frame.add(tabs, BorderLayout.CENTER);
        } else {
            // Setup single panel interface (for PLAY, MONITOR, etc.)
            if (config.contentSupplier != null) {
                JPanel content = config.contentSupplier.get();
                frame.add(content, BorderLayout.CENTER);
            }
        }
    }

    private void focusExistingTab(JTabbedPane tabs, Component existing) {
        int count = tabs.getTabCount();
        for (int i = 0; i < count; i++) {
            if (tabs.getComponentAt(i) == existing) {
                tabs.setSelectedIndex(i);
                break;
            }
        }
    }

    private void createBracketTab(DatabaseService service, int idNoiDung, String title,
            JTabbedPane tabs, JFrame parent) throws Exception {
        Connection conn = service.current();
        if (conn == null) {
            JOptionPane.showMessageDialog(parent, "Chưa có kết nối CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SoDoThiDauPanel panel = new SoDoThiDauPanel(conn);
        try {
            // Set NetworkInterface nếu có
            if (bracketNetworkInterface != null) {
                panel.setNetworkInterface(bracketNetworkInterface);
            }
            panel.selectNoiDungById(idNoiDung);
            panel.setNoiDungLabelMode(true);
            panel.reloadData();
        } catch (Throwable ignore) {
        }

        bracketPanelsByNoiDung.put(idNoiDung, panel);
        tabs.addTab(title, panel);

        int idx = tabs.indexOfComponent(panel);
        makeTabClosable(tabs, idx, title, () -> {
            bracketPanelsByNoiDung.remove(idNoiDung);
            int i = tabs.indexOfComponent(panel);
            if (i >= 0)
                tabs.removeTabAt(i);

            // Close window if no tabs remain
            if (tabs.getTabCount() == 0) {
                closeWindow(WindowType.BRACKET);
            }
        });

        tabs.setSelectedComponent(panel);
    }

    private void makeTabClosable(JTabbedPane tabs, int index, String title, Runnable onClose) {
        JPanel tabHeader = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
        tabHeader.setOpaque(false);

        javax.swing.JLabel lbl = new javax.swing.JLabel(title);
        javax.swing.JButton btn = new javax.swing.JButton("×");
        btn.setMargin(new java.awt.Insets(0, 4, 0, 4));
        btn.setBorder(javax.swing.BorderFactory.createEmptyBorder());
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

    private void reloadActiveTab(JTabbedPane tabs) {
        try {
            int sel = tabs.getSelectedIndex();
            if (sel >= 0) {
                Component c = tabs.getComponentAt(sel);
                if (c instanceof SoDoThiDauPanel p) {
                    p.reloadData();
                }
            }
        } catch (Exception ignore) {
        }
    }

    /**
     * Đóng window theo loại
     */
    public void closeWindow(WindowType type) {
        try {
            JFrame frame = windows.get(type);
            if (frame != null) {
                frame.dispose();
            }
        } catch (Exception ignore) {
        } finally {
            cleanup(type);
        }
    }

    /**
     * Làm mới tất cả tabs đang mở của một loại window
     */
    public void reloadOpenTabs(WindowType type) {
        try {
            JTabbedPane tabs = tabPanes.get(type);
            if (tabs == null)
                return;

            int count = tabs.getTabCount();
            for (int i = 0; i < count; i++) {
                Component c = tabs.getComponentAt(i);
                if (c instanceof SoDoThiDauPanel p) {
                    p.reloadData();
                }
            }
        } catch (Exception ignore) {
        }
    }

    /**
     * Kiểm tra window có đang mở không
     */
    public boolean isOpen(WindowType type) {
        return windows.get(type) != null;
    }

    /**
     * Reset tất cả windows
     */
    public void reset() {
        for (WindowType type : WindowType.values()) {
            closeWindow(type);
        }
    }

    /**
     * Lấy bracket panel theo ID nội dung
     */
    public SoDoThiDauPanel getBracketPanelByNoiDungId(Integer id) {
        return bracketPanelsByNoiDung.get(id);
    }

    private void cleanup(WindowType type) {
        windows.remove(type);
        tabPanes.remove(type);

        if (type == WindowType.BRACKET) {
            bracketPanelsByNoiDung.clear();
        }
    }
}