package com.wanted.momocity.calendar.application.command;

import java.time.LocalDate;

/*
 * comment.
 *  userId(토큰) + calendarId(PathVariable) + Title, start(RequestBody)
 * */

public record UpdateTodoCommand(
        Long userId,
        Long calendarId,
        String title,
        LocalDate start
) {
}
