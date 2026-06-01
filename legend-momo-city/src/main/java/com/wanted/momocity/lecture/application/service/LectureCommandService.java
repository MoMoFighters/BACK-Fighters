package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.command.ChangeLectureStatusCommand;
import com.wanted.momocity.lecture.application.command.CreateLectureCommand;
import com.wanted.momocity.lecture.application.port.TeacherAccountPort;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.momocity.lecture.domain.event.LectureCreatedEvent;
import com.wanted.momocity.lecture.domain.exception.LectureNotFoundException;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.domain.repository.ChapterRepository;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

// LectureCommandService는 강의 등록 유스케이스의 실제 흐름을 담당
@Service
@Transactional
@Slf4j
public class LectureCommandService implements LectureCommandUseCase {

    private final LectureRepository lectureRepository;
    private final TeacherAccountPort teacherAccountPort;
    private final ChapterRepository chapterRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LectureCommandService(
            LectureRepository lectureRepository,
            TeacherAccountPort teacherAccountPort,
            ChapterRepository chapterRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.lectureRepository = lectureRepository;
        this.teacherAccountPort = teacherAccountPort;
        this.chapterRepository = chapterRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public LectureAggregate createLecture(CreateLectureCommand command) {
        /*
         * Authorization 토큰에서 얻은 email로 강사 id를 조회
         */
        Long teacherId = teacherAccountPort.getTeacherId(command.teacherId());

        // command.thumbnailUrl()은 S3 업로드 후 생성된 이미지 URL
        LectureAggregate lecture = LectureAggregate.create(
                teacherId,
                command.title(),
                command.description(),
                command.thumbnailUrl(),
                command.category()
        );

        LectureAggregate savedLecture = lectureRepository.save(lecture);

        eventPublisher.publishEvent(new LectureCreatedEvent(
                savedLecture.getId(),
                savedLecture.getTeacherId(),
                savedLecture.getTitle(),
                Instant.now()
        ));

        // 강의 등록이 정상 완료되었는지 추적하기 위한 로그
        // S3 업로드 후 DB 저장까지 성공한 시점이므로 lectureId, teacherId, status만 남긴다.
        log.info("강의 등록 완료 - lectureId={}, teacherId={}, status={}",
                savedLecture.getId(),
                savedLecture.getTeacherId(),
                savedLecture.getStatus());

        return savedLecture;
    }

    @Override
    public LectureAggregate changeLectureStatus(ChangeLectureStatusCommand command) {
        // Authorization 토큰에서 얻은 email로 강사 id를 조회
        Long teacherId = teacherAccountPort.getTeacherId(command.teacherId());

        // 상태를 변경할 강의를 조회
        LectureAggregate lecture = lectureRepository.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        // 본인이 등록한 강의만 상태를 변경할 수 있음.
        // 즉, 다른 강사가 로그인해서 해당 강사의 강인 승인 요청하는건 오류 뜸
        if (!lecture.isOwnedBy(teacherId)) {
            throw new AccessDeniedException("본인이 등록한 강의만 상태를 변경할 수 있습니다.");
        }

        // 강사는 검수 요청 상태인 WAITING으로만 변경
        if (command.lectureStatus() != LectureStatus.WAITING) {
            throw new DomainRuleViolationException("강의 공개는 관리자만 할 수 있습니다.");
        }

        /* comment
         * WAITING은 강사가 강의를 등록 하자마자 대기 상태로 변한다.
         * 관리자가 승인 해줘야 ACTIVE로 학생에게 공개
         */
        if (command.lectureStatus() == LectureStatus.WAITING) {
            validateLectureReadyForReview(command.lectureId());
        }

        // 도메인 모델에 상태 변경을 요청
        LectureAggregate changedLecture = lecture.changeStatus(command.lectureStatus());

        // 변경된 강의를 저장
        return lectureRepository.save(changedLecture);
    }

    /* comment
     * 강의를 ACTIVE 상태로 변경할 수 있는지 검증
     * 조건:
     * 1. 챕터가 최소 1개 이상 있어야 함
     * 2. 모든 챕터에 동영상이 등록되어 있어야 함
     */
    private void validateLectureReadyForReview(Long lectureId) {
        int chapterCount = chapterRepository.countByLectureId(lectureId);

        if (chapterCount < 1) {
            throw new DomainRuleViolationException("강의 등록하려면 챕터가 최소 1개 이상 필요합니다.");
        }

        boolean hasChapterWithoutVideo =
                chapterRepository.existsByLectureIdAndVideoUrlIsNull(lectureId);

        if (hasChapterWithoutVideo) {
            throw new DomainRuleViolationException("강의 등록하려면 모든 챕터에 동영상이 등록되어야 합니다.");
        }
    }
}