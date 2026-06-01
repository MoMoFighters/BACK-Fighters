package com.wanted.momocity.calendar.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wanted.momocity.calendar.domain.model.Calendar;

import java.time.LocalDate;

/*
 * comment.
 *  Todo 단건 반환
 *  category 는 항상 TODO
 * */

public record TodoResponse(
        Long calendarId,
        String title,
        Calendar.Category category,
        LocalDate start,
        // Java record 의 boolean 필드는 getter 가 isCompleted() 가 아닌 completed() 로 생성되어
        // JSON 직렬화 시 "completed" 로 나옴
        // @JsonProperty 로 강제로 "isCompleted" 로 직렬화되게 지정
        @JsonProperty("isCompleted")
        boolean isCompleted
) {
}
