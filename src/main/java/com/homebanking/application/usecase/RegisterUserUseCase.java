package com.homebanking.application.usecase;

import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.UserAlreadyExistsException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.out.AccountRepository;
import com.homebanking.port.out.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;


    public RegisterUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;

    }

    @Transactional
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
        User savedUser = userRepository.save(user);

        createInitialAccount(savedUser);

        return savedUser;
    }

    private void createInitialAccount(User user) {
        String randomCbu = generateRandomCbu();
        String randomAlias = generateRandomAlias(user.getName(), user.getLastName());

        Account newAccount = new Account(
                user.getId(),
                randomCbu,
                randomAlias,
                BigDecimal.ZERO
        );

        accountRepository.save(newAccount);
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