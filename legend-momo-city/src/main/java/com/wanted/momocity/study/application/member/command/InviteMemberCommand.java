package com.wanted.momocity.study.application.member.command;

/*
 * comment.
 *  그룹방 초대 발송 시 사용하는 커맨드 DTO
 *  -> application 계층에서 사용
 *  -> Controller 에서 Request -> Command 변환 후 UseCase 에 전달
 *  -
 *  member 도메인 액션 중 Request Body가 필요한 건 초대 발송뿐
 *  (취소/수락/거절/타이머/퇴장/강퇴는 PathVariable + 인증 정보만으로 충분해 Command 불필요)
 * */

public record InviteMemberCommand(
        Long inviteeId
) {
}