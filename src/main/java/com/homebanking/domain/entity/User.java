package com.homebanking.domain.entity;

import com.homebanking.domain.exception.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
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



    private void validateMandatoryFields(String name, String lastName, String email, String password, String dni, String address) {
        if (name == null || name.isBlank() ||
                lastName == null || lastName.isBlank() ||
                email == null || email.isBlank() ||
                password == null || password.isBlank() ||
                dni == null || dni.isBlank() ||
                address == null || address.isBlank()) {

            throw new InvalidUserDataException(DomainErrorMessages.MANDATORY_FIELDS);
        }
    }

    private void validateDniFormat(String dni) {
        // 1. Longitud
        if (dni.length() < DNI_MIN_LENGTH) {
            throw new InvalidUserDataException(DomainErrorMessages.DNI_INVALID);
        }
        // 2. Solo números (Regex)
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