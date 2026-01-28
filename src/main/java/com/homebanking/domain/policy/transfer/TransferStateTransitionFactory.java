package com.homebanking.domain.policy.transfer;

import com.homebanking.domain.policy.transition.MarkAsCompletedTransition;
import com.homebanking.domain.policy.transition.MarkAsFailedTransition;
import com.homebanking.domain.policy.transition.MarkAsRejectedTransition;
import com.homebanking.domain.policy.transition.PrepareForRetryTransition;
import com.homebanking.domain.policy.transition.TakeForProcessingTransition;

public class TransferStateTransitionFactory {

    public TransferStateTransition create(TransferStateTransition.Type type) {
        return switch (type) {
            case TAKE_FOR_PROCESSING -> new TakeForProcessingTransition();
            case MARK_AS_COMPLETED -> new MarkAsCompletedTransition();
            case MARK_AS_FAILED -> new MarkAsFailedTransition();
            case MARK_AS_REJECTED -> new MarkAsRejectedTransition();
            case PREPARE_FOR_RETRY -> new PrepareForRetryTransition();
        };
    }
}
