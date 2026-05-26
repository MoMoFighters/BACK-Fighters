package com.wanted.momocity.calendar.application.command;

/*
 * comment.
 *  userId(토큰) + calendarId(PathVariable) + title, start, end(RequestBody)
 * */

import java.time.LocalDate;

public record UpdateMemoCommand(
        Long userId,
        Long calendarId,
        String title,
        LocalDate start,
        LocalDate end
) {
}
