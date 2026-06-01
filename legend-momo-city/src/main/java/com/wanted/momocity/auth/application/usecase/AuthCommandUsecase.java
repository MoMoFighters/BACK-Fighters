package com.wanted.momocity.auth.application.usecase;

import com.wanted.momocity.auth.application.command.*;
import com.wanted.momocity.auth.presentation.api.response.EmailSendResponse;
import com.wanted.momocity.auth.presentation.api.response.LoginResponse;

public interface AuthCommandUsecase {

    // 강사 회원가입
    void signup(TeacherSignupCommand command);

    // 학생 회원가입
    void signup(StudentSignupCommand command);

    // 로그인
    LoginResponse login(LoginCommand command);

    // 로그아웃
    void logout(LogoutCommand command);

    // 소셜로그인
    LoginResponse socialLogin(SocialLoginCommand command);

    // 이메일 전송
    EmailSendResponse emailSend(EmailSendCommand command);

    // 임시비번 전송
    void sendTempPassword(EmailSendCommand command);


}
