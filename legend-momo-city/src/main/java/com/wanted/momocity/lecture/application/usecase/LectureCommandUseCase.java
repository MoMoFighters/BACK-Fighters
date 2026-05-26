package com.wanted.momocity.lecture.application.usecase;

import com.wanted.momocity.lecture.application.command.CreateLectureCommand;
import com.wanted.momocity.lecture.domain.model.Lecture;

/*
 * LectureCommandUseCase는 강의 상태를 변경하는 인터페이스
 * Controller는 Service 구현체가 아니라 이 인터페이스를 바라본다.
 */
public interface LectureCommandUseCase {
    // 강의를 등록
    Lecture createLecture(CreateLectureCommand command);
}