package com.wanted.momocity.friend.application.usecase;

import com.wanted.momocity.friend.application.query.*;

import java.util.List;

//포트가 만들어준 문
public interface FriendQueryUseCase {

    //친구 목록 조회
    //로그인한 유저 ID 받아서 친구 목록 List 반환
    List<FriendView> getFriendQueryHandle(GetFriendQuery query);

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

    //사용자 검색
    List<FindView> findUserQueryHandle(FindUserQuery query);

    record FindView(
            Long userId,
            String name,
            String nickname,
            String status,
            String role,
            Boolean isNotActive, //user 테이블의 활성 상태가 아닌 것
            List<String> lectureTitle,
            String profileImageUrl
    ) {}

    //보낸 친구 요청 목록
    List<SentRequestView> getSentRequestFriendQueryHandle(SentRequestQuery query);

    record SentRequestView(
            Long userId,
            String nickname,
            String role,
            String status, //친구 테이블 상태(SENT)
            Boolean isNotActive, //user 테이블의 활성 상태가 아닌 것,
            String profileImageUrl
    ) {}

    //받은 친구 요청 목록
    List<ReceivedRequestView> getReceivedRequestFriendQueryHandle(ReceivedRequestQuery query);

    record ReceivedRequestView(
            Long userId, //요청자(fromUserId)
            String nickname, //요청자 닉네임
            String role,
            String status, //무조건 SENT
            Boolean isNotActive, //활성 상태 아닌 것
            String profileImageUrl
    ) {}

    //친구 차단 목록 조회
    List<BlockedView> getBlockedFriendQueryHandle(BlockedFriendQuery query);

    record BlockedView(
            Long userId,
            String nickname,
            String role,
            String status,
            Boolean isNotActive,
            String profileImageUrl
    ) {}

    //강사, 비활성 유저 제외 친구 목록
    List<StudentFriendsView> getStudentFriendsQueryHandle(GetStudentFriendsQuery query);

    record StudentFriendsView(
            Long userId,
            String nickname,
            String role,
            String status,
            String profileImageUrl
    ) {}
}
