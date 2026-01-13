package com.homebanking.application.usecase;

import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.InvalidUserDataException;
import com.homebanking.domain.exception.UserAlreadyExistsException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.out.UserRepository;

import java.util.Optional;

public class RegisterUserUseCase {

    private final UserRepository userRepository;

    public RegisterUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(User user) {
        // NOTE: For now, validation logic is kept here for simplicity (KISS).
        // If validation rules grow complex, extract to a separate UserValidator strategy.

        // 1. Buscamos si existe
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

        // 3. Si no existe, guardamos
        return userRepository.save(user);
    }
}