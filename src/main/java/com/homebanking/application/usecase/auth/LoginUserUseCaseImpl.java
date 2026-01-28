package com.homebanking.application.usecase.auth;

import com.homebanking.application.dto.authentication.request.LoginInputRequest;
import com.homebanking.application.dto.authentication.response.TokenOutputResponse;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.authentication.LoginUserInputPort;
import com.homebanking.port.out.UserRepository;
import com.homebanking.port.out.PasswordHasher;
import com.homebanking.port.out.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del caso de uso de autenticación de usuario.

 * Responsabilidades:
 * - Validar credenciales contra el repositorio
 * - Verificar contraseña contra hash almacenado
 * - Generar token JWT en caso de éxito

 * No tiene dependencias de Spring Web, fácil de testear.
 */
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LoginUserUseCaseImpl implements LoginUserInputPort {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenGenerator tokenGenerator;

    /**
     * Autentica un usuario validando credenciales y generando token.
     *
     * @param request Email y contraseña del usuario
     * @return Token JWT si la autenticación es exitosa
     * @throws InvalidUserDataException Si el usuario no existe o credenciales son inválidas
     */
    @Override
    public TokenOutputResponse login(LoginInputRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Intento de login con email no registrado: {}", request.email());
                    return new InvalidUserDataException(DomainErrorMessages.INVALID_CREDENTIALS);
                });

        if (!passwordHasher.matches(request.password(), user.getPassword().value())) {
            log.warn("Intento de login fallido para usuario: {}", request.email());
            throw new InvalidUserDataException(DomainErrorMessages.INVALID_CREDENTIALS);
        }

        String token = tokenGenerator.generateToken(user.getEmail().value());
        log.info("Login exitoso para usuario: {}", request.email());

        return new TokenOutputResponse(token);
    }
}

