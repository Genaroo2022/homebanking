package com.homebanking.config;

import com.homebanking.application.usecase.account.DepositAccountUseCase;
import com.homebanking.application.usecase.account.DepositAccountUseCaseImpl;
import com.homebanking.port.out.AccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevAccountConfig {

    @Bean
    public DepositAccountUseCase depositAccountUseCase(AccountRepository accountRepository) {
        return new DepositAccountUseCaseImpl(accountRepository);
    }
}
