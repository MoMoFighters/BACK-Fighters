package com.wanted.momocity.viewing.application.service;

import com.wanted.momocity.viewing.application.command.SaveProgressCommand;
import com.wanted.momocity.viewing.application.policy.EnrollmentAccessPolicy;
import com.wanted.momocity.viewing.application.port.ChapterPort;
import com.wanted.momocity.viewing.application.port.EnrollmentPort;
import com.wanted.momocity.viewing.application.port.LecturePort;
import com.wanted.momocity.viewing.domain.exception.ViewingAccessDeniedException;
import com.wanted.momocity.viewing.domain.model.Chapter;
import com.wanted.momocity.viewing.domain.model.LearningHistory;
import com.wanted.momocity.viewing.domain.repository.LearningHistoryRepository;
import com.wanted.momocity.viewing.presentation.api.response.SaveProgressResponse;
import com.wanted.momocity.viewing.presentation.api.response.TotalProgressResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/*
 * ProgressServiceTest
 *
 * [테스트 전략]
 * @ExtendWith(MockitoExtension.class)
 * → Spring 전체 컨텍스트 로드 없이
 *   Mockito 가 필요한 Mock 객체만 생성해서 주입
 * → 가볍고 빠른 단위 테스트 가능
 *
 * [테스트 대상]
 * ProgressService 의 핵심 비즈니스 로직 검증
 * → 진척도 저장, 전체 진척도 조회
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("ProgressService 테스트")
public class ProgressServiceTest {

    /*
     * @Mock
     * → 실제 구현체 없이 가짜 객체 생성
     * → when().thenReturn() 으로 원하는 반환값 지정 가능
     */

    @Mock private ChapterPort chapterPort;
    @Mock private LecturePort lecturePort;
    @Mock private EnrollmentPort enrollmentPort;
    @Mock private LearningHistoryRepository learningHistoryRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private EnrollmentAccessPolicy enrollmentAccessPolicy;

    /*
     * @InjectMocks
     * → @Mock 으로 만든 가짜 객체를 ProgressService 에 주입
     * → 실제 Service 로직만 독립적으로 테스트 가능
     */

    @InjectMocks
    private ProgressService progressService;

    private Long userId;
    private Long lectureId;
    private Long chapterId;
    private Chapter mockChapter;
    private LearningHistory mockHistory;

    @BeforeEach
    void setUp() {
        userId = 1L;
        lectureId = 1L;
        chapterId = 1L;

        // Mock Chapter (durationSec = 600초)
        mockChapter = Chapter.reconstitute(
                chapterId, lectureId, "임시 챕터",
                1, "video/chapter1.mp4", 600,
                Chapter.VideoStatus.READY
        );

        // Mock LearningHistory (신규 생성)
        mockHistory = LearningHistory.reconstitute(
                1L, userId, lectureId, chapterId,
                0, false, 0, 0
        );
    }

    @Test
    @DisplayName("진척도 저장 성공 테스트")
    void 진척도_저장_성공_테스트() {

        // given
        SaveProgressCommand command = new SaveProgressCommand(
                userId, lectureId, chapterId, 300
        );

        // Mock 설정
        // enrollmentAccessPolicy.ensureEnrolled() → 아무것도 안 함 (정상)
        // chapterPort.findById() → mockChapter 반환
        // learningHistoryRepository.findByUserIdAndChapterId() → 기존 기록 없음
        // learningHistoryRepository.save() → 저장 후 반환
        // learningHistoryRepository.findByUserIdAndLectureId() → 빈 리스트
        when(chapterPort.findById(chapterId)).thenReturn(mockChapter);
        when(learningHistoryRepository.findByUserIdAndChapterId(userId, chapterId))
                .thenReturn(Optional.empty());
        when(learningHistoryRepository.save(any(LearningHistory.class)))
                .thenReturn(LearningHistory.reconstitute(
                        1L, userId, lectureId, chapterId,
                        300, false, 0, 50
                ));
        when(learningHistoryRepository.findByUserIdAndLectureId(userId, lectureId))
                .thenReturn(List.of());
        when(chapterPort.findAllByLectureId(lectureId))
                .thenReturn(List.of(mockChapter));

        // when
        SaveProgressResponse response = progressService.saveProgress(command);

        // then
        // response 가 null 이 아닌지 확인
        assertNotNull(response);
        // chapterId 가 맞는지 확인
        assertEquals(chapterId, response.chapterId());
        // 300초 시청 → isCompleted = false (90% 미만)
        assertFalse(response.isCompleted());
        // watchedSeconds = 300 확인
        assertEquals(300, response.watchedSeconds());
    }

    @Test
    @DisplayName("진척도 저장 - 90% 이상 시청 시 챕터 완료 처리 테스트")
    void 진척도_저장_챕터_완료_테스트() {

        // given
        // durationSec = 600, 90% = 540초 이상이면 완료
        SaveProgressCommand command = new SaveProgressCommand(
                userId, lectureId, chapterId, 550
        );

        when(chapterPort.findById(chapterId)).thenReturn(mockChapter);
        when(learningHistoryRepository.findByUserIdAndChapterId(userId, chapterId))
                .thenReturn(Optional.empty());

        // 550초 시청 → isCompleted = true 로 저장
        when(learningHistoryRepository.save(any(LearningHistory.class)))
                .thenReturn(LearningHistory.reconstitute(
                        1L, userId, lectureId, chapterId,
                        600, true, 0, 100  // 완료 처리
                ));
        when(learningHistoryRepository.findByUserIdAndLectureId(userId, lectureId))
                .thenReturn(List.of());
        when(chapterPort.findAllByLectureId(lectureId))
                .thenReturn(List.of(mockChapter));

        // when
        SaveProgressResponse response = progressService.saveProgress(command);

        // then
        assertNotNull(response);
        // 550 >= 600 * 0.9(540) 이므로 완료 처리
        assertTrue(response.isCompleted());
    }

    @Test
    @DisplayName("전체 진척도 조회 성공 테스트")
    void 전체_진척도_조회_성공_테스트() {

        // given
        // 챕터 1개 완료 상태 Mock
        LearningHistory completedHistory = LearningHistory.reconstitute(
                1L, userId, lectureId, chapterId,
                600, true, 0, 100
        );

        when(chapterPort.findAllByLectureId(lectureId))
                .thenReturn(List.of(mockChapter));
        when(learningHistoryRepository.findByUserIdAndLectureId(userId, lectureId))
                .thenReturn(List.of(completedHistory));

        // when
        TotalProgressResponse response = progressService.getTotalProgress(userId, lectureId);

        // then
        assertNotNull(response);
        assertEquals(lectureId, response.lectureId());
        // 챕터 1개 완료 → totalProgress = 100
        assertEquals(100, response.totalProgress());
        // 완료 챕터 수 = 1
        assertEquals(1, response.completedCount());
        // 전체 챕터 수 = 1
        assertEquals(1, response.totalChapterCount());
    }

    @Test
    @DisplayName("수강 권한 없는 강의 진척도 저장 시 ViewingAccessDeniedException 발생")
    void 수강_권한_없음_예외_테스트() {

        // given
        SaveProgressCommand command = new SaveProgressCommand(
                userId, lectureId, chapterId, 300
        );

        // enrollmentAccessPolicy.ensureEnrolled() → 예외 발생
        org.mockito.Mockito.doThrow(
                new ViewingAccessDeniedException("수강 신청된 강의만 시청할 수 있습니다.")
        ).when(enrollmentAccessPolicy).ensureEnrolled(userId, lectureId);

        // when & then
        ViewingAccessDeniedException exception = assertThrows(
                ViewingAccessDeniedException.class,
                () -> progressService.saveProgress(command)
        );

        assertEquals("수강 신청된 강의만 시청할 수 있습니다.", exception.getMessage());
    }
}