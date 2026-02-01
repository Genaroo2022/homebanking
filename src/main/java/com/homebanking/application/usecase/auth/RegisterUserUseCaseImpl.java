package com.homebanking.application.usecase.auth;

import com.homebanking.application.dto.registration.request.RegisterUserInputRequest;
import com.homebanking.application.dto.registration.response.UserRegisteredOutputResponse;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.user.UserAlreadyExistsException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.registration.RegisterUserInputPort;
import com.homebanking.port.out.account.AccountRepository;
import com.homebanking.port.out.auth.PasswordHasher;
import com.homebanking.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementación del caso de uso de registro de usuario.

 * Responsabilidades:
 * - Validar que email y DNI no estén duplicados
 * - Crear nuevo usuario con contraseña encriptada
 * - Crear cuenta bancaria inicial con saldo cero

 * Nota: Esta clase NO utiliza herencia (Liskov Substitution).
 */
@RequiredArgsConstructor
@Slf4j
public class RegisterUserUseCaseImpl implements RegisterUserInputPort {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AccountRepository accountRepository;

    /**
     * Registra un nuevo usuario en el sistema.

     * Flujo:
     * 1. Validar que email y DNI sean únicos
     * 2. Encriptar contraseña
     * 3. Guardar usuario en base de datos
     * 4. Crear cuenta bancaria inicial
     *
     * @param request Datos del nuevo usuario
     * @return ID del usuario creado
     * @throws UserAlreadyExistsException Si email o DNI ya existen
     */
    @Override
    @Transactional
    public UserRegisteredOutputResponse register(RegisterUserInputRequest request) {
        validateUniqueEmailAndDni(request.email(), request.dni());

        User newUser = new User(
                request.email(),
                request.password(),
                request.name(),
                request.lastName(),
                request.dni(),
                request.birthDate(),
                request.address()
        );

        String encodedPassword = passwordHasher.hash(request.password());
        newUser.changePassword(encodedPassword);

        User savedUser = userRepository.save(newUser);
        log.info("Usuario registrado exitosamente: {}", savedUser.getEmail().value());

        createInitialAccount(savedUser);

        return new UserRegisteredOutputResponse(savedUser.getId());
    }

    /**
     * Valida que el email y DNI sean únicos en el sistema.
     *
     * @throws UserAlreadyExistsException Si email o DNI ya existen
     */
    private void validateUniqueEmailAndDni(String email, String dni) {
        userRepository.findByEmailOrDni(email, dni)
                .ifPresent(existingUser -> {
                    if (existingUser.getEmail().value().equalsIgnoreCase(email)) {
                        throw new UserAlreadyExistsException(
                                DomainErrorMessages.EMAIL_ALREADY_EXISTS);
                    }
                    if (existingUser.getDni().value().equals(dni)) {
                        throw new UserAlreadyExistsException(
                                DomainErrorMessages.DNI_ALREADY_EXISTS);
                    }
                });
    }

    /**
     * Crea una cuenta bancaria inicial para el nuevo usuario.

     * Genera:
     * - CBU: 22 dígitos aleatorios
     * - Alias: nombre.apellido.números (ej: juan.perez.123)
     * - Balance: 0.00
     */
    private void createInitialAccount(User user) {
        String randomCbu = generateRandomCbu();
        String randomAlias = generateRandomAlias(user.getName().value(), user.getLastName().value());

        Account newAccount = new Account(
                user.getId(),
                randomCbu,
                randomAlias,
                BigDecimal.ZERO
        );

        accountRepository.save(newAccount);
        log.debug("Cuenta inicial creada para usuario: {}", user.getEmail().value());
    }

    private String generateRandomCbu() {
        StringBuilder cbu = new StringBuilder();
        for (int i = 0; i < 22; i++) {
            cbu.append(ThreadLocalRandom.current().nextInt(0, 10));
        }
        return cbu.toString();
    }

    private String generateRandomAlias(String name, String lastName) {
        String cleanName = name.toLowerCase().replaceAll("[^a-z]", "");
        String cleanLastName = lastName.toLowerCase().replaceAll("[^a-z]", "");
        int randomSuffix = ThreadLocalRandom.current().nextInt(100, 999);
        return cleanName + "." + cleanLastName + "." + randomSuffix;
    }
}



