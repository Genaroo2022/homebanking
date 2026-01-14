package com.homebanking.adapter.out.persistence.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class UserJpaEntity {

    public static final int DNI_MAX_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public UserJpaEntity(String name, String lastName, String email, String password,
                         String dni, LocalDate birthDate, String address, LocalDateTime createdAt) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.dni = dni;
        this.birthDate = birthDate;
        this.address = address;
        this.createdAt = createdAt;
    }

    public static UserJpaEntity withId(Long id, String name, String lastName, String email, String password,
                                       String dni, LocalDate birthDate, String address, LocalDateTime createdAt) {
        UserJpaEntity entity = new UserJpaEntity(name, lastName, email, password, dni, birthDate, address, createdAt);
        entity.id = id;
        return entity;
    }
}