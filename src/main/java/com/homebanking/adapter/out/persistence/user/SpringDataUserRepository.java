package com.homebanking.adapter.out.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmailOrDni(String email, String dni);
    Optional<UserJpaEntity> findByEmail(String email);
}