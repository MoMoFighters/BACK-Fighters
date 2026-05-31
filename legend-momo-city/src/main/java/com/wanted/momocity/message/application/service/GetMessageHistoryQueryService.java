package com.wanted.momocity.message.application.service;


import com.wanted.momocity.friend.enrollment.EnrollmentWithFMJpaEntity;
import com.wanted.momocity.friend.fmexception.FMResourceAccessDeniedException;
import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;

import com.wanted.momocity.friend.lecture.LectureWithFMJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.message.application.usecase.GetMessageHistoryQueryUseCase;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMessageHistoryQueryService implements GetMessageHistoryQueryUseCase {

    private final MessageRepository messageRepository;
    private final MessageSideFriendRepository messageSideFriendRepository;
    private final MessageEligibilityPolicy messageEligibilityPolicy;
    private final MessageSideUserRepository messageSideUserRepository;
    private final MessageSideEnrollmentRepository messageSideEnrollmentRepository;
    private final SpringDataChatRoomMemberRepository springDataChatRoomMemberRepository;
    private final SpringDataMessageRepository springDataMessageRepository;
    private final SpringDataChatRoomRepository springDataChatRoomRepository;

    //메시지 내역 조회
    @Override
    public List<MessageHistoryView> handle(Long roomId, Long userId, Long lastMessageId) {
        log.info("[GetMessageHistoryQueryService] 내역 조회 시작 - 유저: {}, 방: {}, 커서ID: {}", userId, roomId, lastMessageId);

        // 1. 유저 정보 및 권한 확인
        UserWithFMJpaEntity loginUser = messageSideUserRepository.findUserById(userId)
                .map(obj -> (UserWithFMJpaEntity) obj)
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 유저입니다."));

        //방 존재 검증
        boolean existsRoom = springDataChatRoomRepository.existsById(roomId);
        if (!existsRoom) {
            throw new FMResourceNotFoundException("존재하지 않거나 삭제된 채팅방입니다.");
        }

        //방 멤버가 맞는지 검증
        boolean isCurrentMember = springDataChatRoomMemberRepository.existsByRoomId_IdAndUserId_Id(roomId, userId);
        //멤버엔 없지만 과거 메시지엔 있는지
        boolean hasPastMessage = springDataMessageRepository.existsByRoomId_IdAndSenderId_Id(roomId, userId);
        if (!isCurrentMember && !hasPastMessage) {
            throw new FMResourceAccessDeniedException("해당 채팅방에 접근할 권한이 없습니다.");
        }

        List<ChatRoomMemberJpaEntity> allMembers = springDataChatRoomMemberRepository.findByRoomId_Id(roomId);

        //채팅방 나갔다 들어온 사람 처리
        //기존 멤버는 모든 메시지 다 보여줌(채팅방 생성 시간==멤버 생성 시간)
        LocalDateTime chatCreatedAt = allMembers.isEmpty() ? LocalDateTime.now() : allMembers.get(0).getRoomId().getCreatedAt();
        LocalDateTime messageVisibleStartTimeLine = chatCreatedAt;

        if (isCurrentMember) {
            //현재 멤버인 경우 나의 멤버 정보 추출
            ChatRoomMemberJpaEntity myMembership = allMembers.stream()
                    .filter(member -> member.getUserId().getId().equals(userId))
                    .findFirst()
                    .orElse(null);

            if (myMembership != null) {
                ChatRoomJpaEntity chatRoom = myMembership.getRoomId();
                //방 생성 날짜와 멤버 생성 날자가 다른 경우(나갔다 들어옴)
                if (!chatRoom.getCreatedAt().equals(myMembership.getJoinedAt())) {
                    messageVisibleStartTimeLine = myMembership.getJoinedAt(); //나갔다 들어온 날짜 이후 메시지만 보여줌
                    log.info("[타임라인 필터] 재입장 유저 감지 - 멤버 가입일({}) 이후의 메시지만 조회합니다.", messageVisibleStartTimeLine);
                }
            }
        }


        // 2. 상대방 유저 특정 및 나가기 역추적 (목록 조회 로직 이식)
        UserWithFMJpaEntity targetUser = null;
        for (ChatRoomMemberJpaEntity member : allMembers) {
            if (!member.getUserId().getId().equals(userId)) {
                targetUser = member.getUserId();
                break;
            }
        }

        String friendStatus = "none";
        boolean isLeftRoom = false;

        // 나와의 채팅 혹은 상대방 퇴장 방 판별
        if (targetUser == null && allMembers.size() == 1) {
            // 목록에서 구한 최초 방 ID 조회 방식을 대용하기 위해, 메시지 역추적 진행
            Optional<MessageJpaEntity> otherMsgOpt = springDataMessageRepository
                    .findFirstByRoomId_IdAndSenderId_IdNotOrderByIdDesc(roomId, userId);

            if (otherMsgOpt.isPresent()) {
                targetUser = otherMsgOpt.get().getSenderId();
                isLeftRoom = true;
            } else {

                // 🎯 변경 2: 메시지가 0개일 때, 로그인 유저가 참여한 방 중 가장 작은 ID(최초 생성 방)와 비교
                List<ChatRoomMemberJpaEntity> myAllRooms = springDataChatRoomMemberRepository.findByUserId_Id(userId);
                Long firstRoomId = myAllRooms.stream()
                        .map(member -> member.getRoomId().getId())
                        .min(Long::compare)
                        .orElse(-1L);

                if (roomId.equals(firstRoomId)) {
                    // 내 첫 번째 자동 개설 방이 맞다면 진짜 '나와의 채팅방'
                    targetUser = loginUser;
                    friendStatus = "me";
                } else {
                    // 첫 번째 방이 아닌데 메시지도 없고 나 혼자 남았다면 상대방이 나가버린 방
                    isLeftRoom = true;
                    friendStatus = "none";
                }
            }
        }

        // 3. 친구 상태 추출 (나와의 채팅이 아닐 때)
        if (!"me".equals(friendStatus) && targetUser != null && !targetUser.getId().equals(userId)) {
            Optional<FriendJpaEntity> relationOpt = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(userId, targetUser.getId());
            if (relationOpt.isEmpty()) {
                relationOpt = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(targetUser.getId(), userId);
            }
            if (relationOpt.isPresent()) {
                friendStatus = relationOpt.get().getStatus();
            }
        }

        // 4. 활성화 여부 정책 판별
        boolean isNotActive;
        if (isLeftRoom) {
            isNotActive = true;
        } else if (targetUser != null) {
            isNotActive = messageEligibilityPolicy.determineNotActive(targetUser, friendStatus, userId);
        } else {
            isNotActive = false;
        }

        // 5. 강의명 리스트 추출
        List<String> lectureTitleList = new ArrayList<>();
        if (targetUser != null && !targetUser.getId().equals(userId) &&
                !("STUDENT".equals(loginUser.getRole()) && "STUDENT".equals(targetUser.getRole()))) {

            if ("STUDENT".equals(loginUser.getRole())) {
                List<EnrollmentWithFMJpaEntity> myEnrollments = messageSideEnrollmentRepository.findByUserId_Id(userId);
                for (EnrollmentWithFMJpaEntity enrollment : myEnrollments) {
                    LectureWithFMJpaEntity lecture = enrollment.getLectureId();
                    if (lecture.getTeacherId().getId().equals(targetUser.getId())) {
                        lectureTitleList.add(lecture.getTitle());
                    }
                }
            } else if ("TEACHER".equals(loginUser.getRole())) {
                List<EnrollmentWithFMJpaEntity> targetEnrollments = messageSideEnrollmentRepository.findByUserId_Id(targetUser.getId());
                for (EnrollmentWithFMJpaEntity enrollment : targetEnrollments) {
                    LectureWithFMJpaEntity lecture = enrollment.getLectureId();
                    if (lecture.getTeacherId().getId().equals(userId)) {
                        lectureTitleList.add(lecture.getTitle());
                    }
                }
            }
        }

        //메시지 내역 자르기
        List<MessageJpaEntity> messages;
        if (lastMessageId == null) {
            messages = springDataMessageRepository.findTop20ByRoomId_IdAndCreatedAtGreaterThanEqualOrderByIdDesc(roomId, messageVisibleStartTimeLine);
        } else {
            messages = springDataMessageRepository.findTop20ByRoomId_IdAndIdLessThanAndCreatedAtGreaterThanEqualOrderByIdDesc(roomId, lastMessageId, messageVisibleStartTimeLine);
        }

        //프론트 응답: 과거 대화가 위로, 최신 대화가 아래로
        List<MessageJpaEntity> sortedMessages = new ArrayList<>(messages);
        Collections.reverse(sortedMessages);

        // 4. View 주머니에 차곡차곡 담기 (스트림 안 쓰고 향상된 for문으로 안전하고 쉽게)
        List<MessageHistoryView> viewList = new ArrayList<>();
        for (MessageJpaEntity msg : sortedMessages) {
            boolean isMine = msg.getSenderId().getId().equals(userId);
            UserWithFMJpaEntity sender = msg.getSenderId();

            String notMeTargetName = sender.getName();
            String notMeNickname = sender.getNickname();
            String notMeRole = sender.getRole();

            if (isMine && targetUser != null) {
                notMeTargetName = targetUser.getName();
                notMeNickname = targetUser.getNickname();
                notMeRole = targetUser.getRole();
            }

            viewList.add(new MessageHistoryView(
                    msg.getId(),
                    msg.getSenderId().getId(),
                    targetUser != null ? targetUser.getName() : null, //강사 실제 이름
                    msg.getSenderId().getNickname(),
                    msg.getSenderId().getRole(), //역할
                    friendStatus, //친구 상태
                    isNotActive,
                    lectureTitleList,
                    msg.getContent(), // 🎯 빠져있던 본문 추가!
                    msg.getCreatedAt(),
                    true, // 과거 내역은 무조건 다 읽음 처리
                    isMine,
                    targetUser != null ? targetUser.getProfileImageUrl() : loginUser.getProfileImageUrl(),
                    notMeTargetName,
                    notMeNickname,
                    notMeRole
            ));
        }

        return viewList;
    }
}
