package com.wanted.momocity.enrollment.domain.model;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

import java.time.LocalDateTime;

// Enrollment는 "수강 신청"을 의미하는 도메인
public class Enrollment {

    // 수강신청 고유 ID
    private final Long id;

    // 수강신청한 사용자 ID
    private final Long userId;

    // 수강신청한 강의 ID
    private final Long lectureId;

    // 전체 진도율
    private final int totalProgress;

    // 완료한 챕터 개수
    private final int completedCount;

    // 수강신청한 시간
    private final LocalDateTime enrolledAt;

    // 생성자는 private으로 막아둠
    private Enrollment(
            Long id,
            Long userId,
            Long lectureId,
            int totalProgress,
            int completedCount,
            LocalDateTime enrolledAt
    ) {
        validateUserId(userId);
        validateLectureId(lectureId);
        validateTotalProgress(totalProgress);
        validateCompletedCount(completedCount);
        validateEnrolledAt(enrolledAt);

        this.id = id;
        this.userId = userId;
        this.lectureId = lectureId;
        this.totalProgress = totalProgress;
        this.completedCount = completedCount;
        this.enrolledAt = enrolledAt;
    }

    // 새 수강신청을 만들 때 사용하는 메서드
    public static Enrollment create(Long userId, Long lectureId) {
        return new Enrollment(
                null,
                userId,
                lectureId,
                0,
                0,
                LocalDateTime.now()
        );
    }

    // DB에서 조회한 수강신청 데이터를 도메인 객체로 복원할 때 사용하는 메서드
    public static Enrollment reconstitute(
            Long id,
            Long userId,
            Long lectureId,
            int totalProgress,
            int completedCount,
            LocalDateTime enrolledAt
    ) {
        return new Enrollment(
                id,
                userId,
                lectureId,
                totalProgress,
                completedCount,
                enrolledAt
        );
    }

    // userId가 없으면 누가 수강신청했는지 알 수 없음.
    private static void validateUserId(Long userId) {
        if (userId == null) {
            throw new DomainRuleViolationException("사용자 ID는 필수입니다.");
        }
    }

    // lectureId가 없으면 어떤 강의를 수강신청했는지 알 수 없음.
    private static void validateLectureId(Long lectureId) {
        if (lectureId == null) {
            throw new DomainRuleViolationException("강의 ID는 필수입니다.");
        }
    }

    // 전체 진도율은 0 이상 100 이하만 가능함.
    private static void validateTotalProgress(int totalProgress) {
        if (totalProgress < 0 || totalProgress > 100) {
            throw new DomainRuleViolationException("전체 진도율은 0 이상 100 이하이어야 합니다.");
        }
    }

    // 완료한 챕터 개수는 음수가 될 수 없음.
    private static void validateCompletedCount(int completedCount) {
        if (completedCount < 0) {
            throw new DomainRuleViolationException("완료한 챕터 수는 0 이상이어야 합니다.");
        }
    }

    // 수강신청 시간이 없으면 언제 신청했는지 알 수 없음
    private static void validateEnrolledAt(LocalDateTime enrolledAt) {
        if (enrolledAt == null) {
            throw new DomainRuleViolationException("수강신청 시간은 필수입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getLectureId() {
        return lectureId;
    }

    public int getTotalProgress() {
        return totalProgress;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }
}