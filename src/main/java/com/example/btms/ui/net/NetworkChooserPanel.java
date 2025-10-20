package com.example.btms.ui.net;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.example.btms.config.NetworkConfig;
import com.example.btms.model.net.NifItem;
import com.formdev.flatlaf.extras.FlatSVGIcon;

/**
 * Panel chọn Network Interface – phiên bản gọn: dùng JComboBox thay vì JList
 */
public class NetworkChooserPanel extends JPanel {

    private final DefaultComboBoxModel<NifItem> model = new DefaultComboBoxModel<>();
    private final JComboBox<NifItem> combo = new JComboBox<>(model);
    private final JTextField search = new JTextField(14);
    private final List<NifItem> allItems = new ArrayList<>();

    public NetworkChooserPanel() {
        super(new BorderLayout(10, 10));
        setOpaque(true);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        putClientProperty("FlatLaf.style", "background:@background");

        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout(6, 0));
        header.setOpaque(false);
        JLabel title = new JLabel("Network Interface");
        title.putClientProperty("FlatLaf.style", "font: bold +1");
        try {
            title.setIcon(new FlatSVGIcon("icons/monitor.svg", 20, 20));
        } catch (Exception ignore) {
        }
        header.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        right.setOpaque(false);
        search.putClientProperty("JTextField.placeholderText", "Lọc…");
        search.addCaretListener(e -> applyFilter(search.getText().trim()));
        JButton btnClear = new JButton();
        btnClear.putClientProperty("JButton.buttonType", "toolBar");
        btnClear.setToolTipText("Xóa lọc");
        try {
            btnClear.setIcon(new FlatSVGIcon("icons/x.svg", 16, 16));
        } catch (Exception ignore) {
        }
        btnClear.addActionListener(e -> {
            search.setText("");
            applyFilter("");
        });

        JButton btnRefresh = new JButton();
        btnRefresh.putClientProperty("JButton.buttonType", "toolBar");
        btnRefresh.setToolTipText("Tải lại");
        try {
            btnRefresh.setIcon(new FlatSVGIcon("icons/refresh.svg", 16, 16));
        } catch (Exception ignore) {
        }
        btnRefresh.addActionListener(e -> refreshInterfaces());

        right.add(search);
        right.add(btnClear);
        right.add(btnRefresh);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ===== ComboBox =====
        combo.setRenderer(new ComboRenderer());
        combo.setMaximumRowCount(14);
        add(combo, BorderLayout.CENTER);

        refreshInterfaces();
        if (model.getSize() > 0)
            combo.setSelectedIndex(0);
    }

    public NetworkConfig getSelectedConfig() {
        Object sel = combo.getSelectedItem();
        return (sel instanceof NifItem ni) ? new NetworkConfig(ni.getName(), ni.getIpv4Address()) : null;
    }

    private void refreshInterfaces() {
        allItems.clear();
        model.removeAllElements();
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            if (en != null) {
                while (en.hasMoreElements()) {
                    NetworkInterface nif = en.nextElement();
                    if (!nif.isUp() || nif.isLoopback() || nif.isVirtual())
                        continue;
                    allItems.add(new NifItem(nif));
                }
            }
            Collections.sort(allItems,
                    (a, b) -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName()));
            for (NifItem i : allItems)
                model.addElement(i);
        } catch (SocketException | SecurityException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không đọc được danh sách interface: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        applyFilter(search.getText().trim());
    }

    private void applyFilter(String q) {
        model.removeAllElements();
        if (q == null || q.isBlank()) {
            for (NifItem i : allItems)
                model.addElement(i);
        } else {
            String s = q.toLowerCase();
            for (NifItem i : allItems) {
                if (i.getDisplayName().toLowerCase().contains(s)
                        || i.getName().toLowerCase().contains(s)) {
                    model.addElement(i);
                }
            }
        }
        if (model.getSize() > 0)
            combo.setSelectedIndex(0);
    }

    /* ============== Renderer cho JComboBox ============== */
    private static class ComboRenderer extends JPanel implements ListCellRenderer<NifItem> {
        private final JLabel lbIcon = new JLabel();
        private final JLabel lbTitle = new JLabel();
        private final JLabel lbSub = new JLabel();
        private final JLabel lbIpv4 = new JLabel();
        private final JLabel badge = new JLabel();
        private final Icon icWifi;
        private final Icon icLan;

        ComboRenderer() {
            super(new BorderLayout(8, 0));
            setOpaque(true);
            setBorder(new EmptyBorder(6, 8, 6, 8));
            putClientProperty("FlatLaf.style", "arc:14;");
            Icon wifi = null, lan = null;
            try {
                wifi = new FlatSVGIcon("icons/wifi.svg", 25, 25);
            } catch (Exception ignore) {
            }
            try {
                lan = new FlatSVGIcon("icons/ethernet.svg", 25, 25);
            } catch (Exception ignore) {
            }
            icWifi = wifi;
            icLan = lan;

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new javax.swing.BoxLayout(center, javax.swing.BoxLayout.Y_AXIS));
            lbTitle.putClientProperty("FlatLaf.style", "font: bold +3");
            lbIpv4.putClientProperty("FlatLaf.style", "font: bold +2");

            // Thiết lập màu xám cho IPv4 label
            Color grayColor = UIManager.getColor("Label.disabledForeground");
            if (grayColor == null) {
                grayColor = Color.GRAY;
            }
            lbIpv4.setForeground(grayColor);

            center.add(lbTitle);
            center.add(lbIpv4);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            right.setOpaque(false);
            styleBadge(badge);
            right.add(badge);

            add(lbIcon, BorderLayout.WEST);
            add(center, BorderLayout.CENTER);
            add(right, BorderLayout.EAST);
        }

        private void styleBadge(JLabel b) {
            b.setOpaque(true);
            b.setBorder(new EmptyBorder(2, 6, 2, 6));
            b.putClientProperty("JComponent.roundRect", Boolean.TRUE);
            b.setBackground(UIManager.getColor("Component.accentColor"));
            if (b.getBackground() == null)
                b.setBackground(UIManager.getColor("List.selectionBackground"));
            b.setForeground(UIManager.getColor("Label.foreground"));
            b.setFont(b.getFont().deriveFont(b.getFont().getSize2D() - 1f));
        }

        @Override
        public Component getListCellRendererComponent(
                javax.swing.JList<? extends NifItem> list, NifItem value,
                int index, boolean isSelected, boolean cellHasFocus) {

            if (value == null)
                return this;
            lbIcon.setIcon(value.isWifi() ? icWifi : icLan);
            lbTitle.setText(value.getDisplayName());
            lbIpv4.setText(value.getIpv4Address() != null ? value.getIpv4Address() : "No IPv4");
            lbSub.setText(value.getName());
            badge.setText("IPv4 " + value.getIpv4Count() + "/v6 " + value.getIpv6Count());
            setToolTipText(value.getTooltip());

            Color bgSel = UIManager.getColor("List.selectionBackground");
            Color fgSel = UIManager.getColor("List.selectionForeground");
            setBackground(isSelected ? bgSel : UIManager.getColor("List.background"));
            setForeground(isSelected ? fgSel : UIManager.getColor("List.foreground"));
            return this;
        }
    }
}
