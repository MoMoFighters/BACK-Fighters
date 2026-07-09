package com.wanted.momocity.viewing.application.service;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.viewing.application.port.EnrollmentPort;
import com.wanted.momocity.viewing.domain.event.ProgressSavedEvent;
import com.wanted.momocity.viewing.application.command.SaveProgressCommand;
import com.wanted.momocity.viewing.application.policy.EnrollmentAccessPolicy;
import com.wanted.momocity.viewing.application.port.ChapterPort;
import com.wanted.momocity.viewing.application.usecase.ViewingCommandUseCase;
import com.wanted.momocity.viewing.domain.event.ChapterCompletedEvent;
import com.wanted.momocity.viewing.domain.model.Chapter;
import com.wanted.momocity.viewing.domain.model.LearningHistory;
import com.wanted.momocity.viewing.domain.repository.LearningHistoryRepository;
import com.wanted.momocity.viewing.infrastructure.metrics.ViewingMetrics;
import com.wanted.momocity.viewing.presentation.api.response.SaveProgressResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/*
 * comment.
 *  - 트랜잭션 경계 안에서 Policy 검증 + Domain 상태 전이 + 저장 조율
 *  - 규칙 구현은 Domain/Policy 에 두고, Service 는 실행 순서에 집중
 *  - 저장 후 DomainEvent 발행
 *  -
 *  [담당 UseCase]
 *  - ViewingCommandUseCase : 진척도 저장
 */

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ViewingCommandService implements ViewingCommandUseCase {

    private final ChapterPort chapterPort;
    private final LearningHistoryRepository learningHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EnrollmentAccessPolicy enrollmentAccessPolicy;
    private final ViewingMetrics viewingMetrics;
    private final EnrollmentPort enrollmentPort;

    @Override
    public SaveProgressResponse handle(SaveProgressCommand command) {

        // 최대 재시도 횟수
        int maxRetry = 3;
        int retryCount = 0;

        while (true) {
            try {
                // 저장 시도
                return doSaveProgress(command);
                // handle() - 낙관적 락 catch 블록에 메트릭 추가
            } catch (ObjectOptimisticLockingFailureException e) {
                // 충돌 발생
                retryCount++;

                // 낙관적 락 충돌 횟수 카운트
                viewingMetrics.recordOptimisticLockConflict();

                log.warn("[Viewing] 낙관적 락 충돌 발생 | 재시도 {}/{} | userId={}, chapterId = {}",
                        retryCount, maxRetry,
                        command.userId(), command.chapterId());

                // 최대 재시도 횟수 초과시 예외 발생
                if (retryCount >= maxRetry) {
                    throw new DomainRuleViolationException(
                            "진척도 저장에 실패했습니다. 잠시 후 다시 시도해주세요."
                    );
                }

                try {
                    // 재시도 전 50ms 대기 (서버 부하 방지)
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }


    // doSaveProgress
    // 실제 진척도 저장 로직 -> handle() 에서 재시도 시 반복 호출
    private SaveProgressResponse doSaveProgress(SaveProgressCommand command) {

        // doSaveProgress() - Timer 로 전체 감싸기
        return viewingMetrics.getSaveProgressTimer().record(() -> {


            // 수강 여부 확인 (Policy)
            enrollmentAccessPolicy.ensureEnrolled(command.userId(), command.lectureId());

            // 챕터 정보 조회 (durationSec 필요)
            Chapter chapter = chapterPort.findById(command.chapterId());

            // 시청 기록 조회 or 신규 생성
            LearningHistory history = learningHistoryRepository
                    .findByUserIdAndChapterId(command.userId(), command.chapterId())
                    .orElse(LearningHistory.create(
                            command.userId(), command.lectureId(), command.chapterId()
                    ));

            // 완료 전 상태 저장
            // -> 이미 완료된 챕터 재시청 시 이벤트 중복 발행 방지
            boolean wasCompleted = history.isCompleted();

            // prevWatchedSeconds 는 history.updateProgress() 호출 전에 선언 필요
            int prevWatchedSeconds = history.getWatchedSeconds();

            // updateProgress() 반환값으로 skip 차단 여부 판단
            boolean hasMeaningfulProgress = history.updateProgress(
                    command.playbackSeconds(), chapter.getDurationSec()
            );

            // skip 차단 시 메트릭 증가
            if (!hasMeaningfulProgress) {
                viewingMetrics.recordSkipBlocked();
            }

            // watchedSeconds 분포 기록
            viewingMetrics.recordWatchedSeconds(history.getWatchedSeconds());

            // 진척도 업데이트 (도메인 메서드)
            history.updateProgress(command.playbackSeconds(), chapter.getDurationSec());

            // 챕터 완료 처리 (도메인 메서드)
            history.complete(chapter.getDurationSec());

            // lastPositionSec null 여부에 따라 저장 분기
            if (command.lastPositionSec() != null) {
                history.saveLastPosition(command.lastPositionSec());
            }

            // 시청 기록 저장
            LearningHistory savedHistory = learningHistoryRepository.save(history);

            // 챕터 완료 시 이벤트 발행
            // wasCompleted = false -> isCompleted = true 일 때만 발행
            if (!wasCompleted && savedHistory.isCompleted()) {
                eventPublisher.publishEvent(new ChapterCompletedEvent(
                        command.userId(),
                        command.lectureId(),
                        command.chapterId(),
                        savedHistory.getWatchedSeconds(),
                        Instant.now()
                ));
                log.info("[Viewing] ChapterCompletedEvent 발행 | userId={}, lectureId={}, chapterId={}",
                        command.userId(), command.lectureId(), command.chapterId());
            }

            /*
             * comment.
             *  ProgressSavedEvent 발행 (잔디 누적용)
             *  - 미완료 챕터
             *  hasMeaningfulProgress = true 일 때
             *  -> delta = playbackSeconds - prevWatchedSeconds
             *  -> 실제 앞으로 나아간 만큼만 누적
             *  - 완료된 챕터 재시청
             *  wasCompleted = true 일 때
             *  -> playbackSeconds 만큼 누적
             *  -> 재시청도 학습으로 인정
             */

            // ProgressSavedEvent 발행 (잔디 누적용)
            if (hasMeaningfulProgress && !wasCompleted) {
                // 미완료 챕터: 실제 증분만큼 누적
                int delta = command.playbackSeconds() - prevWatchedSeconds;
                eventPublisher.publishEvent(new ProgressSavedEvent(
                        command.userId(), command.lectureId(), command.chapterId(),
                        delta, LocalDate.now()
                ));
            } else if (wasCompleted && command.playbackSeconds() > 0) {
                // 완료된 챕터 재시청: playbackSeconds 만큼 누적
                eventPublisher.publishEvent(new ProgressSavedEvent(
                        command.userId(), command.lectureId(), command.chapterId(),
                        command.playbackSeconds(), LocalDate.now()
                ));
            }


            // 전체 진척도 재계산 및 enrollment 업데이트
            int totalProgress = calculateTotalProgress(command.userId(), command.lectureId());
            int completedCount = calculateCompletedCount(command.userId(), command.lectureId());
            enrollmentPort.updateProgress(command.userId(), command.lectureId(), totalProgress);

            log.info("[Viewing] 진척도 저장 완료 | userId={}, lectureId={}, chapterId={}, isCompleted={}, totalProgress={}",
                    command.userId(), command.lectureId(), command.chapterId(),
                    savedHistory.isCompleted(), totalProgress);



            return new SaveProgressResponse(
                    savedHistory.getChapterId(),
                    savedHistory.getWatchedSeconds(),
                    savedHistory.getProgressRate(),
                    savedHistory.isCompleted(),
                    totalProgress,
                    completedCount
            );
        });
    }

    // private 메서드 (내부 로직)
    // enrollment 진척도 재계산 및 저장
    private int calculateTotalProgress(Long userId, Long lectureId) {

        List<Chapter> chapters = chapterPort.findAllByLectureId(lectureId)
                .stream()
                .toList();
        List<LearningHistory> histories = learningHistoryRepository
                .findByUserIdAndLectureId(userId, lectureId);

        // 완료 챕터 durationSec 합산
        int completedDurationSum = chapters.stream()
                .filter(chapter -> histories.stream()
                        .anyMatch(h -> h.getChapterId().equals(chapter.getId())
                                && h.isCompleted()))
                .mapToInt(Chapter::getDurationSec)
                .sum();

        // 미완료 챕터 watchedSeconds 합산
        // watchedSeconds 가 durationSec 초과 방지
        int inProgressWatchedSum = histories.stream()
                .filter(h -> !h.isCompleted())
                .mapToInt(h ->{
                    // 해당 챕터의 durationSec 찾기
                    int durationSec = chapters.stream()
                            .filter(c -> c.getId().equals(h.getChapterId()))
                            .findFirst()
                            .map(Chapter::getDurationSec)
                            .orElse(0);
                    // watchedSeconds 가 durationSec 초과 방지
                    return Math.min(h.getWatchedSeconds(), durationSec);
                })
                .sum();

        // 전체 durationSec 합산
        int totalDurationSum = chapters.stream()
                .mapToInt(Chapter::getDurationSec)
                .sum();

        if (totalDurationSum == 0) return 0;

        int result = (int) Math.round(
                (double) (completedDurationSum + inProgressWatchedSum)
                        / totalDurationSum * 100
        );

        log.debug("[Viewing] 전체 진척도 계산 | userId={}, lectureId={}, " +
                        "completedDurationSum={}, inProgressWatchedSum={}, totalDurationSum={}, result={}",
                userId, lectureId, completedDurationSum, inProgressWatchedSum, totalDurationSum, result);

        // 결과값 100 초과 방지
        return Math.min(result, 100);
    }

    // 완료 된 챕터 수 계산 (learning_history 집계)
    private int calculateCompletedCount(Long userId, Long lectureId) {

        int count = (int) learningHistoryRepository
                .findByUserIdAndLectureId(userId, lectureId)
                .stream()
                .filter(LearningHistory::isCompleted)
                .count();

        log.debug("[Viewing] 완료 챕터 수 계산 | userId={}, lectureId={}, completedCount={}",
                userId, lectureId, count);

        return count;
    }

}
