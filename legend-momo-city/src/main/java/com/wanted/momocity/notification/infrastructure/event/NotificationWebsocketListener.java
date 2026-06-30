package com.wanted.momocity.notification.infrastructure.event;

import com.wanted.momocity.notification.application.query.GetMainTotalCountsQuery;
import com.wanted.momocity.notification.application.query.GetNotificationQuery;
import com.wanted.momocity.notification.application.query.GetPhoneAppCountsQuery;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationWebsocketListener {
    private final NotificationQueryUseCase notificationQueryUseCase;

    @Async("domainEventExecutor") // 비동기로 처리하여 메인 알림 저장 흐름에 영향을 주지 않음
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // DB 반영 완료 후 실행
    public void handleNotificationCreated(NotificationCreatedPublishedEvent event) {
        Long userId = event.userId();
        String type = event.type();

        if ("ALL".equals(type)) {
            // 여기서 웹소켓 전송을 일괄 위임!
            notificationQueryUseCase.getMainTotalCountsQueryHandle(new GetMainTotalCountsQuery(userId));
            notificationQueryUseCase.getNotificationQueryHandle(new GetNotificationQuery(userId));
            notificationQueryUseCase.getPhoneAppCountsQueryHandle(new GetPhoneAppCountsQuery(userId));
        }

        // 2. NOTPHONE : 휴대폰 앱 화면과 무관한 일반 알림(예: 웹 전용 알림 등)이 터졌을 때
        else if ("NOTPHONE".equals(type)) {
            notificationQueryUseCase.getMainTotalCountsQueryHandle(new GetMainTotalCountsQuery(userId));
            notificationQueryUseCase.getNotificationQueryHandle(new GetNotificationQuery(userId));
            // 🎯 폰 전용 카운트 쿼리(getPhoneAppCountsQueryHandle)를 패스하여 DB 커넥션을 절약합니다!
        }
    }
}
