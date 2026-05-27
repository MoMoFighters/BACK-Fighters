package com.wanted.momocity.enrollment.presentation.api.response;

import com.wanted.momocity.enrollment.domain.model.Enrollment;

import java.time.LocalDateTime;

// CreateEnrollmentResponse는 수강신청 성공 응답 DTO
public record CreateEnrollmentResponse(

        // 수강신청 ID
        Long enrollmentId,

        // 수강신청한 강의 ID
        Long lectureId,

        // 수강신청한 사용자 ID
        Long userId,

        // 전체 진도율
        int totalProgress,

        // 완료한 챕터 수
        int completedCount,

        // 수강신청한 시간
        LocalDateTime enrolledAt

) {

    /*
     * Enrollment 도메인 객체를 응답 DTO로 변환
     * Controller에서 응답을 만들 때 사용
     */
    public static CreateEnrollmentResponse from(Enrollment enrollment) {
        return new CreateEnrollmentResponse(
                enrollment.getId(),
                enrollment.getLectureId(),
                enrollment.getUserId(),
                enrollment.getTotalProgress(),
                enrollment.getCompletedCount(),
                enrollment.getEnrolledAt()
        );
    }
}