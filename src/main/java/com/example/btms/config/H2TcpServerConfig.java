package com.example.btms.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

/**
 * H2 TCP Server Configuration cho phép máy khác kết nối đến H2 thông qua TCP.
 * 
 * Lưu ý: H2 TCP Server sẽ bind tất cả interfaces (0.0.0.0) vì tham số
 * -tcpListenAddress không được hỗ trợ trong phiên bản hiện tại.
 * 
 * - serverIP chỉ dùng để tạo connection URL cho client
 * - Server thực tế bind 0.0.0.0 và listen trên tất cả network interfaces
 * - Client có thể kết nối qua bất kỳ IP nào của máy server
 * - Sử dụng -tcpAllowOthers để cho phép remote connections
 */
@Component
public class H2TcpServerConfig {

    private static final Logger log = LoggerFactory.getLogger(H2TcpServerConfig.class);

    private volatile Server tcpServer;
    private volatile boolean isServerStarted = false;

    private String serverIP = "127.0.0.1";
    private int serverPort = 9092; // Default H2 TCP port
    private String baseDirAbsolute = Paths.get("./database").toAbsolutePath().toString();
    private String defaultDbName = "badminton_tournament";

    /**
     * Khởi động H2 TCP Server với IP và port cụ thể.
     *
     * @param bindIP IP để bind (ví dụ "192.168.1.10" hoặc "127.0.0.1")
     * @param port   TCP port (mặc định 9092 nếu <= 0)
     * @throws SQLException nếu không thể khởi động server
     */
    public synchronized void startTcpServer(String bindIP, int port) throws SQLException {
        // Nếu đã chạy, dừng trước để khởi động lại theo cấu hình mới
        if (isServerStarted) {
            stopTcpServer();
        }

        this.serverIP = (bindIP == null || bindIP.isBlank()) ? "127.0.0.1" : bindIP.trim();
        this.serverPort = (port > 0) ? port : 9092;

        // Dựng args cho H2
        final List<String> args = new ArrayList<>();
        args.add("-tcp");
        args.add("-tcpPort");
        args.add(String.valueOf(this.serverPort));

        // H2 TCP Server sẽ bind to all interfaces (0.0.0.0)
        // Vì -tcpListenAddress không được hỗ trợ trong phiên bản này
        // Bảo mật sẽ được đảm bảo qua firewall và network configuration

        // Cho phép máy khác truy cập (trong cùng mạng LAN)
        args.add("-tcpAllowOthers");

        // Log thông tin bảo mật
        log.info("🔒 H2 Server bind to 0.0.0.0:{} - bảo mật qua firewall/network", this.serverPort);
        log.info("🌐 Target LAN IP: {} - máy cùng mạng có thể kết nối", this.serverIP);

        // Tăng timeout để tránh connection timeout
        args.add("-tcpShutdownForce");
        args.add("false");

        // Cố định thư mục chứa file DB
        args.add("-baseDir");
        args.add(baseDirAbsolute);
        // Tạo DB nếu chưa có khi bên client mở lần đầu
        args.add("-ifNotExists");

        try {
            tcpServer = Server.createTcpServer(args.toArray(String[]::new));
            tcpServer.start();

            // Kiểm tra trạng thái thật sự
            if (!tcpServer.isRunning(true)) {
                isServerStarted = false;
                throw new SQLException("H2 TCP Server không ở trạng thái RUNNING sau khi start().");
            }

            isServerStarted = true;

            log.info("🚀 H2 TCP Server started");
            log.info("📍 Server binds to 0.0.0.0:{} (all interfaces)", this.serverPort);
            log.info("🔒 LAN Access: Máy cùng mạng {} có thể kết nối", getNetworkPrefix());
            log.info("🛡️ Bảo mật qua: Windows Firewall + Network configuration");
            log.info("📁 BaseDir: {}", baseDirAbsolute);
            log.info("🔗 Connection URL: {}", getConnectionUrl());
            log.info("👤 Username: sa | 🔑 Password: (empty)");

            System.out.println("🚀 H2 TCP Server started successfully!");
            System.out.println("📍 Server binds to 0.0.0.0:" + this.serverPort + " (all interfaces)");
            System.out.println("🔒 LAN Access: Máy cùng mạng " + getNetworkPrefix() + ".x có thể kết nối");
            System.out.println("🛡️ Bảo mật: Dựa vào Windows Firewall và cấu hình mạng");
            System.out.println("🌐 Máy cùng LAN kết nối bằng: " + this.serverIP + ":" + this.serverPort);
            System.out.println("🔗 Connection URL từ máy cùng LAN: " + getConnectionUrl());
            System.out.println(
                    "🔗 Connection URL từ localhost: jdbc:h2:tcp://localhost:" + this.serverPort + "/" + defaultDbName);
            System.out.println("👤 Username: sa");
            System.out.println("🔑 Password: (empty)");
            System.out.println("📁 Database directory: " + baseDirAbsolute);
            System.out.println("🔥 Lưu ý: Đảm bảo Windows Firewall cho phép port " + this.serverPort);
            System.out.println("🛡️ Bảo mật: Cấu hình firewall để chỉ cho máy cùng mạng LAN");

        } catch (SQLException e) {
            isServerStarted = false;
            // Đảm bảo server tham chiếu bị thu hồi nếu start lỗi
            if (tcpServer != null) {
                try {
                    tcpServer.stop();
                } catch (Exception ignore) {
                }
                tcpServer = null;
            }
            throw new SQLException("❌ Không thể khởi động H2 TCP Server: " + e.getMessage(), e);
        }
    }

