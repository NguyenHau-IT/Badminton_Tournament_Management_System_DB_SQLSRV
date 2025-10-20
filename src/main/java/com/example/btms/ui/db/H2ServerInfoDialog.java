package com.example.btms.ui.db;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.example.btms.config.H2TcpServerConfig;
import com.example.btms.util.ui.IconUtil;
import com.formdev.flatlaf.FlatClientProperties;

/**
 * Dialog hiển thị thông tin H2 TCP Server
 */
public class H2ServerInfoDialog extends JDialog {
    private final H2ServerInfoPanel infoPanel;

    public H2ServerInfoDialog(Window parent, H2TcpServerConfig h2ServerConfig) {
        super(parent, "H2 Database TCP Server Info", ModalityType.MODELESS);
        this.infoPanel = new H2ServerInfoPanel(h2ServerConfig);

        initDialog();
        setupLayout();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        // Apply app icons
        IconUtil.applyTo(this);

        // Set minimum size
        setMinimumSize(new Dimension(500, 400));
    }

    private void setupLayout() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        rootPanel.putClientProperty(FlatClientProperties.STYLE, "background: @background");

        rootPanel.add(infoPanel, BorderLayout.CENTER);

        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        closeButton.putClientProperty("JButton.buttonType", "default");

        buttonPanel.add(closeButton);
        rootPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    /**
     * Cập nhật thông tin server
     */
    public void refreshInfo() {
        infoPanel.updateInfo();
    }
}