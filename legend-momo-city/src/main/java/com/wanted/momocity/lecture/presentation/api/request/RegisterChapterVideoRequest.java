package com.wanted.momocity.lecture.presentation.api.request;

import com.wanted.momocity.lecture.application.command.RegisterChapterVideoCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/*
 * 챕터 동영상 등록 요청 DTO입니다.
 * multipart/form-data 요청에서 video 파일과 durationSec 값을 받습니다.
 */
public record RegisterChapterVideoRequest(

        // 업로드할 동영상 파일입니다.
        @Schema(description = "업로드할 동영상 파일", type = "string", format = "binary")
        @NotNull(message = "동영상 파일은 필수입니다.")
        MultipartFile video,

        // 동영상 재생 시간입니다. 1초 이상이어야 합니다.
        @Schema(description = "동영상 재생 시간(초)", example = "600")
        @NotNull(message = "동영상 재생 시간은 필수입니다.")
        @Min(value = 1, message = "동영상 재생 시간은 1초 이상이어야 합니다.")
        Integer durationSec
) {

    /*
     * Presentation 계층의 요청 DTO를 Application 계층의 Command로 변환합니다.
     * Controller는 HTTP 요청만 알고, Service는 Command만 보고 처리하게 분리합니다.
     */
    public RegisterChapterVideoCommand toCommand(
            String teacherEmail,
            Long lectureId,
            Long chapterId
    ) {
        return new RegisterChapterVideoCommand(
                teacherEmail,
                lectureId,
                chapterId,
                video,
                durationSec
        );
    }
}