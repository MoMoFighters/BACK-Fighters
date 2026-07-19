package com.wanted.momocity.study.application.member.usecase;

import com.wanted.momocity.study.presentation.api.response.member.InvitationListResponse;
import com.wanted.momocity.study.presentation.api.response.member.SentInvitationListResponse;

/*
 * comment.
 *  그룹방 멤버 읽기 작업 전용 UseCase 인터페이스
 *  - 내가 받은 초대 목록 조회, 내가 보낸 초대 목록 조회
 * */

public interface MemberQueryUseCase {

    // 내가 받은 초대 목록 조회 (status=INVITED, roomId 무관)
    InvitationListResponse getMyInvitations(Long userId);

    // 내가 보낸 초대 목록 조회 (본인이 방장인 방들 중, status=INVITED인 건들)
    SentInvitationListResponse getSentInvitations(Long userId);

}