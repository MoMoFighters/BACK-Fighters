package com.wanted.momocity.message.domain.repository;

import com.wanted.momocity.message.infrastructure.persistence.MessageJpaEntity;

import java.time.LocalDateTime;

//Response(표현 계층-Presentation): api의 최종 출력물(json)
//project(인프라 계층->도메인 계층 데이터 배달 주머니): DB 조인 결과물을 담는 기술적인 그릇
//->∵ 메시지 기능은 하나지만 chat_room, chat_room_member, message 테이블로 나뉘므로 같은 기능의 정보를 담기위한 그릇
public record ChatRoomQueryProjection(
        Long roomId,
        MessageJpaEntity lastMessage //마지막 메시지
) {
}
