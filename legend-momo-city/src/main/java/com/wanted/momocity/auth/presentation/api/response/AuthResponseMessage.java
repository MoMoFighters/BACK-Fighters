package com.wanted.momocity.auth.presentation.api.response;

public final class AuthResponseMessage {

   private AuthResponseMessage(){}

    public static final String STUDENT_CREATED = "회원가입이 완료되었습니다.";
    public static final String TEACHER_CREATED = "회원가입이 완료되었습니다. 강사로 승인된 후 로그인 하실 수 있습니다.";

    public static final String LOGIN_SUCCESS = "로그인 성공하였습니다. 모모시티에 오신 걸 환영합니다.";

    public static final String EMAIL_VALIDATION_ERROR = "이메일 형식을 다시 확인해주십시오.";
    public static final String PASSWORD_VALIDATION_ERROR = "비밀번호는 특수기호 포함 8자리 이상이어야 합니다.";
    public static final String EMAIL_DUPLICATE = "이미 가입된 이메일입니다.";

}
