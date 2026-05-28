package com.wanted.momocity.viewing.application.service;

import com.wanted.momocity.viewing.application.command.SaveProgressCommand;
import com.wanted.momocity.viewing.application.policy.EnrollmentAccessPolicy;
import com.wanted.momocity.viewing.application.port.ChapterPort;
import com.wanted.momocity.viewing.application.usecase.ViewingCommandUseCase;
import com.wanted.momocity.viewing.domain.event.ChapterCompletedEvent;
import com.wanted.momocity.viewing.domain.model.Chapter;
import com.wanted.momocity.viewing.domain.model.LearningHistory;
import com.wanted.momocity.viewing.domain.repository.LearningHistoryRepository;
import com.wanted.momocity.viewing.presentation.api.response.SaveProgressResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

    @Override
    public SaveProgressResponse handle(SaveProgressCommand command) {

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
        // → 이미 완료된 챕터 재시청 시 이벤트 중복 발행 방지
        boolean wasCompleted = history.isCompleted();

        // 진척도 업데이트 (도메인 메서드)
        history.updateProgress(command.playbackSeconds(), chapter.getDurationSec());

        // 챕터 완료 처리 (도메인 메서드)
        history.complete(command.playbackSeconds(), chapter.getDurationSec());

        // 시청 기록 저장
        LearningHistory savedHistory = learningHistoryRepository.save(history);

        // 챕터 완료 시 이벤트 발행
        // wasCompleted = false → isCompleted = true 일 때만 발행
        if (!wasCompleted && savedHistory.isCompleted()) {
            eventPublisher.publishEvent(new ChapterCompletedEvent(
                    command.userId(),
                    command.lectureId(),
                    command.chapterId(),
                    Instant.now()
            ));
            log.info("[Viewing] ChapterCompletedEvent 발행 | userId={}, lectureId={}, chapterId={}",
                    command.userId(), command.lectureId(), command.chapterId());
        }

        // 전체 진척도 계산
        int totalProgress = calculateTotalProgress(command.userId(), command.lectureId());
        int completedCount = calculateCompletedCount(command.userId(), command.lectureId());

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
    }

    // private 메서드 (내부 로직)
    // enrollment 진척도 재계산 및 저장
    private int calculateTotalProgress(Long userId, Long lectureId) {

        List<Chapter> chapters = chapterPort.findAllByLectureId(lectureId);
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
        int inProgressWatchedSum = histories.stream()
                .filter(h -> !h.isCompleted())
                .mapToInt(LearningHistory::getWatchedSeconds)
                .sum();

        // 전체 durationSec 합산
        int totalDurationSum = chapters.stream()
                .mapToInt(Chapter::getDurationSec)
                .sum();

        if (totalDurationSum == 0) return 0;

        int result = (int) Math.round(
                (double)(completedDurationSum + inProgressWatchedSum)
                        / totalDurationSum * 100
        );

        log.debug("[Viewing] 전체 진척도 계산 | userId={}, lectureId={}, " +
                        "completedDurationSum={}, inProgressWatchedSum={}, totalDurationSum={}, result={}",
                userId, lectureId, completedDurationSum, inProgressWatchedSum, totalDurationSum, result);

        return result;
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
