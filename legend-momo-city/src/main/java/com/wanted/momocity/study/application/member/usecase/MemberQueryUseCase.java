package com.wanted.momocity.study.application.member.usecase;

import com.wanted.momocity.study.presentation.api.response.member.InvitationListResponse;

/*
 * comment.
 *  그룹방 멤버 읽기 작업 전용 UseCase 인터페이스
 *  - 내가 받은 초대 목록 조회 (room을 거치지 않는 독립 경로)
 *  -
 *  member 도메인 조회는 이 하나뿐이라 메서드가 하나지만,
 *  추후 "내 그룹 참여 이력 조회" 등이 추가되면 여기 늘어남
 * */

public interface MemberQueryUseCase {

    // 내가 받은 초대 목록 조회 (status=INVITED, roomId 무관)
    InvitationListResponse getMyInvitations(Long userId);

}