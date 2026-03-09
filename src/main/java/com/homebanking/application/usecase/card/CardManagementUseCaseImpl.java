package com.homebanking.application.usecase.card;

import com.homebanking.application.dto.card.request.IssueCardInputRequest;
import com.homebanking.application.dto.card.response.CardOutputResponse;
import com.homebanking.application.mapper.CardMapper;
import com.homebanking.domain.entity.Account;
import com.homebanking.domain.entity.Card;
import com.homebanking.domain.entity.User;
import com.homebanking.domain.exception.account.AccountNotFoundException;
import com.homebanking.domain.exception.card.CardNotFoundException;
import com.homebanking.domain.exception.security.AccessDeniedException;
import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.card.ActivateCardInputPort;
import com.homebanking.port.in.card.DeactivateCardInputPort;
import com.homebanking.port.in.card.GetCardsInputPort;
import com.homebanking.port.in.card.IssueCardInputPort;
import com.homebanking.port.out.account.AccountRepository;
import com.homebanking.port.out.card.CardRepository;
import com.homebanking.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CardManagementUseCaseImpl implements
        IssueCardInputPort, GetCardsInputPort, ActivateCardInputPort, DeactivateCardInputPort {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final CardMapper mapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public CardOutputResponse issue(IssueCardInputRequest request) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        DomainErrorMessages.ACCOUNT_NOT_FOUND,
                        request.accountId()
                ));
        User owner = validateOwnership(account, request.requesterEmail());

        Card card = new Card(
                request.accountId(),
                generateValidCardNumber(),
                generateCvv(),
                owner.getName().value() + " " + owner.getLastName().value(),
                LocalDate.now(),
                LocalDate.now().plusYears(5),
                request.type(),
                request.color()
        );

        Card saved = cardRepository.save(card);
        return mapper.toDto(saved);
    }

    @Override
    public List<CardOutputResponse> getByAccount(UUID accountId, String requesterEmail) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(DomainErrorMessages.ACCOUNT_NOT_FOUND, accountId));
        validateOwnership(account, requesterEmail);
        return cardRepository.findByAccountId(accountId).stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional
    public CardOutputResponse activate(UUID cardId, String requesterEmail) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(DomainErrorMessages.CARD_NOT_FOUND, cardId));
        Account account = accountRepository.findById(card.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(DomainErrorMessages.ACCOUNT_NOT_FOUND, card.getAccountId()));
        validateOwnership(account, requesterEmail);
        card.activate();
        return mapper.toDto(cardRepository.save(card));
    }

    @Override
    @Transactional
    public CardOutputResponse deactivate(UUID cardId, String requesterEmail) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(DomainErrorMessages.CARD_NOT_FOUND, cardId));
        Account account = accountRepository.findById(card.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(DomainErrorMessages.ACCOUNT_NOT_FOUND, card.getAccountId()));
        validateOwnership(account, requesterEmail);
        card.deactivate();
        return mapper.toDto(cardRepository.save(card));
    }

    private User validateOwnership(Account account, String requesterEmail) {
        if (requesterEmail == null || requesterEmail.isBlank()) {
            throw new InvalidUserDataException(DomainErrorMessages.USER_NOT_FOUND);
        }
        User user = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new InvalidUserDataException(DomainErrorMessages.USER_NOT_FOUND));
        if (!account.getUserId().equals(user.getId())) {
            throw new AccessDeniedException(DomainErrorMessages.ACCESS_DENIED);
        }
        return user;
    }

    private String generateCvv() {
        return String.valueOf(100 + secureRandom.nextInt(900));
    }

    private String generateValidCardNumber() {
        StringBuilder builder = new StringBuilder("4");
        while (builder.length() < 15) {
            builder.append(secureRandom.nextInt(10));
        }
        int checkDigit = computeLuhnCheckDigit(builder.toString());
        builder.append(checkDigit);
        return builder.toString();
    }

    private int computeLuhnCheckDigit(String partialCardNumber) {
        int sum = 0;
        boolean shouldDouble = true;
        for (int i = partialCardNumber.length() - 1; i >= 0; i--) {
            int digit = partialCardNumber.charAt(i) - '0';
            if (shouldDouble) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            shouldDouble = !shouldDouble;
        }
        return (10 - (sum % 10)) % 10;
    }
}
