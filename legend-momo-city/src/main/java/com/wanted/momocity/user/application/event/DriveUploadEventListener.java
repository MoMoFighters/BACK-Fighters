package com.wanted.momocity.user.application.event;

import com.wanted.momocity.global.application.s3.S3DownloadPort;
import com.wanted.momocity.user.application.port.GoogleDriveUploadPort;
import com.wanted.momocity.user.domain.event.DriveUploadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriveUploadEventListener {

    private final GoogleDriveUploadPort googleDriveUploadPort;
    private final StringRedisTemplate redisTemplate;
    private final S3DownloadPort s3DownloadPort;

    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DriveUploadEvent event) { // fileName, proofKey, userId
        try {
            String contentType = event.proofKey().endsWith(".pdf") ? "application/pdf" : "video/mp4";
            byte[] fileBytes = s3DownloadPort.download(event.proofKey());
            googleDriveUploadPort.uploadGoogleDrive(fileBytes, contentType, event.fileName());
            log.info("[drive] 비동기 업로드 성공 | fileName={}", event.fileName());
        } catch (Exception e) {
            log.error("[drive] 비동기 업로드 실패 | fileName={}", event.fileName());
            redisTemplate.opsForSet().add("drive:retry", String.valueOf(event.userId()));
        }
    }
}
