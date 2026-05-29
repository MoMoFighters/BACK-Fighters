package com.wanted.momocity.viewing.domain.model;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class Lecture implements Serializable {

    private Long id;
    private Long teacherId;
    private String title;
    private String thumbnailUrl;
    private String category;
    private String instructorName;

    // DB 에서 조회한 데이터로 도메인 객체 복원용
    // create() 는 신규 생성, reconstitute() 는 DB 복원
    public static Lecture reconstitute(
            Long id, Long teacherId, String title,
            String thumbnailUrl, String category, String instructorName
    ) {
        Lecture lecture = new Lecture();
        lecture.id = id;
        lecture.teacherId = teacherId;
        lecture.title = title;
        lecture.thumbnailUrl = thumbnailUrl;
        lecture.category = category;
        lecture.instructorName = instructorName;
        return lecture;
    }

}
