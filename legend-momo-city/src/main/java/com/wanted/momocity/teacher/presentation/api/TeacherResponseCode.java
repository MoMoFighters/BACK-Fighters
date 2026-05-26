package com.wanted.momocity.teacher.presentation.api;

/*
 * 강사 영역 전용 응답 코드 모음.
 * 공통 코드는 global/presentation/api/common/ApiResponseCode 가 담당.
 *
 * 명명 규약: TEACHER-XXX
 */
public final class TeacherResponseCode {

    private TeacherResponseCode() {
    }

    public static final String APPLICATION_LIST_FETCHED = "TEACHER-001";
    public static final String APPLICATION_DETAIL_FETCHED = "TEACHER-002";
    public static final String APPROVED = "TEACHER-003";
    public static final String REJECTED = "TEACHER-004";
}