    /**
     * Khởi động H2 TCP Server với IP từ NetworkConfig.
     */
    public void startTcpServer(NetworkConfig networkConfig) throws SQLException {
        String ip = (networkConfig != null && networkConfig.ipv4Address() != null
                && !networkConfig.ipv4Address().isBlank())
                        ? networkConfig.ipv4Address().trim()
                        : "127.0.0.1";
        startTcpServer(ip, 9092);
    }

    /**
     * Dừng H2 TCP Server.
     */
    public synchronized void stopTcpServer() {
        if (tcpServer != null && isServerStarted) {
            try {
                tcpServer.stop();
                log.info("🛑 H2 TCP Server stopped.");
                System.out.println("🛑 H2 TCP Server stopped.");
            } finally {
                tcpServer = null;
                isServerStarted = false;
            }
        }
    }

    /**
     * Kiểm tra trạng thái server.
     */
    public boolean isServerRunning() {
        Server s = this.tcpServer;
        return isServerStarted && s != null && s.isRunning(true);
    }

    /**
     * Thông tin kết nối tổng quát.
     */
    public String getConnectionInfo() {
        if (isServerRunning()) {
            return String.format(
                    "H2 TCP Server đang chạy trên port %d (bind 0.0.0.0 - all interfaces)%n" +
                            "🔒 Remote Access: Máy cùng mạng LAN (qua firewall config)%n" +
                            "🔗 Connection URL từ máy cùng LAN: %s%n" +
                            "🔗 Connection URL từ localhost: jdbc:h2:tcp://localhost:%d/%s%n" +
                            "👤 Username: sa%n" +
                            "🔑 Password: (để trống)%n" +
                            "📁 Database directory: %s%n" +
                            "%n" +
                            "🔥 QUAN TRỌNG:%n" +
                            "1. Đảm bảo Windows Firewall cho phép port %d%n" +
                            "2. Cấu hình firewall để CHỈ cho máy cùng mạng LAN (%s/24)%n" +
                            "3. Server bind to 0.0.0.0 (H2 limitation) - bảo mật qua firewall%n" +
                            "4. Khuyến nghị: Cấu hình advanced firewall rules cho LAN-only",
                    serverPort, getConnectionUrl(), serverPort, defaultDbName, baseDirAbsolute,
                    serverPort, getNetworkPrefix());
        } else {
            return "H2 TCP Server chưa khởi động";
        }
    }

    /**
     * Tạo firewall rule command cho Windows để mở port H2.
     */
    public String getFirewallCommand() {
        return String.format(
                "netsh advfirewall firewall add rule name=\"H2 TCP Server\" dir=in action=allow protocol=TCP localport=%d",
                serverPort);
    }

    /**
     * Kiểm tra và hiển thị thông tin debug kết nối.
     */
    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("🔍 H2 TCP Server Debug Info:\n");
        sb.append("Server Status: ").append(isServerRunning() ? "RUNNING" : "STOPPED").append("\n");
        sb.append("Bind Address: 0.0.0.0 (all interfaces - H2 limitation)\n");
        sb.append("Port: ").append(serverPort).append("\n");
        sb.append("Target LAN IP: ").append(serverIP).append("\n");
        sb.append("Network Access: Máy cùng mạng LAN (").append(getNetworkPrefix()).append("/24)\n");
        sb.append("Allow Others: TRUE\n");
        sb.append("Base Directory: ").append(baseDirAbsolute).append("\n");
        sb.append("\n🔗 Connection URLs:\n");
        sb.append("From LAN machines: ").append(getConnectionUrl()).append("\n");
        sb.append("From localhost: jdbc:h2:tcp://localhost:").append(serverPort).append("/").append(defaultDbName)
                .append("\n");
        sb.append("\n🔥 Firewall Command:\n");
        sb.append(getFirewallCommand()).append("\n");
        sb.append("\n🛡️ Security Info:\n");
        sb.append("- H2 không hỗ trợ -tcpListenAddress, phải bind 0.0.0.0\n");
        sb.append("- Bảo mật dựa vào Windows Firewall configuration\n");
        sb.append("- Khuyến nghị: Advanced firewall rules cho LAN-only access\n");
        sb.append("- Cấu hình router/switch để isolate network nếu cần\n");
        return sb.toString();
    }

    /**
     * Lấy network prefix từ IP address (ví dụ: 192.168.1 từ 192.168.1.100)
     */
    private String getNetworkPrefix() {
        if (serverIP != null && serverIP.contains(".")) {
            String[] parts = serverIP.split("\\.");
            if (parts.length >= 3) {
                return parts[0] + "." + parts[1] + "." + parts[2];
            }
        }
        return serverIP;
    }

    /**
     * Connection URL cho DB mặc định (tên nằm trong baseDir của server).
     * Client mở bằng: jdbc:h2:tcp://<ip>:<port>/<dbName>
     */
    public String getConnectionUrl() {
        return String.format("jdbc:h2:tcp://%s:%d/%s", serverIP, serverPort, defaultDbName);
    }

    /**
     * Connection URL cho DB cụ thể.
     */
    public String getConnectionUrl(String databaseName) {
        String db = (databaseName == null || databaseName.isBlank()) ? defaultDbName : databaseName.trim();
        return String.format("jdbc:h2:tcp://%s:%d/%s", serverIP, serverPort, db);
    }

    public String getServerIP() {
        return serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getBaseDirAbsolute() {
        return baseDirAbsolute;
    }

    public void setBaseDir(String baseDirRelative) {
        Path p = (baseDirRelative == null || baseDirRelative.isBlank())
                ? Paths.get("./database")
                : Paths.get(baseDirRelative);
        this.baseDirAbsolute = p.toAbsolutePath().toString();
    }

    @PreDestroy
    public void cleanup() {
        stopTcpServer();
    }
}
