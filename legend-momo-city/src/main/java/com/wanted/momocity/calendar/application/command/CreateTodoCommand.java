package com.wanted.momocity.calendar.application.command;

/*
 * comment.
 *  userId(토큰) + title(RequestBody) + start(RequestBody)
 * */

import java.time.LocalDate;

public record CreateTodoCommand(
        Long userId,
        String title,
        LocalDate start
) {
}