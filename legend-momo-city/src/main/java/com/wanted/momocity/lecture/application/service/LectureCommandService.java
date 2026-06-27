package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.command.LectureCommand.AdminChangeLectureStatusCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.ChangeLectureStatusCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.CreateChapterCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.CreateLectureCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.RegisterChapterVideoCommand;
import com.wanted.momocity.lecture.application.port.TeacherAccountPort;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCases.AdminLectureCommandUseCase;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCases.ChapterCommandUseCase;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCases.LectureCommandUseCase;
import com.wanted.momocity.lecture.domain.event.LectureCreatedEvent;
import com.wanted.momocity.lecture.domain.exception.ChapterLimitExceededException;
import com.wanted.momocity.lecture.domain.exception.ChapterNotFoundException;
import com.wanted.momocity.lecture.domain.exception.ChapterVideoAlreadyExistsException;
import com.wanted.momocity.lecture.domain.exception.DuplicateChapterOrderException;
import com.wanted.momocity.lecture.domain.exception.LectureNotFoundException;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.domain.repository.ChapterRepository;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import com.wanted.momocity.lecture.presentation.api.response.AdminLectureResponse.AdminChangeLectureStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Lecture 명령 기능을 처리하는 Application Service.
 *
 * 기존 LectureCommandService, ChapterCommandService,
 * AdminLectureCommandService를 하나로 합친 형태.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LectureCommandService implements
        LectureCommandUseCase,
        ChapterCommandUseCase,
        AdminLectureCommandUseCase {

    // 강의당 등록 가능한 최대 챕터 수
    private static final int MAX_CHAPTER_COUNT = 5;

    // 챕터 동영상 최대 업로드 크기: 500MB
    private static final long MAX_VIDEO_SIZE_BYTES = 500 * 1024 * 1024;

    private final LectureRepository lectureRepository;
    private final ChapterRepository chapterRepository;
    private final TeacherAccountPort teacherAccountPort;
    private final S3UploadPort s3UploadPort;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 강의 생성.
     */
    @Override
    public LectureAggregate createLecture(CreateLectureCommand command) {
        Long teacherId = teacherAccountPort.getTeacherId(command.teacherId());

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

        log.info("강의 등록 완료 - lectureId={}, teacherId={}, status={}",
                savedLecture.getId(),
                savedLecture.getTeacherId(),
                savedLecture.getStatus());

        return savedLecture;
    }

    /**
     * 강사가 본인 강의를 심사 대기 상태로 변경.
     */
    @Override
    public LectureAggregate changeLectureStatus(ChangeLectureStatusCommand command) {
        Long teacherId = teacherAccountPort.getTeacherId(command.teacherId());

        LectureAggregate lecture = lectureRepository.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        if (!lecture.isOwnedBy(teacherId)) {
            throw new AccessDeniedException("본인이 등록한 강의만 상태를 변경할 수 있습니다.");
        }

        if (command.lectureStatus() != LectureStatus.WAITING) {
            throw new DomainRuleViolationException("강의 공개는 관리자만 할 수 있습니다.");
        }

        validateLectureReadyForReview(command.lectureId());

        LectureAggregate changedLecture = lecture.changeStatus(command.lectureStatus());

        return lectureRepository.save(changedLecture);
    }

    /**
     * 챕터 생성.
     */
    @Override
    public LectureChapter createChapter(CreateChapterCommand command) {
        Long teacherId = teacherAccountPort.getTeacherId(command.teacherId());

        LectureAggregate lecture = lectureRepository.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        if (!lecture.isOwnedBy(teacherId)) {
            throw new AccessDeniedException("본인이 등록한 강의에만 챕터를 등록할 수 있습니다.");
        }

        int chapterCount = chapterRepository.countByLectureId(command.lectureId());

        if (chapterCount >= MAX_CHAPTER_COUNT) {
            throw new ChapterLimitExceededException("챕터는 최대 5개까지만 등록할 수 있습니다.");
        }

        boolean duplicatedOrderNo = chapterRepository.existsByLectureIdAndOrderNo(
                command.lectureId(),
                command.orderNo()
        );

        if (duplicatedOrderNo) {
            throw new DuplicateChapterOrderException("동일 강의 안에 이미 사용 중인 챕터 순서입니다.");
        }

        LectureChapter chapter = LectureChapter.createWithoutThumbnail(
                command.lectureId(),
                command.title(),
                command.orderNo()
        );

        LectureChapter savedChapter = chapterRepository.save(chapter);

        String chapterThumbnailUrl = s3UploadPort.upload(
                command.thumbnail(),
                "lecture/" + command.lectureId() + "/chapter/" + savedChapter.getId()
        );

        LectureChapter chapterWithThumbnail = savedChapter.changedChapterThumbnailUrl(
                chapterThumbnailUrl
        );

        LectureChapter resultChapter = chapterRepository.save(chapterWithThumbnail);

        log.info("챕터 등록 완료 - chapterId={}, lectureId={}, orderNo={}",
                savedChapter.getId(),
                savedChapter.getLectureId(),
                savedChapter.getOrderNo());

        return resultChapter;
    }

    /**
     * 챕터 동영상 등록.
     */
    @Override
    public LectureChapter registerChapterVideo(RegisterChapterVideoCommand command) {
        Long teacherId = teacherAccountPort.getTeacherId(command.teacherId());

        LectureAggregate lecture = lectureRepository.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        if (!lecture.isOwnedBy(teacherId)) {
            throw new AccessDeniedException("본인이 등록한 강의의 챕터에만 동영상을 등록할 수 있습니다.");
        }

        LectureChapter chapter = chapterRepository.findById(command.chapterId())
                .orElseThrow(() -> new ChapterNotFoundException("챕터를 찾을 수 없습니다."));

        if (!chapter.belongsTo(command.lectureId())) {
            throw new ChapterNotFoundException("유효하지 않은 챕터 식별자입니다.");
        }

        if (chapter.hasVideo()) {
            throw new ChapterVideoAlreadyExistsException("이미 동영상이 등록된 챕터입니다.");
        }

        if (command.video() == null || command.video().isEmpty()) {
            throw new DomainRuleViolationException("동영상 파일은 필수입니다.");
        }

        if (command.video().getSize() > MAX_VIDEO_SIZE_BYTES) {
            throw new DomainRuleViolationException("동영상 파일 크기는 500MB 이하만 가능합니다.");
        }

        // S3 파일 구조에 맞게 수정
        // EX) Lecutures/1/chapters/1
        String videoUrl = s3UploadPort.upload(
                command.video(),
                "lectures/" + command.lectureId() + "/chapters/" + command.chapterId());

        LectureChapter updatedChapter = chapter.registerVideo(
                videoUrl,
                command.video().getSize(),
                command.durationSec(),
                command.video().getOriginalFilename()
        );

        LectureChapter savedChapter = chapterRepository.save(updatedChapter);

        log.info("챕터 동영상 등록 완료 - chapterId={}, lectureId={}, videoSizeBytes={}",
                savedChapter.getId(),
                savedChapter.getLectureId(),
                savedChapter.getVideoSizeBytes());

        return savedChapter;
    }

    /**
     * 관리자가 강의를 승인 또는 거절 상태로 변경.
     */
    @Override
    public AdminChangeLectureStatusResponse changeLectureStatus(AdminChangeLectureStatusCommand command) {
        LectureAggregate lecture = lectureRepository.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        if (command.lectureStatus() == LectureStatus.ACTIVE) {
            validateLectureReadyForApproval(command.lectureId());
        }

        LectureAggregate changedLecture = lecture.changeStatus(command.lectureStatus());

        LectureAggregate savedLecture = lectureRepository.save(changedLecture);

        log.info("관리자 강의 상태 변경 완료 - adminId={}, lectureId={}, lectureStatus={}",
                command.adminId(),
                savedLecture.getId(),
                savedLecture.getStatus());

        return AdminChangeLectureStatusResponse.from(savedLecture);
    }

    /**
     * 강사가 강의를 심사 요청할 수 있는 상태인지 검증.
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

    /**
     * 관리자가 강의를 승인할 수 있는 상태인지 검증.
     */
    private void validateLectureReadyForApproval(Long lectureId) {
        int chapterCount = chapterRepository.countByLectureId(lectureId);

        if (chapterCount < 1) {
            throw new DomainRuleViolationException("강의를 승인하려면 최소 1개 이상의 챕터가 필요합니다.");
        }

        if (chapterRepository.existsByLectureIdAndVideoUrlIsNull(lectureId)) {
            throw new DomainRuleViolationException("강의를 승인하려면 모든 챕터에 동영상이 등록되어야 합니다.");
        }
    }
}