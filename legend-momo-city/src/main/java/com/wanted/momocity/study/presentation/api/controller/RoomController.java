package com.wanted.momocity.study.presentation.api.controller;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.study.application.room.result.RoomCreateResult;
import com.wanted.momocity.study.application.room.usecase.RoomCommandUseCase;
import com.wanted.momocity.study.application.room.usecase.RoomQueryUseCase;
import com.wanted.momocity.study.presentation.api.common.StudyResponseCode;
import com.wanted.momocity.study.presentation.api.response.room.GroupRoomDetailResponse;
import com.wanted.momocity.study.presentation.api.response.room.GroupRoomListResponse;
import com.wanted.momocity.study.presentation.api.response.room.GroupRoomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/*
 * comment.
 *  그룹방 자체(room 실체) HTTP 요청 처리
 *  - 비즈니스 로직 없음, UseCase 호출 + Result -> Response 변환만 담당
 *  - 방 안에서 일어나는 초대/타이머/퇴장/강퇴는 MemberController/TimerController가 담당한다.
 * */

@Tag(name = "Room", description = "Study(열품타) 도메인 - 그룹방 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v3/study/rooms")
public class RoomController {

    private final RoomCommandUseCase roomCommandUseCase;
    private final RoomQueryUseCase roomQueryUseCase;

    // 그룹방 생성
    @Operation(summary = "그룹방 생성", description = "새 그룹방을 생성합니다. 생성자가 자동으로 방장이 됩니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<GroupRoomResponse>> createRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RoomCreateResult result = roomCommandUseCase.createRoom(userDetails.getUserId());

        return ResponseEntity.status(201).body(ApiResponse.created(
                StudyResponseCode.ROOM_CREATED,
                "그룹방을 생성했습니다.",
                new GroupRoomResponse(
                        result.roomId(), result.hostUserId(), result.hostNickname(),
                        result.status(), result.maxMember()
                )
        ));
    }

    // 그룹방 상세 조회
    @Operation(summary = "그룹방 상세 조회", description = "방 정보와 현재 참가자 목록을 조회합니다.")
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<GroupRoomDetailResponse>> getRoomDetail(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.ROOM_FETCHED,
                "그룹방 상세 정보를 조회했습니다.",
                roomQueryUseCase.getRoomDetail(userDetails.getUserId(), roomId)
        ));
    }

    // 내가 속한 그룹방 목록 조회
    @Operation(summary = "내 그룹방 목록 조회", description = "내가 현재 참가 중인 그룹방 목록을 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<GroupRoomListResponse>> getMyRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.ROOM_LIST_FETCHED,
                "내 그룹방 목록을 조회했습니다.",
                roomQueryUseCase.getMyRooms(userDetails.getUserId())
        ));
    }
}