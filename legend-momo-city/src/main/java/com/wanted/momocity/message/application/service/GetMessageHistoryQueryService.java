package com.wanted.momocity.message.application.service;


import com.wanted.momocity.friend.enrollment.EnrollmentWithFMJpaEntity;
import com.wanted.momocity.friend.fmexception.FMResourceAccessDeniedException;
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

    //메시지 내역 조회
    @Override
    public List<MessageHistoryView> handle(Long roomId, Long userId, Long lastMessageId) {
        log.info("[GetMessageHistoryQueryService] 내역 조회 시작 - 유저: {}, 방: {}, 커서ID: {}", userId, roomId, lastMessageId);

        // 1. 유저 정보 및 권한 확인
        UserWithFMJpaEntity loginUser = messageSideUserRepository.findUserById(userId)
                .map(obj -> (UserWithFMJpaEntity) obj)
                .orElseThrow(() -> new FMResourceAccessDeniedException("존재하지 않는 유저입니다."));

        //방 멤버가 맞는지 검증
        boolean isMember = springDataMessageRepository.existsByRoomId_IdAndSenderId_Id(roomId, userId);
        if (!isMember) {
            throw new FMResourceAccessDeniedException("해당 채팅방에 접근할 권한이 없습니다.");
        }

        List<ChatRoomMemberJpaEntity> allMembers = springDataChatRoomMemberRepository.findByRoomId_Id(roomId);

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
                targetUser = loginUser;
                friendStatus = "me";
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
            messages = springDataMessageRepository.findTop20ByRoomId_IdOrderByIdDesc(roomId);
        } else {
            messages = springDataMessageRepository.findTop20ByRoomId_IdAndIdLessThanOrderByIdDesc(roomId, lastMessageId);
        }
        //프론트 응답: 과거 대화가 위로, 최신 대화가 아래로
        List<MessageJpaEntity> sortedMessages = new ArrayList<>(messages);
        Collections.reverse(sortedMessages);

        // 4. View 주머니에 차곡차곡 담기 (스트림 안 쓰고 향상된 for문으로 안전하고 쉽게)
        List<MessageHistoryView> viewList = new ArrayList<>();
        for (MessageJpaEntity msg : sortedMessages) {
            boolean isMine = msg.getSenderId().getId().equals(userId);

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
                    isMine
            ));
        }

        return viewList;
    }
}
