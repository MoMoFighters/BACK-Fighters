package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.lecture.application.command.CreateChapterCommand;
import com.wanted.momocity.lecture.application.port.TeacherAccountPort;
import com.wanted.momocity.lecture.application.usecase.ChapterCommandUseCase;
import com.wanted.momocity.lecture.domain.exception.ChapterLimitExceededException;
import com.wanted.momocity.lecture.domain.exception.DuplicateChapterOrderException;
import com.wanted.momocity.lecture.domain.exception.LectureNotFoundException;
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

    private final ChapterRepository chapterRepository;
    private final LectureRepository lectureRepository;
    private final TeacherAccountPort teacherAccountPort;

    public ChapterCommandService(
            ChapterRepository chapterRepository,
            LectureRepository lectureRepository,
            TeacherAccountPort teacherAccountPort
    ) {
        this.chapterRepository = chapterRepository;
        this.lectureRepository = lectureRepository;
        this.teacherAccountPort = teacherAccountPort;
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
}