package com.homebanking.application.service.transfer;

import com.homebanking.application.dto.transfer.response.TransferProcessingResult;
import com.homebanking.application.service.transfer.action.TransferProcessingAction;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.enums.TransferStatus;
import com.homebanking.domain.exception.account.InvalidAccountDataException;
import com.homebanking.domain.exception.transfer.InvalidTransferDataException;
import com.homebanking.domain.valueobject.common.Cbu;
import com.homebanking.domain.valueobject.transfer.IdempotencyKey;
import com.homebanking.domain.valueobject.transfer.TransferAmount;
import com.homebanking.domain.valueobject.transfer.TransferDescription;
import com.homebanking.port.out.AccountRepository;
import com.homebanking.port.out.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferStateTransitionServiceTest {

    @Mock
    private TransferRepository transferRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransferProcessingAction action;

    private TransferStateTransitionService stateService;

    @BeforeEach
    void setUp() {
        stateService = new TransferStateTransitionService(
                transferRepository,
                accountRepository,
                List.of(action)
        );
    }

    @Test
    void shouldThrowWhenTransferNotEligible() {
        Transfer completedTransfer = createSampleTransfer(TransferStatus.COMPLETED);
        UUID transferId = completedTransfer.getId();

        when(transferRepository.findById(transferId)).thenReturn(Optional.of(completedTransfer));

        assertThatThrownBy(() -> stateService.prepareForProcessing(transferId))
                .isInstanceOf(InvalidTransferDataException.class);
    }

    @Test
    void shouldRejectWhenDestinationAccountDoesNotExist() {
        Transfer pendingTransfer = createSampleTransfer(TransferStatus.PENDING);
        UUID transferId = pendingTransfer.getId();

        when(transferRepository.findById(transferId)).thenReturn(Optional.of(pendingTransfer));
        when(accountRepository.existsByCbu(pendingTransfer.getTargetCbu())).thenReturn(false);

        assertThatThrownBy(() -> stateService.prepareForProcessing(transferId))
                .isInstanceOf(InvalidAccountDataException.class);

        ArgumentCaptor<Transfer> captor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TransferStatus.REJECTED);
    }

    @Test
    void shouldApplyActionOnFinalize() {
        Transfer pendingTransfer = createSampleTransfer(TransferStatus.PROCESSING);
        UUID transferId = pendingTransfer.getId();

        when(transferRepository.findById(transferId)).thenReturn(Optional.of(pendingTransfer));
        when(action.outcome()).thenReturn(TransferProcessingResult.Outcome.SUCCESS);

        TransferProcessingResult result = TransferProcessingResult.success();
        stateService.finalizeProcessing(transferId, result);

        verify(action).apply(pendingTransfer, result);
        verify(transferRepository).save(pendingTransfer);
    }

    private Transfer createSampleTransfer(TransferStatus status) {
        return Transfer.reconstruct(
                UUID.randomUUID(),
                IdempotencyKey.of("idem-key"),
                UUID.randomUUID(),
                Cbu.of("1234567890123456789012"),
                TransferAmount.of(new BigDecimal("100")),
                TransferDescription.of("Test"),
                status,
                LocalDateTime.now(), null, null, null, 0, null
        );
    }
}

