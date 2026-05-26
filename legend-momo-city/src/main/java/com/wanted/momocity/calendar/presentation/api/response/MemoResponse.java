package com.wanted.momocity.calendar.presentation.api.response;

import com.wanted.momocity.calendar.domain.model.Calendar;

import java.time.LocalDate;

/*
 * comment.
 *  Memo 단건 반환
 *  category 는 항상 MEMO
 *  end 는 nullable
 * */

public record MemoResponse(
        Long calendarId,
        String title,
        Calendar.Category category,
        LocalDate start,
        LocalDate end
) {
}