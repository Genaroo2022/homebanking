package com.homebanking.adapter.in.web.controller;

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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
    public ResponseEntity<TransferResponse> retryTransfer(@PathVariable("id") Long transferId) {
        TransferOutputResponse output = retryFailedTransferUseCase.retryFailedTransfer(transferId);
        return ResponseEntity.ok(transferWebMapper.toResponse(output));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponse> getTransferEndpoint(@PathVariable("id") Long transferId) {
        TransferOutputResponse output = getTransferUseCase.getTransfer(transferId);
        return ResponseEntity.ok(transferWebMapper.toResponse(output));
    }
}
