
package com.homebanking.adapter.out.persistence.transfer;

import com.homebanking.domain.entity.Transfer;
import com.homebanking.port.out.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter: TransferPersistenceAdapter

 * Implementación del puerto TransferRepository.
 * Actúa como intermediario entre dominio y persistencia técnica (JPA).

 * Responsabilidades:
 * - Traducir entidades de dominio a/desde JPA
 * - Usar Spring Data Repository para acceso a BD
 * - Logging de operaciones

 * No conoce detalles de:
 * - Lógica de negocio (eso es del "use case")
 * - Cómo se serializan los datos (eso es de JPA)
 */
@Repository
@RequiredArgsConstructor
@Slf4j
class TransferPersistenceAdapter implements TransferRepository {

    private final SpringDataTransferRepository springDataRepository;
    private final TransferMapper transferMapper;

    @Override
    public Transfer save(Transfer transfer) {
        TransferJpaEntity entity = transferMapper.toJpaEntity(transfer);
        TransferJpaEntity saved = springDataRepository.save(entity);

        log.debug("Transferencia persistida: id={}, idempotencyKey={}",
                saved.getId(), saved.getIdempotencyKey());

        return transferMapper.toDomain(saved);
    }

    @Override
    public Optional<Transfer> findById(Long id) {
        return springDataRepository.findById(id)
                .map(transferMapper::toDomain);
    }

    @Override
    public Optional<Transfer> findByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey)
                .map(transferMapper::toDomain);
    }

    @Override
    public List<Transfer> findByOriginAccountId(Long accountId) {
        return springDataRepository.findByOriginAccountId(accountId)
                .stream()
                .map(transferMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transfer> findPendingTransfers() {
        return springDataRepository.findPendingTransfers()
                .stream()
                .map(transferMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transfer> findRetryableTransfers() {
        return springDataRepository.findRetryableTransfers()
                .stream()
                .map(transferMapper::toDomain)
                .collect(Collectors.toList());
    }
}