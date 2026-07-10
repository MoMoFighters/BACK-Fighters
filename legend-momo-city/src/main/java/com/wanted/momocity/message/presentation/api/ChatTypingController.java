package com.wanted.momocity.message.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.message.application.manager.ChatTypingBroadcaster;
import com.wanted.momocity.message.application.manager.ChatTypingSessionManager;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;


// "입력중..." 웹소켓 처리용 컨트롤러.
// 복잡하지 않으므로 아키텍처 구분 안하기로 판단.
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatTypingController {

    private final ChatTypingSessionManager typingSessionManager;
    private final ChatTypingBroadcaster typingBroadcaster;
    private final MessageRepository messageRepository; //방 접근 권한 확인을 위함

    //프론트가 보내는 페이로드: {"isTyping": true} 또는 {"isTyping": false}
    public record TypingRequest(boolean isTyping) {}

    @MessageMapping("/chat/typing/{roomId}")
    public void handleTyping(@DestinationVariable Long roomId, TypingRequest request, Principal principal) {

        Long userId = extractUserId(principal);
        if (userId == null) return; // 인증 안 된 요청은 무시

        // 방 멤버십 검증: 비멤버의 타이핑 신호 주입 차단
        boolean isMember = messageRepository.existsMemberByRoomIdAndUserId(roomId, userId);
        if (!isMember) {
            log.warn("[ChatTypingController] 방 멤버가 아닌 유저의 타이핑 요청 차단 - userId: {}, roomId: {}", userId, roomId);
            return;
        }

        if (request.isTyping()) {
            typingSessionManager.startTyping(roomId, userId);
        } else {
            typingSessionManager.stopTyping(roomId, userId);
        }

        typingBroadcaster.broadcast(roomId);
    }

    private Long extractUserId(Principal principal) {
        if (principal instanceof Authentication authentication) {
            if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                return userDetails.getUserId();
            }
        }
        return null;
    }
}
