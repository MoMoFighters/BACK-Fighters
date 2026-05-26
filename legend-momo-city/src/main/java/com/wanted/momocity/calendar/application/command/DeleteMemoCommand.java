package com.wanted.momocity.calendar.application.command;

/*
 * comment.
 *  userId(토큰) + calendarId(PathVariable)
 * */

public record DeleteMemoCommand(
        Long userId,
        Long calendarId
) {
}
