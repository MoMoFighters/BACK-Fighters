package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.lecture.application.command.CreateLectureCommand;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.momocity.lecture.domain.model.Lecture;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * LectureCommandService는 강의 등록 유스케이스의 실제 흐름을 담당
 */
@Service
@Transactional
public class LectureCommandService implements LectureCommandUseCase {

    private final LectureRepository lectureRepository;

    public LectureCommandService(LectureRepository lectureRepository) {
        this.lectureRepository = lectureRepository;
    }

    @Override
    public Lecture createLecture(CreateLectureCommand command) {
        /*
         * Lecture.create()에서 강의 생성에 필요한 도메인 검증을 수행한다.
         */
        Lecture lecture = Lecture.create(
                command.teacherId(),
                command.title(),
                command.description(),
                command.thumbnailUrl(),
                command.category()
        );

        /*
         * DB 저장은 Repository Port를 통해 처리한다.
         */
        return lectureRepository.save(lecture);
    }
}