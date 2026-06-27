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

    // 강사 포기
    void teacherGiveup(Long userId);

    // 회원탈퇴 (소프트 딜리트)
    void softDeleteUser(Long userId);

    // 사용자 신고 횟수 +
    LocalDateTime plusReportCount(Long userId);

    // 사용자 신고 횟수 -
    void minusReportCount(Long userId);
}
