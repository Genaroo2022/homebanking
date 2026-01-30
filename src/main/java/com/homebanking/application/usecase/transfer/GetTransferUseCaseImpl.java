package com.homebanking.application.usecase.transfer;

import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.transfer.TransferNotFoundException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.transfer.GetTransferInputPort;
import com.homebanking.port.out.TransferRepository;
import lombok.RequiredArgsConstructor;

import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
public class GetTransferUseCaseImpl implements GetTransferInputPort {

    private final TransferRepository transferRepository;

    @Override
    public TransferOutputResponse getTransfer(Long transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException(
                        DomainErrorMessages.TRANSFER_NOT_FOUND,
                        transferId
                ));
        return toOutputResponse(transfer);
    }

    private TransferOutputResponse toOutputResponse(Transfer transfer) {
        return new TransferOutputResponse(
                transfer.getId(),
                transfer.getIdempotencyKey().value(),
                transfer.getOriginAccountId(),
                transfer.getTargetCbu().value(),
                transfer.getAmount().value(),
                transfer.getDescription().value(),
                transfer.getStatus().name(),
                transfer.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
