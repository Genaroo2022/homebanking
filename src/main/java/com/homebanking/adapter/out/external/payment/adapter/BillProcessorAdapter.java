package com.homebanking.adapter.out.external.payment.adapter;

import com.homebanking.adapter.out.external.payment.client.BillProcessorClient;
import com.homebanking.domain.entity.BillPayment;
import com.homebanking.port.out.payment.BillProcessorOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillProcessorAdapter implements BillProcessorOutputPort {

    private final BillProcessorClient client;

    @Override
    public boolean process(BillPayment payment) {
        log.info("Processing bill payment id={} biller={} amount={}",
                payment.getId(),
                payment.getBillerCode(),
                payment.getAmount());
        return client.process(
                payment.getId(),
                payment.getBillerCode(),
                payment.getReference(),
                payment.getAmount(),
                payment.getIdempotencyKey()
        );
    }
}

