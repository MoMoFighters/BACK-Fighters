package com.wanted.momocity.global.infrastructure.aop;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/*
 * MdcTaskDecorator의 역할 — 한 줄 요약
 * "@Async 로 비동기 실행될 때 부모 스레드의 MDC(traceId) 를 자식 스레드에 복사한다."
 *
 * 배경:
 * - GlobalFlowLoggingAspect 가 MDC 에 momoTraceId 를 박는다.
 * - MDC 는 ThreadLocal 기반이라 다른 스레드로 자동 전파되지 않는다.
 * - @Async 호출이 풀 스레드로 이동하는 순간 traceId 가 null 로 바뀐다.
 *
 * 이 데코레이터를 ThreadPoolTaskExecutor 에 박으면
 * 비동기 작업이 부모의 MDC 스냅샷을 그대로 들고 실행된다.
 *
 * 위치 의도:
 * GlobalFlowLoggingAspect 와 짝이 되는 동반자라 같은 패키지(aop) 에 둔다.
 * Aspect 가 MDC 를 박고, 이 데코레이터가 MDC 를 전파한다.
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> parentContext = MDC.getCopyOfContextMap();
        return () -> {
            try {
                if (parentContext != null) {
                    MDC.setContextMap(parentContext);
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
