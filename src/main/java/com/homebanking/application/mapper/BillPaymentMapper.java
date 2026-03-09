package com.homebanking.application.mapper;

import com.homebanking.application.dto.payment.response.BillPaymentOutputResponse;
import com.homebanking.domain.entity.BillPayment;
import org.springframework.stereotype.Component;

@Component
public class BillPaymentMapper {

    public BillPaymentOutputResponse toDto(BillPayment payment) {
        return new BillPaymentOutputResponse(
                payment.getId(),
                payment.getAccountId(),
                payment.getBillerCode(),
                payment.getReference(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getFailureReason(),
                payment.getCreatedAt() == null ? null : payment.getCreatedAt().toString(),
                payment.getProcessedAt() == null ? null : payment.getProcessedAt().toString()
        );
    }
}

