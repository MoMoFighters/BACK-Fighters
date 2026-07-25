package com.wanted.momocity.viewing.infrastructure.scheduler;

import com.wanted.momocity.global.infrastructure.s3.S3PresignedUrlAdapter;
import com.wanted.momocity.viewing.application.port.ChapterPort;
import com.wanted.momocity.viewing.application.port.LecturePort;
import com.wanted.momocity.viewing.domain.model.Chapter;
import com.wanted.momocity.viewing.domain.model.Lecture;
import com.wanted.momocity.viewing.infrastructure.persistence.LearningHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class LectureArchiveScheduler {

    private final LecturePort lecturePort;
    private final ChapterPort chapterPort;
    private final LearningHistoryJpaRepository learningHistoryJpaRepository;
    private final S3PresignedUrlAdapter s3PresignedUrlAdapter;

    private static final int INACTIVE_MONTHS_THRESHOLD = 6;

    /*
     * comment.
     *  매일 새벽 3시, HOLD 상태 + 6개월 이상 미시청 강의의 챕터 영상에
     *  archive=true 태그 부착 → S3 Lifecycle Rule이 감지해서 Glacier/Deep Archive 자동 전환
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional(readOnly = true)
    public void tagInactiveLecturesAsArchive() {
        List<Lecture> holdLectures = lecturePort.findAllHoldLectures();

        if (holdLectures.isEmpty()) {
            log.info("[LectureArchiveScheduler] HOLD 상태 강의 없음, 스킵");
            return;
        }

        List<Long> lectureIds = holdLectures.stream()
                .map(Lecture::getId)
                .toList();

        LocalDateTime cutoff = LocalDateTime.now().minusMonths(INACTIVE_MONTHS_THRESHOLD);

        Set<Long> recentlyWatchedLectureIds =
                learningHistoryJpaRepository.findRecentlyWatchedLectureIds(lectureIds, cutoff);

        List<Long> archiveTargetLectureIds = lectureIds.stream()
                .filter(id -> !recentlyWatchedLectureIds.contains(id))
                .toList();

        log.info("[LectureArchiveScheduler] 아카이브 대상 강의 {}건", archiveTargetLectureIds.size());

        for (Long lectureId : archiveTargetLectureIds) {
            List<Chapter> chapters = chapterPort.findAllByLectureId(lectureId);
            for (Chapter chapter : chapters) {
                try {
                    s3PresignedUrlAdapter.markAsArchive(chapter.getVideoUrl());
                    log.info("[LectureArchiveScheduler] 태깅 완료 - lectureId={}, chapterId={}",
                            lectureId, chapter.getId());
                } catch (Exception e) {
                    log.error("[LectureArchiveScheduler] 태깅 실패 - lectureId={}, chapterId={}",
                            lectureId, chapter.getId(), e);
                }
            }
        }
    }
    
}
