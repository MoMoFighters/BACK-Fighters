package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.command.LectureCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.DeleteChapterCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.DeleteLectureCommand;
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
import com.wanted.momocity.lecture.domain.event.LectureStatusChangedEvent;
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
import com.wanted.momocity.viewing.domain.model.Lecture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/*
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
    private static final int MAX_CHAPTER_COUNT = 10;

    // 챕터 동영상 최대 업로드 크기: 500MB
    // 1MB를 바이트 단위로 계산하기 위한 상수
    private static final long BYTES_PER_MB = 1024L * 1024L;

    // 챕터 동영상 최대 업로드 크기를 MB 단위로 표현한 상수
    private static final long MAX_VIDEO_SIZE_MB = 500L;

    // 챕터 동영상 최대 업로드 크기를 바이트 단위로 변환한 상수
    private static final long MAX_VIDEO_SIZE_BYTES = MAX_VIDEO_SIZE_MB * BYTES_PER_MB;

    // S3 경로
    private static final String LECTURE_S3_PREFIX = "lectures"; // 강의 관련 S3 최상위 폴더명
    private static final String CHAPTER_S3_PREFIX = "chapters"; // 챕터 관련 S3 하위 폴더명

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
        long startTime = System.currentTimeMillis();
        log.info("강의 등록 시작 - teacherId={}, category={}, title={}",
                command.teacherId(),
                command.category(),
                command.title()
                );
        Long teacherId = teacherAccountPort.getTeacherId(command.teacherId());

        LectureAggregate lecture = LectureAggregate.create(
                teacherId,
                command.title(),
                command.description(),
                null,
                command.category()
        );

        LectureAggregate savedLecture = lectureRepository.save(lecture);

        // 강의 썸네일 파일을 S3에 업로드
        String thumbnailUrl = s3UploadPort.upload(
                command.thumbnail(),
                // lectures/{lectureId} 경로 생성
                createLectureThumbnailFolder(savedLecture.getId())
        );

        // 저장된 강의의 thumbnailUrl만 업데이트
        LectureAggregate resultLecture = lectureRepository.updateThumbnailUrl(
                savedLecture.getId(),
                thumbnailUrl
        );

        eventPublisher.publishEvent(new LectureCreatedEvent(
                resultLecture.getId(),
                resultLecture.getTeacherId(),
                resultLecture.getTitle(),
                Instant.now()
        ));

        long elapsedTime = System.currentTimeMillis()-startTime;
        log.info("강의 등록 완료 - lectureId={}, teacherId={}, status={}, elapsedTime={}ms",
                resultLecture.getId(),
                resultLecture.getTeacherId(),
                resultLecture.getStatus(),
                elapsedTime
        );

        return resultLecture;
    }

    /**
     * 강사가 강의 신청 시 강의 상태 변경 Waiting
     */
    @Override
    public LectureAggregate changeLectureStatus(ChangeLectureStatusCommand command) {

        long startTime = System.currentTimeMillis();
        log.info("강의 상태 변경 시작 - teacherId={}, lectureId={}, status={}",
                command.teacherId(),
                command.lectureId(),
                command.lectureStatus()
                );

        Long teacherId = teacherAccountPort.getTeacherId(command.teacherId());

        LectureAggregate lecture = lectureRepository.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        if (!lecture.isOwnedBy(teacherId)) {
            throw new AccessDeniedException("본인이 등록한 강의만 상태를 확인 할 수 있습니다.");
        }

        if (command.lectureStatus() != LectureStatus.WAITING) {
            throw new DomainRuleViolationException("강의 공개는 관리자만 할 수 있습니다.");
        }

        validateLectureReadyForReview(command.lectureId());

        LectureAggregate changedLecture = lecture.changeStatus(command.lectureStatus());

        LectureAggregate savedLecture = lectureRepository.save(changedLecture); // 변경된 강의 상태 저장

        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("강사 강의 상태 변경 완료 - teacherId={}, lectureId={}, lectureStatus={}, elapsedTime={}ms",
                teacherId,
                savedLecture.getId(),
                savedLecture.getStatus(),
                elapsedTime
        );
        return savedLecture;
    }

    @Override
    public LectureAggregate deleteLecture(DeleteLectureCommand command) {
        long startTime = System.currentTimeMillis();
        log.info("강의 삭제 시작 - userId={}, role={}, lectureId={}",
                command.userId(),
                command.role(),
                command.lectureId());

        // 삭제 대상 강의 조회
        LectureAggregate lecture = lectureRepository.findById(command.lectureId())
                // 강의가 없다면
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        // 삭제하려는 사람이 강사라면
        if ("ROLE_TEACHER".equals(command.role())) {
            // userId로 강사 조회
            Long teacherId = teacherAccountPort.getTeacherId(command.userId());

            // 만약 본인의 강의가 아니라면
            if (!lecture.isOwnedBy(teacherId)) {
                throw new AccessDeniedException("본인이 등록한 강의만 삭제할 수 있습니다.");
            }
            // 관리자도 아니고 강사도 아닌 Role일 경우
        } else if (!"ROLE_ADMIN".equals(command.role())) {
            throw new AccessDeniedException("강사 또는 관리자만 강의를 삭제할 수 있습니다.");
        }

        // 강의가 이미 삭젝됐을 경우
        if (lecture.getStatus() == LectureStatus.DELETED) {
            throw new DomainRuleViolationException("이미 삭제된 강의입니다.");
        }

        // 강의 삭제 시 해당 강의에 속한 모든 챕터를 실제 삭제
        // 챕터 row에 있는 영상 URL, 파일 크기, 재생시간, 원본파일명이 포함되어 있어, 챕터 삭제 시 DB 기준 영상 정보도 함께 삭제
        chapterRepository.deleteAllByLectureId(lecture.getId());

        // 강의 상태를 DELETED로 변경한 도메인 객체 생성
        LectureAggregate deletedLecture = lecture.changeStatus(LectureStatus.DELETED);

        // 변경된 상태를 DB에 저장
        LectureAggregate savedLecture = lectureRepository.save(deletedLecture);

        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("강의 삭제 완료 - userId={}, role={}, lectureId={}, lectureStatus={}, elapsedTime={}",
                command.userId(),
                command.role(),
                savedLecture.getId(),
                savedLecture.getStatus(),
                elapsedTime
                );

        return savedLecture;
    }

    /**
     * 챕터 생성.
     */
    @Override
    public LectureChapter createChapter(CreateChapterCommand command) {

        long startTime = System.currentTimeMillis();
        log.info("챕터 등록 시작 - teacherId={}, lectureId={}, orderNo={}, title={}",
                command.teacherId(),
                command.lectureId(),
                command.orderNo(),
                command.title()
        );
        Long teacherId = teacherAccountPort.getTeacherId(command.teacherId());

        LectureAggregate lecture = lectureRepository.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        if (!lecture.isOwnedBy(teacherId)) {
            throw new AccessDeniedException("본인이 등록한 강의에만 챕터를 등록할 수 있습니다.");
        }

        int chapterCount = chapterRepository.countByLectureId(command.lectureId());

        if (chapterCount >= MAX_CHAPTER_COUNT) {
            throw new ChapterLimitExceededException("챕터는 최대 10개까지만 등록할 수 있습니다.");
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

        // 챕터 썸네일 파일을 S3에 업로드
        // S3에 파일을 업로드하고, 업로드 결과를 chapterThumbnailUrl 변수에 저장
        String chapterThumbnailUrl = s3UploadPort.upload(

                // 업로드할 챕터 썸네일 파일
                command.thumbnail(),

                // 썸네일이 저장될 S3 폴더 경로 생성
                createChapterFolder(
                        command.lectureId(),
                        savedChapter.getId()
                )

        ); // S3 업로드 후 접근 가능한 URL 반환
        LectureChapter chapterWithThumbnail = savedChapter.changedChapterThumbnailUrl(
                chapterThumbnailUrl
        );

        LectureChapter resultChapter = chapterRepository.save(chapterWithThumbnail);

        long elapsedTime = System.currentTimeMillis()-startTime;
        log.info("챕터 등록 완료 - chapterId={}, lectureId={}, orderNo={}, elapsedTime={}ms",
                savedChapter.getId(),
                savedChapter.getLectureId(),
                savedChapter.getOrderNo(),
                elapsedTime
        );

        return resultChapter;
    }

    /**
     * 챕터 동영상 등록.
     */
    @Override
    public LectureChapter registerChapterVideo(RegisterChapterVideoCommand command) {
        long startTime = System.currentTimeMillis();

        log.info("챕터 동영상 등록 시작 - teacherId={}, lectureId={}, chapterId={}, durationSec={}",
                command.teacherId(),
                command.lectureId(),
                command.chapterId(),
                command.durationSec()
        );
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

        // 챕터 동영상 파일을 S3에 업로드
        // S3 업로드를 실행하고, 업로드 결과로 반환된 값을 videoUrl
        // 동영상은 KEY로 응답
        String videoUrl = s3UploadPort.upload(

                command.video(),

                // 동영상이 저장될 S3 폴더 경로
                // ex) videoUrl = "lectures/6/chapters/1/uuid_chapter01.mp4"
                createChapterFolder(
                        command.lectureId(),
                        command.chapterId()
                )

        ); // S3 업로드 후 접근 가능한 URL 반환

        LectureChapter updatedChapter = chapter.registerVideo(
                videoUrl,
                command.video().getSize(),
                command.durationSec(),
                command.video().getOriginalFilename()
        );

        LectureChapter savedChapter = chapterRepository.save(updatedChapter);

        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("챕터 동영상 등록 완료 - chapterId={}, lectureId={}, videoSizeBytes={}, durationSec={}, elapsedTime={}ms",
                savedChapter.getId(),
                savedChapter.getLectureId(),
                savedChapter.getVideoSizeBytes(),
                savedChapter.getDurationSec(),
                elapsedTime
        );

        return savedChapter;
    }

    // 챕터 삭제
    @Override
    public void deleteChapter(DeleteChapterCommand command) {
        long startTime = System.currentTimeMillis();

        log.info("챕터 삭제 시작 - teacherId={}, lectureId={}, chapterId={}",
                command.lectureId(),
                command.lectureId(),
                command.chapterId()
        );

        // userId로 teacherId로 조회
        Long teacherId = teacherAccountPort.getTeacherId(command.teacherId());

        // 삭제할 챕터의 강의가 있는지 조회
        LectureAggregate lecture = lectureRepository.findById(command.lectureId())
                // 없으면 예외
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        // 해당 강의의 강사가 아닌 경우
        if (!lecture.isOwnedBy(teacherId)) {
            throw new AccessDeniedException("본인이 등록한 강의의 챕터만 삭제 가능합니다.");
        }

        // 이미 삭제된 강의인 경우
        if (lecture.getStatus() == LectureStatus.DELETED) {
            throw new DomainRuleViolationException("삭제된 강의는 찾을 수 없습니다.");
        }

        LectureChapter chapter = chapterRepository.findById(command.chapterId())
                .orElseThrow(() -> new ChapterNotFoundException("챕터를 찾을 수 없습니다."));

        // 챕터가 강의 안에 있는지 확인
        if (!chapter.belongsTo(command.lectureId())) {
            throw new ChapterNotFoundException("유효하지 않은 챕터 식별자입니다.");
        }

        // 검증이 다 끝난 경우 DB에서 실제 삭제 진행
        chapterRepository.deleteById(chapter.getId());

        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("챕터 삭제 완료 - teacherId={}, lectureId={}, chapterId={}, elapsedTime={}",
                command.teacherId(),
                command.lectureId(),
                command.chapterId(),
                elapsedTime
        );
    }

    @Override
    public void deleteChapterVideo(LectureCommand.DeleteChapterVideoCommand command) {
         long startTime = System.currentTimeMillis();

         log.info("동영상 삭제 시작 - teacherId={}, lectureId={}, chapterId={}",
                 command.teacherId(),
                 command.lectureId(),
                 command.chapterId());

         Long teacherId = teacherAccountPort.getTeacherId(command.teacherId());

         LectureAggregate lecture = lectureRepository.findById(command.lectureId())
                 .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

         if (!lecture.isOwnedBy(teacherId)) {
             throw new AccessDeniedException("본인이 등록한 강의의 동영상만 삭제할 수 있습니다.");
         }

         if (lecture.getStatus() == LectureStatus.DELETED) {
             throw new DomainRuleViolationException("삭제된 강의의 동영상은 삭제할 수 없습니다.");
         }

         LectureChapter chapter = chapterRepository.findById(command.chapterId())
                 .orElseThrow(() -> new ChapterNotFoundException("챕터를 찾을 수 없습니다."));

         if (!chapter.belongsTo(command.lectureId())) {
            throw new ChapterNotFoundException("유효하지 않은 챕터 식별자입니다.");
         }

         if (!chapter.hasVideo()) {
             throw new DomainRuleViolationException("삭제할 동영상이 없습니다.");
         }

         chapterRepository.deleteById(chapter.getId());

         long elapsedTime = System.currentTimeMillis() - startTime;

         log.info("동영상 삭제 완료 - teacherId={}, lectureId={}, chapterId={}, elapsedTime={}",
                 teacherId,
                 command.lectureId(),
                 command.chapterId(),
                 elapsedTime);
    }

    /**
     * 관리자가 강의를 승인 또는 거절 상태로 변경.
     */
    @Override
    public AdminChangeLectureStatusResponse changeLectureStatus(AdminChangeLectureStatusCommand command) {
        long startTime = System.currentTimeMillis();

        log.info("관리자 강의 상태 변경 시작 - adminId={}, lectureId={}, targetStatus={}",
                command.adminId(),
                command.lectureId(),
                command.lectureStatus()
        );
        LectureAggregate lecture = lectureRepository.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        if (command.lectureStatus() == LectureStatus.ACTIVE) {
            validateLectureReadyForApproval(command.lectureId());
        }

        LectureAggregate changedLecture = lecture.changeStatus(command.lectureStatus());

        LectureAggregate savedLecture = lectureRepository.save(changedLecture);

        // 강의 승인/거절 상태 변경 이벤트 발행
        eventPublisher.publishEvent(new LectureStatusChangedEvent(
                savedLecture.getId(),
                savedLecture.getTeacherId(),
                command.adminId(),
                savedLecture.getTitle(),
                savedLecture.getStatus(),
                Instant.now()
        ));

        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("관리자 강의 상태 변경 완료 - adminId={}, lectureId={}, beforeStatus={}, afterStatus={}, elapsedTime={}ms",
                command.adminId(),
                savedLecture.getId(),
                lecture.getStatus(),
                savedLecture.getStatus(),
                elapsedTime
        );

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

    // 강의 썸네일 S3 폴더 경로를 생성하는 메서드
    private String createLectureThumbnailFolder(Long lectureId) {

        return LECTURE_S3_PREFIX // lectures
                + "/" + lectureId; // lectures/{lectureId}
    }

    // 특정 강의의 특정 챕터 S3 폴더 경로를 생성하는 메서드
    private String createChapterFolder(Long lectureId, Long chapterId) {

        return LECTURE_S3_PREFIX // lectures
                + "/" + lectureId // lectures/{lectureId}
                + "/" + CHAPTER_S3_PREFIX // lectures/{lectureId}/chapters
                + "/" + chapterId; // lectures/{lectureId}/chapters/{chapterId}

    }
}