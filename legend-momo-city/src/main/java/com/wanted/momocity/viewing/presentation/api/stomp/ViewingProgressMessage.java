package com.wanted.momocity.viewing.presentation.api.stomp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
* comment.
*  [역할]
*  - 프론트 -> 서버로 STOMP 메세지로 전송되는 진척도 데이터 DTO
*  -
*  [record 사용 이윺]
*  - HTTP Request DTO 와 동일하게 불변 객체로 설계
*  - Jackson 역직렬화를 위해 기본 생성자 필요 → @JsonCreator 로 처리
* */

public record ViewingProgressMessage (
        Long lectureId,
        Long chapterId,
        int playbackSeconds
){
    // Jackson 이 JSON -> Record 역직렬화 시 사용할 사용자 명시
    // -> record 는 기본 생성자가 없어서 명시하지 않으면 역직렬화 실패
    @JsonCreator
    public ViewingProgressMessage(
            // JSON 필드명과 파라미터명 매핑
            @JsonProperty("lectureId") Long lectureId,
            @JsonProperty("chapterId") Long chapterId,
            @JsonProperty("playbackSeconds") int playbackSeconds
    ) {
        this.lectureId = lectureId;
        this.chapterId = chapterId;
        this.playbackSeconds = playbackSeconds;
    }
}
