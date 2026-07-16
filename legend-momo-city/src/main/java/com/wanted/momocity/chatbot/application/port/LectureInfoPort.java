package com.wanted.momocity.chatbot.application.port;

import java.util.Optional;

/* comment.
    챗봇이 강의 정보가 필요할 때 Lecture BC 에 요청하는 인터페이스
    Lecture 팀이 lecture/infra/adapter/LectureInfoAdapter 클래스에서 구현
    <완료가 되면 주석 해제 예정>
*/
public interface LectureInfoPort {

    // lectureId 로 강의 존재 여부 + 취소 정보 조회, 없으면 Optional.empty()
    Optional<LectureSummary> findLectureSummary(Long lectureId);

    // 챗봇이 필요로 하는 강의 정보만 담은 DTO (Lecture BC 구조는 몰라도 됨)
    record LectureSummary(
            Long lectureId,
            String title,
            String description
    ) {}
}
