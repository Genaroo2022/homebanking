package com.homebanking.config;

import com.homebanking.application.mapper.CardMapper;
import com.homebanking.application.usecase.card.CardManagementUseCaseImpl;
import com.homebanking.port.in.card.ActivateCardInputPort;
import com.homebanking.port.in.card.DeactivateCardInputPort;
import com.homebanking.port.in.card.GetCardsInputPort;
import com.homebanking.port.in.card.IssueCardInputPort;
import com.homebanking.port.out.account.AccountRepository;
import com.homebanking.port.out.card.CardRepository;
import com.homebanking.port.out.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CardConfig {

    @Bean
    public CardManagementUseCaseImpl cardManagementUseCase(
            CardRepository cardRepository,
            AccountRepository accountRepository,
            UserRepository userRepository,
            CardMapper cardMapper) {
        return new CardManagementUseCaseImpl(
                cardRepository,
                accountRepository,
                userRepository,
                cardMapper
        );
    }

    @Bean
    public IssueCardInputPort issueCardInputPort(CardManagementUseCaseImpl useCase) {
        return useCase;
    }

    @Bean
    public GetCardsInputPort getCardsInputPort(CardManagementUseCaseImpl useCase) {
        return useCase;
    }

    @Bean
    public ActivateCardInputPort activateCardInputPort(CardManagementUseCaseImpl useCase) {
        return useCase;
    }

    @Bean
    public DeactivateCardInputPort deactivateCardInputPort(CardManagementUseCaseImpl useCase) {
        return useCase;
    }
}

