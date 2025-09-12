package com.example.badmintoneventtechnology.util.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class HashUtil {
    private HashUtil() {
    }

    /** MD5 hex từ mật khẩu (char[]). Tự xoá buffer tạm. */
    public static String md5Hex(char[] password) {
        try {
            byte[] data = new String(password).getBytes(StandardCharsets.UTF_8);
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] dig = md.digest(data);
                StringBuilder sb = new StringBuilder(dig.length * 2);
                for (byte b : dig)
                    sb.append(String.format("%02x", b));
                return sb.toString();
            } finally {
                java.util.Arrays.fill(data, (byte) 0);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
