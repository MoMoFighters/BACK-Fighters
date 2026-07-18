package com.wanted.momocity.study.application.common.service;

import com.wanted.momocity.study.application.common.usecase.TimerAvailabilityUseCase;
import com.wanted.momocity.study.domain.model.SoloSession;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import com.wanted.momocity.study.domain.repository.SoloSessionRepository;
import com.wanted.momocity.study.presentation.api.response.common.TimerAvailabilityResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * comment.
 *  타이머 시작 가능 여부 조회 UseCase 구현체
 *  -
 *  두 조건 중 하나라도 해당하면 canStartTimer=false :
 *  1. 그룹방 어딘가에서 timerStatus가 STUDYING인 GroupRoomMember가 존재
 *  2. 솔로 세션이 RUNNING 상태로 존재
 *  둘 다 아니면(RESTING/PAUSED만 있거나 아예 없으면) canStartTimer=true
 * */

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimerAvailabilityService implements TimerAvailabilityUseCase {

    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final SoloSessionRepository soloSessionRepository;

    @Override
    public TimerAvailabilityResponse getAvailability(Long userId) {

        // 조건 1 : 그룹방 어딘가에서 이미 STUDYING 중인지
        boolean studyingInGroup = !groupRoomMemberRepository.findAllByUserIdAndStudying(userId).isEmpty();

        // 조건 2 : 솔로 세션이 RUNNING 중인지 (PAUSED는 "진행 중"이 아니므로 막지 않음 -
        // 기존 validateNoOtherActiveTimer들과 동일하게 RUNNING만 체크)
        boolean runningSolo = soloSessionRepository.findActiveByUserId(userId)
                .filter(session -> session.getStatus() == SoloSession.SoloSessionStatus.RUNNING)
                .isPresent();

        boolean canStart = !studyingInGroup && !runningSolo;

        log.info("[Study] 타이머 가용성 조회 완료 | userId={}, canStartTimer={}", userId, canStart);
        return new TimerAvailabilityResponse(canStart);
    }

}
