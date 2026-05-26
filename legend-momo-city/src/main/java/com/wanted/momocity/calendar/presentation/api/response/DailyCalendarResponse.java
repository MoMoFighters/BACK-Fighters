package com.wanted.momocity.calendar.presentation.api.response;

import java.time.LocalDate;
import java.util.List;

/*
 * comment.
 *  날짜별 캘린더 조회 반환
 *  todos : 해당 날짜 Todo 목록
 *  memos : 해당 날짜 Memo 목록
 * */

public record DailyCalendarResponse(
        LocalDate date,
        List<TodoResponse> todos,
        List<MemoResponse> memos
) {
}
