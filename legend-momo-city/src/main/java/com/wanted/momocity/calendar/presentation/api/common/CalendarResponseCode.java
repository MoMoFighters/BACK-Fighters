package com.wanted.momocity.calendar.presentation.api.common;

/*
 * comment.
 *  calendar 컨텍스트 전용 API 응답 코드 상수 모음
 *  CALENDAR-*
 * */

public class CalendarResponseCode {

    private CalendarResponseCode() {}

    // 캘린더 조회
    public static final String MONTHLY_CALENDAR_FOUND = "CALENDAR-MONTHLY-FOUND";

    // Todo
    public static final String TODO_CREATED          = "CALENDAR-TODO-CREATED";
    public static final String TODO_UPDATED          = "CALENDAR-TODO-UPDATED";
    public static final String TODO_DELETED          = "CALENDAR-TODO-DELETED";
    public static final String TODO_CHECKED          = "CALENDAR-TODO-CHECKED";

    // Memo
    public static final String MEMO_CREATED          = "CALENDAR-MEMO-CREATED";
    public static final String MEMO_UPDATED          = "CALENDAR-MEMO-UPDATED";
    public static final String MEMO_DELETED          = "CALENDAR-MEMO-DELETED";
}

