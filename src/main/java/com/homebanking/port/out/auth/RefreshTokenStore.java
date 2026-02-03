package com.homebanking.port.out.auth;

public interface RefreshTokenStore {
    boolean isBlacklisted(String refreshToken);
    void blacklist(String refreshToken, long expiresAtMillis);
}
