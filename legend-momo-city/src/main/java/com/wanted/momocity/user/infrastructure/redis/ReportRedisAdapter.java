package com.wanted.momocity.user.infrastructure.redis;

import com.wanted.momocity.user.application.port.ReportRedisPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ReportRedisAdapter  implements ReportRedisPort {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "report:";
    private static final Duration TTL = Duration.ofHours(24);

    @Override
    public void saveReportTime(Long userId) {
        redisTemplate.opsForValue().set(KEY_PREFIX + userId, "1", TTL);
    }

    @Override
    public boolean existsReportTime(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + userId));
    }

    @Override
    public void deleteReportTime(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}