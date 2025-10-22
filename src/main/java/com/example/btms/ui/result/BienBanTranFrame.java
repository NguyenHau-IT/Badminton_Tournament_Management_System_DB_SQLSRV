package com.example.btms.ui.result;

import java.awt.BorderLayout;
import java.sql.Connection;

import javax.swing.JFrame;

import com.example.btms.util.ui.IconUtil;

/**
 * Cửa sổ riêng cho biên bản của một trận.
 */
public class BienBanTranFrame extends JFrame {

    private final BienBanTranPanel panel;

    public BienBanTranFrame(Connection conn, String matchId) {
        super("Biên bản trận");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        this.panel = new BienBanTranPanel(conn, matchId);
        add(panel, BorderLayout.CENTER);

        setSize(900, 650);
        setLocationByPlatform(true);
        try {
            IconUtil.applyTo(this);
        } catch (Exception ignore) {
        }
    }

    public void setMatchId(String matchId) {
        panel.setMatchId(matchId);
    }
}
