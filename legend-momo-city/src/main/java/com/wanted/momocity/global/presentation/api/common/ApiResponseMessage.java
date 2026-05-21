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
    public static final String VALIDATION_ERROR        = "Validation failed.";
    public static final String DOMAIN_RULE_VIOLATION   = "Domain rule violated.";
    public static final String NOT_FOUND               = "Resource not found.";
    public static final String UNAUTHORIZED            = "Authentication required.";
    public static final String FORBIDDEN               = "Access denied.";
    public static final String INTERNAL_ERROR          = "Unexpected server error.";
}
