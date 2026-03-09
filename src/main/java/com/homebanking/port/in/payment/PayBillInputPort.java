package com.homebanking.port.in.payment;

import com.homebanking.application.dto.payment.request.PayBillInputRequest;
import com.homebanking.application.dto.payment.response.BillPaymentOutputResponse;

public interface PayBillInputPort {
    BillPaymentOutputResponse pay(PayBillInputRequest request);
}

