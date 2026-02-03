package com.homebanking.application.usecase.auth;

import com.homebanking.application.dto.authentication.request.LogoutInputRequest;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.authentication.LogoutInputPort;
import com.homebanking.port.out.auth.AccessTokenStore;
import com.homebanking.port.out.auth.RefreshTokenService;
import com.homebanking.port.out.auth.RefreshTokenStore;
import com.homebanking.port.out.auth.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LogoutUseCaseImpl implements LogoutInputPort {

    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenStore refreshTokenStore;
    private final AccessTokenStore accessTokenStore;
    private final TokenGenerator tokenGenerator;

    @Override
    public void logout(LogoutInputRequest request) {
        if (!refreshTokenService.isRefreshTokenValid(request.refreshToken())) {
            throw new InvalidUserDataException(DomainErrorMessages.INVALID_REFRESH_TOKEN);
        }

        if (refreshTokenStore.isBlacklisted(request.refreshToken())) {
            return;
        }

        refreshTokenStore.blacklist(
                request.refreshToken(),
                refreshTokenService.getExpirationMillis(request.refreshToken())
        );

        blacklistAccessTokenIfPresent(request.accessToken());
    }

    private void blacklistAccessTokenIfPresent(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        try {
            long expiresAt = tokenGenerator.getExpirationMillis(accessToken);
            accessTokenStore.blacklist(accessToken, expiresAt);
        } catch (Exception ignored) {
            // If the access token is invalid/expired, skip blacklisting.
        }
    }
}
