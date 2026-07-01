package com.wanted.momocity.user.infrastructure.scheduler;

import com.wanted.momocity.global.application.s3.S3DownloadPort;
import com.wanted.momocity.user.application.port.GoogleDriveUploadPort;
import com.wanted.momocity.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriveRetryScheduler {

    private final StringRedisTemplate redisTemplate;
    private final S3DownloadPort s3DownloadPort;
    private final GoogleDriveUploadPort googleDriveUploadPort;
    private final UserRepository userRepository;

    private static final String RETRY_KEY = "drive:retry";
    private static final String RETRY_COUNT_PREFIX = "drive:retry:count:";
    private static final int MAX_RETRY = 3;

    @Scheduled(fixedDelay = 180000) // 3분 마다 스케줄러 진행
    public void retry() {
        Set<String> userIds = redisTemplate.opsForSet().members(RETRY_KEY);
        if (userIds == null || userIds.isEmpty()) return;

        for (String userIdStr : userIds) {
            Long userId = Long.valueOf(userIdStr);

            String countKey = RETRY_COUNT_PREFIX + userId;
            String countStr = redisTemplate.opsForValue().get(countKey);
            int count = countStr == null ? 0 : Integer.parseInt(countStr);

            if (count >= MAX_RETRY) {
                log.error("[drive] 재시도 {}회 모두 실패 | userId={}", MAX_RETRY, userId);
                redisTemplate.opsForSet().remove(RETRY_KEY, userIdStr);
                redisTemplate.delete(countKey);
                continue;
            }

            try {
                // DB에서 유저 정보 조회
                var user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("유저 없음"));

                String proofKey = user.getProof();
                String fileName = user.getName() + " - " + user.getCategory().name() + " - " + proofKey.substring(proofKey.lastIndexOf("/") + 1);
                String contentType = proofKey.endsWith(".pdf") ? "application/pdf" : "video/mp4";

                byte[] fileBytes = s3DownloadPort.download(proofKey);
                googleDriveUploadPort.uploadGoogleDrive(fileBytes, contentType, fileName);

                log.info("[drive] 재시도 성공 | userId={}", userId);
                redisTemplate.opsForSet().remove(RETRY_KEY, userIdStr);
                redisTemplate.delete(countKey);

            } catch (Exception e) {
                log.error("[drive] 재시도 실패 ({}/{}) | userId={}", count + 1, MAX_RETRY, userId);
                redisTemplate.opsForValue().set(countKey, String.valueOf(count + 1));
            }
        }
    }
}