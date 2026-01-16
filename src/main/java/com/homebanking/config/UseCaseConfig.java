package com.homebanking.config;

import com.homebanking.application.usecase.LoginUserUseCaseImpl;
import com.homebanking.application.usecase.GetUserProfileUseCaseImpl;
import com.homebanking.application.usecase.RegisterUserUseCaseImpl;
import com.homebanking.port.in.authentication.LoginUserInputPort;
import com.homebanking.port.in.authentication.GetUserProfileInputPort;
import com.homebanking.port.in.registration.RegisterUserInputPort;
import com.homebanking.port.out.AccountRepository;
import com.homebanking.port.out.UserRepository;
import com.homebanking.adapter.in.web.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UseCaseConfig {

    @Bean
    public LoginUserInputPort loginUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        return new LoginUserUseCaseImpl(userRepository, passwordEncoder, jwtService);
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
            PasswordEncoder passwordEncoder,
            AccountRepository accountRepository) {
        return new RegisterUserUseCaseImpl(userRepository, passwordEncoder, accountRepository);
    }
}