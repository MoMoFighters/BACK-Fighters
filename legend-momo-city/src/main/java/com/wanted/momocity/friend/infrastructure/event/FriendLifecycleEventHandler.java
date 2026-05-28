package com.wanted.momocity.friend.infrastructure.event;


import com.wanted.momocity.enrollment.domain.event.EnrollmentCompletedEvent;
import com.wanted.momocity.friend.application.service.FriendHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendLifecycleEventHandler {

    private final FriendHandlerService friendHandlerService;

    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) //수강신청 트랜잭션 성공 후 실행
    public void handleEnrollmentFriend(EnrollmentCompletedEvent event) {
        log.info("[FriendLifecycleEventHandler] 수강신청 완료로 인한 강사-학생 자동 친구 맺기 시작");

        friendHandlerService.createAndSaveTeacherFriendRelation(
                event.studentId(),
                event.lectureId()
        );
    }
}
