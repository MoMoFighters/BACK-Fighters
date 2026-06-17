package com.wanted.momocity.calendar.application.port;

import java.time.LocalDate;
import java.util.List;

/*
* comment.
*  오늘 수강한 챕터 목록 조회 포트
*  -> calendar 도메인 -> viewing 도메인 의존성 역전
*  -> viewing 도메인에서 구현체(TodayChapterAdapter) 제공
* */

public interface TodayChapterPort {
    List<TodayChapterInfo> findTodayChapters(Long userId, LocalDate date);
}
