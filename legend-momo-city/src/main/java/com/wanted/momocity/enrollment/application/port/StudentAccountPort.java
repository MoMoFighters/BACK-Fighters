package com.wanted.momocity.enrollment.application.port;

public interface StudentAccountPort {

    Long getStudentId(Long userId);

    StudentEnrollmentInfo getStudentEnrollmentInfo(Long userId);

    record StudentEnrollmentInfo(
            Long studentId,
            // 현재 멤버십으로 수강 신청 할 수 있는지 확인
            boolean enrollmentAllowed
    ){}
}
