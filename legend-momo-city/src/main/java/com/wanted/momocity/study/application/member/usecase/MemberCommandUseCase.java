package com.wanted.momocity.study.application.member.usecase;

import com.wanted.momocity.study.application.member.command.InviteMemberCommand;
import com.wanted.momocity.study.application.member.result.InvitationResult;
import com.wanted.momocity.study.application.member.result.KickResult;
import com.wanted.momocity.study.application.member.result.LeaveResult;
import com.wanted.momocity.study.application.member.result.TimerActionResult;

/*
 * comment.
 *  그룹방 멤버(초대-참가-퇴장-타이머) 쓰기 작업 전용 UseCase 인터페이스
 *  - 초대 발송/취소/수락/거절, 타이머 시작/일시정지/종료, 방 나가기, 강퇴
 *  -
 *  타이머 시작/일시정지/종료는 TimerCommandUseCase(application.member.timer)로 분리
 * */

public interface MemberCommandUseCase {

    // 친구 초대 발송
    InvitationResult invite(Long userId, Long roomId, InviteMemberCommand command);

    // 초대 취소 (초대한 사람이)
    InvitationResult cancelInvitation(Long userId, Long roomId, Long invitationId);

    // 초대 수락 (초대받은 사람이, 본인 토큰 기준)
    InvitationResult acceptInvitation(Long userId, Long roomId);

    // 초대 거절 (초대받은 사람이, 본인 토큰 기준)
    InvitationResult rejectInvitation(Long userId, Long roomId);

    // 방 나가기 (자진 퇴장, 진행 중인 타이머가 있으면 함께 종료 처리)
    LeaveResult leave(Long userId, Long roomId);

    // 강퇴 (방장만 가능, 대상은 재초대 불가 상태(KICKED)가 됨)
    KickResult kick(Long hostUserId, Long roomId, Long targetUserId);

}