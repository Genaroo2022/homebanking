package com.homebanking.domain.entity;

import com.homebanking.domain.exception.InvalidAccountDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public Account(Long id, Long userId, String cbu, String alias, BigDecimal balance) {

        validateMandatoryFields(userId, cbu, alias, balance);
        validateCbuFormat(cbu);
        validateAliasFormat(alias);
        validateBalance(balance);


        this.id = id;
        this.userId = userId;
        this.cbu = cbu;
        this.alias = alias;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
    }
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

    private void validateNonBlankField(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new InvalidAccountDataException(errorMessage);
        }
    }

    private void validatePositiveAmount(BigDecimal amount, String errorMessage) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAccountDataException(errorMessage);
        }
    }


    private void validateMandatoryFields(Long userId, String cbu, String alias, BigDecimal balance) {
        if (userId == null) {
            throw new InvalidAccountDataException(DomainErrorMessages.USER_ID_REQUIRED);
        }
        validateNonBlankField(cbu, DomainErrorMessages.CBU_REQUIRED);
        validateNonBlankField(alias, DomainErrorMessages.ALIAS_REQUIRED);
        if (balance == null) {
            throw new InvalidAccountDataException(DomainErrorMessages.BALANCE_REQUIRED);
        }
    }

    private void validateCbuFormat(String cbu) {

        if (!Pattern.matches(CBU_REGEX, cbu)) {
            throw new InvalidAccountDataException(DomainErrorMessages.CBU_ONLY_NUMBERS);
        }

        if (cbu.length() != CBU_LENGTH) {
            throw new InvalidAccountDataException(DomainErrorMessages.CBU_INVALID_LENGTH);
        }
    }

    private void validateAliasFormat(String alias) {
        if (!Pattern.matches(ALIAS_REGEX, alias)) {
            throw new InvalidAccountDataException(DomainErrorMessages.ALIAS_INVALID_FORMAT);
        }
    }

    private void validateBalance(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAccountDataException(DomainErrorMessages.ACCOUNT_BALANCE_NEGATIVE);
        }
    }
}