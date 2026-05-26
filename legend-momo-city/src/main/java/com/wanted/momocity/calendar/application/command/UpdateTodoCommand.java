package com.wanted.momocity.calendar.application.command;

/*
* comment.
*  userId(토큰) + calendarId(PathVariable) + Title, start(RequestBody)
* */

import java.time.LocalDate;

public record UpdateTodoCommand(
        Long userId,
        Long calendarId,
        String title,
        LocalDate start
) {
}
