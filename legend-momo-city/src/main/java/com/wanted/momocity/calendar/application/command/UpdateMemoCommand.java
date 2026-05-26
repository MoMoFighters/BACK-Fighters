package com.wanted.momocity.calendar.application.command;

import java.time.LocalDate;

/*
 * comment.
 *  userId(토큰) + calendarId(PathVariable) + title, start, end(RequestBody)
 * */

public record UpdateMemoCommand(
        Long userId,
        Long calendarId,
        String title,
        LocalDate start,
        LocalDate end
) {
}
