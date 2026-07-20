package com.wanted.momocity.global.infrastructure.config;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.study.domain.model.GroupRoomMember;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StudyStompInterceptor implements ChannelInterceptor {

    // 멤버십 검증용 — "이 userId가 이 roomId에 JOINED 상태로 존재하는가"를 확인하기 위해 주입
    private final GroupRoomMemberRepository groupRoomMemberRepository;

    // study가 관여하는 구독 destination의 접두사 (그룹방 실시간 상태 브로드캐스트 채널)
    private static final String STUDY_ROOM_PREFIX = "/sub/study/room/";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        // [1] SUBSCRIBE가 아니면 study는 관여하지 않고 그대로 통과.
        //     (CONNECT/DISCONNECT/UNSUBSCRIBE 등은 TopicSubscriptionInterceptor가 이미 처리 중)
        if (!StompCommand.SUBSCRIBE.equals(command)) {
            return message;
        }

        String destination = accessor.getDestination();

        // [2] study 소관 destination이 아니면 통과.
        //     - "/sub/study/room/{roomId}"만 검증 대상
        //     - "/user/sub/study/solo"(솔로 개인 큐)는 유저 본인만 받는 destination이라
        //       별도 방 멤버십 검증이 필요 없으므로 여기서 자연스럽게 통과됨
        if (destination == null || !destination.startsWith(STUDY_ROOM_PREFIX)) {
            return message;
        }

        // [3] destination에서 roomId 파싱
        //     예: "/sub/study/room/5" → 5
        Long roomId = parseRoomId(destination);
        if (roomId == null) {
            log.warn("[StudyStompInterceptor] roomId 파싱 실패로 구독 거부 - destination: {}", destination);
            return null; // 조용히 무시 (TopicSubscriptionInterceptor 컨벤션과 동일)
        }

        // [4] 인증된 유저 ID 추출
        //     TopicSubscriptionInterceptor가 CONNECT 시점에 심어둔 정보를 그대로 재사용.
        //     Principal 우선 → 세션 attributes 차선, 2단 폴백만 사용
        //     (Native Header 직접 파싱까지 가면 JWT 검증 로직이 중복되므로 여기선 제외)
        Long userId = getUserIdFromAccessor(accessor);
        if (userId == null) {
            log.warn("[StudyStompInterceptor] 인증 정보 없음으로 구독 거부 - destination: {}", destination);
            return null;
        }

        // [5] 실제 멤버십 검증 — 이 유저가 이 방에 JOINED 상태인지 DB 조회
        // GroupRoomMember는 room 전체 생명주기를 하나의 row로 관리하는 구조
        // -> row가 존재해도 status가 INVITED/LEFT/KICKED 상태 가능 -> JOINED인지까지 확인
        Optional<GroupRoomMember> memberOpt =
                groupRoomMemberRepository.findByGroupRoomIdAndUserId(roomId, userId);

        boolean isMember = memberOpt.isPresent() && memberOpt.get().isJoined();

        if (!isMember) {
            log.warn("[StudyStompInterceptor] 방 멤버 아님(또는 JOINED 아님)으로 구독 거부 - userId: {}, roomId: {}", userId, roomId);
            return null; // 방 멤버가 아닌 사람이 다른 방 실시간 상태를 엿보는 것 차단
        }

        log.info("[StudyStompInterceptor] 유저 {}번이 study room {}번 구독 허용됨.", userId, roomId);
        return message;
    }

    // "/sub/study/room/5" 형태에서 마지막 segment를 Long으로 파싱
    private Long parseRoomId(String destination) {
        try {
            String roomIdStr = destination.substring(destination.lastIndexOf("/") + 1);
            return Long.parseLong(roomIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // TopicSubscriptionInterceptor.getUserIdFromAccessor()와 동일한 패턴,
    // 다만 3차 폴백(Native Header 직접 검증)은 의도적으로 제외함
    private Long getUserIdFromAccessor(StompHeaderAccessor accessor) {
        // 1차: CONNECT 시점에 setUser()로 심어둔 Principal
        if (accessor.getUser() != null) {
            try {
                Authentication authentication = (Authentication) accessor.getUser();
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                return userDetails.getUserId();
            } catch (Exception e) {
                log.warn("[StudyStompInterceptor] Principal에서 유저 ID 추출 실패: {}", e.getMessage());
            }
        }

        // 2차: CONNECT 시점에 세션 attributes에 심어둔 userId
        if (accessor.getSessionAttributes() != null
                && accessor.getSessionAttributes().containsKey("userId")) {
            return (Long) accessor.getSessionAttributes().get("userId");
        }

        return null;
    }

}
