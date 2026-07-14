package com.wanted.momocity.chatbot.application.port;

// LectureReviewQueryPort/LectureEnrollmentQueryPort 컨벤션과 동일하게,
// 포트가 필요로 하는 조회 결과 DTO(LectureSummary)를 인터페이스 안에 nested record로 둔다.
public interface LectureInfoPort {

    record LectureSummary() {
    }
}
