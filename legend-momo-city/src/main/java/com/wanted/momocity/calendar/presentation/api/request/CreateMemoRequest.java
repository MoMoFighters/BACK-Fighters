package com.wanted.momocity.calendar.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/*
 * comment.
 *  title, start 필수값
 *  end nullable (없으면 start 하루만 표시)
 * */

public record CreateMemoRequest(

        @NotBlank(message = "제목은 필수 항목입니다.")
        String title,

        @NotNull(message = "시작 날짜는 필수 항목입니다")
        LocalDate start,

        LocalDate end // nullable

) {
}
