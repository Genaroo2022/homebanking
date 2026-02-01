package com.homebanking.application.mapper;

import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.domain.entity.Transfer;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class TransferMapper {

    public TransferOutputResponse toDto(Transfer transfer) {
        if (transfer == null) {
            return null;
        }
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


