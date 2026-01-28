package com.homebanking.adapter.in.web.controller;

import com.homebanking.adapter.in.web.mapper.TransferWebMapper;
import com.homebanking.adapter.in.web.request.CreateTransferRequest;
import com.homebanking.adapter.in.web.response.TransferResponse;
import com.homebanking.application.dto.transfer.request.CreateTransferInputRequest;
import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.application.usecase.transfer.CreateTransferUseCase;
import com.homebanking.application.usecase.transfer.GetTransferUseCase;
import com.homebanking.application.usecase.transfer.ProcessTransferUseCase;
import com.homebanking.application.usecase.transfer.RetryFailedTransferUseCase;
import com.homebanking.port.in.transfer.CreateTransferInputPort;
import com.homebanking.port.in.transfer.GetTransferInputPort;
import com.homebanking.port.in.transfer.ProcessTransferInputPort;
import com.homebanking.port.in.transfer.RetryTransferInputPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController implements
        CreateTransferInputPort,
        ProcessTransferInputPort,
        RetryTransferInputPort,
        GetTransferInputPort {

    private final CreateTransferUseCase createTransferUseCase;
    private final GetTransferUseCase getTransferUseCase;
    private final ProcessTransferUseCase processTransferUseCase;
    private final RetryFailedTransferUseCase retryFailedTransferUseCase;
    private final TransferWebMapper transferWebMapper;

    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(@Valid @RequestBody CreateTransferRequest request) {
        TransferOutputResponse output = createTransfer(transferWebMapper.toInputRequest(request));
        return ResponseEntity
                .created(URI.create("/api/transfers/" + output.id()))
                .body(transferWebMapper.toResponse(output));
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<TransferResponse> processTransferEndpoint(@PathVariable("id") Long transferId) {
        TransferOutputResponse output = processTransfer(transferId);
        return ResponseEntity.ok(transferWebMapper.toResponse(output));
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<TransferResponse> retryTransfer(@PathVariable("id") Long transferId) {
        TransferOutputResponse output = retryFailedTransfer(transferId);
        return ResponseEntity.ok(transferWebMapper.toResponse(output));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponse> getTransferEndpoint(@PathVariable("id") Long transferId) {
        TransferOutputResponse output = getTransfer(transferId);
        return ResponseEntity.ok(transferWebMapper.toResponse(output));
    }

    @Override
    public TransferOutputResponse createTransfer(CreateTransferInputRequest request) {
        return createTransferUseCase.createTransfer(request);
    }

    @Override
    public TransferOutputResponse processTransfer(Long transferId) {
        return processTransferUseCase.processTransfer(transferId);
    }

    @Override
    public TransferOutputResponse retryFailedTransfer(Long transferId) {
        return retryFailedTransferUseCase.retryFailedTransfer(transferId);
    }

    @Override
    public TransferOutputResponse getTransfer(Long transferId) {
        return getTransferUseCase.getTransfer(transferId);
    }
}
