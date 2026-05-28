package com.wanted.cleanarchitecture.global.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/*
 * AsyncConfig는 Domain Event 후속 처리를 비동기로 실행하기 위한 기술 설정이다.
 * 비즈니스 규칙과 무관한 실행 환경 설정은 infrastructure에 둔다는 점을 보여준다.
 */
@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "domainEventExecutor")
    public Executor domainEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("domain-event-");
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.initialize();
        return executor;
    }
}
