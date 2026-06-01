package com.wanted.momocity.viewing.application.service;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.viewing.application.policy.EnrollmentAccessPolicy;
import com.wanted.momocity.viewing.application.policy.SequentialAccessPolicy;
import com.wanted.momocity.viewing.application.port.ChapterPort;
import com.wanted.momocity.viewing.application.port.EnrollmentPort;
import com.wanted.momocity.viewing.application.port.LecturePort;
import com.wanted.momocity.viewing.application.port.S3Port;
import com.wanted.momocity.viewing.application.usecase.ViewingQueryUseCase;
import com.wanted.momocity.viewing.domain.model.Chapter;
import com.wanted.momocity.viewing.domain.model.LearningHistory;
import com.wanted.momocity.viewing.domain.model.Lecture;
import com.wanted.momocity.viewing.domain.repository.LearningHistoryRepository;
import com.wanted.momocity.viewing.presentation.api.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * comment.
 *  - 읽기 전용 UseCase 구현체
 *  - @Transactional(readOnly = true) 로 DB 부하 최소화
 *  - 상태 변경 없음, 조회만 담당
 *  -
 *  [담당 UseCase]
 *  - ViewingQueryUseCase : 스트리밍 URL, 강의 메타, 이어보기, 진척도, 수강 목록 조회
 */

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ViewingQueryService implements ViewingQueryUseCase {

    private final S3Port s3Port;
    private final ChapterPort chapterPort;
    private final LecturePort lecturePort;
    private final EnrollmentPort enrollmentPort;
    private final LearningHistoryRepository learningHistoryRepository;
    private final EnrollmentAccessPolicy enrollmentAccessPolicy;
    private final SequentialAccessPolicy sequentialAccessPolicy;

    @Override
    public StreamingUrlResponse getStreamingUrl(Long userId, Long lectureId, Long chapterId) {

        // 수강 여부 확인 (Policy)
        enrollmentAccessPolicy.ensureEnrolled(userId, lectureId);

        // 챕터 정보 조회
        Chapter chapter = chapterPort.findById(chapterId);

        // 재생 가능 여부 확인 (도메인 메서드)
        if (!chapter.isPlayable()) {
            throw new DomainRuleViolationException("현재 재생할 수 없는 영상입니다.");
        }

        // 순차 시청 제한 (Policy)
        sequentialAccessPolicy.ensureSequentialAccess(userId, lectureId, chapterId);  // ← 추가

        // S3 Presigned URL 발급
        String presignedUrl = s3Port.generatePresignedUrl(chapter.getVideoUrl());

        log.info("[Viewing] S3 Presigned URL 발급 완료 | userId={}, lectureId={}, chapterId={}",
                userId, lectureId, chapterId);

        return new StreamingUrlResponse(
                chapter.getId(), presignedUrl, 3600,
                chapter.getTitle(), chapter.getDurationSec()
        );
    }

    @Override
    public LectureMetaResponse getLectureMeta(Long userId, Long lectureId) {

        // 수강 여부 확인 (Policy)
        enrollmentAccessPolicy.ensureEnrolled(userId, lectureId);

        // 강의 정보 조회
        Lecture lecture = lecturePort.findById(lectureId);

        // 전체 챕터 수 조회
        List<Chapter> chapters = chapterPort.findAllByLectureId(lectureId)
                .stream()
                .filter(Chapter::isPlayable)
                .toList();

        // 시청 기록 전체 조회
        List<LearningHistory> histories = learningHistoryRepository
                .findByUserIdAndLectureId(userId, lectureId);

        // 현재 챕터 조회 (가장 최근 업데이트된 챕터)
        LearningHistory currentHistory = learningHistoryRepository
                .findLatestByUserIdAndLectureId(userId, lectureId)
                .orElse(null);

        // 현재 챕터 정보 (시청 기록 없으면 첫 번째 챕터)
        Chapter currentChapter = currentHistory != null
                ? chapterPort.findById(currentHistory.getChapterId())
                : chapters.get(0);

        List<LectureMetaResponse.ChapterItem> chaptersItem = chapters.stream()
                        .map(chapter -> {
                            LearningHistory history = histories.stream()
                                    .filter(h -> h.getChapterId().equals(chapter.getId()))
                                    .findFirst()
                                    .orElse(LearningHistory.create(userId, lectureId, chapter.getId()));

                            // isAccessible 계산
                            // 1. 첫 번째 챕터는 무조건 접근 가능
                            // 2. 본인 챕터가 완료됐으면 접근 가능
                            // 3. 이전 챕터가 완료됐으면 접근 가능
                            boolean isAccessible =
                                    // 첫 챕터는 무조건
                                    chapter.getOrderNo() == 1
                                            // 본인이 완료한 챕터
                                            || history.isCompleted()
                                            // 이전 챕터가 완료된 경우
                                            || chapters.stream()
                                            .filter(c -> c.getOrderNo() == chapter.getOrderNo() - 1)
                                            .findFirst()
                                            .map(prevChapter -> histories.stream()
                                                    .anyMatch(h -> h.getChapterId().equals(prevChapter.getId())
                                                            && h.isCompleted()))
                                            .orElse(false);

                                    return new LectureMetaResponse.ChapterItem(
                                            chapter.getId(),
                                            chapter.getTitle(),
                                            chapter.getOrderNo(),
                                            chapter.getDurationSec(),
                                            history.getProgressRate(),
                                            history.isCompleted(),
                                            isAccessible
                                    );
                        })
                                .toList();

        log.info("[Viewing] 강의 메타데이터 조회 완료 | userId={}, lectureId={}",
                userId, lectureId);

        return new LectureMetaResponse(
                lecture.getId(), lecture.getTitle(), lecture.getThumbnailUrl(),
                chapters.size(), currentChapter.getId(), currentChapter.getOrderNo(), currentChapter.getTitle(), chaptersItem
        );
    }

    @Override
    public ChapterResumeResponse getChapterResume(Long userId, Long lectureId, Long chapterId) {

        // 수강 여부 확인 (Policy)
        enrollmentAccessPolicy.ensureEnrolled(userId, lectureId);

        // 챕터 정보 조회
        Chapter chapter = chapterPort.findById(chapterId);

        // 시청 기록 조회
        LearningHistory history = learningHistoryRepository
                .findByUserIdAndChapterId(userId, chapterId)
                .orElse(LearningHistory.create(userId, lectureId, chapterId));

        int totalProgress = calculateTotalProgress(userId, lectureId);

        log.info("[Viewing] 챕터 이어보기 조회 완료 | userId={}, lectureId={}, chapterId={}, lastPositionSec={}",
                userId, lectureId, chapterId, history.getLastPositionSec());

        return new ChapterResumeResponse(
                lectureId, chapter.getId(), chapter.getTitle(),
                history.getLastPositionSec(), chapter.getDurationSec(), totalProgress
        );
    }

    @Override
    public TotalProgressResponse getTotalProgress(Long userId, Long lectureId) {

        // 수강 여부 확인 (Policy)
        enrollmentAccessPolicy.ensureEnrolled(userId, lectureId);

        // 전체 챕터 수 조회
        List<Chapter> chapters = chapterPort.findAllByLectureId(lectureId)
                .stream()
                .filter(Chapter::isPlayable)
                .toList();

        // 진척도 계산 (learning_history 집계)
        int totalProgress = calculateTotalProgress(userId, lectureId);
        int completedCount = calculateCompletedCount(userId, lectureId);

        log.info("[Viewing] 전체 진척도 조회 완료 | userId={}, lectureId={}, totalProgress={}, completedCount={}",
                userId, lectureId, totalProgress, completedCount);

        return new TotalProgressResponse(
                lectureId, totalProgress, completedCount, chapters.size()
        );
    }

    @Override
    public ChapterProgressResponse getChapterProgress(Long userId, Long lectureId) {

        // 수강 여부 확인 (Policy)
        enrollmentAccessPolicy.ensureEnrolled(userId, lectureId);

        // 전체 챕터 목록 조회
        List<Chapter> chapters = chapterPort.findAllByLectureId(lectureId)
                .stream()
                .filter(Chapter::isPlayable)
                .toList();

        // 시청 기록 전체 조회
        List<LearningHistory> histories = learningHistoryRepository
                .findByUserIdAndLectureId(userId, lectureId);

        // 현재 챕터 조회
        LearningHistory currentHistory = learningHistoryRepository
                .findLatestByUserIdAndLectureId(userId, lectureId)
                .orElse(null);

        int currentOrderNo = currentHistory != null
                ? chapterPort.findById(currentHistory.getChapterId()).getOrderNo()
                : 1;  // 시청 기록 없으면 1번 챕터

        // 챕터별 전체 진척도 매핑
        List<ChapterProgressResponse.ChapterProgressItem> items = chapters.stream()
                .map(chapter -> {
                    // 해당 챕터 시청 기록 찾기 (없으면 0 으로 처리)
                    LearningHistory history = histories.stream()
                            .filter(h -> h.getChapterId().equals(chapter.getId()))
                            .findFirst()
                            .orElse(LearningHistory.create(userId, lectureId, chapter.getId()));

                    // isAccessible 계산
                    // 1. 첫 번째 챕터는 무조건 접근 가능
                    // 2. 본인 챕터가 완료됐으면 접근 가능
                    // 3. 이전 챕터가 완료됐으면 접근 가능
                    boolean isAccessible =
                            // 첫 챕터는 무조건
                            chapter.getOrderNo() == 1
                                    // 본인이 완료한 챕터
                                    || history.isCompleted()
                                    // 이전 챕터가 완료된 경우
                                    || chapters.stream()
                                    .filter(c -> c.getOrderNo() == chapter.getOrderNo() - 1)
                                    .findFirst()
                                    .map(prevChapter -> histories.stream()
                                            .anyMatch(h -> h.getChapterId().equals(prevChapter.getId())
                                                    && h.isCompleted()))
                                    .orElse(false);

                    return new ChapterProgressResponse.ChapterProgressItem(
                            chapter.getId(), chapter.getTitle(), chapter.getOrderNo(),
                            Math.min(history.getWatchedSeconds(), chapter.getDurationSec()), chapter.getDurationSec(),
                            history.getProgressRate(), history.isCompleted(),
                            isAccessible
                    );
                })
                .toList();

        log.info("[Viewing] 챕터별 진척도 조회 완료 | userId={}, lectureId={}, chapterCount={}",
                userId, lectureId, chapters.size());

        return new ChapterProgressResponse(lectureId, items);
    }

    @Override
    public MyLecturesResponse getMyLectures(Long userId) {

        // EnrollmentProt 로 수강목록 조회
        List<MyLecturesResponse.LectureItem> lectures = enrollmentPort.findAllByUserId(userId)
                .stream()
                .map(enrollment -> lecturePort.findById(enrollment.lectureId()))
                .filter(Lecture::isViewable)
                .map(lecture -> {
                    int totalProgress = calculateTotalProgress(userId, lecture.getId());
                    // learning_history 집계로 전체 진척도 계산
                    // -> enrollment 테이블에 캐싱 없이 직접 계산
                    EnrollmentPort.EnrollmentInfo enrollment = enrollmentPort
                            .findByUserIdAndLectureId(userId, lecture.getId())
                            .orElseThrow();
                    return new MyLecturesResponse.LectureItem(
                            lecture.getId(), lecture.getTitle(),
                            lecture.getThumbnailUrl(), lecture.getCategory(),
                            totalProgress, enrollment.enrolledAt()
                    );

                })
                .toList();

        log.info("[Viewing] 내 수강 강의 목록 조회 완료 | userId={}, lectureCount={}",
                userId, lectures.size());

        // MyLecturesResponse 로 래핑하여 반환
        return new MyLecturesResponse(lectures);
    }



    // private 메서드 (내부 로직)
    // enrollment 진척도 재계산 및 저장
    private int calculateTotalProgress(Long userId, Long lectureId) {

        List<Chapter> chapters = chapterPort.findAllByLectureId(lectureId)
                .stream()
                // READY 챕터만 필터링
                .filter(Chapter::isPlayable)
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
        int inProgressWatchedSum = histories.stream()
                .filter(h -> !h.isCompleted())
                .mapToInt(h -> {
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

        int result =  (int) Math.round(
                (double)(completedDurationSum + inProgressWatchedSum)
                        / totalDurationSum * 100
        );

        // 결과값 100 초과 방지
        return Math.min(result, 100);

    }

    // 완료 된 챕터 수 계산 (learning_history 집계)
    private int calculateCompletedCount(Long userId, Long lectureId) {
        return (int) learningHistoryRepository
                .findByUserIdAndLectureId(userId, lectureId)
                .stream()
                .filter(LearningHistory::isCompleted)
                .count();
    }

}
