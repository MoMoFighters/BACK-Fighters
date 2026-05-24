package com.wanted.momocity.viewing.application.service;

/*
* comment.
*  영상 재생 관련 UseCase 구현체
*  Domain 객체들을 불러서 비지니스 호출 조율
*  HTTP 모름, JPA 모름, 순수 비지니스 흐름만 담당
*  -
*  [담당 UseCase]
*  - GetStreamingUrlUseCase : S3 Presigned URL 발급
*  - GetLectureMetaUseCase : 플레이어 UI 상단 정보
*  - GetChapterResumeUseCase : 챕터 이어보기 지점 조회
* */

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.viewing.application.port.ChapterPort;
import com.wanted.momocity.viewing.application.port.LecturePort;
import com.wanted.momocity.viewing.application.port.S3Port;
import com.wanted.momocity.viewing.application.usecase.GetChapterResumeUseCase;
import com.wanted.momocity.viewing.application.usecase.GetLectureMetaUseCase;
import com.wanted.momocity.viewing.application.usecase.GetStreamingUrlUseCase;
import com.wanted.momocity.viewing.domain.model.Chapter;
import com.wanted.momocity.viewing.domain.model.LearningHistory;
import com.wanted.momocity.viewing.domain.model.Lecture;
import com.wanted.momocity.viewing.domain.repository.LearningHistoryRepository;
import com.wanted.momocity.viewing.presentation.api.response.ChapterResumeResponse;
import com.wanted.momocity.viewing.presentation.api.response.LectureMetaResponse;
import com.wanted.momocity.viewing.presentation.api.response.StreamingUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ViewingService implements
        GetStreamingUrlUseCase,
        GetLectureMetaUseCase,
        GetChapterResumeUseCase {

    private final S3Port s3Port;
    private final ChapterPort chapterPort;
    private final LecturePort lecturePort;
    private final LearningHistoryRepository learningHistoryRepository;

    // GetStreamingUrlUseCase
    @Override
    public StreamingUrlResponse getStreamingUrl (Long userId, Long lectureId, Long chapterId) {

        // 챕터 정보 조회
        Chapter chapter = chapterPort.findById(chapterId);

        // 재생 가능 여부 확인 (도메인 메서드)
        if(!chapter.isPlayable()) {
            throw new DomainRuleViolationException("현재 재생할 수 없는 영상입니다.");
        }

        // S3 Presigned URL 발급
        String presignedUrl = s3Port.generatePresignedUrl(
                chapter.getVideoUrl()
        );

        return new StreamingUrlResponse(
                chapter.getId(),
                presignedUrl,
                3600,
                chapter.getTitle(),
                chapter.getDurationSec()
        );

    }

    // GetLectureMeteUseCase
    @Override
    public LectureMetaResponse getLectureMeta (Long userId, Long lectureId) {
        // 강의 정보 조회
        Lecture lecture = lecturePort.findById(lectureId);

        // 전체 챕터 수 조회
        List<Chapter> chapters = chapterPort.findAllByLectureId(lectureId);

        // 현재 챕터 조회
        LearningHistory currentHistory = learningHistoryRepository
                .findLatestByUserIdAndLectureId(userId, lectureId)
                .orElse(null);

        // 현재 챕터 정보 (시청 기록 없으면 첫 번째 챕터)
        Chapter currentChapter = currentHistory != null
                ? chapterPort.findById(currentHistory.getChapterId())
                : chapters.get(0);

        return new LectureMetaResponse(
                lecture.getId(),
                lecture.getTitle(),
                lecture.getInstructorName(),
                chapters.size(),
                currentChapter.getOrderNo(),
                currentChapter.getId(),
                currentChapter.getTitle()
        );
    }

    // GetChapterResumeUseCase
    @Override
    public ChapterResumeResponse getChapterResume (
            Long userId, Long lectureId, Long chapterId
    ) {
        // 챕터 정보 조회
        Chapter chapter = chapterPort.findById(chapterId);

        // 시청 기록 조회
        LearningHistory history = learningHistoryRepository
                .findByUserIdAndChapterId(userId, chapterId)
                .orElse(LearningHistory.create(userId, lectureId, chapterId));

        // 전체 진척도 조회
        int totalProgress = learningHistoryRepository
                .findByUserIdAndLectureId(userId, lectureId)
                .stream()
                .mapToInt(LearningHistory::getProgressRate)
                .sum();


        return new ChapterResumeResponse(
                lectureId,
                chapter.getId(),
                chapter.getTitle(),
                history.getLastPositionSec(),
                chapter.getDurationSec(),
                totalProgress
        );
    }

}
