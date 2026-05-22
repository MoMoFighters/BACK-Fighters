package com.wanted.momocity.global.infrastructure.config;

import com.wanted.momocity.global.infrastructure.aop.MdcTaskDecorator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.Arrays;
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
@EnableConfigurationProperties(AsyncProperties.class)
public class AsyncConfig implements AsyncConfigurer {

    private final AsyncProperties asyncProperties;

    public AsyncConfig(AsyncProperties asyncProperties) {
        this.asyncProperties = asyncProperties;
    }

    @Bean(name = "domainEventExecutor")
    public Executor domainEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(asyncProperties.threadNamePrefix());
        executor.setCorePoolSize(asyncProperties.corePoolSize());
        executor.setMaxPoolSize(asyncProperties.maxPoolSize());
        executor.setQueueCapacity(asyncProperties.queueCapacity());
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    // 비동기 예외를 핸들링 할 수 있는 메서드
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new LoggingAsyncExceptionHandler();
    }

    // 비동기 관련 예외처리를 커스텀하는 내부 클래스
    /*comment
     *  void 형태의 비동기 메서드의 예외는 호출자에게 작접 전달할 방법이 없다
     *  고로,AsyncUncaughtExceptionHandler 에서 별도로 로깅 / 알림 처리 / 예외 처리를 해야한다  */
    @Slf4j
    private static class LoggingAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            log.error("[비동기 전용 예외 처리기] method = {}, params = {}, message = {}",
                    method.getName(), Arrays.toString(params), ex.getMessage());
        }
    }
}
