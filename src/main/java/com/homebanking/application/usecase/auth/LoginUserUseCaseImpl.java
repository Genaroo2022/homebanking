package com.homebanking.application.usecase.auth;

import com.homebanking.application.dto.authentication.request.LoginInputRequest;
import com.homebanking.application.dto.authentication.response.TokenOutputResponse;
import com.homebanking.application.dto.security.RateLimitStatus;
import com.homebanking.application.exception.RateLimitExceededException;
import com.homebanking.application.service.auth.LoginAttemptService;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.exception.user.TooManyLoginAttemptsException;
import com.homebanking.domain.event.LoginAttemptedEvent;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.domain.valueobject.user.UserEmail;
import com.homebanking.port.in.authentication.LoginUserInputPort;
import com.homebanking.port.out.EventPublisher;
import com.homebanking.port.out.LoginRateLimiter;
import com.homebanking.port.out.UserRepository;
import com.homebanking.port.out.PasswordHasher;
import com.homebanking.port.out.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
/**
 * Implementacion del caso de uso de autenticacion de usuario.

 * Responsabilidades:
 * - Validar credenciales contra el repositorio
 * - Verificar contrasena contra hash almacenado
 * - Generar token JWT en caso de exito

 * No tiene dependencias de Spring Web, facil de testear.
 */
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LoginUserUseCaseImpl implements LoginUserInputPort {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenGenerator tokenGenerator;
    private final LoginAttemptService loginAttemptService;
    private final LoginRateLimiter loginRateLimiter;
    private final EventPublisher eventPublisher;

    /**
     * Autentica un usuario validando credenciales y generando token.
     *
     * @param request Email y contrasena del usuario
     * @return Token JWT si la autenticacion es exitosa
     * @throws InvalidUserDataException Si el usuario no existe o credenciales son invalidas
     */
    @Override
    public TokenOutputResponse login(LoginInputRequest request) {
        RateLimitStatus rateLimitStatus = loginRateLimiter.checkLimit(request.ipAddress());
        if (!rateLimitStatus.allowed()) {
            publishLoginAttemptedEvent(request, false, true);
            throw new RateLimitExceededException(
                    "Demasiados intentos de login desde esta IP. Intente mas tarde.",
                    rateLimitStatus.retryAfterSeconds()
            );
        }

        try {
            loginAttemptService.checkLoginAllowed(request.email());
        } catch (TooManyLoginAttemptsException ex) {
            publishLoginAttemptedEvent(request, false, true);
            throw ex;
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Intento de login con email no registrado: {} desde IP: {}",
                            request.email(), request.ipAddress());
                    loginAttemptService.handleFailedAttempt(request.email(), request.ipAddress());
                    publishLoginAttemptedEvent(request, false, false);
                    return new InvalidUserDataException(DomainErrorMessages.INVALID_CREDENTIALS);
                });

        if (!passwordHasher.matches(request.password(), user.getPassword().value())) {
            log.warn("Intento de login fallido para usuario: {} desde IP: {}",
                    request.email(), request.ipAddress());
            loginAttemptService.handleFailedAttempt(request.email(), request.ipAddress());
            publishLoginAttemptedEvent(request, false, false);
            throw new InvalidUserDataException(DomainErrorMessages.INVALID_CREDENTIALS);
        }

        loginAttemptService.handleSuccessfulAttempt(request.email(), request.ipAddress());
        String token = tokenGenerator.generateToken(user.getEmail().value());
        log.info("Login exitoso para usuario: {} desde IP: {}", request.email(), request.ipAddress());
        publishLoginAttemptedEvent(request, true, false);

        return new TokenOutputResponse(token);
    }

    private void publishLoginAttemptedEvent(LoginInputRequest request, boolean successful, boolean blocked) {
        eventPublisher.publish(new LoginAttemptedEvent(
                UserEmail.of(request.email()),
                request.ipAddress(),
                successful,
                blocked,
                LocalDateTime.now()
        ));
    }
}
