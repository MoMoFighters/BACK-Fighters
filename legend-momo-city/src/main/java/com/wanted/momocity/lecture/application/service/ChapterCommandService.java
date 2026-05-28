package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.command.CreateChapterCommand;
import com.wanted.momocity.lecture.application.command.RegisterChapterVideoCommand;
import com.wanted.momocity.lecture.application.port.TeacherAccountPort;
import com.wanted.momocity.lecture.application.usecase.ChapterCommandUseCase;
import com.wanted.momocity.lecture.domain.exception.*;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.domain.repository.ChapterRepository;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ChapterCommandService는 챕터 등록 유스케이스를 처리하는 Service
@Service
@Transactional
public class ChapterCommandService implements ChapterCommandUseCase {

    // 한 강의에 등록 가능한 최대 챕터 개수입니다.
    private static final int MAX_CHAPTER_COUNT = 5;
    // 챕터 동영상은 500MB까지만 업로드 가능
    private static final long MAX_VIDEO_SIZE_BYTES = 500 * 1024 * 1024;

    private final ChapterRepository chapterRepository;
    private final LectureRepository lectureRepository;
    private final TeacherAccountPort teacherAccountPort;
    private final S3UploadPort s3UploadPort;


    public ChapterCommandService(
            ChapterRepository chapterRepository,
            LectureRepository lectureRepository,
            TeacherAccountPort teacherAccountPort,
            S3UploadPort s3UploadPort
    ) {
        this.chapterRepository = chapterRepository;
        this.lectureRepository = lectureRepository;
        this.teacherAccountPort = teacherAccountPort;
        this.s3UploadPort = s3UploadPort;
    }

    @Override
    public LectureChapter createChapter(CreateChapterCommand command) {
        // Authorization 토큰에서 가져온 email로 강사 ID를 조회합니다.
        Long teacherId = teacherAccountPort.getTeacherId(command.teacherEmail());

        // 챕터를 등록할 강의를 조회
        LectureAggregate lecture = lectureRepository.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        // 본인이 등록한 강의에만 챕터를 등록할 수 있음
        if (!lecture.isOwnedBy(teacherId)) {
            throw new AccessDeniedException("본인이 등록한 강의에만 챕터를 등록할 수 있습니다.");
        }

        // 현재 강의에 등록된 챕터 개수를 조회
        int chapterCount = chapterRepository.countByLectureId(command.lectureId());

        // 한 강의에는 최대 5개의 챕터만 등록할 수 있음.
        if (chapterCount >= MAX_CHAPTER_COUNT) {
            throw new ChapterLimitExceededException("챕터는 최대 5개까지만 등록할 수 있습니다.");
        }

        // 같은 강의 안에서 챕터 순서가 중복되는지 확인
        boolean duplicatedOrderNo = chapterRepository.existsByLectureIdAndOrderNo(
                command.lectureId(),
                command.orderNo()
        );

        // 동일 강의 내 orderNo는 중복될 수 없음
        if (duplicatedOrderNo) {
            throw new DuplicateChapterOrderException("동일 강의 내 이미 사용 중인 챕터 순서입니다.");
        }

        // 챕터 도메인 객체를 생성합니다.
        LectureChapter chapter = LectureChapter.create(
                command.lectureId(),
                command.title(),
                command.orderNo()
        );

        // 생성된 챕터를 저장합니다.
        return chapterRepository.save(chapter);
    }

    @Override
    public LectureChapter registerChapterVideo(RegisterChapterVideoCommand command) {
        // Authrization 토큰에서 가져온 email로 강사 Id 조회
        Long teacherId = teacherAccountPort.getTeacherId(command.teacherEmail());

        // 동영상을 등록할 강의를 조회
        LectureAggregate lecture = lectureRepository.findById(command.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        // 본인이 등록한 강읭에만 동영상을 등록 할 수 있음
        if (!lecture.isOwnedBy(teacherId)) {
            throw new AccessDeniedException("본인이 등록한 강의의 챕터에만 동영상을 등록 할 수 있습니다.");
        }

        // 동영상을 등록할 챕터를 조회
        LectureChapter chapter = chapterRepository.findById(command.chapterId())
                .orElseThrow(() -> new ChapterNotFoundException("챕터를 찾을 수 없습니다."));

        // 요청한 챕터가 요청한 강의에 속해 있는지 확인합니다.
        if (!chapter.belongsTo(command.lectureId())) {
            throw new ChapterNotFoundException("유효하지 않은 챕터 식별자입니다.");
        }

        // 하나의 챕터에는 하나의 동영상만 등록할 수 있습니다.
        if (chapter.hasVideo()) {
            throw new ChapterVideoAlreadyExistsException("이미 동영상이 등록된 챕터입니다.");
        }

        // 파일이 비어 있거나 전달되지 않은 경우를 막습니다.
        if (command.video() == null || command.video().isEmpty()) {
            throw new DomainRuleViolationException("동영상 파일은 필수입니다.");
        }

        // S3 업로드 전에 500MB 제한을 먼저 검증합니다.
        if (command.video().getSize() > MAX_VIDEO_SIZE_BYTES) {
            throw new DomainRuleViolationException("동영상 파일 크기는 500MB 이하만 가능합니다.");
        }

        // 모든 검증이 끝난 뒤 S3에 업로드합
        String videoUrl = s3UploadPort.upload(command.video());

        // S3 URL과 파일 정보를 챕터 도메인에 반영합니다.
        LectureChapter updatedChapter = chapter.registerVideo(
                videoUrl,
                command.video().getSize(),
                command.durationSec(),
                command.video().getOriginalFilename()
        );

        // 변경된 챕터 정보를 저장합니다.
        return chapterRepository.save(updatedChapter);
    }
}