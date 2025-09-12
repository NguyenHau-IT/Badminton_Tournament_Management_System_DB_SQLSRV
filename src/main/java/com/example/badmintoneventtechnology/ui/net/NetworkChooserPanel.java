package com.example.badmintoneventtechnology.ui.net;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.extras.FlatSVGIcon;

/** Panel chọn Network Interface – hiện đại & gọn. */
public class NetworkChooserPanel extends JPanel {

    private final DefaultListModel<NifItem> model = new DefaultListModel<>();
    private final JList<NifItem> list = new JList<>(model);
    private final JTextField search = new JTextField(16);
    private final List<NifItem> allItems = new ArrayList<>();

    public NetworkChooserPanel() {
        super(new BorderLayout(12, 12));
        setOpaque(true);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        putClientProperty("FlatLaf.style", "background:@background");

        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("Network Interfaces");
        title.putClientProperty("FlatLaf.style", "font:+2; font.bold:true");

        // icon tiêu đề (tùy: có thể bỏ)
        try {
            title.setIcon(new FlatSVGIcon("icons/monitor.svg", 22, 22));
        } catch (Exception ignored) {
        }

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

        search.putClientProperty("JTextField.placeholderText", "Tìm theo tên…");
        search.putClientProperty("FlatLaf.style", "arc:12; margin:4,8,4,8; minimumWidth:220");
        search.addCaretListener(e -> applyFilter(search.getText().trim()));

        JButton btnClear = new JButton();
        btnClear.putClientProperty("JButton.buttonType", "toolBar");
        btnClear.putClientProperty("FlatLaf.style", "arc:12; margin:4,8,4,8;");
        btnClear.setToolTipText("Xóa ô tìm");
        try {
            btnClear.setIcon(new FlatSVGIcon("icons/x.svg", 18, 18));
        } catch (Exception ignored) {
        }
        btnClear.addActionListener(e -> {
            search.setText("");
            applyFilter("");
        });

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.putClientProperty("JButton.buttonType", "toolBar");
        btnRefresh.putClientProperty("FlatLaf.style", "arc:12; margin:4,12,4,12;");
        try {
            btnRefresh.setIcon(new FlatSVGIcon("icons/refresh.svg", 18, 18));
        } catch (Exception ignored) {
        }
        btnRefresh.addActionListener(e -> refreshInterfaces());

        right.add(search);
        right.add(btnClear);
        right.add(btnRefresh);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ===== List (renderer 2 dòng + badge) =====
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(12);
        list.setCellRenderer(new NifItemRenderer());
        JScrollPane sc = new JScrollPane(list);
        sc.putClientProperty("FlatLaf.style",
                "arc:16; borderWidth:1; borderColor:@Component.borderColor; background:derive(@background,3%);");
        add(sc, BorderLayout.CENTER);

        // data
        refreshInterfaces();
        if (!model.isEmpty())
            list.setSelectedIndex(0);
    }

    /** Trả về cấu hình chỉ với tên interface (name). */
    public NetworkConfig getSelectedConfig() {
        NifItem it = list.getSelectedValue();
        return (it == null) ? null : new NetworkConfig(it.name);
    }

    /* ---------------- load/filter ---------------- */

    private void refreshInterfaces() {
        allItems.clear();
        model.clear();
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            if (en != null) {
                while (en.hasMoreElements()) {
                    NetworkInterface nif = en.nextElement();
                    if (!nif.isUp() || nif.isLoopback() || nif.isVirtual())
                        continue;
                    NifItem item = new NifItem(nif);
                    allItems.add(item);
                }
            }
            Collections.sort(allItems, (a, b) -> a.displayName.compareToIgnoreCase(b.displayName));
            for (NifItem i : allItems)
                model.addElement(i);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không đọc được danh sách interface: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        applyFilter(search.getText().trim());
    }

    private void applyFilter(String q) {
        model.clear();
        if (q == null || q.isBlank()) {
            for (NifItem i : allItems)
                model.addElement(i);
        } else {
            String s = q.toLowerCase();
            for (NifItem i : allItems) {
                if (i.displayName.toLowerCase().contains(s) || i.name.toLowerCase().contains(s)) {
                    model.addElement(i);
                }
            }
        }
        if (!model.isEmpty())
            list.setSelectedIndex(0);
    }

