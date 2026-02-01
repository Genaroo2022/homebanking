package com.homebanking.application.usecase.transfer;

import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.application.mapper.TransferMapper;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.transfer.TransferNotFoundException;
import com.homebanking.domain.util.DomainErrorMessages;
import com.homebanking.port.in.transfer.GetTransferInputPort;
import com.homebanking.port.out.transfer.TransferRepository;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@RequiredArgsConstructor
public class GetTransferUseCaseImpl implements GetTransferInputPort {

    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;

    @Override
    public TransferOutputResponse getTransfer(UUID transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException(
                        DomainErrorMessages.TRANSFER_NOT_FOUND,
                        transferId
                ));
        return transferMapper.toDto(transfer);
    }
}


