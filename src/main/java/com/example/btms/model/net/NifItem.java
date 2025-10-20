package com.example.btms.model.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public final class NifItem {
    private final String displayName;
    private final String name;
    private final String ipv4Address;
    private final int ipv4Count;
    private final int ipv6Count;
    private final String tooltip;

    public NifItem(NetworkInterface nif) throws SocketException {
        Objects.requireNonNull(nif, "NetworkInterface must not be null");

        this.displayName = nullToEmpty(nif.getDisplayName());
        this.name = nullToEmpty(nif.getName());

        Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
        String ipv4Addr = "";
        if (inetAddresses.hasMoreElements()) {
            ipv4Addr = inetAddresses.nextElement().getHostAddress();
        }
        this.ipv4Address = nullToEmpty(ipv4Addr);

        int v4 = 0, v6 = 0;
        StringBuilder tip = new StringBuilder(128);
        tip.append("<html>")
                .append("<b>").append(escape(displayName)).append("</b>")
                .append(" (").append(escape(name)).append(")");

        try {
            tip.append("<br/>Up: ").append(nif.isUp());
        } catch (SocketException ignored) {
        }
        try {
            tip.append(" | Loopback: ").append(nif.isLoopback());
        } catch (SocketException ignored) {
        }
        tip.append(" | Virtual: ").append(nif.isVirtual());
        try {
            tip.append(" | Multicast: ").append(nif.supportsMulticast());
        } catch (SocketException ignored) {
        }

        tip.append("<br/>");

        List<InterfaceAddress> addrs = nif.getInterfaceAddresses();
        if (addrs != null) {
            for (InterfaceAddress ia : addrs) {
                if (ia == null || ia.getAddress() == null)
                    continue;

                if (ia.getAddress() instanceof Inet4Address)
                    v4++;
                else if (ia.getAddress() instanceof Inet6Address)
                    v6++;

                String ip = ia.getAddress().getHostAddress();
                Integer prefix = ia.getNetworkPrefixLength() >= 0 ? (int) ia.getNetworkPrefixLength() : null;
                if (prefix != null)
                    tip.append(escape(ip)).append("/").append(prefix).append("<br/>");
                else
                    tip.append(escape(ip)).append("<br/>");
            }
        }
        tip.append("</html>");

        this.ipv4Count = v4;
        this.ipv6Count = v6;
        this.tooltip = tip.toString();
    }

    public boolean isWifi() {
        String n = name.toLowerCase();
        String d = displayName.toLowerCase();
        return n.contains("wlan") || n.contains("wifi") || n.contains("wi-fi")
                || d.contains("wlan") || d.contains("wifi") || d.contains("wi-fi");
    }

    @Override
    public String toString() {
        return getDisplayName().isBlank() ? getName() : getDisplayName();
    }

    // getters
    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return name;
    }

    public int getIpv4Count() {
        return ipv4Count;
    }

    public int getIpv6Count() {
        return ipv6Count;
    }

    public String getIpv4Address() {
        return ipv4Address;
    }

    public String setIpv4Address() {
        return ipv4Address;
    }

    public String getTooltip() {
        return tooltip;
    }

    // helpers
    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String escape(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}