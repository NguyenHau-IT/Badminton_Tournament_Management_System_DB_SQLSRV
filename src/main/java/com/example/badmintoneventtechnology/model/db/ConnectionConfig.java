package com.example.badmintoneventtechnology.model.db;

import java.util.Objects;

public class ConnectionConfig {

    public enum Mode {
        NAME, HOME, ABSOLUTE
    }

    // ===== Core (giữ nguyên như cũ) =====
    private String host; // Có thể là "SERVER" hoặc "SERVER\\INSTANCE"
    private String port; // Có thể rỗng nếu dùng instance (SQL Browser) -> mặc định 1433
    private String databaseInput; // Tên DB raw người dùng nhập
    private String user;
    private String password;
    private Mode mode = Mode.NAME;

    // ===== SQL Server options (bổ sung, tùy chọn) =====
    private Boolean encrypt; // default: true (khuyến nghị)
    private Boolean trustServerCertificate; // default: true (dev/test). Prod nên dùng cert chuẩn
    private Integer loginTimeoutSeconds; // default: 30
    private String applicationName; // ví dụ: "BadmintonEventTech"
    private Boolean integratedSecurity; // Windows auth (nếu dùng, thường bỏ user/pass)

    // ======== Getters (giữ tên như cũ để tương thích) ========
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
        return user;
    }

    public String password() {
        return password;
    }

    public Mode mode() {
        return mode;
    }

    // ======== Setters theo kiểu builder (giữ nguyên API) ========
    public ConnectionConfig host(String v) {
        this.host = trimOrNull(v);
        return this;
    }

    public ConnectionConfig port(String v) {
        this.port = trimOrNull(v);
        return this;
    }

    public ConnectionConfig databaseInput(String v) {
        this.databaseInput = trimOrNull(v);
        return this;
    }

    public ConnectionConfig user(String v) {
        this.user = trimOrNull(v);
        return this;
    }

    public ConnectionConfig password(String v) {
        this.password = v; // không trim password
        return this;
    }

    public ConnectionConfig mode(Mode v) {
        this.mode = (v == null ? Mode.NAME : v);
        return this;
    }

    // ======== Setters cho SQL Server options (mới) ========
    public ConnectionConfig encrypt(Boolean v) {
        this.encrypt = v;
        return this;
    }

    public ConnectionConfig trustServerCertificate(Boolean v) {
        this.trustServerCertificate = v;
        return this;
    }

    public ConnectionConfig loginTimeoutSeconds(Integer v) {
        this.loginTimeoutSeconds = v;
        return this;
    }

    public ConnectionConfig applicationName(String v) {
        this.applicationName = trimOrNull(v);
        return this;
    }

    public ConnectionConfig integratedSecurity(Boolean v) {
        this.integratedSecurity = v;
        return this;
    }

    // ======== Getters cho options (mới) ========
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

    // ======== Helpers hữu ích cho sqlsrv ========
    /** Host đã trim; có thể chứa \\INSTANCE. */
    public String normalizedHost() {
        return trimOrEmpty(host);
    }

    /** Port hợp lệ; mặc định "1433" nếu bỏ trống. */
    public String normalizedPort() {
        String p = trimOrEmpty(port);
        return p.isEmpty() ? "1433" : p;
    }

    /** Tên DB đã trim; có thể rỗng nếu muốn connect master mặc định. */
    public String normalizedDatabase() {
        return trimOrEmpty(databaseInput);
    }

    /** Giá trị encrypt mặc định true nếu chưa set. */
    public boolean effectiveEncrypt() {
        return encrypt == null ? true : encrypt;
    }

    /** Giá trị trustServerCertificate mặc định true (dev/test) nếu chưa set. */
    public boolean effectiveTrustServerCertificate() {
        return trustServerCertificate == null ? true : trustServerCertificate;
    }

    /** Login timeout mặc định 30s nếu chưa set. */
    public int effectiveLoginTimeoutSeconds() {
        return loginTimeoutSeconds == null ? 30 : Math.max(0, loginTimeoutSeconds);
    }

    /** integratedSecurity mặc định false nếu chưa set. */
    public boolean effectiveIntegratedSecurity() {
        return integratedSecurity != null && integratedSecurity;
    }

    // ======== Utils ========
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
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", databaseInput='" + databaseInput + '\'' +
                ", user='" + (user == null ? null : "***") + '\'' +
                ", mode=" + mode +
                ", encrypt=" + encrypt +
                ", trustServerCertificate=" + trustServerCertificate +
                ", loginTimeoutSeconds=" + loginTimeoutSeconds +
                ", applicationName='" + applicationName + '\'' +
                ", integratedSecurity=" + integratedSecurity +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, databaseInput, user, password, mode,
                encrypt, trustServerCertificate, loginTimeoutSeconds, applicationName, integratedSecurity);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConnectionConfig cfg))
            return false;
        return Objects.equals(host, cfg.host)
                && Objects.equals(port, cfg.port)
                && Objects.equals(databaseInput, cfg.databaseInput)
                && Objects.equals(user, cfg.user)
                && Objects.equals(password, cfg.password)
                && mode == cfg.mode
                && Objects.equals(encrypt, cfg.encrypt)
                && Objects.equals(trustServerCertificate, cfg.trustServerCertificate)
                && Objects.equals(loginTimeoutSeconds, cfg.loginTimeoutSeconds)
                && Objects.equals(applicationName, cfg.applicationName)
                && Objects.equals(integratedSecurity, cfg.integratedSecurity);
    }
}
