package com.homebanking.adapter.out.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    // MAGIA DE SPRING DATA ✨
    // Al escribir "findByEmailOrDni", Spring analiza los atributos de tu UserJpaEntity
    // y genera automáticamente el SQL:
    // SELECT * FROM users WHERE email = ? OR dni = ?
    Optional<UserJpaEntity> findByEmailOrDni(String email, String dni);
}