package com.wanted.momocity.study.application.member.result;

/*
 * comment.
 *  방 나가기 결과 DTO
 *  -
 *  status는 항상 "LEFT" 고정값 (LeaveResponse.status로 매핑)
 *  roomEnded=true 인 경우 hostChanged는 항상 false, newHostId는 항상 null로 고정
 *  (마지막 사람이 나가서 방이 끝난 경우, 방장 위임 자체가 의미 없으므로)
 *  세 가지 케이스가 필드 값만으로 자연스럽게 구분되도록 설계
 *    1) 일반 멤버 퇴장   : hostChanged=false, newHostId=null,  roomEnded=false
 *    2) 방장 퇴장(위임)  : hostChanged=true,  newHostId=8,     roomEnded=false
 *    3) 마지막 인원 퇴장 : hostChanged=false, newHostId=null,  roomEnded=true
 * */

public record LeaveResult(
        Long roomId,
        String status,
        boolean hostChanged,
        Long newHostId,
        boolean roomEnded
) {

    public static LeaveResult of(Long roomId, boolean hostChanged, Long newHostId, boolean roomEnded) {
        if (roomEnded) {
            return new LeaveResult(roomId, "LEFT", false, null, true);
        }
        return new LeaveResult(roomId, "LEFT", hostChanged, newHostId, false);
    }
}