package com.wanted.momocity.user.presentation.api.response;

public final class UserResponseMessage {

    private UserResponseMessage(){}

    public static final String VIEW_SUCCESS = "회원정보가 조회되었습니다.";
    public static final String USER_SOFT_DELETED = "회원탈퇴 되었습니다. 계정을 복구하시려면 3개월 이내에 yourmomocity@gmail.com 로 연락주시길 바랍니다. ";

    public static final String NICKNAME_REGISTERED = "님 모모시티에 오신 걸 환영합니다. ";
    public static final String USER_INFO_UPDATE_SUCCESS = "정보가 수정되었습니다. ";
    public static final String NICKNAME_AVAILABLE = "사용 가능한 닉네임입니다.";


    public static final String USER_REPORT_PLUS = "사용자 제재 완료";
    public static final String USER_REPORT_MINUS = "사용자 제재 취소";

}
