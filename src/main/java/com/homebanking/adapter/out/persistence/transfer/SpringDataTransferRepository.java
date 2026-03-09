package com.homebanking.adapter.out.persistence.transfer;

import com.homebanking.domain.enums.TransferStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface SpringDataTransferRepository extends JpaRepository<TransferJpaEntity, UUID> {

    Optional<TransferJpaEntity> findByIdempotencyKey(String idempotencyKey);

    List<TransferJpaEntity> findByOriginAccountId(UUID originAccountId);

    List<TransferJpaEntity> findByStatusOrderByCreatedAtAsc(
            TransferStatus status,
            Pageable pageable);

    List<TransferJpaEntity> findByStatusAndRetryCountLessThanOrderByLastRetryAtAsc(
            TransferStatus status,
            Integer retryCount,
            Pageable pageable);
}

