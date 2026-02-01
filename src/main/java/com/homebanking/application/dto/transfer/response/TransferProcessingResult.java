package com.homebanking.application.dto.transfer.response;

import java.util.Optional;

public record TransferProcessingResult(
        Outcome outcome,
        Optional<String> errorMessage
) {
    public enum Outcome {
        SUCCESS,
        RECOVERABLE_FAILURE,
        NON_RECOVERABLE_FAILURE
    }

    public static TransferProcessingResult success() {
        return new TransferProcessingResult(Outcome.SUCCESS, Optional.empty());
    }

    public static TransferProcessingResult recoverableFailure() {
        return new TransferProcessingResult(Outcome.RECOVERABLE_FAILURE, Optional.empty());
    }

    public static TransferProcessingResult nonRecoverableFailure(String errorMessage) {
        return new TransferProcessingResult(Outcome.NON_RECOVERABLE_FAILURE, Optional.of(errorMessage));
    }
}


