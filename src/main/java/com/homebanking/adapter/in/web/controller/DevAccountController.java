package com.homebanking.adapter.in.web.controller;

import com.homebanking.adapter.in.web.request.DepositAccountRequest;
import com.homebanking.adapter.in.web.response.DepositAccountResponse;
import com.homebanking.application.dto.account.request.DepositAccountInputRequest;
import com.homebanking.application.dto.account.response.DepositAccountOutputResponse;
import com.homebanking.port.in.account.DepositAccountInputPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Profile("dev")
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class DevAccountController {

    private final DepositAccountInputPort depositAccountUseCase;

    @PostMapping("/{id}/deposit")
    public ResponseEntity<DepositAccountResponse> depositEndpoint(
            @PathVariable("id") UUID accountId,
            @Valid @RequestBody DepositAccountRequest request) {
        DepositAccountOutputResponse output = depositAccountUseCase.deposit(
                new DepositAccountInputRequest(accountId, request.amount())
        );
        return ResponseEntity.ok(new DepositAccountResponse(
                output.accountId(),
                output.balance()
        ));
    }
}


