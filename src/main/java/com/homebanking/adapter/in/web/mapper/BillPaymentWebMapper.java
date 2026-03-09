package com.homebanking.adapter.in.web.mapper;

import com.homebanking.adapter.in.web.request.PayBillRequest;
import com.homebanking.adapter.in.web.response.BillPaymentResponse;
import com.homebanking.application.dto.payment.request.PayBillInputRequest;
import com.homebanking.application.dto.payment.response.BillPaymentOutputResponse;
import org.springframework.stereotype.Component;

@Component
public class BillPaymentWebMapper {

    public PayBillInputRequest toInput(
            PayBillRequest request,
            String idempotencyKey,
            String requesterEmail) {
        return new PayBillInputRequest(
                request.accountId(),
                request.billerCode(),
                request.reference(),
                request.amount(),
                idempotencyKey,
                requesterEmail
        );
    }

    public BillPaymentResponse toResponse(BillPaymentOutputResponse output) {
        return new BillPaymentResponse(
                output.id(),
                output.accountId(),
                output.billerCode(),
                output.reference(),
                output.amount(),
                output.status(),
                output.failureReason(),
                output.createdAt(),
                output.processedAt()
        );
    }
}

