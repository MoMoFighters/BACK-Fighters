package com.wanted.momocity.auth.application.usecase;

import com.wanted.momocity.auth.application.command.StudentSignupCommand;

public interface StudentSignupCommandUsecase {

    // 학생 회원가입 기능 선언
    // 돌려줄 값은 성공/실패에 대한 메시지 뿐 , 특정한 값을 넘겨주지 않기에 void 사용
    void signup(StudentSignupCommand command);


}
