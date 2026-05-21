package com.wanted.momocity.global.presentation.api.common;

/*
 * ApiResponseCode는 외부 API 계약에서 사용하는 비즈니스 응답 코드 모음이다.
 * HTTP status와 분리해 두어, 클라이언트가 업무 시나리오 기준으로도 응답을 해석할 수 있게 한다.
 *
 * 코드 명명 규약:
 * - 공통:        COMMON-*
 * - 컨텍스트별:  USER-*, LECTURE-*, ENROLLMENT-*, PAYMENT-*, COMMENT-* ...
 *
 * 컨텍스트별 코드는 이 클래스에 모으지 않고, 각 컨텍스트의 presentation 패키지에서
 * 별도 상수 클래스(예: UserResponseCode) 로 관리한다.
 */
public final class ApiResponseCode {

    private ApiResponseCode() {
    }

    // ===== 공통 성공 =====
    public static final String SUCCESS = "COMMON-SUCCESS";
    public static final String CREATED = "COMMON-CREATED";

    // ===== 공통 실패 =====
    public static final String VALIDATION_ERROR        = "COMMON-VALIDATION-ERROR";
    public static final String DOMAIN_RULE_VIOLATION   = "COMMON-DOMAIN-RULE-VIOLATION";
    public static final String NOT_FOUND               = "COMMON-NOT-FOUND";
    public static final String UNAUTHORIZED            = "COMMON-UNAUTHORIZED";
    public static final String FORBIDDEN               = "COMMON-FORBIDDEN";
    public static final String INTERNAL_ERROR          = "COMMON-INTERNAL-ERROR";
}
