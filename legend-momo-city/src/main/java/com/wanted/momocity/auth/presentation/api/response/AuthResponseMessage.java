package com.wanted.momocity.auth.presentation.api.response;

public final class AuthResponseMessage {

   private AuthResponseMessage(){}

    public static final String STUDENT_CREATED = "회원가입이 완료되었습니다.";
    public static final String TEACHER_CREATED = "회원가입이 완료되었습니다. 강사로 승인된 후 로그인 하실 수 있습니다.";

    public static final String LOGIN_SUCCESS = "로그인 성공하였습니다.";
    public static final String LOGIN_COMPLETED = "사용자 정보가 성공적으로 조회되었습니다.";

    public static final String TEMP_PASSWORD_CREATED = "임시 비밀번호가 이메일로 전송되었습니다.\n 발급된 임시 비밀번호로 로그인 후 마이페이지에서 비밀번호를 변경해주세요.";

    public static final String EMAIL_SEND_SUCCESS = "인증 코드가 이메일로 전송되었습니다. 3분 안에 입력해주십시오.";
    public static final String EMAIL_VERIFY_SUCCESS = "인증이 완료되었습니다.";

    public static final String LOGOUT_SUCCESS = "로그아웃 되었습니다.";

    public static final String NEW_TOKEN_CREATED = "토큰이 재발급되었습니다.";

    public static final String EMAIL_VALIDATION_ERROR = "이메일 형식을 다시 확인해주십시오.";
    public static final String PASSWORD_VALIDATION_ERROR = "비밀번호는 특수기호 포함 8자리 이상이어야 합니다.";

}
