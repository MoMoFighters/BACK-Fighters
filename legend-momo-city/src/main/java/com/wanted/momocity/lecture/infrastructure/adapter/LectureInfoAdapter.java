package com.wanted.momocity.lecture.infrastructure.adapter;

import com.wanted.momocity.chatbot.application.port.LectureInfoPort;
import com.wanted.momocity.chatbot.application.port.LectureInfoPort.LectureSummary;
import com.wanted.momocity.chatbot.application.port.LectureInfoPort.LectureRecommendation;
import com.wanted.momocity.lecture.application.port.LectureReviewQueryPort;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LecturePage;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// chatbot의 포트를 Lecture 영역에서 구현하는 어뎁터
@Component
@RequiredArgsConstructor
public class LectureInfoAdapter implements LectureInfoPort {

    private final LectureRepository lectureRepository;
    private final LectureReviewQueryPort lectureReviewQueryPort;
    // 페이징 없이 전체 다 를 흉내내기 위한 임시 상한값
    private static final int MAX_ACTIVE_LECTURES = 1000;

    // LectureInfoPort에 정의된 강의 요약 조회 기능 구현
    @Override
    public Optional<LectureSummary> findLectureSummary(Long lectureId) {
        // LectureRepository에서 전달받은 강의 ID로 강의를 조회
        return lectureRepository.findById(lectureId)
                // 강의가 존재할 때만 LectureSummary로 변환
                .map(lecture -> new LectureSummary(
                        lecture.getId(),
                        lecture.getTitle(),
                        lecture.getDescription()
                ));
    }

    /* comment.
        [챗봇팀 추가] 챗봇 BC에서 "메인페이지 등 어디서든 강의 질문 가능" 기능 지원을 위해 추가.
        본래 lecture BC 소유 파일이라 직접 수정 권한이 없으나, lecture 담당자 승인 받고 진행함.
        기존 findLecture Summary()는 손대지 않았고, 이 메서드만 신규 추가.
     */

    // 새 메서드 선언, 목록만 반환
    @Override
    public List<LectureSummary> findAllActiveLectures() {
        LecturePage page = lectureRepository.findLectures(
                null, null, false, List.of(), 1, MAX_ACTIVE_LECTURES
        );

        return page.content().stream()
                .map(lecture -> new LectureSummary(
                        lecture.getId(),
                        lecture.getTitle(),
                        lecture.getDescription()
                ))
                .toList();
    }

    /* comment.
        [챗봇팀 추가] 카테고리 추천 기능 지원. lecture 담당자 승인 받음.
        카테고리로 강의 조회 후, 평점(getReviewStatsMap 일괄조회)순 정렬해서 상위 limit개만 반환.
     */
    @Override
    public List<LectureRecommendation> recommendTopRatedLecturesByCategory(String category, int limit) {
        LectureCategory lectureCategory = LectureCategory.valueOf(category);
        LecturePage page = lectureRepository.findLectures(
                lectureCategory, null, false, List.of(), 1, MAX_ACTIVE_LECTURES
        );

        List<Long> lectureIds = page.content().stream().map(LectureAggregate::getId).toList();
        Map<Long, LectureReviewQueryPort.ReviewStats> statsMap = lectureReviewQueryPort.getReviewStatsMap(lectureIds);

        return page.content().stream()
                .sorted(Comparator.comparingDouble((LectureAggregate lecture) ->
                        statsMap.getOrDefault(lecture.getId(), new LectureReviewQueryPort.ReviewStats(0.0, 0)).averageRating()
                ).reversed())
                .limit(limit)
                .map(lecture -> new LectureRecommendation(
                        lecture.getId(),
                        lecture.getTitle(),
                        lecture.getDescription(),
                        statsMap.getOrDefault(lecture.getId(), new LectureReviewQueryPort.ReviewStats(0.0, 0)).averageRating()
                ))
                .toList();
    }

}
