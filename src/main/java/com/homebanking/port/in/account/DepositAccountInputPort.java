package com.homebanking.port.in.account;

import com.homebanking.application.dto.account.request.DepositAccountInputRequest;
import com.homebanking.application.dto.account.response.DepositAccountOutputResponse;

public interface DepositAccountInputPort {
    DepositAccountOutputResponse deposit(DepositAccountInputRequest request);
}


