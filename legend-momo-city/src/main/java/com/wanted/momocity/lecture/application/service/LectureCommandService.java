package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.lecture.application.command.CreateLectureCommand;
import com.wanted.momocity.lecture.application.port.TeacherAccountPort;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.momocity.lecture.domain.model.Lecture;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// LectureCommandService는 강의 등록 유스케이스의 실제 흐름을 담당
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
         * Authorization 토큰에서 얻은 email로 강사 id를 조회
         * 이 과정에서 사용자가 존재하지 않거나 강사 권한이 아니면 예외가 발생.
         */
        Long teacherId = teacherAccountPort.getTeacherId(command.teacherEmail());

        // Lecture.create()에서 강의 생성에 필요한 도메인 검증을 수행한다.
        Lecture lecture = Lecture.create(
                teacherId,
                command.title(),
                command.description(),
                command.thumbnailUrl(),
                command.category()
        );

         // DB 저장은 Repository Port를 통해 처리
        return lectureRepository.save(lecture);
    }
}