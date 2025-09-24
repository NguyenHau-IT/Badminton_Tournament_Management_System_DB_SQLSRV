package com.example.btms.util.ui;

import javax.swing.*;
import java.awt.*;

public final class FullscreenHelper {
    private FullscreenHelper() {
    }

    public static void enterFullscreenOnScreen(JFrame frame, int screenIndex) {
        GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        if (screens == null || screens.length == 0) {
            if (frame.isDisplayable())
                frame.dispose();
            frame.setUndecorated(true);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
            frame.toFront();
            return;
        }
        if (screenIndex < 0 || screenIndex >= screens.length)
            screenIndex = 0;
        GraphicsDevice gd = screens[screenIndex];
        if (frame.isDisplayable())
            frame.dispose();
        frame.setUndecorated(true);
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(frame);
            frame.validate();
            frame.setVisible(true);
        } else {
            Rectangle b = gd.getDefaultConfiguration().getBounds();
            frame.setBounds(b);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
            frame.toFront();
        }
    }

    public static void exitFullscreen(JFrame frame) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice gd : ge.getScreenDevices()) {
            if (gd.getFullScreenWindow() == frame)
                gd.setFullScreenWindow(null);
        }
        if (frame.isDisplayable())
            frame.dispose();
        frame.setUndecorated(false);
        frame.setExtendedState(JFrame.NORMAL);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static boolean isFullscreen(JFrame frame) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice gd : ge.getScreenDevices())
            if (gd.getFullScreenWindow() == frame)
                return true;
        return false;
    }
}