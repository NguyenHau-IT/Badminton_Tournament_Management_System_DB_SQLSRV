package com.example.btms.config;

public class ConnectionH2Config {

    public enum Mode {
        NAME, HOME, ABSOLUTE
    }

    private String host;
    private String port;
    private String databaseInput; // raw user input
    private String user;
    private String password;
    private Mode mode = Mode.NAME;

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

    public ConnectionH2Config host(String v) {
        this.host = v;
        return this;
    }

    public ConnectionH2Config port(String v) {
        this.port = v;
        return this;
    }

    public ConnectionH2Config databaseInput(String v) {
        this.databaseInput = v;
        return this;
    }

    public ConnectionH2Config user(String v) {
        this.user = v;
        return this;
    }

    public ConnectionH2Config password(String v) {
        this.password = v;
        return this;
    }

    public ConnectionH2Config mode(Mode v) {
        this.mode = v;
        return this;
    }
}
