package com.wanted.momocity.user.application.event;

import com.wanted.momocity.user.application.port.ReportRedisPort;
import com.wanted.momocity.user.domain.event.ReportRedisEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReportRedisEventListener {

    private final ReportRedisPort reportRedisPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReportRedisEvent event) {
        if (event.isSave()) {
            reportRedisPort.saveReportTime(event.userId());
        } else {
            reportRedisPort.deleteReportTime(event.userId());
        }
    }
}