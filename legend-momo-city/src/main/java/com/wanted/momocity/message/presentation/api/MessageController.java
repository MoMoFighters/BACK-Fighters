package com.wanted.momocity.message.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.message.application.command.CreateChatRoomCommand;
import com.wanted.momocity.message.application.usecase.*;
import com.wanted.momocity.message.application.usecase.CreateChatRoomCommandUseCase.CreateRoomView;
import com.wanted.momocity.message.application.usecase.FindChatRoomQueryUseCase.ChatRoomView;
import com.wanted.momocity.message.application.usecase.ReadMessageCommandUseCase.ReadView;
import com.wanted.momocity.message.application.usecase.SendMessageCommandUseCase.SendView;
import com.wanted.momocity.message.application.usecase.GetMessageHistoryQueryUseCase.MessageHistoryView;
import com.wanted.momocity.message.application.usecase.LeaveChatRoomCommandUseCase.LeaveChatRoomView;
import com.wanted.momocity.message.presentation.api.request.SendMessageRequest;
import com.wanted.momocity.message.presentation.api.response.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final FindChatRoomQueryUseCase findChatRoomQueryUseCase;
    private final CreateChatRoomCommandUseCase createChatRoomCommandUseCase;
    private final SendMessageCommandUseCase sendMessageCommandUseCase;
    private final ReadMessageCommandUseCase readMessageCommandUseCase;
    private final GetMessageHistoryQueryUseCase getMessageHistoryQueryUseCase;
    private final LeaveChatRoomCommandUseCase leaveChatRoomCommandUseCase;

    @GetMapping("/rooms")
    @Operation(summary = "채팅방 목록", description = "로그인 유저가 존재하는 모든 채팅방을 조회한다.")
    public ResponseEntity<ApiResponse<List<FindChatRoomResponse>>> getChatRooms(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();

        List<ChatRoomView> view = findChatRoomQueryUseCase.handle(userId);

        //채팅방이 한개도 없을 때
        if (view.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(
                    "SUCCESS",
                    "채팅을 시작한 친구가 없어요. 채팅을 시작해보세요!",
                    List.of()
            ));
        }

        //DTO 가공 변환
        List<FindChatRoomResponse> responseData = view.stream()
                .map(FindChatRoomResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                "채팅 친구 목록 불러오기 성공",
                responseData
        ));
    }

    @PostMapping("/chatrooms/create/{userId}")
    @Operation(summary = "채팅방 조회 및 개설", description = "채팅방 개설 시 기존 채팅방 존재 여부 확인 후 있으면 기존 채팅방으로 보내고 없으면 개설한다.")
    public ResponseEntity<ApiResponse<CreateChatRoomResponse>> findAndNewChatRoom(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("userId") Long targetUserId) {

        Long userId = userDetails.getUserId();

        //커맨드 조립해서 유스케이스 발송
        CreateRoomView view = createChatRoomCommandUseCase.handle(userId, targetUserId);

        //공통 응답 데이터 그릇
        CreateChatRoomResponse responseData = new CreateChatRoomResponse(
                view.roomId(),
                view.targetUserId(),
                view.nickname(),
                view.role(),
                view.status()
        );

        //기존 채팅방 존재할 때
        if (view.isExisting()) {
            return ResponseEntity.ok(ApiResponse.success(
                    "SUCCESS",
                    "기존 채팅방이 존재하여 이전 대화창으로 연결합니다.",
                    responseData
            ));
        }

        //새로운 채팅방 개설
        String successMessage = String.format("%s님과의 대화창을 개설했습니다. 대화를 시작해보세요!", responseData.nickname());
        return ResponseEntity.ok(ApiResponse.created(
                "SUCCESS",
                successMessage,
                responseData
        ));
    }

    @PostMapping("/send/{roomId}")
    @Operation(summary = "메시지 전송", description = "채팅방을 선택하고 메시지를 전송한다.")
    public ResponseEntity<ApiResponse<SendMessageResponse>> sendMessage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                        @PathVariable("roomId") Long roomId,
                                                                        @Valid @RequestBody SendMessageRequest request) {
        Long userId = userDetails.getUserId();

        SendView view = sendMessageCommandUseCase.handle(userId, roomId, request.content());

        SendMessageResponse responseData = new SendMessageResponse(
                view.roomId(),
                view.targetUserId(),
                view.targetNickname(),
                view.targetRole(),
                view.friendStatus(),
                view.content(),
                view.createdAt()
        );

        String successMessage = String.format("%s님에게 메시지를 성공적으로 전송했습니다.", view.targetNickname());

        return ResponseEntity.ok(ApiResponse.created(
                "SUCCESS",
                successMessage,
                responseData
        ));
    }

    @PatchMapping("/read/{roomId}")
    @Operation(summary = "메시지 읽음 처리", description = "채팅방 진입 시 읽음과 내역 조회 두 개의 API 호출 및 웹소켓으로 채팅방 머무르는 여부 확인")
    public ResponseEntity<ApiResponse<ReadMessageResponse>> readMessages(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("roomId") Long roomId) {

        Long userId = userDetails.getUserId();

        ReadView view = readMessageCommandUseCase.handle(roomId, userId);

        ReadMessageResponse responseData = new ReadMessageResponse(
                view.roomId(),
                view.targetUserId(),
                view.nickname(),
                true
        );

        String successMessage = view.hasUnread()
                ? String.format("%s님에게 온 메시지를 성공적으로 읽었습니다.", view.nickname())
                : "새로 온 메시지가 없어 읽음 상태가 유지됩니다.";

        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                successMessage,
                responseData
        ));
    }

    @GetMapping("/history/{roomId}")
    @Operation(summary = "메시지 내역 조회", description = "메시지 내역을 최신 20개씩 보내고 최상단 스크롤하면 마지막 메시지 아이디 기준으로 최신순 보여준다.")
    public ResponseEntity<ApiResponse<List<GetMessageHistoryResponse>>> getMessageHistory(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                 @PathVariable("roomId") Long roomId,
                                                                 @RequestParam(value = "lastMessageId", required = false) Long lastMessgeId) {
        Long userId = userDetails.getUserId();

        List<MessageHistoryView> viewList = getMessageHistoryQueryUseCase.handle(roomId, userId, lastMessgeId);

        //채팅 내역이 없을 때
        if (viewList.isEmpty() && lastMessgeId == null) {
            return ResponseEntity.ok(ApiResponse.success(
                    "SUCCESS",
                    "아직 대화 기록이 없습니다. 첫 메시지를 보내보세요!",
                    List.of()
            ));
        }

        //채팅 내역 있을 때
        List<GetMessageHistoryResponse> responseData = viewList.stream()
                .map(GetMessageHistoryResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                "채팅 내역 조회 성공",
                responseData
        ));
    }

    @DeleteMapping("/chatRooms/leave/{roomId}")
    @Operation(summary = "채팅방 나가기", description = "혼자 남으면 전체 폭파하고 누군가 남아있으면 멤버에서만 삭제한다.")
    public ResponseEntity<ApiResponse<LeaveChatRoomResponse>> leaveChatRoom(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("roomId") Long roomId) {

        Long userId = userDetails.getUserId();

        LeaveChatRoomView view = leaveChatRoomCommandUseCase.handle(roomId, userId);

        //채팅방에 마지막 남은 사용자일 때
        if (view.isLastMember()) {
            return ResponseEntity.ok(ApiResponse.success(
                    "SUCCESS",
                    "해당 채팅방을 나갔습니다. 다시 채팅방을 개설할 수 있습니다.",
                    new LeaveChatRoomResponse(null, null, null, null, null)
            ));
        }

        //상대방이 남아있을 때
        LeaveChatRoomResponse responseData = new LeaveChatRoomResponse(
                view.roomId(),
                view.userId(),
                view.nickname(),
                view.role(),
                view.status()
        );

        String successMessage = String.format("%s님과의 대화창을 나갔습니다.", view.nickname());
        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                successMessage,
                responseData
        ));
    }
}
