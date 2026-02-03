package com.homebanking.config;

import com.homebanking.application.service.auth.LoginAttemptService;
import com.homebanking.application.usecase.auth.LoginUserUseCaseImpl;
import com.homebanking.application.usecase.auth.LogoutUseCaseImpl;
import com.homebanking.application.usecase.auth.RefreshTokenUseCaseImpl;
import com.homebanking.application.usecase.auth.StartTotpSetupUseCaseImpl;
import com.homebanking.application.usecase.auth.EnableTotpUseCaseImpl;
import com.homebanking.application.usecase.auth.GetTotpProvisioningUriUseCaseImpl;
import com.homebanking.application.usecase.user.GetUserProfileUseCaseImpl;
import com.homebanking.application.usecase.auth.RegisterUserUseCaseImpl;
import com.homebanking.port.in.authentication.LoginUserInputPort;
import com.homebanking.port.in.authentication.LogoutInputPort;
import com.homebanking.port.in.authentication.RefreshTokenInputPort;
import com.homebanking.port.in.authentication.StartTotpSetupInputPort;
import com.homebanking.port.in.authentication.EnableTotpInputPort;
import com.homebanking.port.in.authentication.GetTotpProvisioningUriInputPort;
import com.homebanking.port.in.profile.GetUserProfileInputPort;
import com.homebanking.port.in.registration.RegisterUserInputPort;
import com.homebanking.port.out.account.AccountRepository;
import com.homebanking.port.out.auth.PasswordHasher;
import com.homebanking.port.out.auth.AccessTokenStore;
import com.homebanking.port.out.auth.RefreshTokenService;
import com.homebanking.port.out.auth.RefreshTokenStore;
import com.homebanking.port.out.auth.TokenGenerator;
import com.homebanking.port.out.auth.TotpService;
import com.homebanking.port.out.event.EventPublisher;
import com.homebanking.port.out.security.LoginRateLimiter;
import com.homebanking.port.out.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public LoginUserInputPort loginUserUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenGenerator tokenGenerator,
            RefreshTokenService refreshTokenService,
            TotpService totpService,
            LoginAttemptService loginAttemptService,
            LoginRateLimiter loginRateLimiter,
            EventPublisher eventPublisher) {
        return new LoginUserUseCaseImpl(
                userRepository,
                passwordHasher,
                tokenGenerator,
                refreshTokenService,
                totpService,
                loginAttemptService,
                loginRateLimiter,
                eventPublisher
        );
    }

    @Bean
    public RefreshTokenInputPort refreshTokenUseCase(
            RefreshTokenService refreshTokenService,
            TokenGenerator tokenGenerator,
            UserRepository userRepository,
            RefreshTokenStore refreshTokenStore) {
        return new RefreshTokenUseCaseImpl(
                refreshTokenService,
                tokenGenerator,
                userRepository,
                refreshTokenStore
        );
    }

    @Bean
    public LogoutInputPort logoutUseCase(
            RefreshTokenService refreshTokenService,
            RefreshTokenStore refreshTokenStore,
            AccessTokenStore accessTokenStore,
            TokenGenerator tokenGenerator) {
        return new LogoutUseCaseImpl(
                refreshTokenService,
                refreshTokenStore,
                accessTokenStore,
                tokenGenerator
        );
    }

    @Bean
    public StartTotpSetupInputPort startTotpSetupUseCase(
            UserRepository userRepository,
            TotpService totpService) {
        return new StartTotpSetupUseCaseImpl(userRepository, totpService);
    }

    @Bean
    public EnableTotpInputPort enableTotpUseCase(
            UserRepository userRepository,
            TotpService totpService) {
        return new EnableTotpUseCaseImpl(userRepository, totpService);
    }

    @Bean
    public GetTotpProvisioningUriInputPort getTotpProvisioningUriUseCase(
            UserRepository userRepository,
            TotpService totpService) {
        return new GetTotpProvisioningUriUseCaseImpl(userRepository, totpService);
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



