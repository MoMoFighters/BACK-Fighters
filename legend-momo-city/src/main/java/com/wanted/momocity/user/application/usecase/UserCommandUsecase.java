package com.wanted.momocity.user.application.usecase;

import com.wanted.momocity.user.application.command.*;

import java.time.LocalDateTime;

public interface UserCommandUsecase {

    String registerNickname(NicknameRegisterCommand command);

    void updateUserInfo(UpdateUserInfoCommand command);

    // 강사 신청
    void teacherApply(TeacherApplyCommand command);

    // 강사 승인 처리
    void approve(ApproveTeacherCommand command);

    // 강사 거절 처리
    void reject(RejectTeacherCommand command);

    // 밤티 알림 설정
    boolean setAlarm(Long userId);

    /**
     * 승인/반려 처리 결과.
     * status: ACTIVE (승인) 또는 REJECTED (반려)
     * reason: 반려 시 사유, 승인 시 null
     */
    record TeacherActionResult(
            Long userId,
            String status,
            String reason,
            LocalDateTime processedAt
    ) {
    }

}
