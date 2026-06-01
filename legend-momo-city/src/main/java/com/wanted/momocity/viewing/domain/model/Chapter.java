package com.wanted.momocity.viewing.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;

import java.io.Serializable;

/*
* comment.
*  Chapter 은 catalog 컨텍스트 소유 -> READ 전용
*  생성 / 수정 없이 조회만 하기 때문에 create() 는 생성하지 않음
*  isPlayable() : S3 URL 발급 전에 반드시 체크해야 하는 비지니스 규칙
*  -
*  Redis 직렬화를 위해 Serializable 구현 필요
*  -> Redis 에 객체 저장 시 직렬화/역직렬화 필요
* */

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
public class Chapter implements Serializable {

    private static final long serialVersionUID = 1L;

    // 기본 생성자 추가 (Jackson 역직렬화 필요)
    protected Chapter() {}

    private Long id;
    private Long lectureId;
    private String title;
    private int orderNo;
    private String videoUrl;
    private int durationSec;
    private VideoStatus videoStatus;
    // createdAt, updateAt 은 JpaEntity 에서 관리

    public enum VideoStatus{
        UPLOADING, ENCODING, READY, FAILED
    }

    // 재생 가능 여부 확인
    public boolean isPlayable() {
        return this.videoStatus == VideoStatus.READY;
    }

    // DB 에서 조회한 데이터로 도메인 객체 복원용
    // create() 는 신규 생성, reconstitute() 는 DB 복원
    public static Chapter reconstitute(
            Long id, Long lectureId, String title,
            int orderNo, String videoUrl, int durationSec,
            VideoStatus videoStatus
    ) {
        Chapter chapter = new Chapter();
        chapter.id = id;
        chapter.lectureId = lectureId;
        chapter.title = title;
        chapter.orderNo = orderNo;
        chapter.videoUrl = videoUrl;
        chapter.durationSec = durationSec;
        chapter.videoStatus = videoStatus;
        return chapter;

    }

}
