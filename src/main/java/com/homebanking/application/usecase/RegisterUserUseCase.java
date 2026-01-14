package com.homebanking.application.usecase;

import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.UserAlreadyExistsException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.out.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(User user) {

        Optional<User> existingUserOpt = userRepository.findByEmailOrDni(user.getEmail(), user.getDni());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            if (existingUser.getEmail().equalsIgnoreCase(user.getEmail())) {
                throw new UserAlreadyExistsException(DomainErrorMessages.EMAIL_ALREADY_EXISTS);
            }

            if (existingUser.getDni().equals(user.getDni())) {
                throw new UserAlreadyExistsException(DomainErrorMessages.DNI_ALREADY_EXISTS);
            }
        }
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.changePassword(encodedPassword);

        return userRepository.save(user);
    }
}