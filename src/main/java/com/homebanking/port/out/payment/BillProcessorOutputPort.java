package com.homebanking.port.out.payment;

import com.homebanking.domain.entity.BillPayment;

public interface BillProcessorOutputPort {
    boolean process(BillPayment payment);
}

