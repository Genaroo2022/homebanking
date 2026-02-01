package com.homebanking.domain.event;

import java.util.UUID;

public record TransferCreatedEvent(UUID transferId) {
}
