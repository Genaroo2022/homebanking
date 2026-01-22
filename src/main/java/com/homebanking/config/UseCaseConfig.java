package com.homebanking.config;

import com.homebanking.application.usecase.LoginUserUseCaseImpl;
import com.homebanking.application.usecase.GetUserProfileUseCaseImpl;
import com.homebanking.application.usecase.RegisterUserUseCaseImpl;
import com.homebanking.port.in.authentication.LoginUserInputPort;
import com.homebanking.port.in.authentication.GetUserProfileInputPort;
import com.homebanking.port.in.registration.RegisterUserInputPort;
import com.homebanking.port.out.AccountRepository;
import com.homebanking.port.out.PasswordHasher;
import com.homebanking.port.out.TokenGenerator;
import com.homebanking.port.out.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public LoginUserInputPort loginUserUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenGenerator tokenGenerator) {
        return new LoginUserUseCaseImpl(userRepository, passwordHasher, tokenGenerator);
    }

    @Bean
    public GetUserProfileInputPort getUserProfileUseCase(
            UserRepository userRepository,
            AccountRepository accountRepository) {
        return new GetUserProfileUseCaseImpl(userRepository, accountRepository);
    }

    @Bean
    public RegisterUserInputPort registerUserUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            AccountRepository accountRepository) {
        return new RegisterUserUseCaseImpl(userRepository, passwordHasher, accountRepository);
    }
}
