package com.wanted.momocity.viewing;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.viewing.application.command.SaveProgressCommand;
import com.wanted.momocity.viewing.application.policy.EnrollmentAccessPolicy;
import com.wanted.momocity.viewing.application.port.ChapterPort;
import com.wanted.momocity.viewing.application.service.ViewingCommandService;
import com.wanted.momocity.viewing.domain.model.Chapter;
import com.wanted.momocity.viewing.domain.model.LearningHistory;
import com.wanted.momocity.viewing.domain.repository.LearningHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * comment.
 *  ViewingCommandService 단위 테스트
 *
 *  [테스트 대상 UseCase]
 *  - ViewingCommandUseCase : 진척도 저장
 *
 *  [주요 예외 시나리오]
 *  1. 수강하지 않은 사용자가 진척도 저장 시도 → DomainRuleViolationException
 *  2. 존재하지 않는 챕터 조회 시 → DomainRuleViolationException
 *  3. 낙관적 락 충돌이 최대 재시도 횟수 초과 시 → DomainRuleViolationException
 *  4. 낙관적 락 충돌 후 재시도 성공 시 → 정상 응답 반환
 *  5. 이미 완료된 챕터 재시청 시 → 이벤트 중복 발행 방지 검증
 *  6. 챕터 전체 durationSec 이 0 인 경우 → 진척률 0 반환
 */
class ViewingCommandServiceTest {

    private ViewingCommandService viewingCommandService;
    private ChapterPort chapterPort;
    private LearningHistoryRepository learningHistoryRepository;
    private ApplicationEventPublisher eventPublisher;
    private EnrollmentAccessPolicy enrollmentAccessPolicy;

