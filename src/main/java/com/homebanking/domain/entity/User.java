package com.homebanking.domain.entity;

import com.homebanking.domain.exception.user.InvalidUserDataException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.domain.valueobject.user.UserAddress;
import com.homebanking.domain.valueobject.user.UserBirthDate;
import com.homebanking.domain.valueobject.user.UserDni;
import com.homebanking.domain.valueobject.user.UserEmail;
import com.homebanking.domain.valueobject.user.UserFirstName;
import com.homebanking.domain.valueobject.user.UserLastName;
import com.homebanking.domain.valueobject.user.UserPassword;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    private Long id;
    private UserEmail email;
    private UserPassword password;
    private UserFirstName name;
    private UserLastName lastName;
    private UserDni dni;
    private UserBirthDate birthDate;
    private UserAddress address;
    private LocalDateTime createdAt;

    public User(String email, String password, String name, String lastName, String dni, LocalDate birthDate, String address) {
        validateUserData(email, password, name, lastName, dni, birthDate, address);

        this.email = UserEmail.of(email);
        this.password = UserPassword.of(password);
        this.name = UserFirstName.of(name);
        this.lastName = UserLastName.of(lastName);
        this.dni = UserDni.of(dni);
        this.birthDate = UserBirthDate.of(birthDate);
        this.address = UserAddress.of(address);
        this.createdAt = LocalDateTime.now();
    }

    // Factory Method: Reconstitution from Persistence
    public static User withId(Long id, String email, String password, String name, String lastName, String dni, LocalDate birthDate, String address, LocalDateTime createdAt) {
        validateStructuralData(id, createdAt);
        validateUserData(email, password, name, lastName, dni, birthDate, address);
        return hydrate(
                id,
                UserEmail.of(email),
                UserPassword.of(password),
                UserFirstName.of(name),
                UserLastName.of(lastName),
                UserDni.of(dni),
                UserBirthDate.of(birthDate),
                UserAddress.of(address),
                createdAt
        );
    }

    // --- BUSINESS METHODS (Mutators Controlados) ---

    public void changePassword(String newPassword) {
        validateNonBlankField(newPassword, DomainErrorMessages.PASSWORD_REQUIRED);
        this.password = UserPassword.of(newPassword);
    }

    public void changeAddress(String newAddress) {
        validateNonBlankField(newAddress, DomainErrorMessages.ADDRESS_REQUIRED);
        this.address = UserAddress.of(newAddress);
    }

    // --- VALIDATION METHODS (Private Static) ---

    private static void validateUserData(String email, String password, String name, String lastName, String dni, LocalDate birthDate, String address) {
        validateMandatoryFields(name, lastName, email, password, dni, address);
    }

    private static User hydrate(Long id, UserEmail email, UserPassword password, UserFirstName name, UserLastName lastName,
                                UserDni dni, UserBirthDate birthDate, UserAddress address, LocalDateTime createdAt) {
        User user = new User();
        user.id = id;
        user.email = email;
        user.password = password;
        user.name = name;
        user.lastName = lastName;
        user.dni = dni;
        user.birthDate = birthDate;
        user.address = address;
        user.createdAt = createdAt;
        return user;
    }

    private static void validateNonBlankField(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserDataException(errorMessage);
        }
    }

    private static void validateMandatoryFields(String name, String lastName, String email, String password, String dni, String address) {
        validateNonBlankField(name, DomainErrorMessages.MANDATORY_FIELDS);
        validateNonBlankField(lastName, DomainErrorMessages.MANDATORY_FIELDS);
        validateNonBlankField(email, DomainErrorMessages.MANDATORY_FIELDS);
        validateNonBlankField(password, DomainErrorMessages.MANDATORY_FIELDS);
        validateNonBlankField(dni, DomainErrorMessages.MANDATORY_FIELDS);
        validateNonBlankField(address, DomainErrorMessages.MANDATORY_FIELDS);
    }

    private static void validateStructuralData(Long id, LocalDateTime createdAt) {
        if (id == null) {
            throw new InvalidUserDataException(DomainErrorMessages.ID_REQUIRED);
        }
        if (createdAt == null) {
            throw new InvalidUserDataException(DomainErrorMessages.CREATED_AT_REQUIRED);
        }
    }
}

