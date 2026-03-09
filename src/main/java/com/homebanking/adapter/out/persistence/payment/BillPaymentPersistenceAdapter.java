package com.homebanking.adapter.out.persistence.payment;

import com.homebanking.domain.entity.BillPayment;
import com.homebanking.port.out.payment.BillPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class BillPaymentPersistenceAdapter implements BillPaymentRepository {

    private final SpringDataBillPaymentRepository repository;
    private final BillPaymentPersistenceMapper mapper;

    @Override
    public BillPayment save(BillPayment payment) {
        BillPaymentJpaEntity saved = repository.save(mapper.toJpa(payment));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<BillPayment> findById(UUID paymentId) {
        return repository.findById(paymentId).map(mapper::toDomain);
    }

    @Override
    public Optional<BillPayment> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
    }
}

