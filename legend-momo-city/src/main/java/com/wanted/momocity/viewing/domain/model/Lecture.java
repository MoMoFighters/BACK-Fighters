package com.wanted.momocity.viewing.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;

import java.io.Serializable;

/*
* Unrecognized field "playable"
→ Chapter 에 isPlayable() 메서드가 있는데
→ Jackson 이 getter 로 인식해서
  "playable" 필드로 직렬화
→ 역직렬화 시 "playable" 필드 없어서 오류

isPlayable() → @Getter 때문에
Jackson 이 playable 이라는 프로퍼티로 인식
* */

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class Lecture implements Serializable {

    private static final long serialVersionUID = 1L;

    // 기본 생성자 추가
    protected Lecture() {}

    private Long id;
    private Long teacherId;
    private String title;
    private String thumbnailUrl;
    private String category;
    private String instructorName;
    private String status;

    public enum VideoStatus{
        WAITING, ACTIVE, HOLD, DELETED
    }

    // 수강 가능 여부 확인
    public boolean isViewable() {
        return "ACTIVE".equals(this.status);
    }

    // DB 에서 조회한 데이터로 도메인 객체 복원용
    // create() 는 신규 생성, reconstitute() 는 DB 복원
    public static Lecture reconstitute(
            Long id, Long teacherId, String title, String thumbnailUrl,
            String category, String instructorName, String status
    ) {
        Lecture lecture = new Lecture();
        lecture.id = id;
        lecture.teacherId = teacherId;
        lecture.title = title;
        lecture.thumbnailUrl = thumbnailUrl;
        lecture.category = category;
        lecture.instructorName = instructorName;
        lecture.status = status;
        return lecture;
    }

}
