package com.example.btms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Bind trực tiếp spring.datasource.* từ application.properties
 *
 * Ví dụ:
 * spring.datasource.url=jdbc:sqlserver://GODZILLA\\SQLDEV:1433;databaseName=badminton;encrypt=true;trustServerCertificate=true
 * spring.datasource.username=hau2
 * spring.datasource.password=hau123
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class ConnectionConfig {

    public enum Mode {
        NAME, HOME, ABSOLUTE
    }

    // ===== Values bind trực tiếp =====
    private String url; // jdbc:sqlserver://...
    private String username; // spring.datasource.username
    private String password; // spring.datasource.password

    // ===== Cache đã parse từ URL =====
    private String host; // "SERVER" hoặc "SERVER\\INSTANCE"
    private String port; // mặc định 1433 nếu rỗng
    private String databaseInput; // databaseName
    private Boolean encrypt; // default true
    private Boolean trustServerCertificate; // default true (dev/test)
    private Integer loginTimeoutSeconds; // default 30 (property chuẩn là loginTimeout)
    private String applicationName; // optional
    private Boolean integratedSecurity; // optional
    private Mode mode = Mode.NAME;

    // ===== Getters theo API cũ (để code cũ không phải đổi) =====
    public String host() {
        return host;
    }

    public String port() {
        return port;
    }

    public String databaseInput() {
        return databaseInput;
    }

    public String user() {
        return username;
    }

    public String password() {
        return password;
    }

    public Mode mode() {
        return mode;
    }

    public Boolean encrypt() {
        return encrypt;
    }

    public Boolean trustServerCertificate() {
        return trustServerCertificate;
    }

    public Integer loginTimeoutSeconds() {
        return loginTimeoutSeconds;
    }

    public String applicationName() {
        return applicationName;
    }

    public Boolean integratedSecurity() {
        return integratedSecurity;
    }

    // ===== Effective helpers =====
    public String normalizedHost() {
        return trimOrEmpty(host);
    }

    public String normalizedPort() {
        String p = trimOrEmpty(port);
        return p.isEmpty() ? "1433" : p;
    }

    public String normalizedDatabase() {
        return trimOrEmpty(databaseInput);
    }

    public boolean effectiveEncrypt() {
        return encrypt == null || encrypt;
    }

    public boolean effectiveTrustServerCertificate() {
        return trustServerCertificate == null || trustServerCertificate;
    }

    public int effectiveLoginTimeoutSeconds() {
        return loginTimeoutSeconds == null ? 30 : Math.max(0, loginTimeoutSeconds);
    }

    public boolean effectiveIntegratedSecurity() {
        return integratedSecurity != null && integratedSecurity;
    }

    /** Xây JDBC URL từ các thuộc tính đã parse (dùng được cho DriverManager) */
    public String buildJdbcUrl() {
        String h = normalizedHost();
        String p = normalizedPort();
        String db = normalizedDatabase().isEmpty() ? "master" : normalizedDatabase();

        StringBuilder sb = new StringBuilder("jdbc:sqlserver://").append(h);
        if (p != null && !p.isBlank())
            sb.append(":").append(p);

        sb.append(";databaseName=").append(db);
        sb.append(";encrypt=").append(effectiveEncrypt());
        sb.append(";trustServerCertificate=").append(effectiveTrustServerCertificate());
        sb.append(";loginTimeout=").append(effectiveLoginTimeoutSeconds());
        if (applicationName != null && !applicationName.isBlank())
            sb.append(";applicationName=").append(applicationName);
        if (effectiveIntegratedSecurity())
            sb.append(";integratedSecurity=true");
        return sb.toString();
    }

    // ===== Setters để Spring bind =====
    public void setUrl(String url) {
        this.url = trimOrNull(url);
        parseSqlServerUrl(this.url);
    }

    public void setUsername(String username) {
        this.username = trimOrNull(username);
    }

    public void setPassword(String password) {
        this.password = password;
    } // không trim

    public ConnectionConfig mode(Mode v) {
        this.mode = (v == null ? Mode.NAME : v);
        return this;
    }

    // ===== URL parser cho SQL Server JDBC =====
    private void parseSqlServerUrl(String url) {
        host = port = databaseInput = null;
        encrypt = trustServerCertificate = integratedSecurity = null;
        applicationName = null;
        loginTimeoutSeconds = null;

        if (url == null)
            return;
        String prefix = "jdbc:sqlserver://";
        if (!url.startsWith(prefix))
            return;

        String body = url.substring(prefix.length()); // server[\instance][:port];k=v;...
        String serverPart;
        String propsPart = "";
        int semi = body.indexOf(';');
        if (semi >= 0) {
            serverPart = body.substring(0, semi);
            propsPart = body.substring(semi + 1);
        } else
            serverPart = body;

        String tmpHost = serverPart;
        String tmpPort = null;
        int colon = serverPart.lastIndexOf(':');
        if (colon >= 0) {
            tmpHost = serverPart.substring(0, colon);
            tmpPort = serverPart.substring(colon + 1);
        }
        host = trimOrEmpty(tmpHost);
        port = trimOrEmpty(tmpPort);

        Map<String, String> kv = splitProps(propsPart);
        databaseInput = trimOrEmpty(kv.getOrDefault("databaseName", kv.getOrDefault("database", "")));
        applicationName = trimOrNull(kv.get("applicationName"));
        encrypt = parseBool(kv.get("encrypt"));
        trustServerCertificate = parseBool(kv.get("trustServerCertificate"));
        integratedSecurity = parseBool(kv.get("integratedSecurity"));
        Integer lt = parseInt(kv.get("loginTimeout"));
        loginTimeoutSeconds = lt;

        if (port == null || port.isEmpty())
            port = "1433";
        if (encrypt == null)
            encrypt = true;
        if (trustServerCertificate == null)
            trustServerCertificate = true;
        if (loginTimeoutSeconds == null)
            loginTimeoutSeconds = 30;
    }

    private static Map<String, String> splitProps(String props) {
        Map<String, String> map = new HashMap<>();
        if (props == null || props.isEmpty())
            return map;
        for (String p : props.split(";")) {
            if (p.isEmpty())
                continue;
            int eq = p.indexOf('=');
            if (eq > 0)
                map.put(p.substring(0, eq).trim(), p.substring(eq + 1).trim());
        }
        return map;
    }

    private static Boolean parseBool(String v) {
        if (v == null)
            return null;
        String s = v.trim().toLowerCase();
        if (s.equals("true") || s.equals("1") || s.equals("yes"))
            return true;
        if (s.equals("false") || s.equals("0") || s.equals("no"))
            return false;
        return null;
    }

    private static Integer parseInt(String v) {
        if (v == null || v.isEmpty())
            return null;
        try {
            return Integer.parseInt(v.trim());
        } catch (Exception ignore) {
            return null;
        }
    }

    private static String trimOrNull(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String trimOrEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    @Override
    public String toString() {
        return "ConnectionConfig{" +
                "url='" + url + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", databaseInput='" + databaseInput + '\'' +
                ", user='" + (username == null ? null : "***") + '\'' +
                ", encrypt=" + encrypt +
                ", trustServerCertificate=" + trustServerCertificate +
                ", loginTimeoutSeconds=" + loginTimeoutSeconds +
                ", applicationName='" + applicationName + '\'' +
                ", integratedSecurity=" + integratedSecurity +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, username, password, host, port, databaseInput,
                encrypt, trustServerCertificate, loginTimeoutSeconds, applicationName, integratedSecurity, mode);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConnectionConfig cfg))
            return false;
        return Objects.equals(url, cfg.url)
                && Objects.equals(username, cfg.username)
                && Objects.equals(password, cfg.password)
                && Objects.equals(host, cfg.host)
                && Objects.equals(port, cfg.port)
                && Objects.equals(databaseInput, cfg.databaseInput)
                && Objects.equals(encrypt, cfg.encrypt)
                && Objects.equals(trustServerCertificate, cfg.trustServerCertificate)
                && Objects.equals(loginTimeoutSeconds, cfg.loginTimeoutSeconds)
                && Objects.equals(applicationName, cfg.applicationName)
                && Objects.equals(integratedSecurity, cfg.integratedSecurity)
                && mode == cfg.mode;
    }
}
