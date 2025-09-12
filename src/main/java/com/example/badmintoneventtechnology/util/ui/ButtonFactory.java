package com.example.badmintoneventtechnology.util.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;

public final class ButtonFactory {
    private ButtonFactory() {
    }

    public static JButton filled(String text, Color bg, Color fg, Dimension size, Font font) {
        JButton b = new JButton(text);
        b.setUI(new BasicButtonUI());
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(font);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(8, 16, 8, 16));
        b.setPreferredSize(size);
        b.setMinimumSize(size);
        b.setBorder(new CompoundBorder(new LineBorder(bg.darker(), 1, true), new EmptyBorder(4, 12, 4, 12)));
        Color hover = mix(bg, Color.WHITE, 0.12f);
        Color normal = bg;
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (b.isEnabled())
                    b.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(normal);
            }
        });
        return b;
    }

    public static JButton outlined(String text, Color color, Dimension size, Font font) {
        JButton b = new JButton(text);
        b.setUI(new BasicButtonUI());
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setBackground(UIManager.getColor("Panel.background"));
        b.setForeground(color.darker());
        b.setFont(font);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(8, 16, 8, 16));
        b.setPreferredSize(size);
        b.setMinimumSize(size);
        b.setBorder(new CompoundBorder(new LineBorder(color, 2, true), new EmptyBorder(4, 12, 4, 12)));
        Color hover = mix(color, Color.WHITE, 0.85f);
        Color base = b.getBackground();
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (b.isEnabled())
                    b.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(base);
            }
        });
        return b;
    }

    private static Color mix(Color a, Color b, float t) {
        int r = Math.round(a.getRed() * (1 - t) + b.getRed() * t);
        int g = Math.round(a.getGreen() * (1 - t) + b.getGreen() * t);
        int bl = Math.round(a.getBlue() * (1 - t) + b.getBlue() * t);
        return new Color(r, g, bl);
    }
}