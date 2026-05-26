package com.wanted.momocity.viewing.domain.model;

import lombok.Getter;

/*
* comment.
*  Chapter 은 catalog 컨텍스트 소유 -> READ 전용
*  생성 / 수정 없이 조회만 하기 때문에 create() 는 생성하지 않음
*  isPlayable() : S3 URL 발급 전에 반드시 체크해야 하는 비지니스 규칙
* */

@Getter
public class Chapter {

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

}
