package com.homebanking.application.usecase;

import com.homebanking.application.dto.authentication.request.RefreshTokenInputRequest;
import com.homebanking.application.dto.authentication.response.TokenOutputResponse;
import com.homebanking.application.usecase.auth.RefreshTokenUseCaseImpl;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.port.out.auth.RefreshTokenService;
import com.homebanking.port.out.auth.RefreshTokenStore;
import com.homebanking.port.out.auth.TokenGenerator;
import com.homebanking.port.out.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseImplTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @Mock
    private UserRepository userRepository;

    private RefreshTokenUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new RefreshTokenUseCaseImpl(
                refreshTokenService,
                tokenGenerator,
                userRepository,
                refreshTokenStore
        );
    }

    @Test
    void shouldReturnNewTokens_WhenRefreshTokenIsValid() {
        String refreshToken = "refresh_token_123";
        String subject = "user@example.com";
        String accessToken = "access_token_123";
        String newRefreshToken = "refresh_token_456";
        User user = createTestUser(subject);

        when(refreshTokenService.isRefreshTokenValid(refreshToken)).thenReturn(true);
        when(refreshTokenStore.isBlacklisted(refreshToken)).thenReturn(false);
        when(refreshTokenService.extractSubject(refreshToken)).thenReturn(subject);
        when(userRepository.findByEmail(subject)).thenReturn(Optional.of(user));
        when(tokenGenerator.generateToken(subject)).thenReturn(accessToken);
        when(refreshTokenService.generateRefreshToken(subject)).thenReturn(newRefreshToken);
        when(refreshTokenService.getExpirationMillis(refreshToken)).thenReturn(System.currentTimeMillis() + 10000);

        TokenOutputResponse response = useCase.refresh(new RefreshTokenInputRequest(refreshToken));

        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(newRefreshToken);
        verify(refreshTokenService).isRefreshTokenValid(refreshToken);
        verify(refreshTokenStore).isBlacklisted(refreshToken);
        verify(refreshTokenService).extractSubject(refreshToken);
        verify(userRepository).findByEmail(subject);
        verify(tokenGenerator).generateToken(subject);
        verify(refreshTokenService).generateRefreshToken(subject);
        verify(refreshTokenStore).blacklist(eq(refreshToken), anyLong());
    }

    @Test
    void shouldThrow_WhenRefreshTokenIsInvalid() {
        String refreshToken = "bad_token";

        when(refreshTokenService.isRefreshTokenValid(refreshToken)).thenReturn(false);

        assertThatThrownBy(() -> useCase.refresh(new RefreshTokenInputRequest(refreshToken)))
                .isInstanceOf(InvalidUserDataException.class);

        verify(refreshTokenService).isRefreshTokenValid(refreshToken);
        verifyNoMoreInteractions(refreshTokenService, refreshTokenStore, tokenGenerator, userRepository);
    }

    @Test
    void shouldThrow_WhenRefreshTokenIsBlacklisted() {
        String refreshToken = "refresh_token_123";

        when(refreshTokenService.isRefreshTokenValid(refreshToken)).thenReturn(true);
        when(refreshTokenStore.isBlacklisted(refreshToken)).thenReturn(true);

        assertThatThrownBy(() -> useCase.refresh(new RefreshTokenInputRequest(refreshToken)))
                .isInstanceOf(InvalidUserDataException.class);

        verify(refreshTokenService).isRefreshTokenValid(refreshToken);
        verify(refreshTokenStore).isBlacklisted(refreshToken);
        verifyNoMoreInteractions(refreshTokenService, refreshTokenStore, tokenGenerator, userRepository);
    }

    @Test
    void shouldThrow_WhenRefreshTokenSubjectIsBlank() {
        String refreshToken = "refresh_token_123";

        when(refreshTokenService.isRefreshTokenValid(refreshToken)).thenReturn(true);
        when(refreshTokenStore.isBlacklisted(refreshToken)).thenReturn(false);
        when(refreshTokenService.extractSubject(refreshToken)).thenReturn(" ");

        assertThatThrownBy(() -> useCase.refresh(new RefreshTokenInputRequest(refreshToken)))
                .isInstanceOf(InvalidUserDataException.class);

        verify(refreshTokenService).isRefreshTokenValid(refreshToken);
        verify(refreshTokenStore).isBlacklisted(refreshToken);
        verify(refreshTokenService).extractSubject(refreshToken);
        verifyNoMoreInteractions(refreshTokenService, refreshTokenStore, tokenGenerator, userRepository);
    }

    @Test
    void shouldThrow_WhenUserNotFound() {
        String refreshToken = "refresh_token_123";
        String subject = "missing@example.com";

        when(refreshTokenService.isRefreshTokenValid(refreshToken)).thenReturn(true);
        when(refreshTokenStore.isBlacklisted(refreshToken)).thenReturn(false);
        when(refreshTokenService.extractSubject(refreshToken)).thenReturn(subject);
        when(userRepository.findByEmail(subject)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.refresh(new RefreshTokenInputRequest(refreshToken)))
                .isInstanceOf(InvalidUserDataException.class);

        verify(refreshTokenService).isRefreshTokenValid(refreshToken);
        verify(refreshTokenStore).isBlacklisted(refreshToken);
        verify(refreshTokenService).extractSubject(refreshToken);
        verify(userRepository).findByEmail(subject);
        verifyNoMoreInteractions(refreshTokenService, refreshTokenStore, tokenGenerator, userRepository);
    }

    private User createTestUser(String email) {
        return User.withId(
                UUID.randomUUID(),
                email,
                "hashed_password",
                "John",
                "Doe",
                "12345678",
                LocalDate.of(1990, 1, 1),
                "123 Street",
                LocalDateTime.now()
        );
    }
}
