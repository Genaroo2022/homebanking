package com.homebanking.domain.entity;

import com.homebanking.domain.exception.InvalidAccountDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    public static final int CBU_LENGTH = 22;
    private static final String ALIAS_REGEX = "^[a-zA-Z0-9.]{6,20}$";
    private static final String CBU_REGEX = "^\\d+$";

    private Long id;
    private Long userId;
    private String cbu;
    private String alias;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    // To create a new card (without ID)
    public Account(Long userId, String cbu, String alias, BigDecimal balance) {
        validateAccountData(userId, cbu, alias, balance);

        this.userId = userId;
        this.cbu = cbu;
        this.alias = alias;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
    }

    // Factory Method: Reconstitution from Persistence
    public static Account withId(Long id, Long userId, String cbu, String alias, BigDecimal balance, LocalDateTime createdAt) {
        validateStructuralData(id, createdAt);
        validateAccountData(userId, cbu, alias, balance);
        return hydrate(id, userId, cbu, alias, balance, createdAt);
    }
    private static Account hydrate(Long id, Long userId, String cbu, String alias, BigDecimal balance, LocalDateTime createdAt) {
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
        BigDecimal newBalance = this.balance.add(amount);
        validateBalance(newBalance);
        this.balance = newBalance;
    }

    public void debit(BigDecimal amount) {
        validatePositiveAmount(amount, DomainErrorMessages.DEBIT_AMOUNT_MUST_BE_POSITIVE);
        BigDecimal newBalance = this.balance.subtract(amount);
        validateBalance(newBalance);
        this.balance = newBalance;
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
        validateCbuFormat(cbu);
        validateAliasFormat(alias);
        validateBalance(balance);
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

    private static void validateCbuFormat(String cbu) {
        if (!Pattern.matches(CBU_REGEX, cbu)) {
            throw new InvalidAccountDataException(DomainErrorMessages.CBU_ONLY_NUMBERS);
        }
        if (cbu.length() != CBU_LENGTH) {
            throw new InvalidAccountDataException(DomainErrorMessages.CBU_INVALID_LENGTH);
        }
    }

    private static void validateAliasFormat(String alias) {
        if (!Pattern.matches(ALIAS_REGEX, alias)) {
            throw new InvalidAccountDataException(DomainErrorMessages.ALIAS_INVALID_FORMAT);
        }
    }

    private static void validateBalance(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAccountDataException(DomainErrorMessages.ACCOUNT_BALANCE_NEGATIVE);
        }
    }
}