package com.example.btms.model.db;

public record ProbeOutcome(boolean success, String url, String message) {
    public static ProbeOutcome ok(String url) {
        return new ProbeOutcome(true, url, "OK");
    }

    public static ProbeOutcome fail(String url, String msg) {
        return new ProbeOutcome(false, url, msg);
    }
}
