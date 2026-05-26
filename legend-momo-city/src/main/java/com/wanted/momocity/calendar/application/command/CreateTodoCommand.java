package com.wanted.momocity.calendar.application.command;

import java.time.LocalDate;

/*
 * comment.
 *  userId(토큰) + title(RequestBody) + start(RequestBody)
 * */

public record CreateTodoCommand(
        Long userId,
        String title,
        LocalDate start
) {
}