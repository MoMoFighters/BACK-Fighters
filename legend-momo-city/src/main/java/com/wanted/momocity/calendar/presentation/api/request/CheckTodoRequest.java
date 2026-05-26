package com.wanted.momocity.calendar.presentation.api.request;

import jakarta.validation.constraints.NotNull;

/*
 * comment,
 *  isCompleted 필수값
 *  true -> 완료, false -> 미완료 (토글방식)
 * */

public record CheckTodoRequest(

        @NotNull(message =  "체크 상태 값은 필수 항목입니다.")
        boolean isCompleted

) {
}
