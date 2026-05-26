package com.wanted.momocity.teacher.presentation.api;

/*
 * 강사 영역 전용 응답 메시지 모음.
 * 공통 메시지는 global/presentation/api/common/ApiResponseMessage 가 담당.
 */
public final class TeacherResponseMessage {

    private TeacherResponseMessage() {
    }

    public static final String APPLICATION_LIST_FETCHED = "강사 신청자 목록 조회 완료";
    public static final String APPLICATION_DETAIL_FETCHED = "강사 신청자 상세 조회 완료";
    public static final String APPROVED = "강사 승인 완료";
    public static final String REJECTED = "강사 반려 완료";
}
