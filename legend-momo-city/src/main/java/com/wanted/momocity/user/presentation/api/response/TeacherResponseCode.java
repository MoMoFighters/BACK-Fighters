package com.wanted.momocity.user.presentation.api.response;

/* comment
    TeacherResponseCode 정리
    1. 해당 클래스가 하는 일 : 강사 영역 전용 응답 코드 상수 모음
    2. 위치 : teacher/presentation/api
    3. global/ApiResponseCode 와의 관계 :
        - 공통 코드(COMMON-SUCCESS 등) = global/ApiResponseCode
        - 영역별 코드(TEACHER-001 등) = 각 영역의 ResponseCode 클래스
    4. 명명 규약 : "TEACHER-{3자리 숫자}"
        - 다른 영역 예시 : MEMBER-001, LECTURE-001, REPORT-001 (m03 추가 예정)
 */

public final class TeacherResponseCode {

    private TeacherResponseCode() {
    }

    public static final String SUCCESS = "SUCCESS";
    public static final String APPLICATION_LIST_FETCHED = "승인 대기 중인 강사 목록 조회";
    public static final String APPLICATION_DETAIL_FETCHED = "해당 강사의 상세 정보 조회";
    public static final String APPROVED = "강사 승인 처리";
    public static final String REJECTED = "강사 반려 처리";
}
