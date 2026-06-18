package com.wanted.momocity.calendar.presentation.api.response;

import java.time.LocalDate;
import java.util.List;

/*
* comment.
*  월별 캘린더 조회 응답 DTO
*  - startDate : 조회 시작 날짜
*  - endDate : 조회 종료 날짜
*  - memos : 해당 월 전체 Memo 목록
*  - Todo 와 오늘 챕터는 일별 조회 API 에서 반환
* */

public record MonthlyCalendarResponse(
        LocalDate startDate,
        LocalDate endDate,
        List<MemoResponse> memos
){
}
