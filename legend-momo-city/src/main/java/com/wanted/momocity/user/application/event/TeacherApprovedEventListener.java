package com.wanted.momocity.user.application.event;

import com.wanted.momocity.user.application.port.UserEmailSendPort;
import com.wanted.momocity.user.domain.event.TeacherApplicationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TeacherApprovedEventListener {

    private final UserEmailSendPort userEmailSendPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendApprovalEmail(TeacherApplicationEvent event) {
        userEmailSendPort.sendTeacherResult(event.email(), event.status(), event.reason());
    }
}
