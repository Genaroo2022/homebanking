package com.homebanking.application.usecase;

import com.homebanking.application.dto.authentication.request.LoginInputRequest;
import com.homebanking.application.dto.authentication.response.TokenOutputResponse;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.InvalidUserDataException;
import com.homebanking.port.out.UserRepository;
import com.homebanking.adapter.in.web.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private LoginUserUseCaseImpl loginUseCase;

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUserUseCaseImpl(
                userRepository,
                passwordEncoder,
                jwtService
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
        when(passwordEncoder.matches(rawPassword, hashedPassword))
                .thenReturn(true);
        when(jwtService.generateToken(email))
                .thenReturn(expectedToken);

        // Act
        TokenOutputResponse result = loginUseCase.login(request);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(token -> assertThat(token.token()).isEqualTo(expectedToken));


        // Verify interactions
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, hashedPassword);
        verify(jwtService).generateToken(email);
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
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString());
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
        when(passwordEncoder.matches(rawPassword, hashedPassword))
                .thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> loginUseCase.login(request))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Credenciales inválidas");

        // Verify
        verify(jwtService, never()).generateToken(anyString());
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
