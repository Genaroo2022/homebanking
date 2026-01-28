package com.homebanking.domain.entity;

import com.homebanking.domain.exception.account.InvalidAccountDataException;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.domain.valueobject.account.AccountAlias;
import com.homebanking.domain.valueobject.account.AccountBalance;
import com.homebanking.domain.valueobject.common.Cbu;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    private Long id;
    private Long userId;
    private Cbu cbu;
    private AccountAlias alias;
    private AccountBalance balance;
    private LocalDateTime createdAt;

    // To create a new card (without ID)
    public Account(Long userId, String cbu, String alias, BigDecimal balance) {
        validateAccountData(userId, cbu, alias, balance);

        this.userId = userId;
        this.cbu = createCbu(cbu);
        this.alias = AccountAlias.of(alias);
        this.balance = AccountBalance.of(balance);
        this.createdAt = LocalDateTime.now();
    }

    // Factory Method: Reconstitution from Persistence
    public static Account withId(Long id, Long userId, String cbu, String alias, BigDecimal balance, LocalDateTime createdAt) {
        validateStructuralData(id, createdAt);
        validateAccountData(userId, cbu, alias, balance);
        return hydrate(id, userId, createCbu(cbu), AccountAlias.of(alias), AccountBalance.of(balance), createdAt);
    }
    private static Account hydrate(Long id, Long userId, Cbu cbu, AccountAlias alias, AccountBalance balance, LocalDateTime createdAt) {
        Account account = new Account();
        account.id = id;
        account.userId = userId;
        account.cbu = cbu;
        account.alias = alias;
        account.balance = balance;
        account.createdAt = createdAt;
        return account;
    }

    // --- BUSINESS METHODS ---

    public void deposit(BigDecimal amount) {
        validatePositiveAmount(amount, DomainErrorMessages.DEPOSIT_AMOUNT_MUST_BE_POSITIVE);
        BigDecimal newBalance = this.balance.value().add(amount);
        this.balance = AccountBalance.of(newBalance);
    }

    public void debit(BigDecimal amount) {
        validatePositiveAmount(amount, DomainErrorMessages.DEBIT_AMOUNT_MUST_BE_POSITIVE);
        BigDecimal newBalance = this.balance.value().subtract(amount);
        this.balance = AccountBalance.of(newBalance);
    }

    // --- VALIDATIONS (Private Static) ---

    private static void validateStructuralData(Long id, LocalDateTime createdAt) {
        if (id == null) {
            throw new InvalidAccountDataException(DomainErrorMessages.ID_REQUIRED);
        }
        if (createdAt == null) {
            throw new InvalidAccountDataException(DomainErrorMessages.CREATED_AT_REQUIRED);
        }
    }

    private static void validateAccountData(Long userId, String cbu, String alias, BigDecimal balance) {
        validateMandatoryFields(userId, cbu, alias, balance);
        validateUserId(userId);
    }

    private static void validateNonBlankField(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new InvalidAccountDataException(errorMessage);
        }
    }

    private static void validatePositiveAmount(BigDecimal amount, String errorMessage) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAccountDataException(errorMessage);
        }
    }

    private static void validateMandatoryFields(Long userId, String cbu, String alias, BigDecimal balance) {
        if (userId == null) {
            throw new InvalidAccountDataException(DomainErrorMessages.USER_ID_REQUIRED);
        }
        validateNonBlankField(cbu, DomainErrorMessages.CBU_REQUIRED);
        validateNonBlankField(alias, DomainErrorMessages.ALIAS_REQUIRED);
        if (balance == null) {
            throw new InvalidAccountDataException(DomainErrorMessages.BALANCE_REQUIRED);
        }
    }

    private static void validateUserId(Long userId) {
        if (userId <= 0) {
            throw new InvalidAccountDataException(DomainErrorMessages.USER_ID_INVALID);
        }
    }

    private static Cbu createCbu(String value) {
        try {
            return Cbu.of(value);
        } catch (InvalidTransferDataException ex) {
            throw new InvalidAccountDataException(ex.getMessage());
        }
    }
}

