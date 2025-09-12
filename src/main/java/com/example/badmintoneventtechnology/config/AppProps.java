package com.example.badmintoneventtechnology.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppProps {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppProps.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException ignored) {
        }
        // Allow environment/system properties to override
        Properties sys = System.getProperties();
        for (String name : sys.stringPropertyNames()) {
            if (name.startsWith("db.")) {
                PROPS.setProperty(name, sys.getProperty(name));
            }
        }
        for (Object key : System.getenv().keySet()) {
            String k = String.valueOf(key);
            if (k.startsWith("DB_")) {
                String propKey = "db." + k.substring(3).toLowerCase().replace('_', '.');
                PROPS.setProperty(propKey, System.getenv(k));
            }
        }
    }

    public static String get(String key, String def) {
        return PROPS.getProperty(key, def);
    }

    public static boolean getBool(String key, boolean def) {
        String v = PROPS.getProperty(key);
        if (v == null)
            return def;
        return v.equalsIgnoreCase("true") || v.equalsIgnoreCase("yes") || v.equals("1");
    }
}


