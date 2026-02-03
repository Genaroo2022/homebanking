package com.homebanking.adapter.in.web.controller;

import com.homebanking.adapter.in.web.annotation.Auditable;
import com.homebanking.adapter.in.web.mapper.TransferWebMapper;
import com.homebanking.adapter.in.web.request.CreateTransferRequest;
import com.homebanking.adapter.in.web.response.TransferResponse;
import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.port.in.transfer.CreateTransferInputPort;
import com.homebanking.port.in.transfer.GetTransferInputPort;
import com.homebanking.port.in.transfer.RetryTransferInputPort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Validated
public class TransferController {

    private final CreateTransferInputPort createTransferUseCase;
    private final GetTransferInputPort getTransferUseCase;
    private final RetryTransferInputPort retryFailedTransferUseCase;
    private final TransferWebMapper transferWebMapper;

    @PostMapping
    @Auditable(action = "transfer.create")
    public ResponseEntity<TransferResponse> createTransfer(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody CreateTransferRequest request) {
        TransferOutputResponse output =
                createTransferUseCase.createTransfer(
                        transferWebMapper.toInputRequest(request, idempotencyKey));
        return ResponseEntity
                .created(URI.create("/api/transfers/" + output.id()))
                .body(transferWebMapper.toResponse(output));
    }

    @PostMapping("/{id}/retry")
    @Auditable(action = "transfer.retry")
    public ResponseEntity<TransferResponse> retryTransfer(
            @PathVariable("id") UUID transferId,
            @AuthenticationPrincipal UserDetails userDetails) {
        TransferOutputResponse output =
                retryFailedTransferUseCase.retryFailedTransfer(transferId, userDetails.getUsername());
        return ResponseEntity.ok(transferWebMapper.toResponse(output));
    }

    @GetMapping("/{id}")
    @Auditable(action = "transfer.get")
    public ResponseEntity<TransferResponse> getTransferEndpoint(
            @PathVariable("id") UUID transferId,
            @AuthenticationPrincipal UserDetails userDetails) {
        TransferOutputResponse output =
                getTransferUseCase.getTransfer(transferId, userDetails.getUsername());
        return ResponseEntity.ok(transferWebMapper.toResponse(output));
    }
}


