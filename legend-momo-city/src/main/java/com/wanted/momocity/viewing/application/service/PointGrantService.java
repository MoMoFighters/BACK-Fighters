package com.wanted.momocity.viewing.application.service;

import com.wanted.momocity.global.application.point.AddOrderHistory;
import com.wanted.momocity.global.application.point.PointChange;
import com.wanted.momocity.order.domain.model.Reason;
import com.wanted.momocity.order.domain.model.Type;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
* comment.
*  포인트 지급 서비스
*  -
*  ViewingEventHandler 는 @Async + AFTER_COMMIT
*  -> 별도 스레드에서 실행 -> 기존 트랜잭션 없음
*  -> pointChange.gainPoint() 직접 호출 시 @Transactional 없어 flush 안 됨
*  -> 별도 @Transactional 서비스로 분리하여 flush 보장
* */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PointGrantService {

    private final PointChange pointChange;
    private final AddOrderHistory addOrderHistory;

    // 챕터 완료 시 포인트 지급
    public void grantChapterCompletionPoint(Long userId) {
        pointChange.gainPoint(userId, 10L);
        // 포인트 지급 내욕을 order_history 테이블에 저장
        addOrderHistory.saveOrderHistory(userId, Reason.COMPLETE, Type.GAINED, 10L);
        log.info("[Viewing] 포인트 지급 완료 | userId={}, amount=10", userId);
    }

}
