package com.wanted.momocity.lecture.application.port;

// TeacherAccountPort는 lecture가 인증된 강사 정보를 가져오기 위한 application port
public interface TeacherAccountPort {

    // Id로 강사 조회
    Long getTeacherId(Long userId);
}
