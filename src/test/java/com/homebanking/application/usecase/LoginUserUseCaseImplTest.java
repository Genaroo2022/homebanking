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
import com.homebanking.port.out.security.LoginRateLimiter;
import com.homebanking.port.out.auth.TokenGenerator;
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
        String ipAddress = "10.0.0.1";

        LoginInputRequest request = new LoginInputRequest(email, rawPassword, ipAddress);
        User domainUser = createTestUser(email, hashedPassword);

        when(loginRateLimiter.checkLimit(ipAddress))
                .thenReturn(new com.homebanking.application.dto.security.RateLimitStatus(true, 0));
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(domainUser));
        when(passwordHasher.matches(rawPassword, hashedPassword))
                .thenReturn(true);
        when(tokenGenerator.generateToken(email))
                .thenReturn(expectedToken);

        // Act
        TokenOutputResponse result = loginUseCase.login(request);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(token -> assertThat(token.token()).isEqualTo(expectedToken));

        // Verify interactions
        verify(loginRateLimiter).checkLimit(ipAddress);
        verify(loginAttemptService).checkLoginAllowed(email);
        verify(loginAttemptService).handleSuccessfulAttempt(email, ipAddress);
        verify(loginAttemptService, never()).handleFailedAttempt(anyString(), anyString());
        verify(userRepository).findByEmail(email);
        verify(passwordHasher).matches(rawPassword, hashedPassword);
        verify(tokenGenerator).generateToken(email);
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
        LoginInputRequest request = new LoginInputRequest(email, password, ipAddress);

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

        LoginInputRequest request = new LoginInputRequest(email, rawPassword, ipAddress);
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

        LoginInputRequest request = new LoginInputRequest(email, rawPassword, ipAddress);

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
}



