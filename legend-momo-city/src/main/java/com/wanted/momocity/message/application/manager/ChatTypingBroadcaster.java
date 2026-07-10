package com.wanted.momocity.message.application.manager;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.ChatRoomMemberJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatTypingBroadcaster {
    private final ChatTypingSessionManager typingSessionManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;

    public record TypingBroadcast(String message, List<Long> typingUserIds) {}

    public void broadcast(Long roomId) {
        Set<Long> typingUserIds = typingSessionManager.getTypingUsers(roomId);
        List<Long> typingUserIdsList = List.copyOf(typingUserIds);

        // 🎯 [최적화] 타이핑 중인 유저들의 닉네임을 딱 1번만 조회해서 Map으로 캐싱
        Map<Long, String> nicknameMap = typingUserIdsList.isEmpty()
                ? Map.of()
                : messageRepository.findUsersWithFMByIds(typingUserIdsList).stream()
                .collect(Collectors.toMap(u -> u.getId(), UserWithFMJpaEntity::getNickname));

        List<ChatRoomMemberJpaEntity> members = messageRepository.findMembersByRoomId(roomId);

        for (ChatRoomMemberJpaEntity member : members) {
            Long receiverId = member.getUserId().getId();

            List<Long> othersTyping = typingUserIdsList.stream()
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

            // 🎯 [최적화] DB 재조회 없이 캐싱된 Map에서만 꺼내 씀
            List<String> nicknames = othersTyping.stream()
                    .map(nicknameMap::get)
                    .filter(Objects::nonNull)
                    .toList();

            if (nicknames.isEmpty()) continue;

            String message = formatTypingMessage(nicknames);

            messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/sub/chat/typing/" + roomId,
                    new TypingBroadcast(message, othersTyping)
            );
        }
    }

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
}
