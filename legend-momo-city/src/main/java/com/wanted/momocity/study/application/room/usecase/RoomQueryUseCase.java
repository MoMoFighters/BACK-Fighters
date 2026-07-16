package com.wanted.momocity.study.application.room.usecase;

import com.wanted.momocity.study.presentation.api.response.room.GroupRoomDetailResponse;
import com.wanted.momocity.study.presentation.api.response.room.GroupRoomListResponse;

/*
 * comment.
 *  그룹방 자체(room 실체) 읽기 작업 전용 UseCase 인터페이스
 *  - 방 상세 조회, 내가 속한 방 목록 조회
 * */

public interface RoomQueryUseCase {

    // 방 상세 조회 (멤버 목록 포함 - 방 입장/새로고침 시 초기 화면 렌더링용)
    GroupRoomDetailResponse getRoomDetail(Long userId, Long roomId);

    // 내가 속한(JOINED) 그룹방 목록 조회
    GroupRoomListResponse getMyRooms(Long userId);

}