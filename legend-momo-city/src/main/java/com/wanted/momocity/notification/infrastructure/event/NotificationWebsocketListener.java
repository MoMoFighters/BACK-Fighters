package com.wanted.momocity.notification.infrastructure.event;

import com.wanted.momocity.notification.application.query.GetMainTotalCountsQuery;
import com.wanted.momocity.notification.application.query.GetNotificationQuery;
import com.wanted.momocity.notification.application.query.GetPhoneAppCountsQuery;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWebsocketListener {
    private final NotificationQueryUseCase notificationQueryUseCase;
    // 🎯 [핵심] 현재 스프링 서버에 연결된 실시간 세션/유저 정보를 메모리에서 관리하는 레지스트리 주입
    private final SimpUserRegistry simpUserRegistry;

    @Async("domainEventExecutor") // 비동기로 처리하여 메인 알림 저장 흐름에 영향을 주지 않음
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // DB 반영 완료 후 실행
    public void handleNotificationCreated(NotificationCreatedPublishedEvent event) {
        Long userId = event.userId();
        String type = event.type();

        // 🌟 [핵심 최적화: Short-Circuit] 현재 오프라인인 사용자라면 무거운 DB 조회 쿼리 자체를 타지 않도록 조기 차단!
        if (simpUserRegistry.getUser(userId.toString()) == null) {
            log.debug("[NotificationWebsocketListener] 오프라인 유저(ID: {}) 감지 -> DB 조회 및 웹소켓 발행 패스 (Short-Circuit 완료)", userId);
            return;
        }

        try {
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
        } catch (Exception e) {
            log.error("[NotificationWebsocketListener] 유저(ID: {}) 알림 실시간 데이터 갱신 중 실패 (건너뜀)", userId, e);
        }
    }
}
