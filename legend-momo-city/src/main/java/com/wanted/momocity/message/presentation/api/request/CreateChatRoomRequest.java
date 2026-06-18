package com.wanted.momocity.message.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateChatRoomRequest(
        @NotBlank List<Long> chatMember
) {
}
