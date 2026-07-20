package com.wanted.momocity.study.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * comment.
 *  WebSocket 재연결 유예시간(30초) 처리 전용 스케줄러 빈
 *  domainEventExecutor(AsyncConfig)는 도메인 이벤트 비동기 처리 용도라 목적이 다름 -> study가 별도로 소유
 *  스레드 2개면 동시 재연결 유예 다수 발생해도 충분 (트레이닝 프로젝트 규모 기준)
 * */

@Configuration
public class StudySchedulerConfig {

    @Bean(name = "studyDisconnectScheduler")
    public ScheduledExecutorService studyDisconnectScheduler() {
        ThreadFactory namedFactory = new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "study-disconnect-grace-" + count.getAndIncrement());
            }
        };
        return Executors.newScheduledThreadPool(2, namedFactory);
    }

}
