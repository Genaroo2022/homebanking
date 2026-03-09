package com.homebanking.adapter.out.external.payment.client;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Component
public class BillProcessorClient {

    private static final Random RANDOM = new Random();

    public boolean process(
            UUID paymentId,
            String billerCode,
            String reference,
            BigDecimal amount,
            String idempotencyKey) {
        int outcome = RANDOM.nextInt(100);
        if (outcome < 85) {
            return true;
        }
        if (outcome < 95) {
            return false;
        }
        throw new IllegalStateException("processor_unavailable");
    }
}

