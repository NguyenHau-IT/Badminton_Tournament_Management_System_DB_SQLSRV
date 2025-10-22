package com.example.btms.ui.result;

import java.awt.BorderLayout;
import java.sql.Connection;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import com.example.btms.util.ui.IconUtil;

/**
 * Cửa sổ riêng hiển thị Trang biên bản.
 */
public class BienBanFrame extends JFrame {

    public BienBanFrame(Connection conn) {
        super("Trang biên bản");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        BienBanPanel panel = new BienBanPanel(conn);
        // Bọc trong scroll nếu nội dung dài
        JScrollPane sp = new JScrollPane(panel);
        sp.setBorder(null);
        add(sp, BorderLayout.CENTER);

        // Mặc định mở toàn màn hình
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        try {
            IconUtil.applyTo(this);
        } catch (Exception ignore) {
        }
    }
}
