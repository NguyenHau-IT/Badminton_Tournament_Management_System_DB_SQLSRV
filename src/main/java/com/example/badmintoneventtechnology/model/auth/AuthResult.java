package com.example.badmintoneventtechnology.model.auth;

public record AuthResult(boolean found, boolean locked, int userId) {
}
