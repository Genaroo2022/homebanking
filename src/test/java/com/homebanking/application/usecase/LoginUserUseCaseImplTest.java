package com.homebanking.application.usecase;

import com.homebanking.application.dto.authentication.request.LoginInputRequest;
import com.homebanking.application.dto.authentication.response.TokenOutputResponse;
import com.homebanking.application.usecase.auth.LoginUserUseCaseImpl;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.port.out.PasswordHasher;
import com.homebanking.port.out.TokenGenerator;
import com.homebanking.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para LoginUserUseCaseImpl.

 * Ventajas de esta estructura:
 * - Sin dependencias de Spring
 * - Mocks con Mockito
 * - Ejecución rápida
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

    private LoginUserUseCaseImpl loginUseCase;

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUserUseCaseImpl(
                userRepository,
                passwordHasher,
                tokenGenerator
        );
    }

    /**
     * Test: Login exitoso con credenciales válidas.
     */
    @Test
    void shouldLoginSuccessfully_WhenCredentialsAreValid() {
        // Arrange
        String email = "user@example.com";
        String rawPassword = "password123";
        String hashedPassword = "hashed_password_123";
        String expectedToken = "jwt_token_12345";

        LoginInputRequest request = new LoginInputRequest(email, rawPassword);
        User domainUser = createTestUser(email, hashedPassword);

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
        verify(userRepository).findByEmail(email);
        verify(passwordHasher).matches(rawPassword, hashedPassword);
        verify(tokenGenerator).generateToken(email);
    }

    /**
     * Test: Login falla si el usuario no existe.
     */
    @Test
    void shouldThrowException_WhenUserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        String password = "password123";
        LoginInputRequest request = new LoginInputRequest(email, password);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loginUseCase.login(request))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Credenciales inválidas");

        // Verify
        verify(userRepository).findByEmail(email);
        verify(passwordHasher, never()).matches(anyString(), anyString());
        verify(tokenGenerator, never()).generateToken(anyString());
    }

    /**
     * Test: Login falla si la contraseña es incorrecta.
     */
    @Test
    void shouldThrowException_WhenPasswordIsWrong() {
        // Arrange
        String email = "user@example.com";
        String rawPassword = "wrongPassword";
        String hashedPassword = "correct_hash";

        LoginInputRequest request = new LoginInputRequest(email, rawPassword);
        User domainUser = createTestUser(email, hashedPassword);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(domainUser));
        when(passwordHasher.matches(rawPassword, hashedPassword))
                .thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> loginUseCase.login(request))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Credenciales inválidas");

        // Verify
        verify(tokenGenerator, never()).generateToken(anyString());
    }

    // Método helper para crear usuarios de prueba
    private User createTestUser(String email, String hashedPassword) {
        return User.withId(
                1L,
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

