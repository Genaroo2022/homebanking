package com.homebanking.domain.entity;

import com.homebanking.domain.exception.account.InsufficientFundsException;
import com.homebanking.domain.exception.account.InvalidAccountDataException;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.exception.transfer.SameAccountTransferException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.domain.valueobject.account.AccountAlias;
import com.homebanking.domain.valueobject.account.AccountBalance;
import com.homebanking.domain.valueobject.common.Cbu;
import com.homebanking.domain.valueobject.transfer.IdempotencyKey;
import com.homebanking.domain.valueobject.transfer.TransferAmount;
import com.homebanking.domain.valueobject.transfer.TransferDescription;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    private UUID id;
    private UUID userId;
    private Cbu cbu;
    private AccountAlias alias;
    private AccountBalance balance;
    private LocalDateTime createdAt;

    // To create a new card (without ID)
    public Account(UUID userId, String cbu, String alias, BigDecimal balance) {
        validateAccountData(userId, cbu, alias, balance);

        this.userId = userId;
        this.cbu = createCbu(cbu);
        this.alias = AccountAlias.of(alias);
        this.balance = AccountBalance.of(balance);
        this.createdAt = LocalDateTime.now();
    }

    // Factory Method: Reconstitution from Persistence
    public static Account withId(UUID id, UUID userId, String cbu, String alias, BigDecimal balance, LocalDateTime createdAt) {
        validateStructuralData(id, createdAt);
        validateAccountData(userId, cbu, alias, balance);
        return hydrate(id, userId, createCbu(cbu), AccountAlias.of(alias), AccountBalance.of(balance), createdAt);
    }
    private static Account hydrate(UUID id, UUID userId, Cbu cbu, AccountAlias alias, AccountBalance balance, LocalDateTime createdAt) {
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
        if (this.balance.value().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    DomainErrorMessages.INSUFFICIENT_FUNDS,
                    this.id,
                    amount,
                    this.balance.value()
            );
        }
        BigDecimal newBalance = this.balance.value().subtract(amount);
        this.balance = AccountBalance.of(newBalance);
    }

    public Transfer initiateTransferTo(Cbu targetCbu,
                                       TransferAmount amount,
                                       TransferDescription description,
                                       IdempotencyKey idempotencyKey) {

        // 1. Validación de Invariante: No transferir a la misma cuenta.
        // Comparamos Value Objects (CBU), no IDs de base de datos.
        if (this.cbu.equals(targetCbu)) {
            throw new SameAccountTransferException(DomainErrorMessages.TRANSFER_SAME_ACCOUNT);
        }

        // 2. Modificación de estado (Solo modifico MI estado)
        this.debit(amount.value());

        // 3. Creación del nuevo Agregado (Transfer)
        return Transfer.create(
                this.getId(),
                targetCbu, // Pasamos el VO directamente
                amount,
                description,
                idempotencyKey
        );
    }

    // --- VALIDATIONS (Private Static) ---

    private static void validateStructuralData(UUID id, LocalDateTime createdAt) {
        if (id == null) {
            throw new InvalidAccountDataException(DomainErrorMessages.ID_REQUIRED);
        }
        if (createdAt == null) {
            throw new InvalidAccountDataException(DomainErrorMessages.CREATED_AT_REQUIRED);
        }
    }

    private static void validateAccountData(UUID userId, String cbu, String alias, BigDecimal balance) {
        validateMandatoryFields(userId, cbu, alias, balance);
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

    private static void validateMandatoryFields(UUID userId, String cbu, String alias, BigDecimal balance) {
        if (userId == null) {
            throw new InvalidAccountDataException(DomainErrorMessages.USER_ID_REQUIRED);
        }
        validateNonBlankField(cbu, DomainErrorMessages.CBU_REQUIRED);
        validateNonBlankField(alias, DomainErrorMessages.ALIAS_REQUIRED);
        if (balance == null) {
            throw new InvalidAccountDataException(DomainErrorMessages.BALANCE_REQUIRED);
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


