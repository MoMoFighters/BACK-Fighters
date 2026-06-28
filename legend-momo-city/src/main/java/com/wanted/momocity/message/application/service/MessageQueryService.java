package com.wanted.momocity.message.application.service;

import com.wanted.momocity.friend.enrollment.EnrollmentWithFMJpaEntity;
import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.lecture.LectureWithFMJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.application.metric.MessageMetrics;
import com.wanted.momocity.message.application.query.GetMessageHistoryQuery;
import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.message.application.query.FindChatRoomQuery;
import com.wanted.momocity.message.application.usecase.MessageQueryUseCase;
import com.wanted.momocity.message.domain.repository.ChatRoomQueryProjection;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageQueryService implements MessageQueryUseCase {

    private final MessageRepository messageRepository;
    //정책 주입
    private final MessageEligibilityPolicy messageEligibilityPolicy;

    //메트릭
    private final MessageMetrics messageMetrics;

    //메시지 채팅 목록
    @Override
    public List<ChatRoomView> getChatRoomQueryHandle(FindChatRoomQuery query) {
        log.info("[FindChatRoomQueryService] 채팅방 목록 조회 비즈니스 가공 시작 - 조회 요청 유저ID: {}", query.userId());

        //현재 로그인한 유저 정보 확인(학생/강사 판별)
        UserWithFMJpaEntity loginUser = messageRepository.findUserWithFMById(query.userId())
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 유저입니다."));
        String loginUserRole = loginUser.getRole(); //학생 또는 강사

        // 채팅방 리스트 및 수강신청 전체 내역 로드
        List<ChatRoomQueryProjection> pros = messageRepository.findChatRoomByUserId(query.userId());
        List<EnrollmentWithFMJpaEntity> myEnrollments = messageRepository.findEnrollmentsByUserId(query.userId());

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
            List<ChatRoomMemberJpaEntity> allMembers = messageRepository.findMembersByRoomId(roomId);

            List<UserWithFMJpaEntity> targetUsers = new ArrayList<>();

            List<MemberInfo> memberInfo = new ArrayList<>();

            //로그인한 유저가 아닌 멤버를 상대방으로 인식(targetUser)
            for (ChatRoomMemberJpaEntity member : allMembers) {
                if (!member.getUserId().getId().equals(query.userId())) {
                    targetUsers.add(member.getUserId());
                }
            }

            String baseFriendStatus = "none";
            boolean isLeftRoom = false; //상대방이 나갔는지 기록

            // 나와의 채팅 처리
            if (allMembers.size() == 1) {
                //1순위 판별: 가장 작은 방ID
                if (roomId.equals(firstRoomId)) {
                    log.info("[FriendChatRoomQueryService] 가입 시 자동 개설된 진짜 나와의 채팅방 발견 - 방ID: {}", roomId);
                    targetUsers.add(loginUser);
                    baseFriendStatus = "me";
                } //2순위 판별: 최초의 방이 아닌데 혼자(상대방 나간 방)
                else {
                    log.info("[FindChatRoomQueryService] 상대방이 나가서 혼자 남은 방 탐색됨 - 방ID: {}", roomId);
                    //해당 채팅방에 로그인 유저 말고 다른 사람이 보낸 메시지가 있는지 확인
                    // 로그인 유저 외에 다른 사용자가 보낸 메시지가 있다면 그 방은 상대방이 나간 방
                    // 과거 메시지 내역에서 나간 상대방의 유저 정보를 역추적하여 가져옴
                    Optional<MessageJpaEntity> otherMsgOpt = messageRepository
                            .findLatestMessageExceptMe(roomId, query.userId());

                    if (otherMsgOpt.isPresent()) {
                        //나간 상대방 유저 정보 꺼내기
                        targetUsers.add(otherMsgOpt.get().getSenderId());
                        isLeftRoom = true;
                    } else {
                        //안내 문구 역추적
                        Optional<UserWithFMJpaEntity> announceUserOpt = messageRepository.findLatestAnnounceUser(roomId);
                        if (announceUserOpt.isPresent()) {
                            targetUsers.add(announceUserOpt.get());
                            isLeftRoom = true;
                            log.info("[FindChatRoomQueryService] 안내 문구를 통한 나간 상대방 역추적 성공 - 유저ID: {}", targetUsers.get(0).getId());
                        } else {
                            //상대방도 없고 상대방이 보낸 메시지도 없을 때
                            log.warn("[FindChatRoomQueryService] 메시지 내역이 없는 유령 방 - 방ID: {}", roomId);
                            isLeftRoom = true;
                        }
                    }
                }
            }

            if (targetUsers.isEmpty()) {
                isLeftRoom = true;
            }

            for (UserWithFMJpaEntity targetUser : targetUsers) {

                //개별 유저별 로그인 유저와의 친구 상태
                String currentFriendStatus = baseFriendStatus;

                //친구 삭제의 경우에도 (알 수 없음) 처리, 있으면 실제 상태 추출
                // 친구 상태 양방향 조회 (나와의 채팅이면 관계 조회 필요없이 me 상태로 처리)
                if (!"me".equals(currentFriendStatus) && !targetUser.getId().equals(query.userId())) {
                    currentFriendStatus = messageRepository.findFriendRelation(query.userId(), targetUser.getId())
                            .map(FriendJpaEntity::getStatus)
                            .orElse("none");
                }

                //학생끼리의 대화방
                if (allMembers.size() == 2 && "STUDENT".equals(loginUserRole) && "STUDENT".equals(targetUser.getRole())) {
                    if (!"FRIEND".equals(currentFriendStatus)) {
                        log.info("[FindChatRoomQueryService] 학생간 대화 중 SENT 상태인 방 노출 제외 - 방ID: {}", pro.roomId());
                        continue;
                    }
                }

                //만약 진짜 나와의 채팅이 아닌데 targetUser가 loginUser라면
                // 상대방이 나간 방이므로 무조건 (알 수 없음) 처리하기
                // 🚨 v2 -> 일대일 채팅의 경우를 고려해 memberInfo에 데이터 가공 필요.
                // 마스킹 여부 및 유저 활성화 여부 체크
                //BLOCK 상태이면 BLOCK으로 넘기되 (알 수 없음)으로 가공하기 위해 연동 준비
                // 서비스 레이어는 비활성화 상태 여부만 체크
                // 정책 클래스에 위임
                boolean isNotActive = !"ACTIVE".equals(targetUser.getStatus());
                boolean shouldMasked = messageEligibilityPolicy.determineNotActive(targetUser, currentFriendStatus, query.userId());

                //강의명 추출 (나와의 채팅이 아닐 때만)
                List<String> lectureTitleList = new ArrayList<>();

                //학생 간엔 강의명 없음
                if (!targetUser.getId().equals(query.userId())
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
                        List<EnrollmentWithFMJpaEntity> targetEnrollments = messageRepository.findEnrollmentsByUserId(targetUser.getId());
                        for (EnrollmentWithFMJpaEntity enrollment : targetEnrollments) {
                            LectureWithFMJpaEntity lecture = enrollment.getLectureId();
                            if (lecture.getTeacherId().getId().equals(query.userId())) {
                                lectureTitleList.add(lecture.getTitle());
                            }
                        }
                    }
                }

                //멤버 정보
                memberInfo.add(new MemberInfo(
                        targetUser.getId(),
                        "TEACHER".equals(targetUser.getRole()) ? targetUser.getName() : null,
                        targetUser.getNickname(),
                        lectureTitleList,
                        targetUser.getRole(),
                        currentFriendStatus,
                        targetUser.getProfileImageUrl(),
                        isLeftRoom,
                        isNotActive,
                        shouldMasked
                ));
            }

                //마지막 채팅 내역, 마지막 채팅 시간
                String lastContent = (pro.lastMessage() != null) ? pro.lastMessage().getContent() : "";
                LocalDateTime lastChattedAt = (pro.lastMessage() != null) ? pro.lastMessage().getCreatedAt() : null;

                //안내 문구 시간 정렬 기준 추가
                LocalDateTime lastAnnounceAt = messageRepository.findLatestAnnounceTime(roomId).orElse(null);

                //실제 정렬 기준이 될 시간 계산(메시지 시간, 안내 문구 시간)
                LocalDateTime lastestOrderTime;
                if (lastChattedAt != null && lastAnnounceAt != null) {
                    //메시지 정렬 시간, 안내 문구 정렬 시간이 둘 다 있으면 더 최신 시간을 기준으로 정렬
                    lastestOrderTime = lastChattedAt.isAfter(lastAnnounceAt) ? lastChattedAt : lastAnnounceAt;
                } else if (lastChattedAt != null) {
                    //메시지만 있으면 메시지 시간
                    lastestOrderTime = lastChattedAt;
                } else if (lastAnnounceAt != null) {
                    //안내 문구만 있으면 안내 문구 시간
                    lastestOrderTime = lastAnnounceAt;
                } else {
                    //둘다 없으면 채팅방 개설 시간
                    lastestOrderTime = pro.roomCreatedAt();
                }

                //채팅방별 안읽은 메시지
                Long unreadCount = 0L;
                boolean isMeRoom = targetUsers.stream().anyMatch(u -> u.getId().equals(query.userId()));
                if (targetUsers.isEmpty() || "me".equals(baseFriendStatus) || isMeRoom) {
                    //나와의 채팅에 보낸 방은 안읽은 메시지 0개
                    unreadCount = 0L;
                } else {
                    //일반 채팅방만 안읽은 메시지 카운트
                    unreadCount = messageRepository.countUnreadMessage(roomId, query.userId());
                }

                //채팅방 멤버 수(로그인 유저 포함)
                Long inMemberCount = (long) allMembers.size();

                //Query 사용으로 구조 변경
                RoomInfo roomInfo = new RoomInfo(
                        roomId,
                        pro.roomTitle(),
                        inMemberCount, //로그인 유저 포함 멤버수
                        lastContent,
                        lastestOrderTime, //메시지, 안내 문구, 방 생성 시간 중 실제 정렬 기준
                        unreadCount
                );

            result.add(new ChatRoomView(
                    roomInfo,
                    memberInfo,
                    lastestOrderTime
            ));
        }

        //람다식으로 정렬하기: o1, o2의 원소를 무작위로 꺼내서 계속 비교해줌
        // 음수: 위로 올림. 양수: 아래로 내림. 0: 그대로
        // anyMatch: 리스트 안 요소 중 하나라도 조건을 만족하는 게 있다면 true 반환
        // 채팅방 목록 메시지 or 안내 문구 최신순으로 정렬(최신이 최상단)
        result.sort((o1, o2) -> {
            //memberInfo에서 친구 상태 빼기(status)
            boolean isO1Me = o1.memberInfo().stream()
                    .anyMatch(member -> "me".equals(member.status()));
            boolean isO2Me = o2.memberInfo().stream()
                    .anyMatch(member -> "me".equals(member.status()));

            //나와의 채팅방은 최상단 고정
            if (isO1Me && !isO2Me) return -1; //나와의 채팅을 위로 올림
            if (!isO1Me && isO2Me) return 1; //나와의 채팅을 위로 올림
            if (isO1Me && isO2Me) return 0;

            //일반 채팅방은 lastestOrderTime 기준 내림차순(최신순)
            return o2.lastestOrderTime().compareTo(o1.lastestOrderTime());
        });

        log.info("[FindChatRoomQueryService] 채팅 목록 최종 가공 완료. 노출할 채팅방 수: {}개", result.size());
        return result;
    }

    //메시지 내역
    @Override
    public List<MessageHistoryView> getMessageHistoryQueryHandle(GetMessageHistoryQuery query) {
        // 🎯 메트릭 심기: Timer.record() 내부에 로직을 람다로 감싸서 실행 시간 측정
        return messageMetrics.getMessageHistoryTimer().record(() -> {

            log.info("[GetMessageHistoryQueryService] 내역 조회 시작 - 유저: {}, 방: {}, 커서ID: {}", query.userId(), query.roomId(), query.lastMessageId());

            // 1. 유저 정보 및 권한 확인
            UserWithFMJpaEntity loginUser = messageRepository.findUserWithFMById(query.userId())
                    .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 유저입니다."));

            //방 존재 검증
            ChatRoomJpaEntity chatRoom = messageRepository.findChatRoomById(query.roomId())
                    .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않거나 삭제된 채팅방입니다."));

            //방 멤버가 맞는지 검증
            boolean isCurrentMember = messageRepository.existsMemberByRoomIdAndUserId(query.roomId(), query.userId());

            //채팅방 권환 확인 위임
            messageEligibilityPolicy.validateAccess(query.roomId(), query.userId(), isCurrentMember);
            List<ChatRoomMemberJpaEntity> allMembers = messageRepository.findMembersByRoomId(query.roomId());
            Long inMemberCount = (long) allMembers.size();

            //일대일의 경우 방이름 없음
            boolean isOneToOne = chatRoom.getRoomTitle() == null || chatRoom.getRoomTitle().trim().isEmpty();

            // 🎯 [나와의 채팅 검증용 프리로드] 로그인 유저가 참여한 '모든 방' 중 최초 생성된 방 ID 추출
            List<ChatRoomMemberJpaEntity> myAllRooms = messageRepository.findChatRoomMembersByUserId(query.userId());
            Long firstRoomId = myAllRooms.stream()
                    .map(m -> m.getRoomId().getId())
                    .min(Long::compare)
                    .orElse(-1L);

            //수강신청 내역
            List<EnrollmentWithFMJpaEntity> myEnrollments = messageRepository.findEnrollmentsByUserId(query.userId());

            //메시지 조회 기본 기준은 방 생성 시간(나간다 들어온 사람 아니라면 방 생성 시간이 chatRoomMember 테이블의 joinedAt과 같을 것)
            LocalDateTime messageVisibleStartTimeLine = chatRoom.getCreatedAt();

            // 내 가입일 정보 찾아서 타임라인 시작점 세팅
            for (ChatRoomMemberJpaEntity member : allMembers) {
                if (member.getUserId().getId().equals(query.userId())) {
                    if (!messageVisibleStartTimeLine.equals(member.getJoinedAt())) {
                        messageVisibleStartTimeLine = member.getJoinedAt();
                        log.info("[타임라인 필터] 재입장 유저 감지 - 멤버 가입일({}) 이후의 메시지만 조회합니다.", messageVisibleStartTimeLine);
                    }
                }
            }

            //메시지 내역, 안내 문구 내역 불러오기
            List<MessageJpaEntity> rawMessages = messageRepository.findMessageHistory(query.roomId(), query.lastMessageId(), messageVisibleStartTimeLine);

            //마지막 메시지의 시간은 메서드 호출 시점인 현재로 설정
            LocalDateTime endTimeLine = LocalDateTime.now();
            if (query.lastMessageId() != null) {
                endTimeLine = messageRepository.findLatestMessageTimeById(query.lastMessageId())
                        .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 메시지입니다."));
            }

            //멤버의 입장 시간과 마지막 메시지 사이의 안내 문구 내역 조회
            List<MessageAnnounceJpaEntity> rawAnnounces;

            //마지막 메시지 아이디가 없을 때 가입일~현재 사이의 모든 공지를 가져옴
            if (query.lastMessageId() == null) {
                rawAnnounces = messageRepository.findAnnounceHistory(query.roomId(), messageVisibleStartTimeLine, endTimeLine);
            } else {
                //스크롤 페이지: 메시지 중 가장 과거 시간 ~ 기준 메시지 시간 사이의 공지
                // 기준 메시지의 시간을 종료 시점으로 설정
                LocalDateTime baseMessageTime = messageRepository.findLatestMessageTimeById(query.lastMessageId())
                        .orElse(endTimeLine);
                endTimeLine = baseMessageTime;
                if (!rawMessages.isEmpty()) {
                    //맨 마지막이 가장 과거 메시지
                    messageVisibleStartTimeLine = rawMessages.get(rawMessages.size() - 1).getCreatedAt();
                }

                //마지막 메시지 아이디 있을 땐 마지막 메시지의 생성 시간 이후
                rawAnnounces = messageRepository.findAnnounceHistory(query.roomId(), messageVisibleStartTimeLine, endTimeLine);
            }

            //화면에 내려줄 리스트 선언
            List<MessageDetail> messagesView = new ArrayList<>();

            //하나의 루프 돌면서 멤버 정보를 하나씩 처리
            List<MemberInfoView> memberInfoViews = new ArrayList<>();

            //메시지
            for (MessageJpaEntity m : rawMessages) {
                Long senderId = m.getSenderId().getId();
                Boolean isMine = senderId.equals(query.userId());
                String friendStatus = isMine ? "me" : messageRepository.findFriendRelation(query.userId(), senderId)
                        .map(FriendJpaEntity::getStatus)
                        .orElse("none");

                //상대방 나감 여부. 메시지 내역엔 있는데 현재 채팅방 멤버가 아님
                Boolean isLeftRoom = allMembers.stream()
                        .noneMatch(mem -> mem.getUserId().getId().equals(senderId));

                //하나의 메시지를 읽지 않은 사람 수(공지는 읽음 수 없음)
                Long unreadCount = messageRepository.countUnreadMembersForMessage(m.getId());
                messagesView.add(new MessageDetail(
                        m.getId(),
                        senderId,
                        "TEACHER".equals(m.getSenderId().getRole()) ? m.getSenderId().getName() : null,
                        m.getSenderId().getNickname(),
                        m.getSenderId().getRole(),
                        friendStatus,
                        m.getContent(),
                        m.getCreatedAt(),
                        unreadCount,
                        isMine,
                        isLeftRoom,
                        m.getSenderId().getProfileImageUrl(),
                        null,
                        null
                ));
            }

            //안내 문구 내역(첫 페이지 조회 시에만 메시지 내역과 합치기)
            for (MessageAnnounceJpaEntity a : rawAnnounces) {
                messagesView.add(new MessageDetail(
                        a.getId(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        a.getContent(),
                        a.getCreatedAt(),
                        null,
                        null,
                        null,
                        null,
                        a.getTargetId().getId(),
                        a.getType()
                ));
            }

            //시간순으로 정렬
            messagesView.sort(Comparator.comparing(MessageDetail::createdAt));

            for (ChatRoomMemberJpaEntity member : allMembers) {
                UserWithFMJpaEntity targetUser = member.getUserId();

                //개별 친구 계산
                String friendStatus = messageRepository.findFriendRelation(query.userId(), targetUser.getId())
                        .map(FriendJpaEntity::getStatus)
                        .orElse("none");

                boolean isLeftRoom = false;

                // 나와의 채팅 혹은 상대방 퇴장 방 판별
                if (targetUser.getId().equals(query.userId()) && allMembers.size() == 1) {

                    // 목록에서 구한 최초 방 ID 조회 방식을 대용하기 위해, 메시지 역추적 진행
                    Optional<MessageJpaEntity> otherMsgOpt = messageRepository
                            .findLatestMessageExceptMe(query.roomId(), query.userId());
                    if (otherMsgOpt.isPresent()) {
                        targetUser = otherMsgOpt.get().getSenderId();
                        isLeftRoom = true;
                    } else {

                        //안내 문구 검사
                        Optional<UserWithFMJpaEntity> announceUserOpt = messageRepository.findLatestAnnounceUser(query.roomId());
                        if (announceUserOpt.isPresent()) {
                            targetUser = announceUserOpt.get();
                            isLeftRoom = true;
                        } else {
                            if (query.roomId().equals(firstRoomId)) {

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
                }

                // 4. 활성화 여부 정책 판별
                boolean shouldMasked = false;
                if (targetUser != null) {
                    shouldMasked = messageEligibilityPolicy.determineNotActive(targetUser, friendStatus, query.userId());
                }

                // 5. 강의명 리스트 추출
                List<String> lectureTitleList = new ArrayList<>();
                if (targetUser != null && !targetUser.getId().equals(query.userId()) &&
                        !("STUDENT".equals(loginUser.getRole()) && "STUDENT".equals(targetUser.getRole()))) {

                    if ("STUDENT".equals(loginUser.getRole())) {
                        for (EnrollmentWithFMJpaEntity enrollment : myEnrollments) {
                            LectureWithFMJpaEntity lecture = enrollment.getLectureId();
                            if (lecture.getTeacherId().getId().equals(targetUser.getId())) {
                                lectureTitleList.add(lecture.getTitle());
                            }
                        }
                    } else if ("TEACHER".equals(loginUser.getRole())) {

                        //이거나 myEnrollments나 똑같은 역할 아닌가? 변수명 하나로 처리해도 되는 거 아닌가?
                        List<EnrollmentWithFMJpaEntity> targetEnrollments = messageRepository.findEnrollmentsByUserId(targetUser.getId());
                        for (EnrollmentWithFMJpaEntity enrollment : targetEnrollments) {
                            LectureWithFMJpaEntity lecture = enrollment.getLectureId();
                            if (lecture.getTeacherId().getId().equals(query.userId())) {
                                lectureTitleList.add(lecture.getTitle());
                            }
                        }
                    }
                }

                //멤버 정보 조립(로그인 유저 제외)
                if (targetUser != null && !targetUser.getId().equals(query.userId())) {
                    memberInfoViews.add(new MemberInfoView(
                            targetUser.getId(),
                            "TEACHER".equals(targetUser.getRole()) ? targetUser.getName() : null,
                            targetUser.getNickname(),
                            lectureTitleList.isEmpty() ? null : String.join(",", lectureTitleList),
                            targetUser.getRole(),
                            friendStatus,
                            targetUser.getProfileImageUrl(),
                            isLeftRoom,
                            !"ACTIVE".equals(targetUser.getStatus()),
                            shouldMasked
                    ));
                }
            }

            RoomInfoView roomInfoView = new RoomInfoView(
                    query.roomId(),
                    inMemberCount,
                    isOneToOne ? null : chatRoom.getRoomTitle()
            );

            return List.of(new MessageHistoryView(
                    roomInfoView,
                    memberInfoViews,
                    messagesView
            ));
        });
    }
}