package com.homebanking.application.usecase.card;

import com.homebanking.application.dto.card.request.IssueCardInputRequest;
import com.homebanking.application.dto.card.response.CardOutputResponse;
import com.homebanking.application.mapper.CardMapper;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Card;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.enums.CardColor;
import com.homebanking.domain.enums.CardType;
import com.homebanking.port.out.account.AccountRepository;
import com.homebanking.port.out.card.CardRepository;
import com.homebanking.port.out.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardManagementUseCaseImplTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;

    private CardManagementUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CardManagementUseCaseImpl(
                cardRepository,
                accountRepository,
                userRepository,
                new CardMapper()
        );
    }

    @Test
    void shouldIssueAndListCardsForOwner() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Account account = Account.withId(
                accountId,
                userId,
                "1234567890123456789012",
                "owner.alias",
                new BigDecimal("12000.00"),
                LocalDateTime.now()
        );
        User user = User.withId(
                userId,
                "owner@test.com",
                "Password123!",
                "Owner",
                "User",
                "30111222",
                LocalDate.of(1990, 1, 1),
                "Address",
                LocalDateTime.now()
        );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            return Card.withId(
                    UUID.randomUUID(),
                    card.getAccountId(),
                    card.getNumber().value(),
                    card.getCvv().value(),
                    card.getCardHolder().value(),
                    card.getValidity().fromDate(),
                    card.getValidity().thruDate(),
                    card.getType(),
                    card.getColor(),
                    card.isActive()
            );
        });

        CardOutputResponse issued = useCase.issue(new IssueCardInputRequest(
                accountId,
                CardType.DEBIT,
                CardColor.GOLD,
                "owner@test.com"
        ));

        Card persistedCard = Card.withId(
                issued.id(),
                accountId,
                "4111111111111111",
                "123",
                "OWNER USER",
                LocalDate.now(),
                LocalDate.now().plusYears(5),
                CardType.DEBIT,
                CardColor.GOLD,
                true
        );
        when(cardRepository.findByAccountId(accountId)).thenReturn(List.of(persistedCard));

        List<CardOutputResponse> cards = useCase.getByAccount(accountId, "owner@test.com");

        assertThat(issued.maskedNumber()).contains("****");
        assertThat(cards).hasSize(1);
        assertThat(cards.getFirst().type()).isEqualTo("DEBIT");
    }
}
