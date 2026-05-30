package com.wanted.momocity.viewing.infrastructure.adapter;

import com.wanted.momocity.enrollment.application.port.ProgressPort;
import com.wanted.momocity.viewing.application.port.ChapterPort;
import com.wanted.momocity.viewing.domain.model.Chapter;
import com.wanted.momocity.viewing.domain.model.LearningHistory;
import com.wanted.momocity.viewing.domain.repository.LearningHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/*
* comment.
*  ProgressPort 구현체
*  -> lecture 담당자가 주입해서 진척률 조회
*  -> ViewingQueryService 의 calculateTotalProgress() 와 동일한 로직
*  -
*  외부 컨텍스트에서 Viewing 데이터 접근하는 Adapter
* */

@Slf4j
@Component
@RequiredArgsConstructor
public class ProgressAdapter
        implements ProgressPort
{

    private final ChapterPort chapterPort;
    private final LearningHistoryRepository learningHistoryRepository;

    @Override
    public int getTotalProgress(Long userId, Long lectureId) {
        List<Chapter> chapters = chapterPort.findAllByLectureId(lectureId);
        List<LearningHistory> histories = learningHistoryRepository
                .findByUserIdAndLectureId(userId, lectureId);

        // 완료 챕터 durationSec 합산
        int completedDurationSum = chapters.stream()
                .filter(chapter -> histories.stream()
                        .anyMatch(h -> h.getChapterId().equals(chapter.getId())
                        && h.isCompleted()))
                .mapToInt(Chapter::getDurationSec)
                .sum();

        // 미완료 챕터watchedSeconds 합산
        int inProgressWatchedSum = histories.stream()
                .filter(h -> !h.isCompleted())
                .mapToInt(LearningHistory::getWatchedSeconds)
                .sum();

        // 전체 durationSec 합산
        int totalDurationSum = chapters.stream()
                .mapToInt(Chapter::getDurationSec)
                .sum();

        if (totalDurationSum == 0) return 0;

        int result = (int) Math.round(
                (double)(completedDurationSum + inProgressWatchedSum)
                / totalDurationSum * 100
        );

        log.debug("[Progress] 진척률 조회 | userId={}, lectureId={}, result={}",
                userId, lectureId, result);

        return result;

    }
}
