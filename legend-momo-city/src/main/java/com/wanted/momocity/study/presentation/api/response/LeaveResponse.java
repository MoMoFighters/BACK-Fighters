package com.wanted.momocity.study.presentation.api.response;

/*
 * comment.
 *  그룹방 나가기 응답 DTO
 *  - 사용 API : POST /api/v3/study/rooms/{roomId}/members/leave
 *  -
 *  hostChanged/newHostId/roomEnded 세 필드 조합으로 세 가지 케이스를 구분
 *  (application.member.result.LeaveResult 참고) :
 *    1) 일반 멤버 퇴장   : hostChanged=false, newHostId=null,  roomEnded=false
 *    2) 방장 퇴장(위임)  : hostChanged=true,  newHostId=8,     roomEnded=false
 *    3) 마지막 인원 퇴장 : hostChanged=false, newHostId=null,  roomEnded=true
 *  -
 *  해당 값들을 화면에 노출하지않지만, 필요 시(예: 로그) 활용 가능하도록 필드는 유지
 * */

public record LeaveResponse(
        Long roomId,
        String status,
        boolean hostChanged,
        Long newHostId,
        boolean roomEnded
) {
}