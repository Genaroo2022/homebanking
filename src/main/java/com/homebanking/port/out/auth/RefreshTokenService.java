package com.homebanking.port.out.auth;

public interface RefreshTokenService {
    String generateRefreshToken(String subject);
    boolean isRefreshTokenValid(String refreshToken);
    String extractSubject(String refreshToken);
    long getExpirationMillis(String refreshToken);
}
