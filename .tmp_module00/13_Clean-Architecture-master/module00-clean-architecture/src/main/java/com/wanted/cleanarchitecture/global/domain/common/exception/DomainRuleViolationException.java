package com.wanted.cleanarchitecture.global.domain.common.exception;

/*
 * DomainRuleViolationException은 domain 규칙 위반을 표현하는 공통 예외다.
 * 안쪽 계층은 HTTP를 모르고, presentation이 이 예외를 API 오류 응답으로 번역한다.
 */
public class DomainRuleViolationException extends RuntimeException {

    public DomainRuleViolationException(String message) {
        super(message);
    }
}
