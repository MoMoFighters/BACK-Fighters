package com.wanted.momocity.calendar.application.command;

/*
 * comment.
 *  userId(토큰) + calendarId(PathVariable)
 * */

public record DeleteTodoCommand(
        Long userId,
        Long calendarId
) {
}