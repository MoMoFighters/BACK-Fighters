package com.wanted.momocity.lecture.application.usecase;

import com.wanted.momocity.lecture.application.command.AdminChangeLectureStatusCommand;
import com.wanted.momocity.lecture.presentation.api.response.AdminChangeLectureStatusResponse;

// 관리자 강의 명령 기능을 정의하는 UseCase
public interface AdminLectureCommandUseCase {

    // 관리자가 강의 상태를 변경
    AdminChangeLectureStatusResponse changeLectureStatus(AdminChangeLectureStatusCommand command);
}