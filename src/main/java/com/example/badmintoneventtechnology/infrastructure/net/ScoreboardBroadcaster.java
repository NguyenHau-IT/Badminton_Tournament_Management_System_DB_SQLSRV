package com.example.badmintoneventtechnology.infrastructure.net;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.example.badmintoneventtechnology.model.match.BadmintonMatch;

/** Phát (multicast) trạng thái trận đấu ra LAN; Admin sẽ nghe để giám sát. */
public class ScoreboardBroadcaster implements PropertyChangeListener, AutoCloseable {
    private static final String GROUP = "239.255.50.50";
    private static final int PORT = 50505;

    private final String sid = UUID.randomUUID().toString();
    private final BadmintonMatch match;
    private final String clientName;
    private final String hostShown;
    private final String displayKind; // "VERTICAL"/"HORIZONTAL"
    private final String courtId; // Thêm courtId để phân biệt các sân
    @SuppressWarnings("unused")
    private final NetworkInterface nif; // có thể dùng để route, tuỳ OS/JVM

    private volatile String header = "TRẬN ĐẤU";
    private volatile boolean doubles = false;
    private volatile String nameA = "";
    private volatile String nameB = "";

    private DatagramSocket socket;
    private Timer keepAlive;

    public ScoreboardBroadcaster(BadmintonMatch match,
            NetworkInterface nif,
            String clientName,
            String hostShown,
            String displayKind,
            String courtId) {
        this.match = match;
        this.nif = nif;
        this.clientName = clientName == null ? "client" : clientName;
        this.hostShown = hostShown == null ? "" : hostShown;
        this.displayKind = displayKind == null ? "HORIZONTAL" : displayKind;
        this.courtId = courtId == null ? "" : courtId;
    }

    public void setMeta(String header, boolean doubles, String nameA, String nameB) {
        this.header = (header == null || header.isBlank()) ? "TRẬN ĐẤU" : header;
        this.doubles = doubles;
        this.nameA = nameA == null ? "" : nameA;
        this.nameB = nameB == null ? "" : nameB;
        sendOnce();
    }

    public void start() {
        try {
            if (socket == null)
                socket = new DatagramSocket();
        } catch (Exception ignore) {
        }
        match.addPropertyChangeListener(this);
        if (keepAlive == null) {
            keepAlive = new Timer("scoreboard-keepalive", true);
            keepAlive.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    sendOnce();
                }
            }, 0, 1000); // 1s/lần
        }
    }

    public void stop() {
        match.removePropertyChangeListener(this);
        if (keepAlive != null) {
            keepAlive.cancel();
            keepAlive = null;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception ignore) {
            }
            socket = null;
        }
        sendDelete();
    }

    @Override
    public void close() {
        stop();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        sendOnce();
    }

    private void sendOnce() {
        BadmintonMatch.Snapshot s = match.snapshot();

        // Tạo chuỗi điểm các ván đã hoàn thành
        StringBuilder gameScoresStr = new StringBuilder();
        if (s.gameScores != null && s.gameScores.length > 0) {
            for (int i = 0; i < s.gameScores.length; i++) {
                if (s.gameScores[i][0] >= 0 && s.gameScores[i][1] >= 0) {
                    if (gameScoresStr.length() > 0) {
                        gameScoresStr.append(",");
                    }
                    gameScoresStr.append(s.gameScores[i][0]).append(":").append(s.gameScores[i][1]);
                }
            }
        }

        String json = buildJson("UPSERT",
                "sid", sid,
                "client", clientName,
                "host", hostShown,
                "courtId", courtId, // Thêm courtId vào broadcast
                "header", header,
                "kind", displayKind,
                "doubles", String.valueOf(doubles),
                "nameA", nameA,
                "nameB", nameB,
                "game", String.valueOf(s.gameNumber),
                "bestOf", String.valueOf(s.bestOf),
                "scoreA", String.valueOf(s.score[0]),
                "scoreB", String.valueOf(s.score[1]),
                "gamesA", String.valueOf(s.games[0]),
                "gamesB", String.valueOf(s.games[1]),
                "gameScores", gameScoresStr.toString(), // Thêm điểm các ván đã hoàn thành
                "ts", String.valueOf(Instant.now().toEpochMilli()));
        sendPacket(json);
    }

    private void sendDelete() {
        String json = buildJson("DELETE",
                "sid", sid,
                "client", clientName,
                "host", hostShown,
                "courtId", courtId, // Thêm courtId vào DELETE broadcast
                "ts", String.valueOf(Instant.now().toEpochMilli()));
        sendPacket(json);
    }

    private void sendPacket(String json) {
        try {
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            InetAddress group = InetAddress.getByName(GROUP);
            DatagramPacket p = new DatagramPacket(data, data.length, group, PORT);
            if (socket == null)
                socket = new DatagramSocket();
            socket.send(p);
        } catch (Exception ignore) {
        }
    }

    private static String esc(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String buildJson(String op, String... kv) {
        StringBuilder sb = new StringBuilder(256);
        sb.append('{').append("\"op\":\"").append(op).append("\"");
        for (int i = 0; i + 1 < kv.length; i += 2) {
            String k = kv[i], v = kv[i + 1];
            if (k == null)
                continue;
            boolean isNum = switch (k) {
                case "doubles", "game", "bestOf", "scoreA", "scoreB", "gamesA", "gamesB", "ts" -> true;
                default -> false;
            };
            sb.append(",\"").append(esc(k)).append("\":");
            if (isNum)
                sb.append(v);
            else
                sb.append("\"").append(esc(v)).append("\"");
        }
        sb.append('}');
        return sb.toString();
    }
}
