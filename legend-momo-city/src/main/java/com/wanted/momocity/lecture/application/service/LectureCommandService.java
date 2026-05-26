package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.lecture.application.command.CreateLectureCommand;
import com.wanted.momocity.lecture.application.port.TeacherAccountPort;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.momocity.lecture.domain.model.Lecture;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * LectureCommandService는 강의 등록 유스케이스의 실제 흐름을 담당한다.
 */
@Service
@Transactional
public class LectureCommandService implements LectureCommandUseCase {

    private final LectureRepository lectureRepository;
    private final TeacherAccountPort teacherAccountPort;

    public LectureCommandService(
            LectureRepository lectureRepository,
            TeacherAccountPort teacherAccountPort
    ) {
        this.lectureRepository = lectureRepository;
        this.teacherAccountPort = teacherAccountPort;
    }

    @Override
    public Lecture createLecture(CreateLectureCommand command) {
        /*
         * Authorization 토큰에서 얻은 email로 강사 id를 조회한다.
         */
        Long teacherId = teacherAccountPort.getTeacherId(command.teacherEmail());

        // command.thumbnailUrl()은 S3 업로드 후 생성된 이미지 URL이다.
        Lecture lecture = Lecture.create(
                teacherId,
                command.title(),
                command.description(),
                command.thumbnailUrl(),
                command.category()
        );

        return lectureRepository.save(lecture);
    }
}