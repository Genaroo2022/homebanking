package com.homebanking.application.usecase;

import com.homebanking.application.dto.registration.request.RegisterUserInputRequest;
import com.homebanking.application.dto.registration.response.UserRegisteredOutputResponse;
import com.homebanking.application.usecase.auth.RegisterUserUseCaseImpl;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.user.UserAlreadyExistsException;
import com.homebanking.port.out.AccountRepository;
import com.homebanking.port.out.PasswordHasher;
import com.homebanking.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para RegisterUserUseCaseImpl.

 * Demuestra:
 * - Testing con múltiples mocks
 * - ArgumentCaptor para capturar argumentos
 * - Validación de interacciones complejas
 */
@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private AccountRepository accountRepository;

    private RegisterUserUseCaseImpl registerUseCase;

    @BeforeEach
    void setUp() {
        registerUseCase = new RegisterUserUseCaseImpl(
                userRepository,
                passwordHasher,
                accountRepository
        );
    }

    /**
     * Test: Registro exitoso crea usuario y cuenta inicial.
     */
    @Test
    void shouldRegisterUserSuccessfully_AndCreateInitialAccount() {
        // Arrange
        RegisterUserInputRequest request = new RegisterUserInputRequest(
                "John",
                "Doe",
                "john@example.com",
                "password123",
                "12345678",
                LocalDate.of(1990, 1, 1),
                "123 Street"
        );

        User savedUser = User.withId(
                1L,
                request.email(),
                "hashed_password",
                request.name(),
                request.lastName(),
                request.dni(),
                request.birthDate(),
                request.address(),
                java.time.LocalDateTime.now()
        );

        when(userRepository.findByEmailOrDni(request.email(), request.dni()))
                .thenReturn(Optional.empty());
        when(passwordHasher.hash(request.password()))
                .thenReturn("hashed_password");
        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);
        when(accountRepository.save(any(Account.class)))
                .thenReturn(mock(Account.class));

        // Act
        UserRegisteredOutputResponse result = registerUseCase.register(request);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(response -> assertThat(response.userId()).isEqualTo(1L));

        // Verify: Usuario guardado con contraseña encriptada
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUserArg = userCaptor.getValue();
        assertThat(savedUserArg.getEmail().value()).isEqualTo(request.email());
        assertThat(savedUserArg.getPassword().value()).isEqualTo("hashed_password");

        // Verify: Cuenta inicial creada
        verify(accountRepository).save(any(Account.class));
    }

    /**
     * Test: Rechaza registro si email ya existe.
     */
    @Test
    void shouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        RegisterUserInputRequest request = new RegisterUserInputRequest(
                "John",
                "Doe",
                "john@example.com",
                "password123",
                "12345678",
                LocalDate.of(1990, 1, 1),
                "123 Street"
        );

        User existingUser = User.withId(
                1L,
                "john@example.com",
                "hashed123",
                "Jane",
                "Doe",
                "87654321",
                LocalDate.of(1995, 1, 1),
                "456 Street",
                java.time.LocalDateTime.now()
        );

        when(userRepository.findByEmailOrDni(request.email(), request.dni()))
                .thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> registerUseCase.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("email");

        // Verify: No se guardó usuario ni cuenta
        verify(userRepository, never()).save(any(User.class));
        verify(accountRepository, never()).save(any(Account.class));
    }
}

