package com.wanted.momocity.global.infrastructure.aop;

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
 * GlobalFlowLoggingAspect의 역할 — 한 줄 요약
 * "하나의 API 요청이 어느 컨텍스트의 어떤 계층을 거쳐 가는지 traceId 기반으로 추적한다."
 *
 * 강사의 CatalogFlowLoggingAspect 를 전 컨텍스트로 일반화한 버전.
 *
 * 추적 범위:
 * - presentation 계층: 컨트롤러 진입 / 종료
 * - application 계층: UseCase, Service 호출
 * - infrastructure 계층: Repository Adapter, 외부 호출
 * - domain 계층은 명시적으로 추적하지 않는다.
 *   (getter / setter 까지 다 찍히면 로그가 폭발한다.
 *    컨텍스트 담당자는 자기 컨텍스트에 별도 Aspect 를 두고 핵심 메서드만 추가 추적할 수 있다.)
 *
 * traceId 정책:
 * - 요청 헤더 X-Request-Id 가 있으면 그 값 사용 (분산 시스템 호환)
 * - 없으면 UUID 8자리 자동 생성
 * - MDC 에 박혀 logback 패턴에서 %X{momoTraceId} 로 꺼낼 수 있다.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalFlowLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(GlobalFlowLoggingAspect.class);
    private static final String TRACE_ID_KEY = "momoTraceId";

    @Around("""
        (execution(* com.wanted.momocity..presentation..*(..)) ||
        execution(* com.wanted.momocity..application..*(..)) ||
        execution(* com.wanted.momocity..infrastructure..*(..)))
        && !within(com.wanted.momocity..global..*)
        """)
    public Object traceGlobalFlow(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean rootTrace = ensureTraceId();
        String layer = resolveLayer(joinPoint);
        String context = resolveContext(joinPoint);
        String method = joinPoint.getSignature().toShortString();
        String traceId = MDC.get(TRACE_ID_KEY);
        long startedAt = System.currentTimeMillis();

        // 팀원과 협의 완료 (log.info -> log.debug)
        log.debug("[momocity-흐름][{}] 진입 | 컨텍스트={} | 계층={} | 메서드={}",
                traceId, context, layer, method);

        try {
            Object result = joinPoint.proceed();
            long elapsedMs = System.currentTimeMillis() - startedAt;
            log.debug("[momocity-흐름][{}] 종료 | 컨텍스트={} | 계층={} | 메서드={} | 소요시간={}ms",
                    traceId, context, layer, method, elapsedMs);
            return result;
        } catch (Throwable throwable) {
            long elapsedMs = System.currentTimeMillis() - startedAt;
            // 예외는 항상 있어야하기 때문에 유지
            log.warn(
                    "[momocity-흐름][{}] 예외 발생 | 컨텍스트={} | 계층={} | 메서드={} | 소요시간={}ms | 예외메시지={}",
                    traceId, context, layer, method, elapsedMs,
                    resolveKoreanExceptionMessage(throwable)
            );
            throw throwable;
        } finally {
            if (rootTrace) {
                MDC.remove(TRACE_ID_KEY);
            }
        }
    }

        /* comment.
            서비스 계층의 입출력 추적 어드바이스
            그룹화 및 TRACE_ID_KEY 상수를 공유하기 때문에 동일한 클래스에서 진행 / 추후 어드바이스가 늘어난다면 분리 예정
            흐름 추적 어드바이스는 DEBUG 로 강등되었기 때문에 발표할 때는 주석 또는 나오지 않게 처리
         */

    @Around("execution(* com.wanted.momocity..application.service..*(..))")
    public Object logServiceIo(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = MDC.get(TRACE_ID_KEY);
        String method = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.info("[momocity-서비스][{}] 입력 | 메서드={} | 인자={}",
                traceId, method, formatArgs(args));

        Object result = joinPoint.proceed();

        log.info("[momocity-서비스][{}] 출력 | 메서드={} | 반환={}",
                traceId, method, formatResult(result));

        return result;
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "(없음)";
        }
        return java.util.Arrays.toString(args);
    }

    private String formatResult(Object result) {
        if (result == null) {
            return "(반환값 없음)";
        }
        return result.toString();
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

    private String resolveContext(ProceedingJoinPoint joinPoint) {
        // com.wanted.momocity.<context>.<layer>... 형태에서 <context> 추출
        String typeName = joinPoint.getSignature().getDeclaringTypeName();
        String prefix = "com.wanted.momocity.";
        if (!typeName.startsWith(prefix)) {
            return "외부";
        }
        String tail = typeName.substring(prefix.length());
        int dot = tail.indexOf('.');
        return dot == -1 ? tail : tail.substring(0, dot);
    }

    private String resolveKoreanExceptionMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return "예외 메시지가 없습니다.";
        }
        return message;
    }

}
