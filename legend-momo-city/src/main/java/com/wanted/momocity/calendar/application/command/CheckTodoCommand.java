package com.wanted.momocity.calendar.application.command;

/*
 * comment
 *  userId(토큰) + calendarId(PathVariable) + isCompleted(RequestBody)
 * */

public record CheckTodoCommand(
        Long userId,
        Long calendarId,
        boolean isCompleted
) {
}