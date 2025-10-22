package com.example.btms.ui.welcome;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Simple welcome screen for MATCH mode.
 */
public class MatchWelcomePanel extends JPanel {

    private final JLabel title = new JLabel("Chào mừng (MATCH)", SwingConstants.CENTER);
    private final JLabel subtitle = new JLabel("Chưa chọn giải", SwingConstants.CENTER);
    private final JLabel hint = new JLabel(
            "Dùng menu Thi đấu → Giám sát hoặc Quản lý → Sơ đồ thi đấu/Kết quả để theo dõi.", SwingConstants.CENTER);

    public MatchWelcomePanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 16f));
        subtitle.setForeground(new Color(90, 90, 90));
        hint.setForeground(new Color(110, 110, 110));

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(40, 16, 40, 16));
        center.add(title, BorderLayout.NORTH);
        center.add(subtitle, BorderLayout.CENTER);
        center.add(hint, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);
    }

    public void setTournamentName(String name) {
        String text = (name == null || name.isBlank()) ? "Chưa chọn giải" : ("Giải đấu: " + name);
        subtitle.setText(text);
    }
}
