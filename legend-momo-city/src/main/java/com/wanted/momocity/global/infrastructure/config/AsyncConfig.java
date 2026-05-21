package com.wanted.momocity.global.infrastructure.config;

import com.wanted.momocity.global.infrastructure.aop.MdcTaskDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/*
 * AsyncConfig의 역할 — 한 줄 요약
 * "도메인 이벤트 등 후속 처리를 비동기로 실행하기 위한 ThreadPoolExecutor를 정의한다."
 *
 * 비즈니스 규칙과 무관한 실행 환경 설정은 infrastructure 에 둔다.
 *
 * 사용 예 (Application Service 측):
 *   @Async("domainEventExecutor")
 *   public void on(PaymentCompletedEvent event) { ... }
 *
 * 풀 사이즈는 초기 보수적인 값으로 시작. 운영 모니터링하면서 조정한다.
 *
 * MdcTaskDecorator 적용 이유:
 * GlobalFlowLoggingAspect 가 박은 MDC(momoTraceId) 를
 * 비동기 작업 스레드에도 전파해야 로그 추적이 끊기지 않는다.
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
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }
}
