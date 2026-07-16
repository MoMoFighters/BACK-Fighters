package com.wanted.momocity.study.application.common.service;

import com.wanted.momocity.study.domain.model.StudyLap;
import com.wanted.momocity.study.domain.repository.StudyLapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/*
 * comment.
 *  공부 랩(구간) 저장/조회를 전담하는 공용 서비스
 *  -
 *  랩 저장 로직(StudyLapRepository 호출 + 초 계산)이 solo와 member.timer 동일
 *  각 도메인의 Service(SoloCommandService/TimerCommandService)에 중복해서 넣는 대신 공용 서비스 하나 설계
 *  -
 *  랩을 시작/마감/조회하는 것만 책임 -> 그 외 로직은 이 서비스를 호출하는 각 도메인 Service 의 책임
 * */

@Service
@Transactional
@RequiredArgsConstructor
public class StudyLapService {

    private final StudyLapRepository studyLapRepository;

    // 새 랩 시작 (타이머 시작/재개 시 호출)
    public StudyLap startLap(Long userId, Long roomId, Long sessionId, LocalDateTime now) {
        StudyLap lap = StudyLap.start(userId, roomId, sessionId, now);
        return studyLapRepository.save(lap);
    }

    /*
     * comment.
     *  진행 중인 랩을 찾아서 마감 (타이머 일시정지/종료 시 호출)
     *  - 진행 중인 랩이 없으면(ex: 타이머 상태와 랩 상태가 어긋난 경우) 예외를 던지지 않고 null을 반환
     *  - 랩 기록은 부가 기능, 랩 마감 실패로 타이머 자체의 pause/end 흐름이 깨지면 안 되기 때문에 방어적으로 처리
     * */
    public StudyLap closeLap(Long roomId, Long sessionId, LocalDateTime now) {
        return studyLapRepository.findOngoingBySessionId(roomId, sessionId)
                .map(lap -> {
                    long elapsed = Duration.between(lap.getStartedAt(), now).getSeconds();
                    lap.close(now, (int) Math.max(elapsed, 0));
                    return studyLapRepository.save(lap);
                })
                .orElse(null);
    }

    // 특정 세션의 전체 랩 목록 조회 (시작 순서대로)
    @Transactional(readOnly = true)
    public List<StudyLap> getLaps(Long roomId, Long sessionId) {
        return studyLapRepository.findAllBySessionIdOrderByStartedAtAsc(roomId, sessionId);
    }

    /*
     * comment.
     *  특정 세션의 전체 랩 개수 조회
     *  - 각 도메인 Service(SoloCommandService 등)가 lapNumber를 계산할 때 사용
     *  - 전체 랩 목록을 조회하지 않고 COUNT만 가져오므로 pause/end처럼 호출 빈도가 높은 지점에서도 부담이 적음
     * */
    @Transactional(readOnly = true)
    public long countLaps(Long roomId, Long sessionId) {
        return studyLapRepository.countBySessionId(roomId, sessionId);
    }

}
