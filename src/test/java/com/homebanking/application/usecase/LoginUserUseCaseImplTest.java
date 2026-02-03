package com.homebanking.application.usecase;

import com.homebanking.application.dto.authentication.request.LoginInputRequest;
import com.homebanking.application.dto.authentication.response.TokenOutputResponse;
import com.homebanking.application.exception.RateLimitExceededException;
import com.homebanking.application.service.auth.LoginAttemptService;
import com.homebanking.application.usecase.auth.LoginUserUseCaseImpl;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.event.LoginAttemptedEvent;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.port.out.event.EventPublisher;
import com.homebanking.port.out.auth.PasswordHasher;
import com.homebanking.port.out.auth.RefreshTokenService;
import com.homebanking.port.out.security.LoginRateLimiter;
import com.homebanking.port.out.auth.TokenGenerator;
import com.homebanking.port.out.auth.TotpService;
import com.homebanking.port.out.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para LoginUserUseCaseImpl.

 * Ventajas de esta estructura:
 * - Sin dependencias de Spring
 * - Mocks con Mockito
 * - Ejecucion rapida
 * - Responsabilidades claras

 * Patrones aplicados:
 * - AAA (Arrange, Act, Assert)
 * - Given-When-Then
 * - Naming descriptivo
 */
@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private TotpService totpService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private LoginRateLimiter loginRateLimiter;

    @Mock
    private EventPublisher eventPublisher;

    private LoginUserUseCaseImpl loginUseCase;

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUserUseCaseImpl(
                userRepository,
                passwordHasher,
                tokenGenerator,
                refreshTokenService,
                totpService,
                loginAttemptService,
                loginRateLimiter,
                eventPublisher
        );
    }

    /**
     * Test: Login exitoso con credenciales validas.
     */
    @Test
    void shouldLoginSuccessfully_WhenCredentialsAreValid() {
        // Arrange
        String email = "user@example.com";
        String rawPassword = "password123";
        String hashedPassword = "hashed_password_123";
        String expectedToken = "jwt_token_12345";
        String expectedRefreshToken = "refresh_token_12345";
        String ipAddress = "10.0.0.1";

        LoginInputRequest request = new LoginInputRequest(email, rawPassword, ipAddress, null);
        User domainUser = createTestUser(email, hashedPassword);

        when(loginRateLimiter.checkLimit(ipAddress))
                .thenReturn(new com.homebanking.application.dto.security.RateLimitStatus(true, 0));
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(domainUser));
        when(passwordHasher.matches(rawPassword, hashedPassword))
                .thenReturn(true);
        when(tokenGenerator.generateToken(email))
                .thenReturn(expectedToken);
        when(refreshTokenService.generateRefreshToken(email))
                .thenReturn(expectedRefreshToken);

        // Act
        TokenOutputResponse result = loginUseCase.login(request);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(token -> {
                    assertThat(token.accessToken()).isEqualTo(expectedToken);
                    assertThat(token.refreshToken()).isEqualTo(expectedRefreshToken);
                });

        // Verify interactions
        verify(loginRateLimiter).checkLimit(ipAddress);
        verify(loginAttemptService).checkLoginAllowed(email);
        verify(loginAttemptService).handleSuccessfulAttempt(email, ipAddress);
        verify(loginAttemptService, never()).handleFailedAttempt(anyString(), anyString());
        verify(userRepository).findByEmail(email);
        verify(passwordHasher).matches(rawPassword, hashedPassword);
        verify(tokenGenerator).generateToken(email);
        verify(refreshTokenService).generateRefreshToken(email);
        verify(eventPublisher).publish(any(LoginAttemptedEvent.class));
    }

    /**
     * Test: Login falla si el usuario no existe.
     */
    @Test
    void shouldThrowException_WhenUserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        String password = "password123";
        String ipAddress = "10.0.0.2";
        LoginInputRequest request = new LoginInputRequest(email, password, ipAddress, null);

        when(loginRateLimiter.checkLimit(ipAddress))
                .thenReturn(new com.homebanking.application.dto.security.RateLimitStatus(true, 0));
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loginUseCase.login(request))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Credenciales");

        // Verify
        verify(loginRateLimiter).checkLimit(ipAddress);
        verify(loginAttemptService).checkLoginAllowed(email);
        verify(loginAttemptService).handleFailedAttempt(email, ipAddress);
        verify(loginAttemptService, never()).handleSuccessfulAttempt(anyString(), anyString());
        verify(userRepository).findByEmail(email);
        verify(passwordHasher, never()).matches(anyString(), anyString());
        verify(tokenGenerator, never()).generateToken(anyString());
        verify(eventPublisher).publish(any(LoginAttemptedEvent.class));
    }

    /**
     * Test: Login falla si la contrasena es incorrecta.
     */
    @Test
    void shouldThrowException_WhenPasswordIsWrong() {
        // Arrange
        String email = "user@example.com";
        String rawPassword = "wrongPassword";
        String hashedPassword = "correct_hash";
        String ipAddress = "10.0.0.3";

        LoginInputRequest request = new LoginInputRequest(email, rawPassword, ipAddress, null);
        User domainUser = createTestUser(email, hashedPassword);

        when(loginRateLimiter.checkLimit(ipAddress))
                .thenReturn(new com.homebanking.application.dto.security.RateLimitStatus(true, 0));
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(domainUser));
        when(passwordHasher.matches(rawPassword, hashedPassword))
                .thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> loginUseCase.login(request))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Credenciales");

        // Verify
        verify(loginRateLimiter).checkLimit(ipAddress);
        verify(loginAttemptService).checkLoginAllowed(email);
        verify(loginAttemptService).handleFailedAttempt(email, ipAddress);
        verify(loginAttemptService, never()).handleSuccessfulAttempt(anyString(), anyString());
        verify(tokenGenerator, never()).generateToken(anyString());
        verify(eventPublisher).publish(any(LoginAttemptedEvent.class));
    }

    @Test
    void shouldThrowRateLimitExceeded_WhenIpIsBlocked() {
        String email = "user@example.com";
        String rawPassword = "password123";
        String ipAddress = "10.0.0.9";

        LoginInputRequest request = new LoginInputRequest(email, rawPassword, ipAddress, null);

        when(loginRateLimiter.checkLimit(ipAddress))
                .thenReturn(new com.homebanking.application.dto.security.RateLimitStatus(false, 30));

        assertThatThrownBy(() -> loginUseCase.login(request))
                .isInstanceOf(RateLimitExceededException.class)
                .satisfies(ex -> {
                    RateLimitExceededException typed = (RateLimitExceededException) ex;
                    assertThat(typed.getRetryAfterSeconds()).isEqualTo(30);
                });

        verify(loginRateLimiter).checkLimit(ipAddress);
        verify(loginAttemptService, never()).checkLoginAllowed(anyString());
        verify(userRepository, never()).findByEmail(anyString());
        verify(eventPublisher).publish(any(LoginAttemptedEvent.class));
    }

    @Test
    void shouldRequireTotp_WhenEnabledAndMissingCode() {
        String email = "user@example.com";
        String rawPassword = "password123";
        String hashedPassword = "hashed_password_123";
        String ipAddress = "10.0.0.4";

        LoginInputRequest request = new LoginInputRequest(email, rawPassword, ipAddress, null);
        User domainUser = createTestUserWithTotp(email, hashedPassword, "JBSWY3DPEHPK3PXP", true);

        when(loginRateLimiter.checkLimit(ipAddress))
                .thenReturn(new com.homebanking.application.dto.security.RateLimitStatus(true, 0));
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(domainUser));
        when(passwordHasher.matches(rawPassword, hashedPassword))
                .thenReturn(true);

        assertThatThrownBy(() -> loginUseCase.login(request))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("TOTP");

        verify(loginAttemptService).handleFailedAttempt(email, ipAddress);
    }

    @Test
    void shouldRejectTotp_WhenEnabledAndInvalidCode() {
        String email = "user@example.com";
        String rawPassword = "password123";
        String hashedPassword = "hashed_password_123";
        String ipAddress = "10.0.0.5";

        LoginInputRequest request = new LoginInputRequest(email, rawPassword, ipAddress, "000000");
        User domainUser = createTestUserWithTotp(email, hashedPassword, "JBSWY3DPEHPK3PXP", true);

        when(loginRateLimiter.checkLimit(ipAddress))
                .thenReturn(new com.homebanking.application.dto.security.RateLimitStatus(true, 0));
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(domainUser));
        when(passwordHasher.matches(rawPassword, hashedPassword))
                .thenReturn(true);
        when(totpService.verifyCode("JBSWY3DPEHPK3PXP", "000000"))
                .thenReturn(false);

        assertThatThrownBy(() -> loginUseCase.login(request))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("TOTP");

        verify(loginAttemptService).handleFailedAttempt(email, ipAddress);
    }

    @Test
    void shouldLogin_WhenTotpEnabledAndValidCode() {
        String email = "user@example.com";
        String rawPassword = "password123";
        String hashedPassword = "hashed_password_123";
        String expectedToken = "jwt_token_12345";
        String expectedRefreshToken = "refresh_token_12345";
        String ipAddress = "10.0.0.6";

        LoginInputRequest request = new LoginInputRequest(email, rawPassword, ipAddress, "123456");
        User domainUser = createTestUserWithTotp(email, hashedPassword, "JBSWY3DPEHPK3PXP", true);

        when(loginRateLimiter.checkLimit(ipAddress))
                .thenReturn(new com.homebanking.application.dto.security.RateLimitStatus(true, 0));
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(domainUser));
        when(passwordHasher.matches(rawPassword, hashedPassword))
                .thenReturn(true);
        when(totpService.verifyCode("JBSWY3DPEHPK3PXP", "123456"))
                .thenReturn(true);
        when(tokenGenerator.generateToken(email))
                .thenReturn(expectedToken);
        when(refreshTokenService.generateRefreshToken(email))
                .thenReturn(expectedRefreshToken);

        TokenOutputResponse result = loginUseCase.login(request);

        assertThat(result.accessToken()).isEqualTo(expectedToken);
        assertThat(result.refreshToken()).isEqualTo(expectedRefreshToken);
        verify(loginAttemptService).handleSuccessfulAttempt(email, ipAddress);
    }

    // Metodo helper para crear usuarios de prueba
    private User createTestUser(String email, String hashedPassword) {
        return User.withId(
                UUID.randomUUID(),
                email,
                hashedPassword,
                "John",
                "Doe",
                "12345678",
                LocalDate.of(1990, 1, 1),
                "123 Street",
                java.time.LocalDateTime.now()
        );
    }

    private User createTestUserWithTotp(String email, String hashedPassword, String secret, boolean enabled) {
        return User.withId(
                UUID.randomUUID(),
                email,
                hashedPassword,
                "John",
                "Doe",
                "12345678",
                LocalDate.of(1990, 1, 1),
                "123 Street",
                java.time.LocalDateTime.now(),
                secret,
                enabled
        );
    }
}



