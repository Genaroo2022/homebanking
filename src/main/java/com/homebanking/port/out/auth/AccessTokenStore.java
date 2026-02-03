package com.homebanking.port.out.auth;

public interface AccessTokenStore {
    boolean isBlacklisted(String accessToken);
    void blacklist(String accessToken, long expiresAtMillis);
}
