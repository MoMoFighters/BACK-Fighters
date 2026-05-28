package com.wanted.cleanarchitecture.global.presentation.api.common;

/*
 * ApiResponseMessage는 presentation 계층이 사용하는 응답 메시지 상수 모음이다.
 * 문자열 정책을 한곳에 모아 controller와 exception handler의 중복을 줄인다.
 */
public final class ApiResponseMessage {

    private ApiResponseMessage() {
    }

    public static final String SUCCESS = "Request completed successfully.";
    public static final String CREATED = "Resource created successfully.";
    public static final String VALIDATION_ERROR = "Validation failed.";
    public static final String DOMAIN_RULE_VIOLATION = "Domain rule violated.";

    public static final String COURSE_CREATED = "Course created.";
    public static final String COURSE_SECTION_CREATED = "Section added.";
    public static final String COURSE_MODULE_CREATED = "Module added.";
    public static final String COURSE_PUBLISHED = "Course published.";

    public static final String ENROLLMENT_CREATED = "Enrollment created.";
    public static final String MODULE_COMPLETED = "Module completed.";
}
