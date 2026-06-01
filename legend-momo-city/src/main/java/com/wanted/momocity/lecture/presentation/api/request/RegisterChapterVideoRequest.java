package com.wanted.momocity.lecture.presentation.api.request;

import com.wanted.momocity.lecture.application.command.RegisterChapterVideoCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

// 챕터 동영상 등록 요청 DTO
public record RegisterChapterVideoRequest(

        // 업로드할 동영상 파일
        @NotNull(message = "동영상 파일은 필수입니다.")
        MultipartFile video,

        // 동영상 재생 시간
        @NotNull(message = "동영상 재생 시간은 필수입니다.")
        @Min(value = 1, message = "동영상 재생 시간은 1초 이상이어야 합니다.")
        Integer durationSec
) {

    /* comment
     * Presentation 계층의 요청 DTO를 Application 계층의 Command로 변환
     * Controller는 HTTP 요청만 알고, Service는 Command만 보고 처리하게 분리
     */
    public RegisterChapterVideoCommand toCommand(
            Long teacherId,
            Long lectureId,
            Long chapterId
    ) {
        return new RegisterChapterVideoCommand(
                teacherId,
                lectureId,
                chapterId,
                video,
                durationSec
        );
    }
}