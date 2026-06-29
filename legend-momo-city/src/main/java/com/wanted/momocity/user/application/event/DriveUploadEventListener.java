package com.wanted.momocity.user.application.event;

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

    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DriveUploadEvent event) {
        try {
            googleDriveUploadPort.uploadGoogleDrive(
                    event.fileBytes(),
                    event.contentType(),
                    event.fileName()
            );
            log.error("[drive] 비동기 업로드 성공 | fileName={}", event.fileName());
        } catch (Exception e) {
            log.error("[drive] 비동기 업로드 실패 | fileName={}", event.fileName());
            redisTemplate.opsForSet().add("drive:retry", String.valueOf(event.userId()));
        }
    }
}
