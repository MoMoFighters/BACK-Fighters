package com.wanted.momocity.community.infrastructure.event;

import com.wanted.momocity.community.domain.event.PostsCacheEvictRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostsCacheEvictEventHandler {

    private final CacheManager redisCacheManager;

    @Async("domainEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostsCacheEvictRequestedEvent event) {
        Cache cache = redisCacheManager.getCache("posts");
        if (cache != null) {
            cache.clear();
            log.info("[Community] posts 캐시 전체 무효화 완료 (AFTER_COMMIT)");
        }
    }

}
