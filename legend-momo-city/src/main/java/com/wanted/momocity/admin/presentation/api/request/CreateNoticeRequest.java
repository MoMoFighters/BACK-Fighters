package com.wanted.momocity.admin.presentation.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wanted.momocity.admin.application.command.CreateNoticeCommand;
import jakarta.validation.constraints.NotBlank;

public record CreateNoticeRequest(
        // 필수값 검증 — null 또는 공백이면 400 반환
        @NotBlank // 문자열 필드에 붙이는 유효성 검사 어노테이션
        String title,
        @NotBlank
        String content,
        // Jackson 이 pinned 키를 찾아서 매핑 실패가 될 수 있다.
        // 즉, is 를 자기 마음대로 제거할 위험이 있다는 것이다.
        @JsonProperty("isPinned")
        boolean isPinned
) {
        // 요청 DTO → Command 변환 (presentation → application 계층 전달용)
        public CreateNoticeCommand toCommand() {
            return new CreateNoticeCommand(title, content, isPinned);
        }

}
