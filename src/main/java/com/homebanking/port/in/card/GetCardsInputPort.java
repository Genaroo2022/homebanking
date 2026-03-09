package com.homebanking.port.in.card;

import com.homebanking.application.dto.card.response.CardOutputResponse;

import java.util.List;
import java.util.UUID;

public interface GetCardsInputPort {
    List<CardOutputResponse> getByAccount(UUID accountId, String requesterEmail);
}

