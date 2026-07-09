package com.wanted.momocity.message.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.application.manager.ChatTypingSessionManager;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.ChatRoomMemberJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Set;

// "입력중..." 웹소켓 처리용 컨트롤러.
// 복잡하지 않으므로 아키텍처 구분 안하기로 판단.
@Controller
@RequiredArgsConstructor
public class ChatTypingController {

    private final ChatTypingSessionManager typingSessionManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository; //닉네임 조회용

    //프론트가 보내는 페이로드: {"isTyping": true} 또는 {"isTyping": false}
    public record TypingRequest(boolean isTyping) {}

    //방 전체에 브로드캐스트할 페이로드
    public record TypingBroadcast(String message, List<Long> typingUserIds) {}

    @MessageMapping("/chat/typing/{roomId}")
    public void handleTyping(@DestinationVariable Long roomId, TypingRequest request, Principal principal) {

        Long userId = extractUserId(principal);
        if (userId == null) return; // 인증 안 된 요청은 무시

        if (request.isTyping()) {
            typingSessionManager.startTyping(roomId, userId);
        } else {
            typingSessionManager.stopTyping(roomId, userId);
        }

        broadcastTypingStatus(roomId);
    }

    private void broadcastTypingStatus(Long roomId) {
        Set<Long> typingUserIds = typingSessionManager.getTypingUsers(roomId);

        List<ChatRoomMemberJpaEntity> members = messageRepository.findMembersByRoomId(roomId);

        for (ChatRoomMemberJpaEntity member : members) {
            Long receiverId = member.getUserId().getId();

            List<Long> othersTyping = typingUserIds.stream()
                    .filter(id -> !id.equals(receiverId))
                    .toList();

            if (othersTyping.isEmpty()) {
                messagingTemplate.convertAndSendToUser(
                        receiverId.toString(),
                        "/sub/chat/typing/" + roomId,
                        new TypingBroadcast("", List.of())
                );
                continue;
            }

            List<UserWithFMJpaEntity> typingUsers = messageRepository.findUsersWithFMByIds(othersTyping);
            List<String> nicknames = typingUsers.stream().map(UserWithFMJpaEntity::getNickname).toList();
            String message = formatTypingMessage(nicknames);

            messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/sub/chat/typing/" + roomId,
                    new TypingBroadcast(message, othersTyping)
            );
        }
    }

    //2명까지는 이름 다 표시, 3명 이상이면 대표자 외 N명
    private String formatTypingMessage(List<String> nicknames) {
        if (nicknames.size() == 1) {
            return nicknames.get(0) + "님이 입력중입니다.";
        } else if (nicknames.size() == 2) {
            return nicknames.get(0) + "님, " + nicknames.get(1) + "님이 입력중입니다.";
        } else {
            int others = nicknames.size() - 1;
            return nicknames.get(0) + "님 외 " + others + "명이 입력중입니다.";
        }
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
