package com.wanted.momocity.viewing.infrastructure.catalog;

import com.wanted.momocity.calendar.application.port.TodayChapterInfo;
import com.wanted.momocity.calendar.application.port.TodayChapterPort;
import com.wanted.momocity.viewing.application.port.ChapterPort;
import com.wanted.momocity.viewing.application.port.LecturePort;
import com.wanted.momocity.viewing.domain.model.LearningHistory;
import com.wanted.momocity.viewing.domain.repository.LearningHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/*
* comment.
*  TodayChapterPort 구현체
*  -> viewing 도메인에서 calendar 도메인 포트 구현
*  -> learning_history 에서 last_watched_at 날짜 기준 조회
*  -> lectureTitle, chapterTitle 은 LecturePort, ChapterPort 로 조회
* */

@Slf4j
@Component
@RequiredArgsConstructor
public class TodayChapterAdapter implements TodayChapterPort {

    private final LearningHistoryRepository learningHistoryRepository;
    private final ChapterPort chapterPort;
    private final LecturePort lecturePort;

    @Override
    // findTodayChapters
    // last_watched_at 이 오늘인 learning_history 조회
    // -> chapterId 로 챕터 제목 조회, lectureId 로 강의 제목 조회
    public List<TodayChapterInfo> findTodayChapters(Long userId, LocalDate date) {

        List<LearningHistory> histories =
                learningHistoryRepository.findByUserIdAndDate(userId, date);

        return  histories.stream()
                .map(history -> {

                    // 챕터 제목 조회
                    // ChapterPort.findById() 로 조회 -> 없으면 "알 수 없는 챕터" 반환
                    String chapterTitle = chapterPort.findById(history.getChapterId())
                            .getTitle();

                    // 강의 제목 조회
                    // LecturePort.findById() 로 조회 -> 없으면 "알 수 없는 강의" 반환
                    String lectureTitle = lecturePort.findById(history.getLectureId())
                            .getTitle();

                    return new TodayChapterInfo(
                            lectureTitle,
                            chapterTitle
                    );
                })
                .toList();

    }
}
