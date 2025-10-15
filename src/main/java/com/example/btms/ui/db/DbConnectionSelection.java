package com.example.btms.ui.db;

import java.util.Objects;

public class DbConnectionSelection {
    public enum Mode {
        LOCAL_OR_LAN, // Sử dụng DB cục bộ / cùng mạng
        INIT_NEW, // Khởi tạo mới
        ONLINE, // Sử dụng online (Cloud/Hosted)
        IMPORT_FILE // Nhập từ file có sẵn
    }

    public enum DbType {
        H2, SQLSRV
    }

    private Mode mode;
    private DbType dbType;
    private String dbName; // tên DB
    private String existingEntry; // mục chọn từ combobox folder database
    private String server;
    private String port;
    private String username;
    private char[] password;
    private boolean remember;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public DbType getDbType() {
        return dbType;
    }

    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getExistingEntry() {
        return existingEntry;
    }

    public void setExistingEntry(String existingEntry) {
        this.existingEntry = existingEntry;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public boolean isRemember() {
        return remember;
    }

    public void setRemember(boolean remember) {
        this.remember = remember;
    }

    @Override
    public String toString() {
        return "DbConnectionSelection{" +
                "mode=" + mode +
                ", dbType=" + dbType +
                ", dbName='" + dbName + '\'' +
                ", existingEntry='" + existingEntry + '\'' +
                ", server='" + server + '\'' +
                ", port='" + port + '\'' +
                ", username='" + username + '\'' +
                ", remember=" + remember +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, dbType, dbName, existingEntry, server, port, username, remember);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        DbConnectionSelection other = (DbConnectionSelection) obj;
        return remember == other.remember
                && mode == other.mode
                && dbType == other.dbType
                && Objects.equals(dbName, other.dbName)
                && Objects.equals(existingEntry, other.existingEntry)
                && Objects.equals(server, other.server)
                && Objects.equals(port, other.port)
                && Objects.equals(username, other.username);
        // password cố tình không so sánh để tránh vô tình giữ lại trong bộ nhớ lâu hơn
        // cần thiết
    }
}
