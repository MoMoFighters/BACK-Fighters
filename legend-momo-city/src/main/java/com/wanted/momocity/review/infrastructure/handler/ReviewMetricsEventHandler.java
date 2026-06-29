package com.wanted.momocity.review.infrastructure.event;

import com.wanted.momocity.global.infrastructure.metrics.MomoMetrics;
import com.wanted.momocity.review.domain.event.ReviewCreatedEvent;
import com.wanted.momocity.review.domain.event.ReviewDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReviewMetricsEventHandler {

    private final MomoMetrics momoMetrics;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewCreated(ReviewCreatedEvent event) {
        momoMetrics.recordReviewCreated();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewDeleted(ReviewDeletedEvent event) {
        momoMetrics.recordReviewDeleted();
    }
}