package com.wanted.momocity.user.application.usecase;

import com.wanted.momocity.user.application.command.*;

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

}
