package com.example.btms.util.sound;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Scans the root "sounds" directory (next to the application working dir) for
 * wav files.
 * Provides a cached list of available built-in sound filenames.
 */
public final class SoundLibrary {
    private static volatile List<String> cached; // filenames only
    private static final Object LOCK = new Object();

    private SoundLibrary() {
    }

    public static List<String> listBuiltInWavFiles() {
        List<String> local = cached;
        if (local != null)
            return local;
        synchronized (LOCK) {
            if (cached == null) {
                cached = scan();
            }
            return cached;
        }
    }

    public static void refresh() {
        synchronized (LOCK) {
            cached = scan();
        }
    }

    private static List<String> scan() {
        File dir = new File("sounds");
        if (!dir.exists() || !dir.isDirectory())
            return Collections.emptyList();
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".wav"));
        if (files == null || files.length == 0)
            return Collections.emptyList();
        List<String> list = new ArrayList<>();
        for (File f : files) {
            if (f.isFile())
                list.add(f.getName());
        }
        Collections.sort(list);
        return list;
    }
}
