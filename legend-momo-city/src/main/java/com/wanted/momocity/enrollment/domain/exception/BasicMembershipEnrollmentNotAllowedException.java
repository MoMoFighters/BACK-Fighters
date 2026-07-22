package com.wanted.momocity.enrollment.domain.exception;

// BASIC 회원이 수강 신청 할 경우 예회
public class BasicMembershipEnrollmentNotAllowedException extends RuntimeException {
    public BasicMembershipEnrollmentNotAllowedException(String message) {
        super(message);
    }
}
