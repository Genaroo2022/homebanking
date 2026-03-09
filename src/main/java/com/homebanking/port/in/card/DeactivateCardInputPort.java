package com.homebanking.port.in.card;

import com.homebanking.application.dto.card.response.CardOutputResponse;

import java.util.UUID;

public interface DeactivateCardInputPort {
    CardOutputResponse deactivate(UUID cardId, String requesterEmail);
}

