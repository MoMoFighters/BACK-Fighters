package com.wanted.momocity.friend.application.usecase;

import com.wanted.momocity.friend.application.command.*;

public interface FriendCommandUseCase {

    //친구 요청
    //입력 모델로 Command를 받고 출력 모델로 내부 주머니(View)를 반환.
    RequestFriendView requestFriendCommandHandle(RequestFriendCommand command);

    //컨트롤러에 최종 전달할 결과
    record RequestFriendView(
            Long userId,
            String nickname,
            String status,
            String role
    ) {
    }

    //친구 요청 철회
    CancelRequestFriendView cancelRequestFriendCommandHandle(CancelRequestFriendCommand command);

    //컨트롤러에 데이터를 넘겨줄 내부 주머니
    record CancelRequestFriendView(
            Long userId,
            String nickname,
            String role,
            String status
    ) {

    }

    //친구 요청 수락
    AcceptView acceptRequestFriendCommandHandle(AcceptRequestFriendCommand command);

    record AcceptView(
            Long userId,
            String nickname,
            String role,
            String status
    ) {}

    //친구 요청 거절
    RejectView rejectRequestFriendCommandHandle(RejectRequestFriendCommand command);

    record RejectView(
            Long userId, //친구 테이블의 fromUserId(요청자)
            String nickname, //요청자 닉네임
            String role,
            String status //none으로 반환
    ) {}

    //친구 차단
    BlockView blockFriendCommandHandle(BlockFriendCommand command);

    record BlockView(
            Long userId,
            String nickname,
            String role,
            String status
    ) {}

    //친구 차단 해제
    UnblockView unblockFriendCommandHandle(UnblockFriendCommand command);

    record UnblockView(
            Long userId,
            String nickname,
            String role,
            String status
    ) {}

    //친구 삭제
    DeleteView deleteFriendCommandHandle(DeleteFriendCommand command);

    record DeleteView(
            Long userId,
            String nickname,
            String role,
            String status
    ) {}
}
