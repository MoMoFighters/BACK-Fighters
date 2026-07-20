package com.wanted.momocity.study.infrastructure.event;

import com.wanted.momocity.study.domain.event.StudySessionAccumulatedEvent;
import com.wanted.momocity.study.domain.model.DailyStudyRecord;
import com.wanted.momocity.study.domain.model.MonthlyStudyRecord;
import com.wanted.momocity.study.domain.repository.DailyStudyRecordRepository;
import com.wanted.momocity.study.domain.repository.MonthlyStudyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.YearMonth;

/*
 * comment.
 *  StudySessionEndedEvent 수신 -> DailyStudyRecord/MonthlyStudyRecord 동시 누적
 *  -
 *  solo(SoloCommandService.end())와 member.timer(TimerCommandService.end())
 *  양쪽에서 세션/타이머가 종료될 때마다 이 이벤트가 발행되고, 이 핸들러가 수신해서 일별/월별 누적치를 갱신
 *   방 구분 없이 유저 단위로 합산되는 값
 *  -
 *  @TransactionalEventListener(AFTER_COMMIT) : 세션/타이머 종료 트랜잭션이 커밋된 후에만 누적을 반영
 *  (트랜잭션 롤백됐는데 기록만 먼저 반영되는 정합성 문제 방지)
 *  @Async("domainEventExecutor") : Community 패턴과 동일하게 별도 스레드에서 처리
 *  @Transactional(REQUIRES_NEW) : AFTER_COMMIT 시점엔 원본 트랜잭션이 이미 끝나있으므로 새 트랜잭션을 독립적으로 열어야 함
 *  -
 *  [수정 사항]
 *  자정을 걸친 세션의 날짜 분할 로직은 아직 없다. 지금은 이벤트 발행 시점에 넘어온 studyDate 하나에 seconds를 그대로 몰아서 누적
 *  자정 분할이 필요해지면, 이 이벤트를 발행하는 쪽에서 자정 기준으로  이벤트를 2번 나눠 발행하도록 수정
 * */

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyRecordEventHandler {

    private final DailyStudyRecordRepository dailyStudyRecordRepository;
    private final MonthlyStudyRecordRepository monthlyStudyRecordRepository;

    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional (propagation = Propagation.REQUIRES_NEW)
    public void onStudySessionAccumulated(StudySessionAccumulatedEvent event) {

        dailyStudyRecordRepository.incrementSeconds(event.userId(), event.studyDate(), event.seconds());

        YearMonth yearMonth = YearMonth.from(event.studyDate());
        monthlyStudyRecordRepository.incrementSeconds(event.userId(), yearMonth, event.seconds());

        log.info("[Study] 공부 기록 원자적 누적 완료 | userId={}, date={}, seconds={}",
                event.userId(), event.studyDate(), event.seconds());
    }

}
