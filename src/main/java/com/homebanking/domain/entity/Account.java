package com.homebanking.domain.entity;

import com.homebanking.domain.exception.InvalidAccountDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
public class Account {

    // --- CONSTANTES DE NEGOCIO ---
    public static final int CBU_LENGTH = 22;
    // Alias: Entre 6 y 20 caracteres. Letras, números y puntos.
    // Explicación Regex: ^[a-zA-Z0-9.]{6,20}$
    private static final String ALIAS_REGEX = "^[a-zA-Z0-9.]{6,20}$";
    private static final String CBU_REGEX = "^\\d+$"; // Solo números

    private Long id;
    private Long userId;
    private String cbu;
    private String alias;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    public Account(Long id, Long userId, String cbu, String alias, BigDecimal balance) {
        // 1. Validaciones
        validateMandatoryFields(userId, cbu, alias, balance);
        validateCbuFormat(cbu);
        validateAliasFormat(alias);
        validateBalance(balance);

        // 2. Asignaciones
        this.id = id;
        this.userId = userId;
        this.cbu = cbu;
        this.alias = alias;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
    }

    // --- MÉTODOS PRIVADOS ---

    private void validateMandatoryFields(Long userId, String cbu, String alias, BigDecimal balance) {
        if (userId == null ||
                cbu == null || cbu.isBlank() ||
                alias == null || alias.isBlank() ||
                balance == null) {
            throw new InvalidAccountDataException(DomainErrorMessages.ACCOUNT_MANDATORY_FIELDS);
        }
    }

    private void validateCbuFormat(String cbu) {
        // Validación 1: Solo números
        if (!Pattern.matches(CBU_REGEX, cbu)) {
            throw new InvalidAccountDataException(DomainErrorMessages.CBU_ONLY_NUMBERS);
        }
        // Validación 2: Longitud exacta
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