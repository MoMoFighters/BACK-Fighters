package com.wanted.momocity.lecture.application.port;

/*
 * TeacherAccountPort는 lecture가 인증된 강사 정보를 가져오기 위한 application port
 * 가입한 email을 조회해서 사용자를 가져옴
 */
public interface TeacherAccountPort {

    /*
     * email로 강사 조회
     */
    Long getTeacherId(String email);
}
