package com.wanted.momocity.study.infrastructure.scheduler;

import com.wanted.momocity.study.application.member.timer.usecase.TimerCommandUseCase;
import com.wanted.momocity.study.application.solo.usecase.SoloCommandUseCase;
import com.wanted.momocity.study.domain.model.GroupRoomMember;
import com.wanted.momocity.study.domain.model.SoloSession;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import com.wanted.momocity.study.domain.repository.SoloSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyExpirationScheduler {

    private static final long MAX_DURATION_HOURS = 24;

    private final SoloSessionRepository soloSessionRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final SoloCommandUseCase soloCommandUseCase;
    private final TimerCommandUseCase timerCommandUseCase;

    // 매일 10분 마다 스케줄러 확인
    @Scheduled(cron = "0 */10 * * * *")
    public void expireOverdueSessions() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(MAX_DURATION_HOURS);

        expireSoloSessions(threshold);
        expireGroupTimers(threshold);
    }

    // 솔로: 24시간 초과 RUNNING/PAUSED 세션을 찾아 end() 처리
    private void expireSoloSessions(LocalDateTime threshold) {
        List<SoloSession> expired = soloSessionRepository.findExpiredActiveSessions(threshold);

        for (SoloSession session : expired) {
            try {
                soloCommandUseCase.end(session.getUserId());
                log.info("[StudyExpiration] 솔로 세션 24시간 초과 자동 종료 | userId={}, sessionId={}",
                        session.getUserId(), session.getId());
            } catch (Exception e) {
                // 한 건 실패해도 나머지는 계속 처리 - 배치 특성상 개별 실패가 전체를 막으면 안 됨
                log.warn("[StudyExpiration] 솔로 세션 자동 종료 실패 (이미 정리됐을 가능성) | userId={}, sessionId={}, message={}",
                        session.getUserId(), session.getId(), e.getMessage());
            }
        }
    }

    // 그룹: 24시간 초과 STUDYING 멤버를 찾아 pause() 처리
    private void expireGroupTimers(LocalDateTime threshold) {
        List<GroupRoomMember> expired = groupRoomMemberRepository.findExpiredActiveSessions(threshold);

        for (GroupRoomMember member : expired) {
            try {
                timerCommandUseCase.pause(member.getUserId(), member.getGroupRoomId());
                log.info("[StudyExpiration] 그룹 타이머 24시간 초과 자동 일시정지 | userId={}, roomId={}",
                        member.getUserId(), member.getGroupRoomId());
            } catch (Exception e) {
                log.warn("[StudyExpiration] 그룹 타이머 자동 일시정지 실패 (이미 정리됐을 가능성) | userId={}, roomId={}, message={}",
                        member.getUserId(), member.getGroupRoomId(), e.getMessage());
            }
        }
    }

}
