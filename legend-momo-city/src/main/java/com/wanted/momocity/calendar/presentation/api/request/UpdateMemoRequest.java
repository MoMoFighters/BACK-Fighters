package com.wanted.momocity.calendar.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateMemoRequest(

        @NotBlank(message = "수정할 제목은 필수 항목입니다.")
        String title,

        @NotNull(message = "시작 날짜는 필수 항목입니다.")
        LocalDate start,

        LocalDate end // nullable

) {
}