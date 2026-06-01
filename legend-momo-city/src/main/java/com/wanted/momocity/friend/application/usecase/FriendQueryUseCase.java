package com.wanted.momocity.friend.application.usecase;

import java.util.List;

//포트가 만들어준 문
public interface FriendQueryUseCase {

    //로그인한 유저 ID 받아서 친구 목록 List 반환
    List<FriendView> handle(Long userId);

    //응답용 데이터 객체(레코드)
    record FriendView(
        Long userId,
        String name, //강사 이름
        String nickname,
        String role,
        String status, //친구 여부
        Boolean isNotActive, //user테이블의 활성이 아닌 것
        List<String> lectureTitle, //백엔드가 가공해서 보낼 강의명(순수 리스트 상태),
        String profileImageUrl
    ) {
    }

}
