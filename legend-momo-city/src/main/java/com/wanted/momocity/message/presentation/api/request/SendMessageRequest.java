package com.wanted.momocity.message.presentation.api.request;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
        @NotBlank String content
) {
}
