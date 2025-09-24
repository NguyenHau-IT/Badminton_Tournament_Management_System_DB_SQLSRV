package com.example.btms.util.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Quản lý theme và styling cho toàn bộ ứng dụng
 */
public class ThemeManager {

    // Color palette
    public static final Color PRIMARY = new Color(0, 123, 255);
    public static final Color PRIMARY_DARK = new Color(0, 105, 217);
    public static final Color SUCCESS = new Color(40, 167, 69);
    public static final Color WARNING = new Color(255, 193, 7);
    public static final Color DANGER = new Color(220, 53, 69);
    public static final Color INFO = new Color(23, 162, 184);

    public static final Color LIGHT_BG = new Color(248, 249, 250);
    public static final Color LIGHT_BORDER = new Color(222, 226, 230);
    public static final Color DARK_TEXT = new Color(33, 37, 41);
    public static final Color MUTED_TEXT = new Color(108, 117, 125);

    // Fonts
    private static Font primaryFont;
    private static Font secondaryFont;

    static {
        initializeFonts();
        setupUIManagerDefaults();
    }

    private static void initializeFonts() {
        // Try to use modern fonts if available
        String[] preferredFonts = {
                "Segoe UI", "SF Pro Display", "Roboto", "Open Sans",
                "Helvetica Neue", "Arial", "SansSerif"
        };

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();

        for (String fontName : preferredFonts) {
            for (String available : availableFonts) {
                if (available.equalsIgnoreCase(fontName)) {
                    primaryFont = new Font(fontName, Font.PLAIN, 14);
                    secondaryFont = new Font(fontName, Font.PLAIN, 12);
                    return;
                }
            }
        }

        // Fallback to system default
        primaryFont = new Font("Dialog", Font.PLAIN, 14);
        secondaryFont = new Font("Dialog", Font.PLAIN, 12);
    }

    private static void setupUIManagerDefaults() {
        // Set default colors
        UIManager.put("Panel.background", LIGHT_BG);
        UIManager.put("Button.background", PRIMARY);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", primaryFont.deriveFont(Font.BOLD, 13f));
        UIManager.put("Label.font", primaryFont);
        UIManager.put("TextField.font", primaryFont);
        UIManager.put("TextArea.font", new Font("JetBrains Mono", Font.PLAIN, 13));
    }

    // Button styling
    public static void stylePrimaryButton(JButton button) {
        styleButton(button, PRIMARY, Color.WHITE);
    }

    public static void styleSuccessButton(JButton button) {
        styleButton(button, SUCCESS, Color.WHITE);
    }

    public static void styleDangerButton(JButton button) {
        styleButton(button, DANGER, Color.WHITE);
    }

    public static void styleWarningButton(JButton button) {
        styleButton(button, WARNING, DARK_TEXT);
    }

    public static void styleInfoButton(JButton button) {
        styleButton(button, INFO, Color.WHITE);
    }

    public static void styleSecondaryButton(JButton button) {
        styleButton(button, LIGHT_BG, DARK_TEXT);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
    }

    private static void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFont(primaryFont.deriveFont(Font.BOLD, 13f));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    // Toggle button styling
    public static void styleToggleButton(JToggleButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFont(primaryFont.deriveFont(Font.BOLD, 12f));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    // Panel styling
    public static void styleCardPanel(JPanel panel) {
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
    }

    public static void styleHeaderPanel(JPanel panel) {
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LIGHT_BORDER),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));
    }

    // Label styling
    public static void styleTitleLabel(JLabel label) {
        label.setFont(primaryFont.deriveFont(Font.BOLD, 20f));
        label.setForeground(DARK_TEXT);
    }

    public static void styleSubtitleLabel(JLabel label) {
        label.setFont(primaryFont.deriveFont(Font.BOLD, 16f));
        label.setForeground(MUTED_TEXT);
    }

    public static void styleCaptionLabel(JLabel label) {
        label.setFont(secondaryFont.deriveFont(Font.PLAIN, 11f));
        label.setForeground(MUTED_TEXT);
    }

    // Text field styling
    public static void styleTextField(JTextField field) {
        field.setFont(primaryFont);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setBackground(Color.WHITE);
        field.setForeground(DARK_TEXT);
    }

    // Utility methods
    public static Font getPrimaryFont() {
        return primaryFont;
    }

    public static Font getSecondaryFont() {
        return secondaryFont;
    }

    public static Border getStandardBorder() {
        return BorderFactory.createLineBorder(LIGHT_BORDER, 1);
    }

    public static Border getRoundedBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }
}
