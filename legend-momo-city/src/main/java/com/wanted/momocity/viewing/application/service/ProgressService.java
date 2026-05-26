package com.wanted.momocity.viewing.application.service;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.viewing.application.command.SaveProgressCommand;
import com.wanted.momocity.viewing.application.policy.EnrollmentAccessPolicy;
import com.wanted.momocity.viewing.application.port.ChapterPort;
import com.wanted.momocity.viewing.application.port.EnrollmentPort;
import com.wanted.momocity.viewing.application.port.LecturePort;
import com.wanted.momocity.viewing.application.usecase.GetChapterProgressUseCase;
import com.wanted.momocity.viewing.application.usecase.GetMyLectureUseCase;
import com.wanted.momocity.viewing.application.usecase.GetTotalProgressUseCase;
import com.wanted.momocity.viewing.application.usecase.SaveProgressUseCase;
import com.wanted.momocity.viewing.domain.model.Chapter;
import com.wanted.momocity.viewing.domain.model.LearningHistory;
import com.wanted.momocity.viewing.domain.model.Lecture;
import com.wanted.momocity.viewing.domain.repository.LearningHistoryRepository;
import com.wanted.momocity.viewing.presentation.api.response.ChapterProgressResponse;
import com.wanted.momocity.viewing.presentation.api.response.MyLectureResponse;
import com.wanted.momocity.viewing.presentation.api.response.SaveProgressResponse;
import com.wanted.momocity.viewing.presentation.api.response.TotalProgressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
* comment.
*  진척도 관련 UseCase 구현체
*  HTTP 모름, JPA 모름, 순수 비지니스 흐름만 담당
*  -
*  [담당 UseCase]
*  - SaveProgressUseCase : 진척도 저장
*  - GetTotalProgressUseCase : 전체 진척도 조회
*  - GetChapterProgressUseCase : 챕터별 진척도 조회
*  - GetMyLectureUseCase : 내 수강 강의 목록
* */

@Service
@RequiredArgsConstructor
public class ProgressService implements
        SaveProgressUseCase,
        GetTotalProgressUseCase,
        GetChapterProgressUseCase,
        GetMyLectureUseCase {

    private final ChapterPort chapterPort;
    private final LecturePort lecturePort;
    private final EnrollmentPort enrollmentPort;
    private final LearningHistoryRepository learningHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EnrollmentAccessPolicy enrollmentAccessPolicy;
//    private final SaveProgressUseCase saveProgressUseCase;

    @Override
    @Transactional
    // SaveProgressUseCase
    public SaveProgressResponse saveProgress(SaveProgressCommand command) {

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

        // 진척도 업데이트 (도메인 메서드)
        history.updateProgress(command.playbackSeconds(), chapter.getDurationSec());

        // 쳅터 완료 처리 (도메인 메서드)
        history.complete(command.playbackSeconds(), chapter.getDurationSec());

        // 시청 기록 저장
        LearningHistory savedHistory = learningHistoryRepository.save(history);

        // 전체 진척도 계산 (learning_history 집계)
        int totalProgress = calculateTotalProgress (command.userId(), command.lectureId());
        int completedCount = calculateCompletedCount (command.userId(), command.lectureId());

        return new SaveProgressResponse(
                savedHistory.getChapterId(),
                savedHistory.getWatchedSeconds(),
                savedHistory.getProgressRate(),
                savedHistory.isCompleted(),
                totalProgress,
                completedCount
        );
    }

    @Override
    @Transactional(readOnly = true)
    // GetTotalProgressUseCase
    public TotalProgressResponse getTotalProgress(Long userId, Long lectureId) {

        // 수강 여부 확인 (Policy)
        enrollmentAccessPolicy.ensureEnrolled(userId, lectureId);

        // 전체 챕터 수 조회
        List<Chapter> chapters = chapterPort.findAllByLectureId(lectureId);

        // 진척도 계산 (learning_history 집계)
        int totalProgress = calculateTotalProgress(userId, lectureId);
        int completedCount = calculateCompletedCount(userId, lectureId);

        return new TotalProgressResponse(
                lectureId,
                totalProgress,
                completedCount,
                chapters.size()
        );

    }

    @Override
    @Transactional(readOnly = true)
    // GetChapterProgressUseCase
    public ChapterProgressResponse getChapterProgress(Long userId, Long lectureId) {

        // 수강 여부 확인 (Policy)
        enrollmentAccessPolicy.ensureEnrolled(userId, lectureId);

        // 전체 챕터 목록 조회
        List<Chapter> chapters = chapterPort.findAllByLectureId(lectureId);

        // 시청 기록 전체 조회
        List<LearningHistory> histories = learningHistoryRepository
                .findByUserIdAndLectureId(userId, lectureId);

        // 챕터별 전체 진척도 매핑
        List<ChapterProgressResponse.ChapterProgressItem> items = chapters.stream()
                .map(chapter -> {
                    // 해당 챕터 시청 기록 찾기 (없으면 0 으로 처리)
                    LearningHistory history = histories.stream()
                            .filter(h -> h.getChapterId().equals(chapter.getId()))
                            .findFirst()
                            .orElse(LearningHistory.create(userId, lectureId, chapter.getId()));

                    return new ChapterProgressResponse.ChapterProgressItem(
                            chapter.getId(),
                            chapter.getTitle(),
                            chapter.getOrderNo(),
                            history.getWatchedSeconds(),
                            chapter.getDurationSec(),
                            history.getProgressRate(),
                            history.isCompleted()
                    );
                })
                .toList();

        return new ChapterProgressResponse(lectureId, items);

    }

    @Override
    @Transactional(readOnly = true)
    // GetMyLectureUseCase
    public List<MyLectureResponse> getMyLectures(Long userId) {

        // 수강 목록 전체 조회 (EnrollmentPort 직접 사용 - 목록 조회라서 Policy 뷸필요)
        return enrollmentPort.findAllByUserId(userId)
                .stream()
                .map(enrollment -> {
                    Lecture lecture = lecturePort.findById(enrollment.lectureId());
                    int totalProgress = calculateTotalProgress(userId, enrollment.lectureId());
                    return new MyLectureResponse(
                            lecture.getId(),
                            lecture.getTitle(),
                            lecture.getThumbnailUrl(),
                            lecture.getCategory(),
                            totalProgress
                    );
                })
                .toList();

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
        int totalDurationSum =chapters.stream()
                .mapToInt(Chapter::getDurationSec)
                .sum();

        if (totalDurationSum == 0) return 0;
        return (int) Math.round(
                (double)(completedDurationSum + inProgressWatchedSum)
                / totalDurationSum * 100
        );

    }

    // 완료 된 챕터 수 계산 (learning_history 집계)
    private int calculateCompletedCount (Long userId, Long lectureId) {
        return (int) learningHistoryRepository
                .findByUserIdAndLectureId(userId, lectureId)
                .stream()
                .filter(LearningHistory::isCompleted)
                .count();
    }


}
