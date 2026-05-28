package com.wanted.cleanarchitecture.learning.application.port;

public interface EnrollmentAccessPort {

    boolean hasActiveEnrollment(Long userId, Long courseId);
}
