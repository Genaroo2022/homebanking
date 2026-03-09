package com.homebanking.application.dto.card.request;

import com.homebanking.domain.enums.CardColor;
import com.homebanking.domain.enums.CardType;

import java.util.UUID;

public record IssueCardInputRequest(
        UUID accountId,
        CardType type,
        CardColor color,
        String requesterEmail
) {
}

