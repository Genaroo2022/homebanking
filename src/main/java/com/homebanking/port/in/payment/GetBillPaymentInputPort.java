package com.homebanking.port.in.payment;

import com.homebanking.application.dto.payment.response.BillPaymentOutputResponse;

import java.util.UUID;

public interface GetBillPaymentInputPort {
    BillPaymentOutputResponse getById(UUID paymentId, String requesterEmail);
}

