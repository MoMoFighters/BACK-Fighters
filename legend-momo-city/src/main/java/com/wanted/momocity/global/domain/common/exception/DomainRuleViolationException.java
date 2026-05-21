package com.wanted.momocity.global.domain.common.exception;

/*
 * DomainRuleViolationException은 도메인 규칙 위반을 표현하는 공통 예외다.
 *
 * 안쪽 계층(domain / application)은 HTTP를 모른다.
 * presentation 계층의 ApiExceptionHandler가 이 예외를 받아
 * 표준 API 오류 응답(ApiErrorResponse) 으로 번역한다.
 *
 * momocity 사용 예시:
 * - 이미 수강 중인 강의를 다시 신청한 경우
 * - 결제 금액이 0원 이하로 들어온 경우
 * - 영상 상태가 UPLOADING / ENCODING 인데 학습 기록을 적재하려 한 경우
 * - 차단된 사용자가 댓글을 작성하려 한 경우
 *
 * 이 예외를 던질 때 메시지는 "사용자에게 그대로 보여줘도 안전한 표현" 으로 작성한다.
 * 내부 구현 디테일이나 스택 정보는 메시지에 포함하지 않는다.
 */
public class DomainRuleViolationException extends RuntimeException {

    public DomainRuleViolationException(String message) {
        super(message);
    }

    public DomainRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
