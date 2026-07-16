package com.wanted.momocity.study.application.room.usecase;

import com.wanted.momocity.study.application.room.result.RoomCreateResult;

/*
 * comment.
 *  그룹방 자체(room 실체) 쓰기 작업 전용 UseCase 인터페이스
 *  - 방 생성만 담당
 *  -
 *  방장 위임(changeHost), 방 종료(end)는 room 자체의 액션이 아닌 "멤버 퇴장"이라는 사건에 의해 부수적으로 발생하는 결과
 *  -> member 도메인의 MemberCommandService.leave()/kick() 안에서 GroupRoomRepository를 직접 호출해 처리
 *  (room 도메인에 별도 API로 노출하지 않음 - 팀 논의로 확정된 room/member 책임 분리 원칙)
 * */

public interface RoomCommandUseCase {

    // 그룹방 생성 (생성자는 host로 자동 지정되며, 동시에 GroupRoomMember도 JOINED로 함께 생성됨)
    RoomCreateResult createRoom(Long userId);

}