package com.wanted.momocity.notification.presentation.api;


import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.notification.application.query.GetNotificationQuery;
import com.wanted.momocity.notification.application.usecase.NotificationCommandUseCase;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase;
import com.wanted.momocity.notification.application.usecase.NotificationQueryUseCase.NotiView;
import com.wanted.momocity.notification.presentation.api.response.GetNotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    //쿼리(총 알림 개수, 알림 목록, 페이지 이동)
    private final NotificationQueryUseCase notificationQueryUseCase;
    //커맨드(알림 읽음, 알림 삭제)
    private final NotificationCommandUseCase notificationCommandUseCase;

    //알림 목록 조회
    @GetMapping("/api/v2/notice/notificationlist")
    @Operation(summary = "알림 목록", description = "로그인 유저에게 온 모든 알림 내역을 조회한다.")
    public ResponseEntity<ApiResponse<List<GetNotificationResponse>>> getNotification(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();

        GetNotificationQuery query = new GetNotificationQuery(userId);
        List<NotiView> view = notificiationQueryUseCase.getNotificationQueryHandle(query);

        //채팅방이 한개도 없을 때
        if (view.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(
                    "SUCCESS",
                    "받은 알림이 없습니다.",
                    List.of()
            ));
        }

        //DTO 가공 변환
        List<GetNotificationResponse> responseData = view.stream()
                .map(GetNotificationResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS",
                "알림 목록 불러오기 성공",
                responseData
        ));
    }

//    //v2 -> 다대다 확장으로 개설 대상자들 리스트로
//    @PostMapping("/api/v2/messages/chatrooms/create")
//    @Operation(summary = "채팅방 조회 및 개설", description = "채팅방 개설 시 기존 채팅방 존재 여부 확인 후 있으면 기존 채팅방으로 보내고 없으면 개설한다.")
//    public ResponseEntity<ApiResponse<CreateChatRoomResponse>> findAndNewChatRoom(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @RequestParam(value = "roomTitle", required = false) String roomTitle,
//            @RequestBody CreateChatRoomRequest request) {
//
//        Long userId = userDetails.getUserId();
//
//        CreateChatRoomCommand command = new CreateChatRoomCommand(userId, roomTitle, request.chatMember());
//
//        //커맨드 조립해서 유스케이스 발송
//        CreateRoomView view = messageCommandUseCase.createChatRoomCommandHandle(command);
//
//        //공통 응답 데이터 그릇
//        CreateChatRoomResponse responseData = CreateChatRoomResponse.of(view);
//
//        //기존 채팅방 존재할 때
//        if (view.isExisting()) {
//            return ResponseEntity.ok(ApiResponse.success(
//                    "SUCCESS",
//                    "기존 채팅방이 존재하여 이전 대화창으로 연결합니다.",
//                    responseData
//            ));
//        }
//
//        //새로운 채팅방 개설
//        //일대일, 다대다 성공 메시지 분기
//        String successMessage = "";
//
//        //roomTitle이 존재하고 멤버가 여러명: 다대다
//        if (roomTitle != null && !roomTitle.trim().isEmpty()) {
//            successMessage = String.format("'%s' 대화창을 개설했습니다. 대화를 시작해보세요!", roomTitle);
//        } else if (view.memberInfo().size() == 1) {
//            //일대일: 방제목 없고 멤버 정보의 개수가 1개
//            //첫 번째 유저의 닉네임 빼오기
//            String targetNickname =  view.memberInfo().get(0).nickname();
//            successMessage = String.format("%s님과의 대화창을 개설했습니다. 대화를 시작해보세요!", targetNickname);
//        }
//
//        return ResponseEntity.ok(ApiResponse.created(
//                "SUCCESS",
//                successMessage,
//                responseData
//        ));
//    }
//
//
//    @PatchMapping("/api/v1/messages/read/{roomId}")
//    @Operation(summary = "메시지 읽음 처리", description = "채팅방 진입 시 읽음과 내역 조회 두 개의 API 호출 및 웹소켓으로 채팅방 머무르는 여부 확인")
//    public ResponseEntity<ApiResponse<ReadMessageResponse>> readMessages(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("roomId") Long roomId) {
//
//        Long userId = userDetails.getUserId();
//
//        ReadMessageCommand command = new ReadMessageCommand(roomId, userId);
//
//        ReadView view = messageCommandUseCase.readMessageCommandHandle(command);
//
//        ReadMessageResponse responseData = new ReadMessageResponse(
//                view.roomId(),
//                view.targetUserId(),
//                view.nickname(),
//                true
//        );
//
//        String successMessage = "";
//        if (view.hasUnread()) {
//            //안읽은 메시지가 있을 때
//            successMessage = (view.targetUserId() == null)
//                    ? String.format("'%s' 대화창의 메시지를 성공적으로 읽었습니다.", view.nickname())
//                    : String.format("%s님에게 온 메시지를 성공적으로 읽었습니다.", view.nickname());
//
//        } else {
//            //안읽은 메시지가 없을 때
//            successMessage = "새로 온 메시지가 없어 읽음 상태가 유지됩니다.";
//        }
//
//        return ResponseEntity.ok(ApiResponse.success(
//                "SUCCESS",
//                successMessage,
//                responseData
//        ));
//    }
//
//    @DeleteMapping("/api/v1/messages/chatRooms/leave/{roomId}")
//    @Operation(summary = "채팅방 나가기", description = "혼자 남으면 전체 폭파하고 누군가 남아있으면 멤버에서만 삭제한다.")
//    public ResponseEntity<ApiResponse<LeaveChatRoomResponse>> leaveChatRoom(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("roomId") Long roomId) {
//
//        Long userId = userDetails.getUserId();
//
//        LeaveChatRoomCommand command = new LeaveChatRoomCommand(roomId, userId);
//
//        LeaveChatRoomView view = messageCommandUseCase.leaveChatRoomCommandHandle(command);
//
//        //채팅방에 마지막 남은 사용자일 때
//        if (view.isLastMember()) {
//            return ResponseEntity.ok(ApiResponse.success(
//                    "SUCCESS",
//                    "해당 채팅방을 나갔습니다. 다시 채팅방을 개설할 수 있습니다.",
//                    new LeaveChatRoomResponse(null, null, null, null, null)
//            ));
//        }
//
//        //상대방이 남아있을 때
//        LeaveChatRoomResponse responseData = new LeaveChatRoomResponse(
//                view.roomId(),
//                view.userId(),
//                view.nickname(),
//                view.role(),
//                view.status()
//        );
//
//        String successMessage;
//        //남은 사람 여러명, 다대다 채팅이었던 경우
//        if (view.userId() == null) {
//            successMessage = String.format("%s 대화창에서 나갔습니다.", view.nickname());
//        } else {
//            //남은 사람 한 명, 일대일 채팅이었던 경우
//            successMessage = String.format("%s님과의 대화창을 나갔습니다.", view.nickname());
//        }
//        return ResponseEntity.ok(ApiResponse.success(
//                "SUCCESS",
//                successMessage,
//                responseData
//        ));
//    }
//
}
