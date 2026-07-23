package com.wanted.momocity.chatbot.application.port;

import java.util.List;
import java.util.Optional;

/* comment.
    챗봇이 강의 정보가 필요할 때 Lecture BC 에 요청하는 인터페이스
    Lecture 팀이 lecture/infra/adapter/LectureInfoAdapter 클래스에서 구현
*/
public interface LectureInfoPort {

    // lectureId 로 강의 존재 여부 + 취소 정보 조회, 없으면 Optional.empty()
    Optional<LectureSummary> findLectureSummary(Long lectureId);

    // 활성 강의 전체 목록 조회 (매칭 판단은 호출하는 쪽에서 책임)
    List<LectureSummary> findAllActiveLectures();

    // [챗봇팀 추가] 카테고리 내 평점 높은 순 강의 추천, lecture 담당자 승인 받음
    List<LectureRecommendation> recommendTopRatedLecturesByCategory(String category, int limit);

    // 챗봇이 필요로 하는 강의 정보만 담은 DTO (Lecture BC 구조는 몰라도 됨)
    record LectureSummary(
            Long lectureId,
            String title,
            String description
    ) {}

    // 카테고리 내 평점 높은 순 강의 추천을 담은 DTO
    record LectureRecommendation(
            Long lectureId,
            String title,
            String description,
            double averageRating
    ) {}
}
