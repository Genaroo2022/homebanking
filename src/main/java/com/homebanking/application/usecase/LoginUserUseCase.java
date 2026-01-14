package com.homebanking.application.usecase;

import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Intento de login con email no registrado: {}", email);
                    return new InvalidUserDataException(DomainErrorMessages.INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            log.warn("Intento de login fallido para usuario: {}", email);
            throw new InvalidUserDataException(DomainErrorMessages.INVALID_CREDENTIALS);
        }

        log.info("Login exitoso para usuario: {}", email);
        return user;
    }
}