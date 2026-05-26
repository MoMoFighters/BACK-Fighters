package com.wanted.momocity.calendar.application.command;

import java.time.LocalDate;

/*
 * comment.
 *  userId(토큰) + title, start, end(RequestBody)
 *  end = nullable
 * */


public record CreateMemoCommand(
        Long userId,
        String title,
        LocalDate start,
        LocalDate end
) {
}
