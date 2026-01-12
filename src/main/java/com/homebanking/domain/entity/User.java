package com.homebanking.domain.entity;

import com.homebanking.domain.exception.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.regex.Pattern;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    public static final int MIN_LEGAL_AGE = 18;
    public static final int DNI_MIN_LENGTH = 7;
    public static final int PASSWORD_MIN_LENGTH = 8;

    private static final String EMAIL_REGEX = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    private static final String DNI_REGEX = "^\\d+$";
    private static final String NAME_REGEX = "^[a-zA-ZÀ-ÿ\\u00f1\\u00d1\\s]+$";

    private Long id;
    private String email;
    private String password;
    private String name;
    private String lastName;
    private String dni;
    private LocalDate birthDate;
    private String address;
    private LocalDateTime createdAt;

    public User(Long id, String email, String password, String name, String lastName, String dni, LocalDate birthDate, String address) {

        validateMandatoryFields(name, lastName, email, password, dni, address);
        validateDniFormat(dni);
        validateNameFormat(name, lastName);
        validateEmailFormat(email);
        validatePasswordFormat(password);
        validateAge(birthDate);

        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.lastName = lastName;
        this.dni = dni;
        this.birthDate = birthDate;
        this.address = address;
        this.createdAt = LocalDateTime.now();
    }

    // --- MÉTODOS DE NEGOCIO (Mutators Controlados) ---

    public void changePassword(String newPassword) {
        validateNonBlankField(newPassword, DomainErrorMessages.PASSWORD_REQUIRED);
        validatePasswordFormat(newPassword);
        this.password = newPassword;
    }

    public void changeAddress(String newAddress) {
        validateNonBlankField(newAddress, DomainErrorMessages.ADDRESS_REQUIRED);
        this.address = newAddress;
    }

    // --- MÉTODOS DE VALIDACIÓN (Private) ---

    private void validateNonBlankField(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserDataException(errorMessage);
        }
    }

    private void validateMandatoryFields(String name, String lastName, String email, String password, String dni, String address) {
        validateNonBlankField(name, DomainErrorMessages.MANDATORY_FIELDS);
        validateNonBlankField(lastName, DomainErrorMessages.MANDATORY_FIELDS);
        validateNonBlankField(email, DomainErrorMessages.MANDATORY_FIELDS);
        validateNonBlankField(password, DomainErrorMessages.MANDATORY_FIELDS);
        validateNonBlankField(dni, DomainErrorMessages.MANDATORY_FIELDS);
        validateNonBlankField(address, DomainErrorMessages.MANDATORY_FIELDS);
    }

    private void validateDniFormat(String dni) {
        if (dni.length() < DNI_MIN_LENGTH) {
            throw new InvalidUserDataException(DomainErrorMessages.DNI_INVALID);
        }
        if (!Pattern.matches(DNI_REGEX, dni)) {
            throw new InvalidUserDataException(DomainErrorMessages.INVALID_DNI_FORMAT);
        }
    }

    private void validateNameFormat(String name, String lastName) {
        if (!Pattern.matches(NAME_REGEX, name) || !Pattern.matches(NAME_REGEX, lastName)) {
            throw new InvalidUserDataException(DomainErrorMessages.INVALID_NAME_FORMAT);
        }
    }

    private void validateEmailFormat(String email) {
        if (!Pattern.compile(EMAIL_REGEX).matcher(email).matches()) {
            throw new InvalidUserDataException(DomainErrorMessages.INVALID_EMAIL);
        }
    }

    private void validateAge(LocalDate birthDate) {
        if (birthDate == null) {
            throw new InvalidUserDataException(DomainErrorMessages.BIRTHDATE_REQUIRED);
        }
        if (Period.between(birthDate, LocalDate.now()).getYears() < MIN_LEGAL_AGE) {
            throw new InvalidUserDataException(DomainErrorMessages.USER_UNDERAGE);
        }
    }

    private void validatePasswordFormat(String password) {
        if (password.length() < PASSWORD_MIN_LENGTH) {
            throw new InvalidUserDataException(DomainErrorMessages.PASSWORD_FORMAT);
        }
    }
}