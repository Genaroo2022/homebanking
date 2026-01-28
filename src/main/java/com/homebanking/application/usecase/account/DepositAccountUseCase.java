package com.homebanking.application.usecase.account;

import com.homebanking.application.dto.account.request.DepositAccountInputRequest;
import com.homebanking.application.dto.account.response.DepositAccountOutputResponse;

public interface DepositAccountUseCase {
    DepositAccountOutputResponse deposit(DepositAccountInputRequest request);
}
