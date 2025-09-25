package com.example.btms.util.sound;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

/**
 * Simple async WAV sound player for short match event cues.
 * Keys (Preferences):
 * - sound.enabled (boolean)
 * - sound.start.path (String absolute path to wav)
 * - sound.end.path (String absolute path to wav)
 */
public final class SoundPlayer {
    private static final Preferences PREFS = Preferences.userRoot().node("btms.sound");
    private static final String BUILTIN_PREFIX = "builtin:"; // stored as builtin:filename.wav
    private static final ExecutorService EXEC = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "sound-play");
        t.setDaemon(true);
        return t;
    });

    private SoundPlayer() {
    }

    public static void playStartIfEnabled() {
        if (!PREFS.getBoolean("sound.enabled", false))
            return;
        String path = PREFS.get("sound.start.path", "");
        if (path == null || path.isBlank())
            return;
        play(path);
    }

    public static void playEndIfEnabled() {
        if (!PREFS.getBoolean("sound.enabled", false))
            return;
        String path = PREFS.get("sound.end.path", "");
        if (path == null || path.isBlank())
            return;
        play(path);
    }

    public static void play(String path) {
        EXEC.submit(() -> doPlay(path));
    }

    private static void doPlay(String path) {
        if (path == null || path.isBlank())
            return;
        File f;
        if (path.startsWith(BUILTIN_PREFIX)) {
            String name = path.substring(BUILTIN_PREFIX.length());
            f = new File("sounds", name);
        } else {
            f = new File(path);
        }
        if (!f.exists() || !f.isFile())
            return;
        // Only support WAV to keep it simple.
        if (!f.getName().toLowerCase().endsWith(".wav"))
            return;
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(f)) {
            DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat());
            try (Clip clip = (Clip) AudioSystem.getLine(info)) {
                clip.open(ais);
                clip.start();
                // Let it play fully without blocking main thread
                while (clip.isRunning()) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
            // Swallow to avoid disturbing main UI.
        }
    }
}
