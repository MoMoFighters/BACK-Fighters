package com.wanted.momocity.global.presentation.api.common;

/*
 * ApiResponseMessage는 presentation 계층이 사용하는 응답 메시지 상수 모음이다.
 * 문자열 정책을 한곳에 모아 controller와 exception handler의 중복을 줄인다.
 *
 * 컨텍스트별 메시지는 각 컨텍스트의 별도 상수 클래스에서 관리한다.
 */
public final class ApiResponseMessage {

    private ApiResponseMessage() {
    }

    // ===== 공통 성공 =====
    public static final String SUCCESS = "Request completed successfully.";
    public static final String CREATED = "Resource created successfully.";

    // ===== 공통 실패 =====
    public static final String VALIDATION_ERROR        = "입력값이 올바르지 않습니다.";
    public static final String DOMAIN_RULE_VIOLATION   = "요청을 처리할 수 없습니다.";
    public static final String NOT_FOUND               = "조회하신 내용을 찾을 수 없습니다.";
    public static final String UNAUTHORIZED            = "다시 로그인 해주세요.";
    public static final String FORBIDDEN               = "접근 권한이 없습니다.";
    public static final String INTERNAL_ERROR          = "알 수 없는 문제가 발생했습니다. ";
}
