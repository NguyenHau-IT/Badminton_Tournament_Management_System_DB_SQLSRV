package com.example.btms.config;

import java.util.prefs.Preferences;

public class Prefs {
    private final Preferences p = Preferences.userRoot().node("demo.h2client");

    public String get(String key, String def) {
        return p.get(key, def);
    }

    public void put(String key, String value) {
        p.put(key, value);
    }

    public boolean getBool(String key, boolean def) {
        return p.getBoolean(key, def);
    }

    public void putBool(String key, boolean v) {
        p.putBoolean(key, v);
    }

    public int getInt(String key, int def) {
        return p.getInt(key, def);
    }

    public void putInt(String key, int v) {
        p.putInt(key, v);
    }

    public void remove(String key) {
        p.remove(key);
    }
}
