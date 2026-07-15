package com.wanted.momocity.study.application.member.result;

import com.wanted.momocity.study.domain.model.GroupRoomMember;

import java.time.LocalDateTime;

/*
 * comment.
 *  초대 발송/취소/수락/거절 공통 결과 DTO
 *  -> Service가 반환 -> Controller가 Response로 조립
 *  -
 *  invitationId는 group_room_member row의 PK를 가리킴
 *  (INVITED든 JOINED든 REJECTED든 CANCELED든 같은 row, 같은 id를 계속 사용)
 * */

public record InvitationResult(
        Long invitationId,
        Long roomId,
        Long inviteeId,          // 초대 발송 시에만 값 존재, 그 외에는 null
        GroupRoomMember.MemberStatus status,
        LocalDateTime joinedAt   // 수락 시에만 값 존재, 그 외에는 null
) {

    // 초대 발송 결과
    public static InvitationResult ofInvited(GroupRoomMember member) {
        return new InvitationResult(
                member.getId(), member.getGroupRoomId(), member.getUserId(),
                member.getStatus(), null
        );
    }

    // 취소 결과 (invitationId, status만 - roomId도 프론트가 이미 아는 값이라 생략)
    public static InvitationResult ofCanceled(GroupRoomMember member) {
        return new InvitationResult(
                member.getId(), null, null, member.getStatus(), null
        );
    }

    // 거절 결과 (invitationId, roomId, status)
    public static InvitationResult ofRejected(GroupRoomMember member) {
        return new InvitationResult(
                member.getId(), member.getGroupRoomId(), null, member.getStatus(), null
        );
    }

    // 수락 결과
    public static InvitationResult ofAccepted(GroupRoomMember member) {
        return new InvitationResult(
                member.getId(), member.getGroupRoomId(), null,
                member.getStatus(), member.getJoinedAt()
        );
    }
}