    @BeforeEach
    void setUp() {
        chapterPort = mock(ChapterPort.class);
        learningHistoryRepository = mock(LearningHistoryRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        enrollmentAccessPolicy = mock(EnrollmentAccessPolicy.class);

        viewingCommandService = new ViewingCommandService(
                chapterPort,
                learningHistoryRepository,
                eventPublisher,
                enrollmentAccessPolicy
        );
    }

    @Test
    void 수강하지_않은_사용자가_진척도_저장_시도하면_예외가_발생한다() {

        // given
        SaveProgressCommand command = new SaveProgressCommand(1L, 100L, 10L, 120, null);

        doThrow(new DomainRuleViolationException("수강 중인 강의가 아닙니다."))
                .when(enrollmentAccessPolicy).ensureEnrolled(command.userId(), command.lectureId());

        // when & then
        DomainRuleViolationException exception = assertThrows(
                DomainRuleViolationException.class,
                () -> viewingCommandService.handle(command)
        );

        assertEquals("수강 중인 강의가 아닙니다.", exception.getMessage());
        // 수강 검증 실패 시 챕터 조회 자체가 일어나면 안 됨
        verify(chapterPort, never()).findById(any());
    }

    @Test
    void 존재하지_않는_챕터_조회_시_예외가_발생한다() {

        // given
        SaveProgressCommand command = new SaveProgressCommand(1L, 100L, 999L, 120, null);

        doNothing().when(enrollmentAccessPolicy).ensureEnrolled(command.userId(), command.lectureId());
        when(chapterPort.findById(999L))
                .thenThrow(new DomainRuleViolationException("챕터를 찾을 수 없습니다."));

        // when & then
        DomainRuleViolationException exception = assertThrows(
                DomainRuleViolationException.class,
                () -> viewingCommandService.handle(command)
        );

        assertEquals("챕터를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 낙관적_락_충돌이_최대_재시도_횟수_초과_시_예외가_발생한다() {

        // given
        SaveProgressCommand command = new SaveProgressCommand(1L, 100L, 10L, 120, null);

        Chapter chapter = mock(Chapter.class);
        when(chapter.getDurationSec()).thenReturn(300);

        doNothing().when(enrollmentAccessPolicy).ensureEnrolled(command.userId(), command.lectureId());
        when(chapterPort.findById(command.chapterId())).thenReturn(chapter);
        when(chapterPort.findAllByLectureId(command.lectureId())).thenReturn(Collections.emptyList());
        when(learningHistoryRepository.findByUserIdAndChapterId(command.userId(), command.chapterId()))
                .thenReturn(Optional.empty());

        // 저장 시 항상 낙관적 락 충돌 발생
        when(learningHistoryRepository.save(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(LearningHistory.class, null));

        // when & then
        DomainRuleViolationException exception = assertThrows(
                DomainRuleViolationException.class,
                () -> viewingCommandService.handle(command)
        );

        assertEquals("진척도 저장에 실패했습니다. 잠시 후 다시 시도해주세요.", exception.getMessage());
        // 최대 3회 재시도했는지 검증
        verify(learningHistoryRepository, times(3)).save(any());
    }

    @Test
    void 이미_완료된_챕터_재시청_시_이벤트가_중복_발행되지_않는다() {

        // given : 이미 완료 상태인 LearningHistory
        SaveProgressCommand command = new SaveProgressCommand(1L, 100L, 10L, 300, null);

        Chapter chapter = mock(Chapter.class);
        when(chapter.getDurationSec()).thenReturn(300);
        when(chapter.getId()).thenReturn(10L);

        LearningHistory alreadyCompleted = mock(LearningHistory.class);
        when(alreadyCompleted.isCompleted()).thenReturn(true); // 이미 완료 상태
        when(alreadyCompleted.getChapterId()).thenReturn(10L);
        when(alreadyCompleted.getWatchedSeconds()).thenReturn(300);
        when(alreadyCompleted.getProgressRate()).thenReturn(100);

        doNothing().when(enrollmentAccessPolicy).ensureEnrolled(command.userId(), command.lectureId());
        when(chapterPort.findById(command.chapterId())).thenReturn(chapter);
        when(chapterPort.findAllByLectureId(command.lectureId())).thenReturn(List.of(chapter));
        when(learningHistoryRepository.findByUserIdAndChapterId(command.userId(), command.chapterId()))
                .thenReturn(Optional.of(alreadyCompleted));
        when(learningHistoryRepository.findByUserIdAndLectureId(command.userId(), command.lectureId()))
                .thenReturn(List.of(alreadyCompleted));
        when(learningHistoryRepository.save(any())).thenReturn(alreadyCompleted);

        // when
        viewingCommandService.handle(command);

        // then : 이미 완료 상태였으므로 이벤트 발행 X
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void 챕터_durationSec이_0이면_진척률_0을_반환한다() {

        // given
        SaveProgressCommand command = new SaveProgressCommand(1L, 100L, 10L, 0, null);

        Chapter chapter = mock(Chapter.class);
        when(chapter.getDurationSec()).thenReturn(0); // durationSec = 0
        when(chapter.getId()).thenReturn(10L);

        LearningHistory history = mock(LearningHistory.class);
        when(history.isCompleted()).thenReturn(false);
        when(history.getChapterId()).thenReturn(10L);
        when(history.getWatchedSeconds()).thenReturn(0);
        when(history.getProgressRate()).thenReturn(0);

        doNothing().when(enrollmentAccessPolicy).ensureEnrolled(command.userId(), command.lectureId());
        when(chapterPort.findById(command.chapterId())).thenReturn(chapter);
        when(chapterPort.findAllByLectureId(command.lectureId())).thenReturn(List.of(chapter));
        when(learningHistoryRepository.findByUserIdAndChapterId(command.userId(), command.chapterId()))
                .thenReturn(Optional.of(history));
        when(learningHistoryRepository.findByUserIdAndLectureId(command.userId(), command.lectureId()))
                .thenReturn(List.of(history));
        when(learningHistoryRepository.save(any())).thenReturn(history);

        // when
        var response = viewingCommandService.handle(command);

        // then
        assertEquals(0, response.totalProgress());
    }
}