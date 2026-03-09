package com.homebanking.adapter.in.web.mapper;

import com.homebanking.adapter.in.web.request.IssueCardRequest;
import com.homebanking.adapter.in.web.response.CardResponse;
import com.homebanking.application.dto.card.request.IssueCardInputRequest;
import com.homebanking.application.dto.card.response.CardOutputResponse;
import org.springframework.stereotype.Component;

@Component
public class CardWebMapper {

    public IssueCardInputRequest toInput(IssueCardRequest request, String requesterEmail) {
        return new IssueCardInputRequest(
                request.accountId(),
                request.type(),
                request.color(),
                requesterEmail
        );
    }

    public CardResponse toResponse(CardOutputResponse output) {
        return new CardResponse(
                output.id(),
                output.accountId(),
                output.maskedNumber(),
                output.cardHolder(),
                output.fromDate(),
                output.thruDate(),
                output.type(),
                output.color(),
                output.active()
        );
    }
}

