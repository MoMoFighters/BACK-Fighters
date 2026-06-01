package com.wanted.momocity.message.application.service;


import com.wanted.momocity.friend.enrollment.EnrollmentWithFMJpaEntity;
import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.lecture.LectureWithFMJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.message.application.usecase.FindChatRoomQueryUseCase;
import com.wanted.momocity.message.domain.repository.ChatRoomQueryProjection;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindChatRoomQueryService implements FindChatRoomQueryUseCase {

    private final MessageRepository messageRepository;
    private final MessageSideEnrollmentRepository messageSideEnrollmentRepository;
    private final MessageSideUserRepository messageSideUserRepository;
    private final SpringDataChatRoomMemberRepository springDataChatRoomMemberRepository;
    private final MessageSideFriendRepository messageSideFriendRepository;
    //정책 주입
    private final MessageEligibilityPolicy messageEligibilityPolicy;
    private final SpringDataMessageRepository springDataMessageRepository;

    //채팅 목록
    @Override
    public List<ChatRoomView> handle(Long userId) {
        log.info("[FindChatRoomQueryService] 채팅방 목록 조회 비즈니스 가공 시작 - 조회 요청 유저ID: {}", userId);

        //현재 로그인한 유저 정보 확인(학생/강사 판별)
        UserWithFMJpaEntity loginUser = messageSideUserRepository.findById(userId)
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 유저입니다."));
        String loginUserRole = loginUser.getRole(); //학생 또는 강사

        //채팅방 리스트 및 수강신청 전체 내역 로드
        List<ChatRoomQueryProjection> pros = messageRepository.findChatRoomByUserId(userId);
        List<EnrollmentWithFMJpaEntity> myEnrollments = messageSideEnrollmentRepository.findByUserId_Id(userId);

        //로그인 유저가 참여중인 방ID만 모아서 가장 작은 ID(제일 먼저 만든 방) 찾기(나와의 채팅방)
        Long firstRoomId = pros.stream()
                .map(ChatRoomQueryProjection::roomId)
                .min(Long::compare)
                .orElse(-1L);

        List<ChatRoomView> result = new ArrayList<>();

        //채팅방 정보 순회하며 가공 시작
        for (ChatRoomQueryProjection pro : pros) {
            Long roomId = pro.roomId();

            //상대방 유저 찾기
            List<ChatRoomMemberJpaEntity> allMembers = springDataChatRoomMemberRepository.findByRoomId_Id(roomId);
            UserWithFMJpaEntity targetUser = null;

            //로그인한 유저가 아닌 멤버를 상대방으로 인식(targetUser)
            for (ChatRoomMemberJpaEntity member : allMembers) {
                if (!member.getUserId().getId().equals(userId)) {
                    targetUser = member.getUserId();
                    break;
                }
            }

            String friendStatus = "none";
            boolean isLeftRoom = false; //상대방이 나갔는지 기록

            //나와의 채팅 처리
            if (allMembers.size() == 1) {

                //1순위 판별: 가장 작은 방ID
                if (roomId.equals(firstRoomId)) {
                    log.info("[FriendChatRoomQueryService] 가입 시 자동 개설된 진짜 나와의 채팅방 발견 - 방ID: {}", roomId);
                    targetUser = loginUser;
                    friendStatus = "me";
                } //2순위 판별: 최초의 방이 아닌데 혼자(상대방 나간 방)
                else {
                    log.info("[FindChatRoomQueryService] 상대방이 나가서 혼자 남은 방 탐색됨 - 방ID: {}", roomId);
                    //해당 채팅방에 로그인 유저 말고 다른 사람이 보낸 메시지가 있는지 확인
                    //로그인 유저 외에 다른 사용자가 보낸 메시지가 있다면 그 방은 상대방이 나간 방
                    //과거 메시지 내역에서 나간 상대방의 유저 정보를 역추적하여 가져옴
                    Optional<MessageJpaEntity> otherMsgOpt = springDataMessageRepository
                            .findFirstByRoomId_IdAndSenderId_IdNotOrderByIdDesc(roomId, userId);

                    if (otherMsgOpt.isPresent()) {
                            //나간 상대방 유저 정보 꺼내기
                            targetUser = otherMsgOpt.get().getSenderId();
                            isLeftRoom = true;
                    } else {
                            //상대방도 없고 상대방이 보낸 메시지도 없을 때
                            log.warn("[FindChatRoomQueryService] 메시지 내역이 없는 유령 방 - 방ID: {}", roomId);
                            targetUser = null;
                    }
                }
            }

            if (targetUser == null) {
                isLeftRoom = true;
            }

            //친구 삭제의 경우에도 (알 수 없음) 처리, 있으면 실제 상태 추출
            //친구 상태 양방향 조회 (나와의 채팅이면 관계 조회 필요없이 me 상태로 처리)
            if (targetUser != null && !"me".equals(friendStatus) && !targetUser.getId().equals(userId)) {
                Optional<FriendJpaEntity> relationOpt = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(userId, targetUser.getId());
                if (relationOpt.isEmpty()) {
                    relationOpt = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(targetUser.getId(), userId);
                }
                if (relationOpt.isPresent()) {
                    friendStatus = relationOpt.get().getStatus();
                }
            }

            //학생끼리의 대화방
            if (targetUser != null && "STUDENT".equals(loginUserRole) && "STUDENT".equals(targetUser.getRole())) {
                if ("SENT".equals(friendStatus)) {
                    log.info("[FindChatRoomQueryService] 학생간 대화 중 SENT 상태인 방 노출 제외 - 방ID: {}", pro.roomId());
                    continue;
                }
            }

            //만약 진짜 나와의 채팅이 아닌데 targetUser가 loginUser라면
            //상대방이 나간 방이므료 무조건 (알 수 없음) 처리하기
            boolean isNotActive;
            if (isLeftRoom) {
                isNotActive = true; //상대방이 나갔으므로 (알 수 없음) 띄우기 위함
            } else {
                //BLOCK 상태이면 BLOCK으로 넘기되 (알 수 없음)으로 가공하기 위해 연동 준비
                //서비스 레이어는 비활성화 상태 여부만 체크
                //정책 클래스에 위임
                isNotActive = messageEligibilityPolicy.determineNotActive(targetUser, friendStatus,userId);
            }

            //강의명 추출 (나와의 채팅이 아닐 때만)
            List<String> lectureTitleList = new ArrayList<>();

            //학생 간엔 강의명 없음
            if (targetUser != null && !targetUser.getId().equals(userId)
                    && !("STUDENT".equals(loginUserRole)
                    && "STUDENT".equals(targetUser.getRole()))) {
                //로그인 유저가 학생, 상대가 강사
                if ("STUDENT".equals(loginUserRole)) {
                    for (EnrollmentWithFMJpaEntity enrollment : myEnrollments) {
                        LectureWithFMJpaEntity lecture = enrollment.getLectureId();
                        if (lecture.getTeacherId().getId().equals(targetUser.getId())) {
                            lectureTitleList.add(lecture.getTitle());
                        }
                    }
                } else if ("TEACHER".equals(loginUserRole)) {
                    //로그인 유저가 강사, 상대가 학생
                    List<EnrollmentWithFMJpaEntity> targetEnrollments = messageSideEnrollmentRepository.findByUserId_Id(targetUser.getId());
                    for (EnrollmentWithFMJpaEntity enrollment : targetEnrollments) {
                        LectureWithFMJpaEntity lecture = enrollment.getLectureId();
                        if (lecture.getTeacherId().getId().equals(userId)) {
                            lectureTitleList.add(lecture.getTitle());
                        }
                    }
                }
            }

            //마지막 채팅 내역, 마지막 채팅 시간
            String lastContent = (pro.lastMessage() != null) ? pro.lastMessage().getContent() : "";
            LocalDateTime lastChattedAt = (pro.lastMessage() != null) ? pro.lastMessage().getCreatedAt() : null;

            //채팅방별 안읽은 메시지
            Long unreadCount = 0L;
            if (targetUser == null || "me".equals(friendStatus) || targetUser.getId().equals(userId)) {
                //나와의 채팅에 보낸 방은 안읽은 메시지 0개
                unreadCount = 0L;
            } else {
                //일반 채팅방만 안읽은 메시지 카운트
                unreadCount = messageRepository.countUnreadMessage(roomId, userId);
            }


            result.add(new ChatRoomView(
                    targetUser != null ? targetUser.getId() : null,
                    targetUser != null ? targetUser.getName() : null,
                    targetUser != null ? targetUser.getNickname() : null, // ◀️ null로 들어감
                    targetUser != null ? targetUser.getRole() : "STUDENT",
                    friendStatus,
                    isNotActive, //비활성 여부(user 테이블)
                    roomId,
                    lastContent,
                    lastChattedAt,
                    unreadCount,
                    lectureTitleList,
                    targetUser != null ? targetUser.getProfileImageUrl() : null
            ));
        }

        log.info("[FindChatRoomQueryService] 채팅 목록 최종 가공 완료. 노출할 채팅방 수: {}개", result.size());
        return result;

    }
}
