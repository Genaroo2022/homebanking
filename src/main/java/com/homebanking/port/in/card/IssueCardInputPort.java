package com.homebanking.port.in.card;

import com.homebanking.application.dto.card.request.IssueCardInputRequest;
import com.homebanking.application.dto.card.response.CardOutputResponse;

public interface IssueCardInputPort {
    CardOutputResponse issue(IssueCardInputRequest request);
}

