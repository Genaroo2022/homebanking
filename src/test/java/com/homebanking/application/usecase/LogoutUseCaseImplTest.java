package com.homebanking.application.usecase;

import com.homebanking.application.dto.authentication.request.LogoutInputRequest;
import com.homebanking.application.usecase.auth.LogoutUseCaseImpl;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.port.out.auth.AccessTokenStore;
import com.homebanking.port.out.auth.RefreshTokenService;
import com.homebanking.port.out.auth.RefreshTokenStore;
import com.homebanking.port.out.auth.TokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseImplTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @Mock
    private AccessTokenStore accessTokenStore;

    @Mock
    private TokenGenerator tokenGenerator;

    private LogoutUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new LogoutUseCaseImpl(
                refreshTokenService,
                refreshTokenStore,
                accessTokenStore,
                tokenGenerator
        );
    }

    @Test
    void shouldBlacklistValidRefreshToken() {
        String refreshToken = "refresh_token_123";

        when(refreshTokenService.isRefreshTokenValid(refreshToken)).thenReturn(true);
        when(refreshTokenStore.isBlacklisted(refreshToken)).thenReturn(false);
        when(refreshTokenService.getExpirationMillis(refreshToken)).thenReturn(System.currentTimeMillis() + 10000);
        when(tokenGenerator.getExpirationMillis("access_token_123")).thenReturn(System.currentTimeMillis() + 10000);

        useCase.logout(new LogoutInputRequest(refreshToken, "access_token_123"));

        verify(refreshTokenService).isRefreshTokenValid(refreshToken);
        verify(refreshTokenStore).isBlacklisted(refreshToken);
        verify(refreshTokenService).getExpirationMillis(refreshToken);
        verify(refreshTokenStore).blacklist(eq(refreshToken), anyLong());
        verify(tokenGenerator).getExpirationMillis("access_token_123");
        verify(accessTokenStore).blacklist(eq("access_token_123"), anyLong());
    }

    @Test
    void shouldNoopWhenRefreshTokenAlreadyBlacklisted() {
        String refreshToken = "refresh_token_123";

        when(refreshTokenService.isRefreshTokenValid(refreshToken)).thenReturn(true);
        when(refreshTokenStore.isBlacklisted(refreshToken)).thenReturn(true);

        useCase.logout(new LogoutInputRequest(refreshToken, null));

        verify(refreshTokenService).isRefreshTokenValid(refreshToken);
        verify(refreshTokenStore).isBlacklisted(refreshToken);
        verifyNoMoreInteractions(refreshTokenService, refreshTokenStore, accessTokenStore, tokenGenerator);
    }

    @Test
    void shouldThrowWhenRefreshTokenInvalid() {
        String refreshToken = "bad_token";

        when(refreshTokenService.isRefreshTokenValid(refreshToken)).thenReturn(false);

        assertThatThrownBy(() -> useCase.logout(new LogoutInputRequest(refreshToken, null)))
                .isInstanceOf(InvalidUserDataException.class);

        verify(refreshTokenService).isRefreshTokenValid(refreshToken);
        verifyNoMoreInteractions(refreshTokenService, refreshTokenStore, accessTokenStore, tokenGenerator);
    }
}
