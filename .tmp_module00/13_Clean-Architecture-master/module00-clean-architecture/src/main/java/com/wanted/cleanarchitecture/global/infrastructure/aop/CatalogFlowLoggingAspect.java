package com.wanted.cleanarchitecture.global.infrastructure.aop;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/*
 * CatalogFlowLoggingAspect는 수업 중 "하나의 API 요청이 어떤 계층을 지나가는지"를
 * 한글 로그로 확인하기 위한 흐름 추적 AOP다.
 *
 * Domain 계층에도 예외적으로 AOP 추적을 적용하지만,
 * Domain 클래스에 Logger를 직접 넣지는 않는다.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CatalogFlowLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(CatalogFlowLoggingAspect.class);
    private static final String TRACE_ID_KEY = "catalogFlowTraceId";

    @Around("""
            execution(* com.wanted.cleanarchitecture.catalog.presentation..*(..)) ||
            execution(* com.wanted.cleanarchitecture.catalog.application..*(..)) ||
            execution(* com.wanted.cleanarchitecture.catalog.domain.model.Course.publish(..)) ||
            execution(* com.wanted.cleanarchitecture.catalog.domain.model.Course.addSection(..)) ||
            execution(* com.wanted.cleanarchitecture.catalog.domain.model.Course.addModule(..)) ||
            execution(* com.wanted.cleanarchitecture.catalog.domain.model.Course.pullDomainEvents(..)) ||
            execution(* com.wanted.cleanarchitecture.catalog.domain.model.CourseSection.addModule(..)) ||
            execution(* com.wanted.cleanarchitecture.catalog.infrastructure..*(..))
            """)
    public Object traceCatalogFlow(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean rootTrace = ensureTraceId();
        String layer = resolveLayer(joinPoint);
        String method = joinPoint.getSignature().toShortString();
        String traceId = MDC.get(TRACE_ID_KEY);
        long startedAt = System.currentTimeMillis();

        log.info("[카탈로그-흐름][{}] 진입 | 계층={} | 메서드={}", traceId, layer, method);

        try {
            Object result = joinPoint.proceed();
            long elapsedMs = System.currentTimeMillis() - startedAt;
            log.info("[카탈로그-흐름][{}] 종료 | 계층={} | 메서드={} | 소요시간={}ms", traceId, layer, method, elapsedMs);
            return result;
        } catch (Throwable throwable) {
            long elapsedMs = System.currentTimeMillis() - startedAt;
            log.warn(
                    "[카탈로그-흐름][{}] 예외 발생 | 계층={} | 메서드={} | 소요시간={}ms | 예외메시지={}",
                    traceId,
                    layer,
                    method,
                    elapsedMs,
                    resolveKoreanExceptionMessage(throwable)
            );
            throw throwable;
        } finally {
            if (rootTrace) {
                MDC.remove(TRACE_ID_KEY);
            }
        }
    }

    private boolean ensureTraceId() {
        if (MDC.get(TRACE_ID_KEY) != null) {
            return false;
        }

        MDC.put(TRACE_ID_KEY, resolveRequestTraceId());
        return true;
    }

    private String resolveRequestTraceId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return UUID.randomUUID().toString().substring(0, 8);
        }

        HttpServletRequest request = attributes.getRequest();
        String requestId = request.getHeader("X-Request-Id");
        if (requestId != null && !requestId.isBlank()) {
            return requestId;
        }
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String resolveLayer(ProceedingJoinPoint joinPoint) {
        String typeName = joinPoint.getSignature().getDeclaringTypeName();

        if (typeName.contains(".presentation.")) {
            return "프레젠테이션";
        }
        if (typeName.contains(".application.")) {
            return "애플리케이션";
        }
        if (typeName.contains(".domain.")) {
            return "도메인";
        }
        if (typeName.contains(".infrastructure.")) {
            return "인프라스트럭처";
        }
        if (typeName.startsWith("org.springframework.data.repository")
                || typeName.startsWith("org.springframework.data.jpa.repository")) {
            return "인프라스트럭처";
        }
        return "분류되지 않은 계층";
    }

    private String resolveKoreanExceptionMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return "예외 메시지가 없습니다.";
        }
        return message;
    }
}
