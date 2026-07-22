package com.wanted.momocity.notification.application.policy;

import com.wanted.momocity.friend.fmexception.FMBusinessRuleViolationException;
import com.wanted.momocity.friend.fmexception.FMResourceAccessDeniedException;
import com.wanted.momocity.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEligibilityPolicy {

    private final NotificationRepository notificationRepository;

    //알림 읽기 빈 값 확인
    public void validateReadRequest(List<Long> targetId) {

        if (targetId == null || targetId.isEmpty()) {
            log.warn("[NotificationEligibilityPolicy] 알림 읽기 실패 - 읽을 알림이 비어있음. 개수:{}", targetId.size());
            throw new FMBusinessRuleViolationException("읽음 처리할 알림이 선택되지 않았습니다.");
        }
    }

    //알림 읽을 권한 확인
    public void readNotification(boolean hasGeneralAccess, boolean hasMsgNotiAccess) {
        // 둘 중 하나라도 권한이 누락되었다면 (false가 있다면) 403 예외 발생
        //일반 알림 읽을 권한, 메시지 알림 읽을 권한 확인
        if (!hasGeneralAccess || !hasMsgNotiAccess) {
            log.warn("[NotificationEligibilityPolicy] 알림 읽기 권한 검증 실패 (일반권한: {}, 메시지권한: {})", hasGeneralAccess, hasMsgNotiAccess);
            throw new FMResourceAccessDeniedException("해당 알림에 접근할 권한이 없습니다.");
        }
    }

    //알림 삭제 빈 값 확인
    public void validateRemoveRequest(List<Long> targetId) {
        if (targetId == null || targetId.isEmpty()) {
            log.warn("[NotificationEligibilityPolicy] 알림 삭제 실패 - 삭제할 알림이 비어있음. 개수:{}", targetId.size());
            throw new FMBusinessRuleViolationException("삭제 처리할 알림이 선택되지 않았습니다.");
        }
    }

    //알림 삭제 권한 확인
    public void removeNotification(boolean hasGeneralAccess, boolean hasMsgNotiAccess) {
        // 둘 중 하나라도 권한이 누락되었다면 (false가 있다면) 403 예외 발생
        //일반 알림 삭제 권한, 메시지 알림 삭제 권한 확인
        if (!hasGeneralAccess || !hasMsgNotiAccess) {
            log.warn("[NotificationEligibilityPolicy] 알림 삭제 권한 검증 실패 (일반권한: {}, 메시지권한: {})", hasGeneralAccess, hasMsgNotiAccess);
            throw new FMResourceAccessDeniedException("해당 알림에 접근할 권한이 없습니다.");
        }
    }

}
