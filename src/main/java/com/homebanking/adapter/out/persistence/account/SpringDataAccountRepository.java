package com.homebanking.adapter.out.persistence.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {
    List<AccountJpaEntity> findByUserId(UUID userId);
    Optional<AccountJpaEntity> findByCbu(String cbu);
    boolean existsByCbu(String cbu);

}