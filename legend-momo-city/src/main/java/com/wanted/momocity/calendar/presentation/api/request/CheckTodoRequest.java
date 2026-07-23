package com.wanted.momocity.calendar.presentation.api.request;

import jakarta.validation.constraints.NotNull;

/*
 * comment,
 *  isCompleted 필수값
 *  true -> 완료, false -> 미완료 (토글방식)
 * */

public record CheckTodoRequest(

        boolean isCompleted

) {
}
