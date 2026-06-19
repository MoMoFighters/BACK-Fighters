package com.wanted.momocity.enrollment.infrastructure.event;

import com.wanted.momocity.enrollment.domain.event.EnrollmentCompletedEvent;
import com.wanted.momocity.enrollment.infrastructure.metrics.EnrollmentMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EnrollmentMetricsEventHandler {

    private final EnrollmentMetrics enrollmentMetrics;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(EnrollmentCompletedEvent event) {
        // 강의별 수강 신청 누적 횟수 기록 -> actuator(인기강의용)
        enrollmentMetrics.recordEnrollmentCreated(event.lectureId());
        // 건물 획득(수강 신청) 누적
        enrollmentMetrics.recordBuildingAcquired();
    }
}