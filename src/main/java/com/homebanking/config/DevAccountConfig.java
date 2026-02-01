package com.homebanking.config;

import com.homebanking.application.usecase.account.DepositAccountUseCaseImpl;
import com.homebanking.port.in.account.DepositAccountInputPort;
import com.homebanking.port.out.account.AccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevAccountConfig {

    @Bean
    public DepositAccountInputPort depositAccountUseCase(AccountRepository accountRepository) {
        return new DepositAccountUseCaseImpl(accountRepository);
    }
}


