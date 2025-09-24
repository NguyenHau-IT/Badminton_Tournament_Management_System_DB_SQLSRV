package com.example.btms.model.auth;

public record AuthResult(boolean found, boolean locked, int userId) {
}
