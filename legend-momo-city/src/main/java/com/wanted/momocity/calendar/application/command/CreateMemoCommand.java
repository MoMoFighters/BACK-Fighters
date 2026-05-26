package com.wanted.momocity.calendar.application.command;

/*
* comment.
*  userId(토큰) + title, start, end(RequestBody)
*  end = nullable
* */

import java.time.LocalDate;

public record CreateMemoCommand(
        Long userId,
        String title,
        LocalDate start,
        LocalDate end
) {
}
