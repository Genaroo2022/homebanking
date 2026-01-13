package com.homebanking.config;

import com.homebanking.application.usecase.RegisterUserUseCase;
import com.homebanking.port.out.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepository userRepository) {
        return new RegisterUserUseCase(userRepository);
    }
}