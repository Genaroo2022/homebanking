package com.homebanking.application.usecase.transfer;

import com.homebanking.application.dto.transfer.response.TransferOutputResponse;
import com.homebanking.application.dto.transfer.response.TransferProcessingResult;
import com.homebanking.application.mapper.TransferMapper;
import com.homebanking.application.service.transfer.TransferStateTransitionService;
import com.homebanking.domain.entity.Transfer;
import com.homebanking.domain.exception.transfer.TransferProcessingException;
import com.homebanking.port.out.TransferProcessorOutputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessTransferUseCaseImplTest {

    @Mock
    private TransferProcessorOutputPort transferProcessor;
    @Mock
    private TransferMapper transferMapper;
    @Mock
    private TransferStateTransitionService stateService;

    private ProcessTransferUseCaseImpl processTransferUseCase;

    @BeforeEach
    void setUp() {
        processTransferUseCase = new ProcessTransferUseCaseImpl(
                transferProcessor,
                transferMapper,
                stateService
        );
    }

    @Test
    void shouldProcessTransferSuccessfully() throws TransferProcessingException {
        Transfer transfer = mock(Transfer.class);
        UUID transferId = UUID.randomUUID();

        when(stateService.prepareForProcessing(transferId)).thenReturn(transfer);
        when(transfer.getId()).thenReturn(transferId);
        when(transferProcessor.processTransfer(transfer)).thenReturn(true);
        when(stateService.finalizeProcessing(eq(transferId), any(TransferProcessingResult.class)))
                .thenReturn(transfer);
        when(transferMapper.toDto(transfer)).thenReturn(mock(TransferOutputResponse.class));

        processTransferUseCase.processTransfer(transferId);

        ArgumentCaptor<TransferProcessingResult> resultCaptor = ArgumentCaptor.forClass(TransferProcessingResult.class);
        verify(stateService).finalizeProcessing(eq(transferId), resultCaptor.capture());
        assertThat(resultCaptor.getValue().outcome())
                .isEqualTo(TransferProcessingResult.Outcome.SUCCESS);
    }

    @Test
    void shouldHandleRetryableFailure() throws TransferProcessingException {
        Transfer transfer = mock(Transfer.class);
        UUID transferId = UUID.randomUUID();
        TransferProcessingException exception = new TransferProcessingException("Error", true, "EXT-01");

        when(stateService.prepareForProcessing(transferId)).thenReturn(transfer);
        when(transfer.getId()).thenReturn(transferId);
        when(transferProcessor.processTransfer(transfer)).thenThrow(exception);
        when(stateService.finalizeProcessing(eq(transferId), any(TransferProcessingResult.class)))
                .thenReturn(transfer);
        when(transferMapper.toDto(transfer)).thenReturn(mock(TransferOutputResponse.class));

        processTransferUseCase.processTransfer(transferId);

        ArgumentCaptor<TransferProcessingResult> resultCaptor = ArgumentCaptor.forClass(TransferProcessingResult.class);
        verify(stateService).finalizeProcessing(eq(transferId), resultCaptor.capture());
        assertThat(resultCaptor.getValue().outcome())
                .isEqualTo(TransferProcessingResult.Outcome.RECOVERABLE_FAILURE);
    }

    @Test
    void shouldHandlePermanentFailure() throws TransferProcessingException {
        Transfer transfer = mock(Transfer.class);
        UUID transferId = UUID.randomUUID();
        TransferProcessingException exception = new TransferProcessingException("Error", false, "EXT-02");

        when(stateService.prepareForProcessing(transferId)).thenReturn(transfer);
        when(transfer.getId()).thenReturn(transferId);
        when(transferProcessor.processTransfer(transfer)).thenThrow(exception);
        when(stateService.finalizeProcessing(eq(transferId), any(TransferProcessingResult.class)))
                .thenReturn(transfer);
        when(transferMapper.toDto(transfer)).thenReturn(mock(TransferOutputResponse.class));

        processTransferUseCase.processTransfer(transferId);

        ArgumentCaptor<TransferProcessingResult> resultCaptor = ArgumentCaptor.forClass(TransferProcessingResult.class);
        verify(stateService).finalizeProcessing(eq(transferId), resultCaptor.capture());
        assertThat(resultCaptor.getValue().outcome())
                .isEqualTo(TransferProcessingResult.Outcome.NON_RECOVERABLE_FAILURE);
        assertThat(resultCaptor.getValue().errorMessage())
                .isPresent();
    }
}
