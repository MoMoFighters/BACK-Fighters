package com.wanted.momocity.lecture.application.usecase;

import com.wanted.momocity.lecture.application.command.ChangeLectureStatusCommand;
import com.wanted.momocity.lecture.application.command.CreateLectureCommand;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;

// LectureCommandUseCase는 강의 상태를 변경하는 인터페이스
public interface LectureCommandUseCase {
    // 강의를 등록
    LectureAggregate createLecture(CreateLectureCommand command);

    // 강사가 본인 강의를 WAITING 상태로 변경
    LectureAggregate changeLectureStatus(ChangeLectureStatusCommand command);
}