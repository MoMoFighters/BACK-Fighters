package com.wanted.momocity.viewing.domain.model;

import lombok.Getter;

@Getter
public class Lecture {

    private Long id;
    private Long teacherId;
    private String title;
    private String thumbnailUrl;
    private String category;
    private String instructorName;


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
