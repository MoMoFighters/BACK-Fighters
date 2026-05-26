package com.wanted.momocity.calendar.presentation.api.response;

import com.wanted.momocity.calendar.domain.model.Calendar;

import java.time.LocalDate;

/*
 * comment.
 *  Todo 단건 반환
 *  category 는 항상 TODO
 * */

public record TodoResponse(
        Long calendarId,
        String title,
        Calendar.Category category,
        LocalDate start,
        boolean isCompleted
) {
}
