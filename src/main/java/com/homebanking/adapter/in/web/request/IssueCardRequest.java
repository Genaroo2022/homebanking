package com.homebanking.adapter.in.web.request;

import com.homebanking.domain.enums.CardColor;
import com.homebanking.domain.enums.CardType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IssueCardRequest(
        @NotNull UUID accountId,
        @NotNull CardType type,
        @NotNull CardColor color
) {
}

