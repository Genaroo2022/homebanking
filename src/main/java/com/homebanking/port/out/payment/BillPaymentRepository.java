package com.homebanking.port.out.payment;

import com.homebanking.domain.entity.BillPayment;

import java.util.Optional;
import java.util.UUID;

public interface BillPaymentRepository {
    BillPayment save(BillPayment payment);
    Optional<BillPayment> findById(UUID paymentId);
    Optional<BillPayment> findByIdempotencyKey(String idempotencyKey);
}

