package com.wanted.momocity.friend.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.friend.application.command.*;
import com.wanted.momocity.friend.application.usecase.*;
import com.wanted.momocity.friend.application.usecase.CancelRequestFriendCommandUseCase.CancelRequestFriendView;
import com.wanted.momocity.friend.application.usecase.FindUserQueryUseCase.FindView;
import com.wanted.momocity.friend.application.usecase.FriendQueryUseCase.FriendView;
import com.wanted.momocity.friend.application.usecase.GetSentRequestFriendQueryUseCase.SentRequestView;
import com.wanted.momocity.friend.application.usecase.RequestFriendCommandUseCase.RequestFriendView;
import com.wanted.momocity.friend.application.usecase.AcceptRequestFriendCommandUseCase.AcceptView;
import com.wanted.momocity.friend.application.usecase.RejectRequestFriendCommandUseCase.RejectView;
import com.wanted.momocity.friend.application.usecase.GetReceivedRequestFriendQueryUseCase.ReceivedRequestView;
import com.wanted.momocity.friend.application.usecase.BlockFriendCommandUseCase.BlockView;
import com.wanted.momocity.friend.application.usecase.GetBlockedFriendQueryUseCase.BlockedView;
import com.wanted.momocity.friend.application.usecase.UnblockFriendCommandUseCase.UnblockView;
import com.wanted.momocity.friend.application.usecase.DeleteFriendCommandUseCase.DeleteView;
import com.wanted.momocity.friend.presentation.api.response.*;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {

    //내 친구 조회
    private final FriendQueryUseCase friendQueryUseCase;
    //사용자 검색
    private final FindUserQueryUseCase findUserQueryUseCase;
    //친구 요청
    private final RequestFriendCommandUseCase requestFriendCommandUseCase;
    //친구 요청 철회
    private final CancelRequestFriendCommandUseCase cancelRequestFriendCommandUseCase;
    //보낸 요청 목록
    private final GetSentRequestFriendQueryUseCase getSentRequestFriendQueryUseCase;
    //친구 요청 수락
    private final AcceptRequestFriendCommandUseCase acceptRequestFriendCommandUseCase;
    //친구 요청 거절
    private final RejectRequestFriendCommandUseCase rejectRequestFriendCommandUseCase;
    //받은 친구 요청 목록
    private final GetReceivedRequestFriendQueryUseCase getReceivedRequestFriendQueryUseCase;
    //친구 차단
    private final BlockFriendCommandUseCase blockFriendCommandUseCase;
    //친구 차단 목록 조회
    private final GetBlockedFriendQueryUseCase getBlockedFriendQueryUseCase;
    //친구 차단 해제
    private final UnblockFriendCommandUseCase unblockFriendCommandUseCase;
    //친구 삭제
    private final DeleteFriendCommandUseCase deleteFriendCommandUseCase;

    @GetMapping
    @Operation(
            summary = "친구 목록 조회",
            description = "로그인한 사용자의 내 친구 목록을 불러온다."
    )
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getFriends(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();

        //서비스를 통해 가공된 친구 목록 받아오기
        List<FriendView> friends = friendQueryUseCase.handle(userId);

        //빈 배열일 때
        if (friends.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(
                    "SUCCESS",
                    "아직 등록된 친구가 없어요. 친구를 추가해 보세요!",
                    List.of()
            ));
        }

        //친구가 있을 때
        List<FriendResponse> responseList = friends.stream()
                .map(FriendResponse::from)
                        .toList();
        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                "친구 목록 불러오기 성공",
                responseList
        ));
    }

    @GetMapping("/find")
    @Operation(summary = "사용자 검색", description = "닉네임(포함)을 통해 사용자를 검색한다. (차단 유저 제외)")
    public ResponseEntity<ApiResponse<List<FindUserResponse>>> findUser(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam("nickname") String findNickname) {

        Long userId = userDetails.getUserId();

        //순수한 검색 결과를 내부 주머니로 받아옴(FindView)
        List<FindView> findResults = findUserQueryUseCase.handle(userId, findNickname);

        //검색 결과가 없을 때
        if (findResults.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(
                    "SUCCESS",
                    "존재하지 않는 사용자입니다. 닉네임을 확인해주세요.",
                    List.of()
            ));
        }

        //검색 결과가 있을 때
        List<FindUserResponse> responseList = findResults.stream()
                .map(FindUserResponse::from)
                        .toList();

        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                "사용자 검색 완료",
                responseList
        ));
    }

    @PostMapping("/request/{userId}")
    @Operation(summary = "친구 요청", description = "상대방에게 친구 요청을 보내고 알림을 생성한다.")
    public ResponseEntity<ApiResponse<RequestFriendResponse>> sentFriend(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("userId") Long targetUserId) {

        Long userId = userDetails.getUserId();

        //입력 모델(Command) 생성
        RequestFriendCommand command = new RequestFriendCommand(userId, targetUserId);

        //서비스를 실행하고 결과 주머니(View) 받아오기
        RequestFriendView view = requestFriendCommandUseCase.handle(command);

        //주머니(View)를 최종 응답 레코드로 변환
        RequestFriendResponse responseData = RequestFriendResponse.from(view);

        //기획서 규격 메시지 조립
        String successMessage = String.format("%s님에게 친구 요청을 보냈습니다.", responseData.nickname());

        //201 Created 응답 반환
        return ResponseEntity.status(201).body(ApiResponse.success(
                "CREATED",
                successMessage,
                responseData
        ));
    }

    @DeleteMapping("/request/{userId}")
    @Operation(summary = "친구 요청 철회", description = "보낸 친구 요청을 철회한다.")
    public ResponseEntity<ApiResponse<CancelRequestFriendResponse>> cancelRequestFriend(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("userId") Long targetUserId) {

        Long userId = userDetails.getUserId();

        //입력 모델 조립
        CancelRequestFriendCommand command = new CancelRequestFriendCommand(userId, targetUserId);

        //유스케이스 핸들러 실행(비즈니스 로직, 이벤트 발행 유도)
        CancelRequestFriendView view = cancelRequestFriendCommandUseCase.handle(command);

        //최종 응답 객체로 변환
        CancelRequestFriendResponse responseData = CancelRequestFriendResponse.from(view);

        //성공 메시지 가공
        String message = String.format("%s님에게 보낸 친구 요청을 철회했습니다.", responseData.nickname());

        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                message,
                responseData
        ));
    }

    @GetMapping("/sent")
    @Operation(summary = "보낸 친구 요청 목록 조회", description = "로그인한 사용자가 타인에게 보낸 친구 요청 중 SENT 상태인 목록을 조회한다.")
    public ResponseEntity<ApiResponse<List<SentRequestResponse>>> sentList(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();

        //유스케이스 레이어 호출하여 데이터 도출
        List<SentRequestView> sentRequests = getSentRequestFriendQueryUseCase.handle(userId);

        //보낸 친구 요청이 아예 없을 때(200)
        if (sentRequests.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(
                    "SUCCESS",
                    "보낸 친구 요청이 없습니다. 친구를 요청해보세요!",
                    List.of()
            ));
        }

        //목록이 존재할 때 응답 변환 후 반환
        List<SentRequestResponse> responseList = sentRequests.stream()
                .map(SentRequestResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                "보낸 친구 요청 목록 불러오기 성공",
                responseList
        ));
    }

    @PatchMapping("/received/{userId}/accept")
    @Operation(summary = "친구 요청 수락", description = "받은 친구 요청을 수락하고 관계를 맺는다.")
    public ResponseEntity<ApiResponse<AcceptRequestFriendResponse>> acceptRequestFriend(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("userId") Long fromUserId) {

        Long userId = userDetails.getUserId();

        //입력 모델(command) 조립
        AcceptRequestFriendCommand command = new AcceptRequestFriendCommand(userId, fromUserId);

        //수락 유스케이스 실행
        AcceptView view = acceptRequestFriendCommandUseCase.handle(command);

        //응답 DTO 변환
        AcceptRequestFriendResponse responseData = AcceptRequestFriendResponse.from(view);

        //응답 메시지
        String successMessage = String.format("%s님의 친구 요청을 수락했습니다.", responseData.nickname());

        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                successMessage,
                responseData
        ));
    }

    @DeleteMapping("/received/{userId}/reject")
    @Operation(summary = "친구 요청 거절", description = "친구 요청을 거절하고 friend 테이블에서 행 삭제")
    public ResponseEntity<ApiResponse<RejectRequestFriendResponse>> rejectRequestFriend(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("userId") Long fromUserId) {

        Long userId = userDetails.getUserId();

        //입력 모델(Command) 조립
        RejectRequestFriendCommand command = new RejectRequestFriendCommand(userId, fromUserId);

        //유스케이스 핸들러 전송
        RejectView view = rejectRequestFriendCommandUseCase.handle(command);

        //응답 템플릿
        RejectRequestFriendResponse responseData = RejectRequestFriendResponse.from(view);

        //응답 메시지
        String successMessage = String.format("%s님의 친구 요청을 거절했습니다.", responseData.nickname());

        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                successMessage,
                responseData
        ));
    }

    @GetMapping("/received")
    @Operation(summary = "받은 친구 요청 목록", description = "로그인한 사용자가 toUserId이면서 SENT 상태인 목록을 조회한다.")
    public ResponseEntity<ApiResponse<List<ReceivedRequestResponse>>> receivedList(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();

        //유스케이스 view 도출
        List<ReceivedRequestView> receivedRequests = getReceivedRequestFriendQueryUseCase.handle(userId);

        //받은 요청이 없을 때
        if (receivedRequests.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(
                    "SUCCESS",
                    "받은 친구 요청이 없습니다. 친구를 요청해보세요!",
                    List.of()
            ));
        }

        //받은 요청이 있을 때
        List<ReceivedRequestResponse> responseList = receivedRequests.stream()
                .map(ReceivedRequestResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                "받은 친구 요청 목록 불러오기 성공",
                responseList
        ));
    }

    @PatchMapping("/block/{userId}")
    @Operation(summary = "친구 차단", description = "선택한 친구를 차단한다.")
    public ResponseEntity<ApiResponse<BlockFriendResponse>> blockFriend(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("userId") Long targetUserId) {

        Long userId = userDetails.getUserId();

        //입력 조립
        BlockFriendCommand command = new BlockFriendCommand(userId, targetUserId);

        //유스 케이스 레이어
        BlockView view = blockFriendCommandUseCase.handle(command);

        //응답 가공
        BlockFriendResponse responseData = BlockFriendResponse.from(view);

        String successMessage = String.format("%s님을 차단했습니다. 이제 서로 검색 및 요청이 불가능합니다.", responseData.nickname());

        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                successMessage,
                responseData
        ));
    }

    @GetMapping("/blocked")
    @Operation(summary = "친구 차단한 목록", description = "로그인 유저가 차단한 목록을 조회한다.")
    public ResponseEntity<ApiResponse<List<BlockedFriendResponse>>> blockedList(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();

        //차단 뷰 목록 획득
        List<BlockedView> blockedViews = getBlockedFriendQueryUseCase.handle(userId);

        //차단한 목록이 없을 때
        if (blockedViews.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(
                    "SUCCESS",
                    "차단한 사용자가 없습니다.",
                    List.of()
            ));
        }

        //차단한 목록이 있을 때
        List<BlockedFriendResponse> responseList = blockedViews.stream()
                .map(BlockedFriendResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                "차단 목록 불러오기 성공",
                responseList
        ));
    }

    @PatchMapping("/unblock/{userId}")
    @Operation(summary = "친구 차단 해제", description = "친구 차단을 해제하여 다시 친구 상태로 만든다.")
    public ResponseEntity<ApiResponse<UnblockFriendResponse>> unblockFriend(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("userId") Long targetUserId) {

        Long userId = userDetails.getUserId();

        //입력 명령(command) 생성
        UnblockFriendCommand command = new UnblockFriendCommand(userId, targetUserId);

        //서비스 핸들러 작동
        UnblockView view = unblockFriendCommandUseCase.handle(command);

        //응답 가공
        UnblockFriendResponse responseData = UnblockFriendResponse.from(view);

        //응답 메시지
        String successMessage = String.format("%s님을 차단 해제했습니다.", responseData.nickname());

        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                successMessage,
                responseData
        ));
    }

    @DeleteMapping("/delete/{userId}")
    @Operation(summary = "친구 삭제", description = "친구 상태이면서 강사가 아닌 친구를 삭제한다.")
    public ResponseEntity<ApiResponse<DeleteFriendResponse>> deleteFriend(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("userId") Long targetUserId) {

        Long userId = userDetails.getUserId();

        //입력 명령(Command) 객체 생성
        DeleteFriendCommand command = new DeleteFriendCommand(userId, targetUserId);

        //유스케이스 레이어 핸들러 호출
        DeleteView view = deleteFriendCommandUseCase.handle(command);

        //응답 가공
        DeleteFriendResponse responseData = DeleteFriendResponse.from(view);

        //응답 메시지
        String successMessage = String.format("%s님을 친구 목록에서 삭제했습니다.", responseData.nickname());

        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                successMessage,
                responseData
        ));
    }

}
