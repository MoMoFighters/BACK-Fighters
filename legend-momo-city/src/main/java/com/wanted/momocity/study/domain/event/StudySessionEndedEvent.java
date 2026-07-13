package com.wanted.momocity.study.domain.event;

import java.time.LocalDate;

/*
 * comment.
 *  공부 세션(솔로 or 그룹 타이머) 종료 이벤트
 *  - infrastructure.event.StudyRecordEventHandler가 수신
 *    -> DailyStudyRecord / MonthlyStudyRecord 동시에 누적(accumulate)
 *  -
 *  자정을 걸친 세션은 Service 단에서 자정 기준으로 분할하여
 *  이 이벤트를 날짜별로 2번 발행 (10분은 어제 날짜로, 20분은 오늘 날짜로 각각)
 *  그래서 이 이벤트 자체는 "분할된 하루 몫"만 표현하면 되므로 studyDate 하나만 가짐
 * */
public record StudySessionEndedEvent(
        Long userId,
        LocalDate studyDate,
        int seconds
) {
}