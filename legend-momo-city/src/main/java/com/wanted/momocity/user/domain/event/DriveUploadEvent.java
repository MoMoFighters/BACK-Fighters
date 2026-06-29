package com.wanted.momocity.user.domain.event;

public record DriveUploadEvent(
        byte[] fileBytes,
        String contentType,
        String fileName,
        String proofKey,
        Long userId
) {
}