    /* ---------------- model ---------------- */

    private static class NifItem {
        final String displayName; // tên dễ đọc
        final String name; // tên hệ thống (eth0, wlan0, ...)
        final int ipv4Count;
        final int ipv6Count;
        final String tooltip;

        NifItem(NetworkInterface nif) throws SocketException {
            this.displayName = String.valueOf(nif.getDisplayName());
            this.name = String.valueOf(nif.getName());
            int v4 = 0, v6 = 0;
            StringBuilder tip = new StringBuilder("<html><b>")
                    .append(displayName).append("</b> (").append(name).append(")<br/>");
            for (InterfaceAddress ia : nif.getInterfaceAddresses()) {
                if (ia == null || ia.getAddress() == null)
                    continue;
                String ip = ia.getAddress().getHostAddress();
                if (ip.contains(":"))
                    v6++;
                else
                    v4++;
                tip.append(ip).append("<br/>");
            }
            tip.append("</html>");
            this.ipv4Count = v4;
            this.ipv6Count = v6;
            this.tooltip = tip.toString();
        }

        @Override
        public String toString() {
            return displayName;
        }

        boolean isWifi() {
            String n = name.toLowerCase();
            return n.contains("wlan") || n.contains("wifi") || n.contains("wi-fi");
        }
    }

    /* ---------------- renderer ---------------- */

    private static class NifItemRenderer extends JPanel implements ListCellRenderer<NifItem> {
        private final JLabel lbIcon = new JLabel();
        private final JLabel lbTitle = new JLabel();
        private final JLabel lbSub = new JLabel();
        private final JLabel badge4 = new JLabel();
        private final JLabel badge6 = new JLabel();

        private final Icon icWifi;
        private final Icon icLan;

        NifItemRenderer() {
            super(new BorderLayout(10, 0));
            setOpaque(true);
            setBorder(new EmptyBorder(8, 10, 8, 10));
            putClientProperty("FlatLaf.style", "arc:14;");

            // preload icon
            Icon wifi = null, lan = null;
            try {
                wifi = new FlatSVGIcon("icons/wifi.svg", 20, 20);
            } catch (Exception ignored) {
            }
            try {
                lan = new FlatSVGIcon("icons/ethernet.svg", 20, 20);
            } catch (Exception ignored) {
            }
            icWifi = wifi;
            icLan = lan;

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            lbTitle.putClientProperty("FlatLaf.style", "font:+1; font.bold:true");
            lbSub.putClientProperty("FlatLaf.style", "foreground:@disabledText");

            center.add(lbTitle);
            center.add(lbSub);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            right.setOpaque(false);

            styleBadge(badge4);
            styleBadge(badge6);

            right.add(badge4);
            right.add(badge6);

            add(lbIcon, BorderLayout.WEST);
            add(center, BorderLayout.CENTER);
            add(right, BorderLayout.EAST);
        }

        private void styleBadge(JLabel b) {
            b.setOpaque(true);
            b.setBorder(new EmptyBorder(2, 8, 2, 8));
            b.putClientProperty("FlatLaf.style",
                    "arc:10; background:derive(@selectionBackground, -8%); foreground:@selectionForeground; font:-1;");
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends NifItem> list, NifItem value,
                int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null)
                return this;

            // icon
            lbIcon.setIcon(value.isWifi() ? icWifi : icLan);

            // text
            lbTitle.setText(value.displayName);
            lbSub.setText(value.name);

            // badges
            badge4.setText("IPv4 " + value.ipv4Count);
            badge6.setText("IPv6 " + value.ipv6Count);

            // tooltip
            setToolTipText(value.tooltip);

            // selection styles
            if (isSelected) {
                setBackground(UIManager.getColor("List.selectionBackground"));
                setForeground(UIManager.getColor("List.selectionForeground"));
            } else {
                setBackground(UIManager.getColor("List.background"));
                setForeground(UIManager.getColor("List.foreground"));
            }
            return this;
        }
    }
}
