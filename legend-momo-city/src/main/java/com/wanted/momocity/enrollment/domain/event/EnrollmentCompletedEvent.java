package com.wanted.momocity.enrollment.domain.event;

// 수강신청 완료 후 다른 기능에서 사용할 이벤트
// 예: 강사 자동 친구 추가
public record EnrollmentCompletedEvent(
        // 수강신청한 학생 ID
        Long studentId,

        // 수강신청한 강의 ID
        Long lectureId
) {
}