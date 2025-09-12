package com.example.badmintoneventtechnology.ui.dashboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.example.badmintoneventtechnology.service.scoreboard.ScoreboardHub;
import com.example.badmintoneventtechnology.ui.scoreboard.MiniScorePanel;

public class HomeDashboardPanel extends JPanel {

    private final JPanel grid = new JPanel();
    private final JScrollPane scroll;

    // Kích thước “card” (để tính số cột tự động)
    private static final int CARD_W = 520;
    private static final int CARD_H = 220;
    private static final int GAP = 12;

    public HomeDashboardPanel() {
        super(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        grid.setOpaque(false);
        grid.setLayout(new GridLayout(0, 1, GAP, GAP)); // khởi tạo 1 cột, sẽ tự đổi theo resize

        scroll = new JScrollPane(grid,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        // Lắng nghe thay đổi của Hub -> refresh lưới
        ScoreboardHub.get().addListener(this::refreshGrid);

        // Tự tính số cột theo độ rộng viewport
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recomputeColumns();
            }
        });

        // vẽ lần đầu
        refreshGrid();
    }

    private void recomputeColumns() {
        int w = scroll.getViewport().getWidth();
        if (w <= 0)
            return;
        int cols = Math.max(1, (w - GAP) / (CARD_W + GAP));
        ((GridLayout) grid.getLayout()).setColumns(cols);
        grid.revalidate();
    }

    private void refreshGrid() {
        List<ScoreboardHub.Board> boards = ScoreboardHub.get().snapshot();
        grid.removeAll();

        if (boards.isEmpty()) {
            grid.add(emptyHint());
        } else {
            for (ScoreboardHub.Board b : boards) {
                grid.add(makeCard(b));
            }
        }

        grid.revalidate();
        grid.repaint();
        recomputeColumns();
    }

    private JPanel emptyHint() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel lb = new JLabel("Chưa có bảng điểm nào đang hoạt động", SwingConstants.CENTER);
        lb.setForeground(new Color(120, 120, 120));
        lb.setBorder(new EmptyBorder(40, 0, 40, 0));
        p.add(lb, BorderLayout.CENTER);
        p.setOpaque(false);
        return p;
    }

    private JPanel makeCard(ScoreboardHub.Board b) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(8, 10, 10, 10)));
        card.setPreferredSize(new Dimension(CARD_W, CARD_H));

        // Header của card
        String title = (b.header == null || b.header.isBlank()) ? "TRẬN ĐẤU" : b.header;
        JLabel head = new JLabel(title);
        head.setBorder(new EmptyBorder(0, 2, 6, 2));
        head.setFont(head.getFont().deriveFont(16f));
        card.add(head, BorderLayout.NORTH);

        // Nội dung chính: MiniScorePanel gắn trực tiếp vào match (live)
        MiniScorePanel mini = new MiniScorePanel(b.match);
        mini.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        mini.setHeader(title);

        // Scale nhẹ (MiniScorePanel tự co theo layout, nên chỉ cần khung rộng là OK)
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(mini, BorderLayout.CENTER);

        card.add(wrap, BorderLayout.CENTER);

        return card;
    }
}
