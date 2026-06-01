package com.wanted.momocity.friend.application.service;


import com.wanted.momocity.friend.application.usecase.FindUserQueryUseCase;
import com.wanted.momocity.friend.domain.repository.FriendRepository;
import com.wanted.momocity.friend.enrollment.EnrollmentWithFMJpaEntity;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.infrastructure.persistence.FriendSideEnrollmentRepository;

import com.wanted.momocity.friend.lecture.LectureWithFMJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FindUserQueryService implements FindUserQueryUseCase {
    private final FriendRepository friendRepository;
    private final FriendSideEnrollmentRepository friendSideEnrollmentRepository;

    @Override
    public List<FindView> handle(Long userId, String findNickname) {
        log.info("[FindUserQueryService] 사용자 검색 시작 - 요청자ID: {}, 검색 키워드: '{}'", userId, findNickname);

        //어댑터들로부터 가공되지 않은 순수 데이터 로드
        List<UserWithFMJpaEntity> foundUsers = friendRepository.findUsersByNicknameKeyword(findNickname);
        List<FriendJpaEntity> myRelations = friendRepository.findAllMyRelations(userId);
        List<EnrollmentWithFMJpaEntity> myEnrollments = friendSideEnrollmentRepository.findByUserId_Id(userId);

        log.info("[FindUserQueryService] 원본 데이터 로드 완료 - 검색된 총 유저: {}명, 나와 엮인 전체 관계: {}건", foundUsers.size(), myRelations.size());

        //로그인한 유저의 친구 관계들을 상대방 유저ID를 key로하는 Map으로 변환(매칭 속도 향상)
        Map<Long, FriendJpaEntity> relationMap = myRelations.stream()
                .collect(Collectors.toMap(
                        relation -> relation.getFromUserId().getId().equals(userId) ? relation.getToUserId().getId() : relation.getFromUserId().getId(),
                        relation -> relation,
                        (existing, replacement) -> existing //혹시 모를 중복 데이터 방어
                ));

        List<FindView> result = new ArrayList<>();

        //검색된 전체 사용자를 기준으로 한번 루프 돌기(친구가 아니어도 나옴)
        for (UserWithFMJpaEntity targetUser : foundUsers) {

            //로그인한 유저 본인은 제외
            if (targetUser.getId().equals(userId)) continue;

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
            if ("ACTIVE".equals(targetUser.getStatus()) && "TEACHER".equals(targetUser.getRole()) && "FRIEND".equals(status)) {
                for (EnrollmentWithFMJpaEntity enrollment : myEnrollments) {
                    LectureWithFMJpaEntity lecture = enrollment.getLectureId();

                    //로그인한 유저와 친구인 강사와 연결된 수강 강의명
                    if (lecture.getTeacherId().getId().equals(targetUser.getId())) {
                        lectureTitleList.add(lecture.getTitle());
                    }
                }
            }

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
        return result;
    }
}
