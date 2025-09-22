package com.example.badmintoneventtechnology.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuration class để đọc thông tin kết nối database từ
 * application.properties
 */
@Component
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * Parse URL để lấy thông tin server, port, database
     * URL format: jdbc:sqlserver://server:port;databaseName=dbname;...
     */
    public String getServer() {
        if (datasourceUrl == null || !datasourceUrl.startsWith("jdbc:sqlserver://")) {
            return "localhost";
        }

        try {
            // Lấy phần sau jdbc:sqlserver://
            String urlPart = datasourceUrl.substring("jdbc:sqlserver://".length());

            // Tìm dấu ; đầu tiên để lấy server:port
            int semicolonIndex = urlPart.indexOf(';');
            if (semicolonIndex == -1) {
                semicolonIndex = urlPart.length();
            }

            String serverPort = urlPart.substring(0, semicolonIndex);

            // Xử lý instance name (ví dụ: GODZILLA\SQLDEV)
            if (serverPort.contains("\\")) {
                return serverPort; // Trả về cả server và instance
            } else if (serverPort.contains(":")) {
                // Chỉ có server:port, không có instance
                return serverPort.substring(0, serverPort.indexOf(':'));
            } else {
                // Chỉ có server
                return serverPort;
            }
        } catch (Exception e) {
            return "localhost";
        }
    }

    public String getPort() {
        if (datasourceUrl == null || !datasourceUrl.startsWith("jdbc:sqlserver://")) {
            return "1433";
        }

        try {
            String urlPart = datasourceUrl.substring("jdbc:sqlserver://".length());
            int semicolonIndex = urlPart.indexOf(';');
            if (semicolonIndex == -1) {
                semicolonIndex = urlPart.length();
            }

            String serverPort = urlPart.substring(0, semicolonIndex);

            if (serverPort.contains(":")) {
                return serverPort.substring(serverPort.indexOf(':') + 1);
            }
        } catch (Exception e) {
            // Ignore
        }

        return "1433";
    }

    public String getDatabaseName() {
        if (datasourceUrl == null) {
            return "badminton";
        }

        try {
            // Tìm databaseName= trong URL
            int dbNameIndex = datasourceUrl.indexOf("databaseName=");
            if (dbNameIndex != -1) {
                dbNameIndex += "databaseName=".length();
                int endIndex = datasourceUrl.indexOf(';', dbNameIndex);
                if (endIndex == -1) {
                    endIndex = datasourceUrl.length();
                }
                return datasourceUrl.substring(dbNameIndex, endIndex);
            }
        } catch (Exception e) {
            // Ignore
        }

        return "badminton";
    }

    // Getters
    public String getDatasourceUrl() {
        return datasourceUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }
}
