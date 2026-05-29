package com.wanted.momocity.user.presentation.api.response;

/* comment.
    1. 해당 클래스가 하는 일 : 강사 영역 전용 응답 메시지(사람이 읽는 텍스트) 상수 모음
    2. 위치 : teacher/presentation/api
    3. TeacherResponseCode 와의 관계 :
        - 같은 변수명 (APPLICATION_LIST_FETCHED 등)
        - 한 쌍으로 사용 : Code = 기계용 식별자, Message = 사람용 텍스트
        - Controller 에서 둘 다 같이 ApiResponse.success() 에 전달
    4. global/ApiResponseMessage 와의 관계 :
        - 공통 메시지(SUCCESS 등) = global/ApiResponseMessage
        - 영역별 메시지 = 각 영역의 ResponseMessage 클래스
        - global 미수정 원칙
    5. 4개 상수 의미 (Code 와 1:1 매칭) :
        - APPLICATION_LIST_FETCHED : "강사 신청자 목록 조회 완료"
        - APPLICATION_DETAIL_FETCHED : "강사 신청자 상세 조회 완료"
        - APPROVED : "강사 승인 완료"
        - REJECTED : "강사 반려 완료"
 */

public final class TeacherResponseMessage {

    private TeacherResponseMessage() {
    }

    public static final String APPLICATION_LIST_FETCHED = "강사 신청자 목록 조회 완료";
    public static final String APPLICATION_DETAIL_FETCHED = "강사 신청자 상세 조회 완료";
    public static final String APPROVED = "강사 승인 완료";
    public static final String REJECTED = "강사 반려 완료";
}
