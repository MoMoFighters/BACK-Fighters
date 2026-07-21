package com.wanted.momocity.friend.application.service;


import com.wanted.momocity.friend.application.metric.FriendMetrics;
import com.wanted.momocity.friend.application.query.*;
import com.wanted.momocity.friend.application.usecase.FriendQueryUseCase;
import com.wanted.momocity.friend.domain.repository.FriendRepository;
import com.wanted.momocity.friend.enrollment.EnrollmentWithFMJpaEntity;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.infrastructure.persistence.GuestBookJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.friend.lecture.LectureWithFMJpaEntity;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//포트가 만든 문을 통해 기능 처리
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FriendQueryService implements FriendQueryUseCase {

    private final FriendRepository friendRepository;
    //메트릭
    private final FriendMetrics friendMetrics;
    //친구 목록 조회
    @Override
    public List<FriendView> getFriendQueryHandle(GetFriendQuery query) {
        // 1. 타이머 측정 시작
        return friendMetrics.getFriendListTimer().record(() -> {
            log.info("[FriendQueryService] 친구 목록 조회 요청 진입 - 조회 요청 유저ID: {}", query.userId());

            //어댑터와 타 영역 저장소로부터 날 것의 데이터 로드
            List<FriendJpaEntity> friends = friendRepository.findFriendsByUserIdAndStatus(query.userId(), "FRIEND");
            //DB에서 친구 행을 몇 개나 긁어왔는지 확인
            log.info("[FriendQueryService] DB 친구 데이터 로드 완료 - 찾아낸 친구 수: {}개", friends.size());

            List<EnrollmentWithFMJpaEntity> myEnrollments = friendRepository.findEnrollmentsByUserId(query.userId());
            log.debug("[FriendQueryService] 로그인 유저의 수강 신청 건수: {}개", myEnrollments.size());

            List<FriendView> result = new ArrayList<>();

            //프론트에 보낼 데이터 가공
            for (FriendJpaEntity friend : friends) {
                //status가 FRIEND가 아니면 다음 루프로 넘김
                if (!"FRIEND".equals(friend.getStatus())) {
                    log.info("[FriendQueryService] FRIEND 상태가 아닌 행 필터링 - 현재 상태: {}", friend.getStatus());
                    continue;
                }

                //상대방 유저 객체 발라내기
                UserWithFMJpaEntity friendUser = friend.getFromUserId().getId().equals(query.userId()) ? friend.getToUserId() : friend.getFromUserId();

                //로그인한 유저와 친구인 강사의 수강중인 강의명
                List<String> lectureTitleList = new ArrayList<>();

                if ("ACTIVE".equals(friendUser.getStatus()) && "TEACHER".equals(friendUser.getRole())) {
                    //로그인한 유저의 수강 내역 중 친구인 강사 ID와 일치하는 강의 교집합 찾기
                    for (EnrollmentWithFMJpaEntity enrollment : myEnrollments) {
                        LectureWithFMJpaEntity lecture = enrollment.getLectureId();

                        //강의의 강사ID와 내 친구인 강사ID가 일치하는 지 확인
                        if (lecture.getTeacherId().getId().equals(friendUser.getId())) {
                            lectureTitleList.add(lecture.getTitle()); //일치하면 강의명 담기
                        }
                    }
                }

                //최종 가공된 결과
                result.add(new FriendView(
                        friendUser.getId(),
                        friendUser.getName(),
                        friendUser.getNickname(),
                        friendUser.getRole(),
                        friend.getStatus(), //친구 상태
                        !"ACTIVE".equals(friendUser.getStatus()), //비활성 여부(user 테이블)
                        lectureTitleList,
                        friendUser.getProfileImageUrl()
                ));
            }

            //가공이 끝나고 최종 리턴하기 직전 기록
            log.info("[FriendQueryService]최종 친구 목록 가공 완료 - 반환할 DTO 개수: {}개", result.size());

            return result;
        });
    }

    //사용자 검색
    @Override
    public List<FindView> findUserQueryHandle(FindUserQuery query) {
        log.info("[FindUserQueryService] 사용자 검색 시작 - 요청자ID: {}, 검색 키워드: '{}'", query.userId(), query.findNickname());

        //시작 시점에 타이머 스타트!
        Timer.Sample sample = io.micrometer.core.instrument.Timer.start();

        //어댑터들로부터 가공되지 않은 순수 데이터 로드
        List<UserWithFMJpaEntity> foundUsers = friendRepository.findUsersByNicknameKeyword(query.findNickname());
        List<FriendJpaEntity> myRelations = friendRepository.findAllMyRelations(query.userId());

        log.info("[FindUserQueryService] 원본 데이터 로드 완료 - 검색된 총 유저: {}명, 나와 엮인 전체 관계: {}건", foundUsers.size(), myRelations.size());

        //로그인한 유저의 친구 관계들을 상대방 유저ID를 key로하는 Map으로 변환(매칭 속도 향상)
        Map<Long, FriendJpaEntity> relationMap = myRelations.stream()
                .collect(Collectors.toMap(
                        relation -> relation.getFromUserId().getId().equals(query.userId()) ? relation.getToUserId().getId() : relation.getFromUserId().getId(),
                        relation -> relation,
                        (existing, replacement) -> existing //혹시 모를 중복 데이터 방어
                ));

        List<FindView> result = new ArrayList<>();

        //검색된 전체 사용자를 기준으로 한번 루프 돌기(친구가 아니어도 나옴)
        for (UserWithFMJpaEntity targetUser : foundUsers) {

            //로그인한 유저 본인은 제외
            if (targetUser.getId().equals(query.userId())) continue;

            //관리자 역할은 검색 결과에서 제외
            if ("ADMIN".equals(targetUser.getRole())) {
                log.info("[FindUserQueryService] 보안 정첵(ADMIN)에 따른 유저 노출 제외 - 대상 유저 ID: {}", targetUser.getId());
                continue;
            }

            //친구 상태 알아내기 (없으면 none으로 가공)
            String status = "none";
            if (relationMap.containsKey(targetUser.getId())) {
                status = relationMap.get(targetUser.getId()).getStatus();
            }

            //차단 상태인 유저는 노출 안됨
            if ("BLOCK".equals(status)) {
                log.info("[FindUserQueryService] 기획 정책(차단)에 따른 유저 노출 제외 - 대상 유저ID: {}", targetUser.getId());
                continue;
            }

            String originalNickname = targetUser.getNickname();

            //강사 쪽에는 강의명
            List<String> lectureTitleList = new ArrayList<>();

            //내부 전용 주머니(FinView)에 결과 담기
            result.add(new FindView(
                    targetUser.getId(),
                    targetUser.getName(),
                    originalNickname,
                    status,
                    targetUser.getRole(),
                    !"ACTIVE".equals(targetUser.getStatus()), //비활성 여부
                    lectureTitleList,
                    targetUser.getProfileImageUrl()
            ));
        }

        log.info("[FindUserQueryService] 사용자 검색 가공 완료 - 최종 반환 결과: {}개", result.size());

        //return 직전에 딱 멈추고 시간 기록하기!
        sample.stop(friendMetrics.getUserSearchTimer());

        return result;
    }

    //보낸 친구 요청 목록
    @Override
    public List<SentRequestView> getSentRequestFriendQueryHandle(SentRequestQuery query) {
        log.info("[GetSentRequestFriendQueryService] 보낸 친구 요청 목록 조회 요청 진입 - 조회 요청 유저ID: {}", query.userId());

        //로그인한 유저가 보낸 SENT 행
        List<FriendJpaEntity> friends = friendRepository.findSentRequestsByFromUserId(query.userId(), "SENT");
        log.info("[GetSentRequestFriendQueryService] DB 보낸 친구 요청 데이터 로드 완료 - 찾아낸 행 수: {}개", friends.size());

        List<SentRequestView> result = new ArrayList<>();

        for (FriendJpaEntity friend : friends) {
            //SENT가 아니면 넘어가기
            if (!"SENT".equals(friend.getStatus())) {
                continue;
            }

            //상대방 유저 객체는 toUserId
            UserWithFMJpaEntity targetUser = friend.getToUserId();

            //강사는 보낸 친구 요청 목록에 뜨면 안됨
            if ("TEACHER".equals(targetUser.getRole())) {
                log.info("[GetSentRequestFriendQueryService] TEACHER 역할 유저 필터링 - 강사ID: {}", targetUser.getId());
                continue;
            }

            //결과 리스트에 담기
            result.add(new SentRequestView(
                    targetUser.getId(),
                    targetUser.getNickname(),
                    targetUser.getRole(),
                    friend.getStatus(),
                    !"ACTIVE".equals(targetUser.getStatus()),
                    targetUser.getProfileImageUrl()
            ));
        }

        log.info("[GetSentRequestFriendQueryService] 최종 보낸 친구 요청 목록 가공 완료 - 반환할 개수: {}개", result.size());
        return result;
    }

    //받은 친구 요청 목록
    @Override
    public List<ReceivedRequestView> getReceivedRequestFriendQueryHandle(ReceivedRequestQuery query) {
        log.info("[GetReceivedRequestFriendQueryService] 받은 친구 요청 목록 조회 시작 - 수신자(로그인 유저): {}", query.userId());

        //toUserId가 로그인 유저이면서 SENT인 데이터만 가져옴
        List<FriendJpaEntity> requests = friendRepository.findReceivedRequestsByToUserId(query.userId(), "SENT");
        log.info("[GetReceivedRequestFriendQueryService] DB 받은 친구 요청 데이터 로드 완료 - 찾아낸 행 수: {}", requests.size());

        //결과 담을 곳
        List<ReceivedRequestView> result = new ArrayList<>();

        //하나씩 add
        for (FriendJpaEntity request : requests) {
            //SENT가 아니면 넘어감
            if (!"SENT".equals(request.getStatus())) {
                continue;
            }

            //요청 보낸 사람 추출
            UserWithFMJpaEntity fromUser = request.getFromUserId();

            result.add(new ReceivedRequestView(
                    fromUser.getId(),
                    fromUser.getNickname(),
                    fromUser.getRole(),
                    request.getStatus(),
                    !"ACTIVE".equals(fromUser.getStatus()), //활성 유저 아니면 true
                    fromUser.getProfileImageUrl()
            ));
        }

        log.info("[GetReceivedRequestFriendQueryService] 최종 받은 친구 요청 목록 가공 완료 - 반환할 개수: {}개", result.size());

        //요청 보낸 사람 정보 담기
        return result;
    }

    //친구 차단 목록
    @Override
    public List<BlockedView> getBlockedFriendQueryHandle(BlockedFriendQuery query) {
        log.info("[GetBlockedFriendQueryService] 내가 차단한 유저 목록 조회 시작 - 유저ID: {}", query.userId());

        //로그인 유저와 연관된 모든 관계 행 가져오기
        List<FriendJpaEntity> allRelations = friendRepository.findAllMyRelations(query.userId());
        List<BlockedView> blockedViews = new ArrayList<>();

        for (FriendJpaEntity relation : allRelations) {
            //BLOCK 상태만 가져오기
            if (!"BLOCK".equals(relation.getStatus())) {
                continue;
            }

            //방어막(로그인 유저가 toUser인 상태에서 BLOCK일 때만 띄움)
            if (relation.getToUserId().getId().equals(query.userId())) {
                log.info("[GetBlockedFriendQueryService] 상대방이 나를 차단한 행이므로 노출 제외 - 관계ID: {}", relation.getId());
                continue;
            }

            //로그인한 유저가 차단한 상대방 누구인지.
            UserWithFMJpaEntity targetUser;

            //로그인 유저가 From이면 상대는 To, 로그인 유저가 To면 상대가 From
            if (relation.getFromUserId().getId().equals(query.userId())) {
                targetUser = relation.getToUserId();
            } else {
                targetUser = relation.getFromUserId();
            }

            blockedViews.add(new BlockedView(
                    targetUser.getId(),
                    targetUser.getNickname(),
                    targetUser.getRole(),
                    relation.getStatus(),
                    !"ACTIVE".equals(targetUser.getStatus()),
                    targetUser.getProfileImageUrl()
            ));
        }

        log.info("[GetBlockedFriendQueryService] 내가 차단한 유저 목록 조회 완료 - 총 {}명", blockedViews.size());
        return blockedViews;
    }

    //강사, 비활성 유저 제외 친구 목록
    @Override
    public List<StudentFriendsView> getStudentFriendsQueryHandle(GetStudentFriendsQuery query) {

        List<FriendJpaEntity> friends = friendRepository.findFriendsByUserIdAndStatus(query.userId(), "FRIEND");

        List<StudentFriendsView> result = new ArrayList<>();

        for (FriendJpaEntity friend : friends) {
            // FRIEND 상태는 쿼리 단계에서 보장되나 명시적 안전장치 유지 가능
            if (!"FRIEND".equals(friend.getStatus())) {
                continue;
            }

            UserWithFMJpaEntity friendUser = friend.getFromUserId().getId().equals(query.userId())
                    ? friend.getToUserId()
                    : friend.getFromUserId();

            //강사 제외하기
            if (!"STUDENT".equals(friendUser.getRole())) {
                continue;
            }
            //비활성 유저 제외(다대다에서 비활성 유저 개설/초대 불가, 친구 도시 놀러가기)
            if (!"ACTIVE".equals(friendUser.getStatus())) {
                log.info("[GetStudentFriendsQueryService] 비활성 학생 친구 제외 - 유저ID:{}", friendUser.getId());
                continue;
            }

            result.add(new StudentFriendsView(
                    friendUser.getId(),
                    friendUser.getNickname(),
                    friendUser.getRole(),
                    friend.getStatus(),
                    friendUser.getProfileImageUrl()
            ));
        }

        log.info("[GetStudentFriendsQueryService] 학생 친구 목록 가공 완료 - 최종 반환 개수:{}개", result.size());
        return result;
    }

    //방명록 목록 조회
    @Override
    public List<GuestBooksView> getGuestBooksQueryHandle(GetGuestBooksQuery query) {
        log.info("[GuestBookQueryService] 방명록 목록 조회 시작 - 조회 요청자ID: {}, 호출 위치ID: {}", query.userId(), query.cityOwnerId());

        Long loginUserId = query.userId();
        Long cityOwnerId = query.cityOwnerId();

        List<GuestBookJpaEntity> guestBooks;

        // 2. 최신 데이터 전체 조회 (Fetch Join)
        //요청자와 도시 위치가 같다면(본인 도시에서 남이 쓴 방명록 조회) ownerId가 로그인 유저인 것
        if (loginUserId.equals(cityOwnerId)) {
            // 내 도시 -> 남이 나에게 남긴 방명록 전체 조회
            log.info("[GuestBookQueryService] 내 도시 방명록 조회 - 주인ID: {}", loginUserId);

            guestBooks = friendRepository.findAllByOwnerIdWithWriter(query.userId());
        } else {
            //요청자와 도시 위치가 다르면(본인이 해당 도시에 남긴 방명록 조회) ownerId가 cityOwnerId이면서 writerId가 로그인 유저인 것
            // 남의 도시 -> 내가 그 사람 도시에 남긴 방명록만 조회
            log.info("[GuestBookQueryService] 남의 도시 방명록 조회 - 로그인 유저:{}, 도시 주인:{}", loginUserId, cityOwnerId);

            guestBooks = friendRepository.findAllByWriterIdAndOwnerIdWithWriter(loginUserId, cityOwnerId);
        }

        // 3. 뷰 모델(record)로 매핑하여 반환
        return guestBooks.stream()
                .map(gb -> new GuestBooksView(
                        gb.getId(),
                        gb.getWriterId().getId(),
                        gb.getWriterId().getNickname(), // 페치 조인했으므로 N+1 문제 없음!
                        gb.getContent(),
                        gb.getCreatedAt()
                ))
                .toList();
    }
}
