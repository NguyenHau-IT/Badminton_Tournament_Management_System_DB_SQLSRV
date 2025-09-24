package com.example.btms.util.net;

import java.net.*;
import java.util.*;

public final class NetworkUtil {
    private NetworkUtil() {
    }

    /**
     * Return the first non-loopback IPv4 address from the preferred interface ONLY.
     * Không fallback về interface khác để đảm bảo IP web scoreboard khớp với IP máy
     * chủ.
     */
    public static String getLocalIpv4(NetworkInterface preferred) {
        if (preferred != null) {
            try {
                String ip = firstIpv4(preferred);
                if (ip != null) {
                    return ip;
                }
                // Nếu interface được chọn không có IPv4, log warning và return null
                System.err.println("WARNING: Interface '" + preferred.getDisplayName() +
                        "' không có IPv4 address. Cần chọn interface khác.");
            } catch (Exception e) {
                System.err.println("ERROR: Không thể đọc thông tin interface: " + e.getMessage());
            }
        }
        return null; // Không fallback, trả về null để caller xử lý
    }

    private static String firstIpv4(NetworkInterface nif) {
        Enumeration<InetAddress> addrs = nif.getInetAddresses();
        while (addrs.hasMoreElements()) {
            InetAddress a = addrs.nextElement();
            if (!a.isLoopbackAddress() && a instanceof Inet4Address) {
                return a.getHostAddress();
            }
        }
        return null;
    }
}