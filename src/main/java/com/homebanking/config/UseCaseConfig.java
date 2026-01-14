package com.homebanking.config;

import com.homebanking.application.usecase.RegisterUserUseCase;
import com.homebanking.port.out.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UseCaseConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return new RegisterUserUseCase(userRepository, passwordEncoder);
    }
}