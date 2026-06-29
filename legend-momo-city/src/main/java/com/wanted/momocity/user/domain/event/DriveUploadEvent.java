package com.wanted.momocity.user.domain.event;

public record DriveUploadEvent(

        String fileName,
        String proofKey,
        Long userId
) {
}
