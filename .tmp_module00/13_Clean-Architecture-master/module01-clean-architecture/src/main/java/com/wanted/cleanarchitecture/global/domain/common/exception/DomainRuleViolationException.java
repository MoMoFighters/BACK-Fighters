package com.wanted.cleanarchitecture.global.domain.common.exception;

/* comment.
*   해당 예외 클래스는 domain 규칙 위반 시 표현하는 공통 예외 클래스이다.
*   만약 각 도메인 별 도메인 특화 예외가 있을 시에는
*   각 domain 패키지에 위치시킨다.
*  */
public class DomainRuleViolationException extends RuntimeException {

    public DomainRuleViolationException(String message) {
        super(message);
    }
}
