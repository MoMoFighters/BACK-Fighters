package com.wanted.cleanarchitecture.global.presentation.api.common;

/*
 * ApiResponseCode는 외부 API 계약에서 사용하는 비즈니스 응답 코드 모음이다.
 * HTTP status와 분리해 두어, 클라이언트가 업무 시나리오 기준으로도 응답을 해석할 수 있게 한다.
 */
public final class ApiResponseCode {

    private ApiResponseCode() {
    }

    public static final String SUCCESS = "COMMON-SUCCESS";
    public static final String CREATED = "COMMON-CREATED";
    public static final String VALIDATION_ERROR = "COMMON-VALIDATION-ERROR";
    public static final String DOMAIN_RULE_VIOLATION = "COMMON-DOMAIN-RULE-VIOLATION";

    public static final String COURSE_CREATED = "COURSE-CREATED";
    public static final String COURSE_SECTION_CREATED = "COURSE-SECTION-CREATED";
    public static final String COURSE_MODULE_CREATED = "COURSE-MODULE-CREATED";
    public static final String COURSE_PUBLISHED = "COURSE-PUBLISHED";

    public static final String ENROLLMENT_CREATED = "ENROLLMENT-CREATED";
    public static final String MODULE_COMPLETED = "LEARNING-MODULE-COMPLETED";
}
