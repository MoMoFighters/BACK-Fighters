package com.wanted.momocity.lecture.presentation.api.request;

import com.wanted.momocity.lecture.application.command.CreateChapterCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

// CreateChapterRequest는 챕터 등록 JSON 요청을 받는 DTO
public record CreateChapterRequest(
        // 챕터명은 필수
        @NotBlank(message = "챕터명은 필수입니다.")
        String title,

        // 챕터 순서는 1 이상
        @Min(value = 1, message = "챕터 순서는 1 이상이어야 합니다.")
        int orderNo
) {

    // Controller에서 받은 요청값을 application 계층의 Command로 변환
    public CreateChapterCommand toCommand(Long teacherId, Long lectureId) {
        return new CreateChapterCommand(
                teacherId,
                lectureId,
                title,
                orderNo
        );
    }
}