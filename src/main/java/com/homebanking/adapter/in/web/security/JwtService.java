package com.homebanking.adapter.in.web.security;

import com.homebanking.port.out.auth.RefreshTokenService;
import com.homebanking.port.out.auth.TokenGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;

@Service
@Slf4j
public class JwtService implements TokenGenerator, RefreshTokenService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    public String generateToken(String username) {
        return buildToken(new HashMap<>(), username, "access", jwtExpiration);
    }

    @Override
    public String generateRefreshToken(String username) {
        return buildToken(new HashMap<>(), username, "refresh", refreshExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            String username,
            String tokenType,
            long expirationMillis) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("typ", tokenType);
        return Jwts
                .builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error extrayendo username del token: {}", e.getMessage());
            return null;
        }
    }


    public boolean isTokenValid(String token, String username) {
        if (!validateToken(token)) {
            return false;
        }
        final String usernameInToken = extractUsername(token);
        return usernameInToken != null && usernameInToken.equals(username) && !isTokenExpired(token);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractTokenType(token));
    }

    @Override
    public boolean isRefreshTokenValid(String refreshToken) {
        return validateToken(refreshToken) && "refresh".equals(extractTokenType(refreshToken));
    }

    @Override
    public String extractSubject(String refreshToken) {
        return extractUsername(refreshToken);
    }

    @Override
    public long getExpirationMillis(String refreshToken) {
        return extractExpiration(refreshToken).getTime();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("typ", String.class));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}


