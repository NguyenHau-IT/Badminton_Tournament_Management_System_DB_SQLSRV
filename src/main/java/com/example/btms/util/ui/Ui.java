package com.example.btms.util.ui;

import javax.swing.*;
import java.awt.*;

public class Ui {
    public static void installModernUi() {
        try {
            Class<?> flat = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            flat.getMethod("setup").invoke(null);
            UIManager.put("Component.arc", 12);
            UIManager.put("Button.arc", 16);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("TitlePane.unifiedBackground", true);
        } catch (Throwable ignore) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {
            }
        }
        UIManager.put("defaultFont", new Font("SansSerif", Font.PLAIN, 13));
    }

    public static void placeholder(JTextField f, String text) {
        f.putClientProperty("JTextField.placeholderText", text);
    }
}
