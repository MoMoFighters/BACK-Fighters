package com.wanted.momocity.viewing.application.port;

/*
* comment.
*  catalog 컨텍스트 소유의 Lecture 를 READ 전용으로 조회
*  viewing 컨텍스트가 catalog 를 직접 참조하지 않고 해당 포트를 통해서만 접근
* */

import com.wanted.momocity.viewing.domain.model.Lecture;

public interface LecturePort {

    // 강의 단건 조회
    // 강의 메타데이터, 내 수강 목록에서 사용
    Lecture findById(Long lectureId);

}
