package com.homebanking.adapter.in.event;

import com.homebanking.domain.event.TransferCreatedEvent;
import com.homebanking.port.in.transfer.ProcessTransferInputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferEventListener {

    private final ProcessTransferInputPort processTransferUseCase;

    @Async
    @EventListener
    public void handle(TransferCreatedEvent event) {
        log.info("Received transfer created event for transferId: {}", event.transferId());
        processTransferUseCase.processTransfer(event.transferId());
    }
}


