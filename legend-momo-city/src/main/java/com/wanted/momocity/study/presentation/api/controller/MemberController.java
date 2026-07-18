package com.wanted.momocity.study.presentation.api.controller;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.study.application.member.command.InviteMemberCommand;
import com.wanted.momocity.study.application.member.result.InvitationResult;
import com.wanted.momocity.study.application.member.result.KickResult;
import com.wanted.momocity.study.application.member.result.LeaveResult;
import com.wanted.momocity.study.application.member.usecase.MemberCommandUseCase;
import com.wanted.momocity.study.application.member.usecase.MemberQueryUseCase;
import com.wanted.momocity.study.presentation.api.common.StudyResponseCode;
import com.wanted.momocity.study.presentation.api.request.InviteMemberRequest;
import com.wanted.momocity.study.presentation.api.response.member.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/*
 * comment.
 *  그룹방 멤버(초대-참가-퇴장-강퇴) HTTP 요청 처리
 *  - 타이머 관련 API(start/pause/end)는 TimerController로 이관
 *  - 비즈니스 로직 없음, UseCase 호출 + Result -> Response 변환만 담당
 *  -
 *  Result -> Response 매핑을 이 Controller가 담당:
 *  Result는 application 계층 산출물이라 4개 API(발송/취소/수락/거절)가 InvitationResult 하나를 공용 사용
 *  실제 프론트에 내려가는 Response는 API별로 필요한 필드만 담은 별도 DTO로 분리
 *  (불필요한 null 필드 노출 방지). 그 변환 지점이 Controller
 * */

@Tag(name = "Member", description = "Study(열품타) 도메인 - 그룹방 멤버 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v3/study")
public class MemberController {

    private final MemberCommandUseCase memberCommandUseCase;
    private final MemberQueryUseCase memberQueryUseCase;

    // 친구 초대 발송
    @Operation(summary = "그룹방 초대 발송", description = "친구를 그룹방에 초대합니다.")
    @PostMapping("/rooms/{roomId}/members/invitations")
    public ResponseEntity<ApiResponse<InvitationSentResponse>> invite(
            @PathVariable Long roomId,
            @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        InvitationResult result = memberCommandUseCase.invite(
                userDetails.getUserId(), roomId, new InviteMemberCommand(request.inviteeId())
        );

        return ResponseEntity.status(201).body(ApiResponse.created(
                StudyResponseCode.INVITATION_SENT,
                "그룹방 초대를 보냈습니다.",
                new InvitationSentResponse(
                        result.invitationId(), result.roomId(), result.inviteeId(), result.status().name()
                )
        ));
    }

    // 초대 취소 (초대한 사람이)
    @Operation(summary = "그룹방 초대 취소", description = "발송한 초대를 취소합니다.")
    @DeleteMapping("/rooms/{roomId}/members/invitations/{invitationId}")
    public ResponseEntity<ApiResponse<InvitationCanceledResponse>> cancelInvitation(
            @PathVariable Long roomId,
            @PathVariable Long invitationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        InvitationResult result = memberCommandUseCase.cancelInvitation(
                userDetails.getUserId(), roomId, invitationId
        );

        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.INVITATION_CANCELED,
                "초대를 취소했습니다.",
                new InvitationCanceledResponse(result.invitationId(), result.status().name())
        ));
    }

    // 초대 수락 (본인 토큰 기준)
    @Operation(summary = "그룹방 초대 수락", description = "받은 초대를 수락하고 그룹방에 참가합니다.")
    @PostMapping("/rooms/{roomId}/members/invitations/accept")
    public ResponseEntity<ApiResponse<InvitationAcceptedResponse>> acceptInvitation(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        InvitationResult result = memberCommandUseCase.acceptInvitation(userDetails.getUserId(), roomId);

        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.INVITATION_ACCEPTED,
                "그룹방에 참가했습니다.",
                new InvitationAcceptedResponse(
                        result.invitationId(), result.roomId(), result.status().name(), result.joinedAt()
                )
        ));
    }

    // 초대 거절 (본인 토큰 기준)
    @Operation(summary = "그룹방 초대 거절", description = "받은 초대를 거절합니다.")
    @PostMapping("/rooms/{roomId}/members/invitations/reject")
    public ResponseEntity<ApiResponse<InvitationRejectedResponse>> rejectInvitation(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        InvitationResult result = memberCommandUseCase.rejectInvitation(userDetails.getUserId(), roomId);

        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.INVITATION_REJECTED,
                "초대를 거절했습니다.",
                new InvitationRejectedResponse(result.invitationId(), result.roomId(), result.status().name())
        ));
    }

    // 내가 받은 초대 목록 조회 (roomId 미경유, 독립 경로)
    @Operation(summary = "받은 초대 목록 조회", description = "내가 받은 그룹방 초대 목록을 조회합니다.")
    @GetMapping("/members/invitations")
    public ResponseEntity<ApiResponse<InvitationListResponse>> getMyInvitations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.INVITATION_LIST_FETCHED,
                "받은 초대 목록을 조회했습니다.",
                memberQueryUseCase.getMyInvitations(userDetails.getUserId())
        ));
    }

    // 내가 보낸 초대 목록 조회
    @Operation(summary = "보낸 초대 목록 조회", description = "내가 방장으로서 보낸 그룹방 초대 목록을 조회합니다.")
    @GetMapping("/members/invitations/sent")
    public ResponseEntity<ApiResponse<SentInvitationListResponse>> getSentInvitations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.INVITATION_SENT_LIST_FETCHED,
                "보낸 초대 목록을 조회했습니다.",
                memberQueryUseCase.getSentInvitations(userDetails.getUserId())
        ));
    }

    // 방 나가기 (자진 퇴장)
    @Operation(summary = "그룹방 나가기", description = "그룹방에서 나갑니다.")
    @PostMapping("/rooms/{roomId}/members/leave")
    public ResponseEntity<ApiResponse<LeaveResponse>> leave(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        LeaveResult result = memberCommandUseCase.leave(userDetails.getUserId(), roomId);

        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.MEMBER_LEFT,
                "그룹방에서 나갔습니다.",
                new LeaveResponse(
                        result.roomId(), result.status(),
                        result.hostChanged(), result.newHostId(), result.roomEnded()
                )
        ));
    }

    // 강퇴 (방장만 가능)
    @Operation(summary = "그룹방 멤버 강퇴", description = "방장이 특정 멤버를 강퇴합니다.")
    @PostMapping("/rooms/{roomId}/members/{targetUserId}/kick")
    public ResponseEntity<ApiResponse<KickResponse>> kick(
            @PathVariable Long roomId,
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        KickResult result = memberCommandUseCase.kick(userDetails.getUserId(), roomId, targetUserId);

        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.MEMBER_KICKED,
                "멤버를 강퇴했습니다.",
                new KickResponse(result.roomId(), result.targetUserId(), result.status())
        ));
    }
}