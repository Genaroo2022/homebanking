package com.homebanking.adapter.out.persistence.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class UserJpaEntity {

    public static final int DNI_MAX_LENGTH = 20;

    @Setter(AccessLevel.PACKAGE)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false, length = DNI_MAX_LENGTH)
    private String dni;

    private LocalDate birthDate;
    private String address;

    @Column(name = "totp_secret")
    private String totpSecret;

    @Column(name = "totp_enabled", nullable = false)
    private boolean totpEnabled;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public UserJpaEntity(String name, String lastName, String email, String password,
                         String dni, LocalDate birthDate, String address, LocalDateTime createdAt,
                         String totpSecret, boolean totpEnabled) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.dni = dni;
        this.birthDate = birthDate;
        this.address = address;
        this.createdAt = createdAt;
        this.totpSecret = totpSecret;
        this.totpEnabled = totpEnabled;
    }
}

