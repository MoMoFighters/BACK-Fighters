package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.command.ChangeLectureStatusCommand;
import com.wanted.momocity.lecture.application.command.CreateLectureCommand;
import com.wanted.momocity.lecture.application.port.TeacherAccountPort;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.momocity.lecture.domain.exception.LectureNotFoundException;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.domain.repository.ChapterRepository;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import org.springframework.security.access.AccessDeniedException;
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
    private final ChapterRepository chapterRepository;

    public LectureCommandService(
            LectureRepository lectureRepository,
            TeacherAccountPort teacherAccountPort,
            ChapterRepository chapterRepository
    ) {
        this.lectureRepository = lectureRepository;
        this.teacherAccountPort = teacherAccountPort;
        this.chapterRepository = chapterRepository;
    }

    @Override
    public LectureAggregate createLecture(CreateLectureCommand command) {
        /*
         * Authorization 토큰에서 얻은 email로 강사 id를 조회한다.
         */
        Long teacherId = teacherAccountPort.getTeacherId(command.teacherEmail());

        // command.thumbnailUrl()은 S3 업로드 후 생성된 이미지 URL이다.
        LectureAggregate lecture = LectureAggregate.create(
                teacherId,
                command.title(),
                command.description(),
                command.thumbnailUrl(),
                command.category()
        );

        return lectureRepository.save(lecture);
    }

    @Override
    public LectureAggregate changeLectureStatus(ChangeLectureStatusCommand command) {
        /*
         * Authorization 토큰에서 얻은 email로 강사 id를 조회합니다.
         */
        Long teacherId = teacherAccountPort.getTeacherId(command.teacherEmail());

        // 상태를 변경할 강의를 조회합니다.
        LectureAggregate lecture = lectureRepository.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        // 본인이 등록한 강의만 상태를 변경할 수 있습니다.
        if (!lecture.isOwnedBy(teacherId)) {
            throw new AccessDeniedException("본인이 등록한 강의만 상태를 변경할 수 있습니다.");
        }

        /*
         * ACTIVE 상태는 학생에게 공개되는 상태입니다.
         * 그래서 공개 전 챕터와 동영상이 모두 준비되어 있는지 확인합니다.
         */
        if (command.lectureStatus() == LectureStatus.ACTIVE) {
            validateLectureReadyForActive(command.lectureId());
        }

        // 도메인 모델에 상태 변경을 요청합니다.
        LectureAggregate changedLecture = lecture.changeStatus(command.lectureStatus());

        // 변경된 강의를 저장합니다.
        return lectureRepository.save(changedLecture);
    }

    /*
     * 강의를 ACTIVE 상태로 변경할 수 있는지 검증합니다.
     * 조건:
     * 1. 챕터가 최소 1개 이상 있어야 합니다.
     * 2. 모든 챕터에 동영상이 등록되어 있어야 합니다.
     */
    private void validateLectureReadyForActive(Long lectureId) {
        // 강의에 등록된 챕터 개수를 조회합니다.
        int chapterCount = chapterRepository.countByLectureId(lectureId);

        if (chapterCount < 1) {
            throw new DomainRuleViolationException("강의를 공개하려면 챕터가 최소 1개 이상 필요합니다.");
        }

        // videoUrl이 없는 챕터가 있는지 확인합니다.
        boolean hasChapterWithoutVideo =
                chapterRepository.existsByLectureIdAndVideoUrlIsNull(lectureId);

        if (hasChapterWithoutVideo) {
            throw new DomainRuleViolationException("강의를 공개하려면 모든 챕터에 동영상이 등록되어야 합니다.");
        }
    }
}