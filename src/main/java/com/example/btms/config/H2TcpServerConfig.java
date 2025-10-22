package com.example.btms.config;

import java.net.ServerSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.h2.tools.Server;
import org.springframework.stereotype.Component;

import com.example.btms.util.log.Log;

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

    private static final Log LOG = new Log();

    // Server status fields
    private volatile Server tcpServer;
    private volatile boolean isServerStarted = false;

    // Configuration
    private String serverIP = "127.0.0.1";
    private int serverPort = 9092; // Default H2 TCP port
    private String baseDirAbsolute = Paths.get("./database").toAbsolutePath().toString();
    private final String defaultDbName = "badminton_tournament";

    // === Log Utilities ===

    private void logH2Info(String message, Object... args) {
        LOG.logTs("[H2-TCP] " + message, args);
    }

    private void logH2Success(String message, Object... args) {
        LOG.logTs("✅ [H2-TCP] " + message, args);
    }

    private void logH2Error(String message, Object... args) {
        LOG.logTs("❌ [H2-TCP] " + message, args);
    }

    private void logH2Console(String message, Object... args) {
        LOG.log("🖥️ [H2-CONSOLE] " + message, args);
    }

    public synchronized void startTcpServer(String bindIP, int port) throws SQLException {
        // Nếu đã chạy, dừng trước để khởi động lại theo cấu hình mới
        if (isServerStarted) {
            stopTcpServer();
        }

        this.serverIP = (bindIP == null || bindIP.isBlank()) ? "127.0.0.1" : bindIP.trim();
        this.serverPort = (port > 0) ? port : 9092;

        // Kiểm tra port availability trước khi start
        if (isPortInUse(this.serverPort)) {
            logH2Error("Port %d is already in use", this.serverPort);
            logH2Console("⚠️ Port %d đang được sử dụng!", this.serverPort);
            logH2Console("💡 Chạy lệnh để kiểm tra: netstat -ano | findstr :%d", this.serverPort);
            throw new SQLException(String.format(
                    "Port %d is already in use. Please kill existing process or use different port.", this.serverPort));
        }

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
        logH2Info("Server bind to 0.0.0.0:%d - bảo mật qua firewall", this.serverPort);
        logH2Info("Target LAN IP: %s - máy cùng mạng có thể kết nối", this.serverIP);

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

            logH2Success("Server started on 0.0.0.0:%d", this.serverPort);
            logH2Info("LAN Access: %s/24 subnet", getNetworkPrefix());
            logH2Info("Security: Windows Firewall + Network config");
            logH2Info("BaseDir: %s", baseDirAbsolute);
            logH2Info("Connection URL: %s", getConnectionUrl());
            logH2Info("Credentials: sa/(empty)");

            // Console output for user visibility
            logH2Console("🚀 H2 TCP Server started successfully!");
            logH2Console("📍 Server binds to 0.0.0.0:%d (all interfaces)", this.serverPort);
            logH2Console("🔒 LAN Access: Máy cùng mạng %s.x có thể kết nối", getNetworkPrefix());
            logH2Console("🛡️ Bảo mật: Dựa vào Windows Firewall và cấu hình mạng");
            logH2Console("🌐 Máy cùng LAN kết nối bằng: %s:%d", this.serverIP, this.serverPort);
            logH2Console("🔗 Connection URL từ máy cùng LAN: %s", getConnectionUrl());
            logH2Console("🔗 Connection URL từ localhost: jdbc:h2:tcp://localhost:%d/%s", this.serverPort,
                    defaultDbName);
            logH2Console("👤 Username: sa");
            logH2Console("🔑 Password: (empty)");
            logH2Console("📁 Database directory: %s", baseDirAbsolute);
            logH2Console("🔥 Lưu ý: Đảm bảo Windows Firewall cho phép port %d", this.serverPort);
            logH2Console("🛡️ Bảo mật: Cấu hình firewall để chỉ cho máy cùng mạng LAN");

        } catch (SQLException e) {
            isServerStarted = false;

            // Xử lý lỗi port conflict
            if (e.getMessage().contains("port may be in use") || e.getMessage().contains("BindException")) {
                logH2Error("Port %d is already in use - checking for existing processes", this.serverPort);
                logH2Console("⚠️ Port %d đang được sử dụng bởi process khác!", this.serverPort);
                logH2Console("💡 Giải pháp:");
                logH2Console("   1. Kiểm tra: netstat -ano | findstr :%d", this.serverPort);
                logH2Console("   2. Kill process: taskkill /PID <process_id> /F");
                logH2Console("   3. Hoặc restart ứng dụng");
            } else {
                logH2Error("Failed to start server: %s", e.getMessage());
            }

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
                logH2Info("Server stopped");
                logH2Console("🛑 H2 TCP Server stopped.");
            } finally {
                tcpServer = null;
                isServerStarted = false;
            }
        }
    }

    /**
     * Hiển thị lại đầy đủ thông tin kết nối khi server đang chạy.
     */
    public void showConnectionInfo() {
        if (!isServerRunning()) {
            logH2Console("❌ H2 TCP Server chưa khởi động");
            return;
        }

        logH2Console("📋 H2 TCP Server Connection Information:");
        logH2Console("🚀 Status: RUNNING");
        logH2Console("📍 Server binds to 0.0.0.0:%d (all interfaces)", this.serverPort);
        logH2Console("🔒 LAN Access: Máy cùng mạng %s.x có thể kết nối", getNetworkPrefix());
        logH2Console("🛡️ Bảo mật: Dựa vào Windows Firewall và cấu hình mạng");
        logH2Console("🌐 Máy cùng LAN kết nối bằng: %s:%d", this.serverIP, this.serverPort);
        logH2Console("🔗 Connection URL từ máy cùng LAN: %s", getConnectionUrl());
        logH2Console("🔗 Connection URL từ localhost: jdbc:h2:tcp://localhost:%d/%s", this.serverPort, defaultDbName);
        logH2Console("👤 Username: sa");
        logH2Console("🔑 Password: (empty)");
        logH2Console("📁 Database directory: %s", baseDirAbsolute);
        logH2Console("🔥 Lưu ý: Đảm bảo Windows Firewall cho phép port %d", this.serverPort);
        logH2Console("🛡️ Bảo mật: Cấu hình firewall để chỉ cho máy cùng mạng LAN");
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

    // === Utility Methods ===

    /**
     * Kiểm tra xem port có đang được sử dụng không.
     */
    private boolean isPortInUse(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return false; // Port available
        } catch (java.io.IOException e) {
            return true; // Port in use
        }
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
