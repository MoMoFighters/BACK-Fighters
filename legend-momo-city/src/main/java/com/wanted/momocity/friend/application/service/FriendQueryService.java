package com.wanted.momocity.friend.application.service;


import com.wanted.momocity.friend.application.usecase.FriendQueryUseCase;
import com.wanted.momocity.friend.domain.repository.FriendRepository;
import com.wanted.momocity.friend.enrollment.EnrollmentWithFMJpaEntity;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.infrastructure.persistence.FriendSideEnrollmentRepository;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.friend.lecture.LectureWithFMJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

//포트가 만든 문을 통해 기능 처리
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FriendQueryService implements FriendQueryUseCase {

    private final FriendRepository friendRepository;
    //충돌 회피로 친구 기능 관련 수강 테이블 인테페이스 저장소
    private final FriendSideEnrollmentRepository friendSideEnrollmentRepository;

    @Override
    public List<FriendView> handle(Long userId) {
        log.info("[FriendQueryService] 친구 목록 조회 요청 진입 - 조회 요청 유저ID: {}", userId);

        //어댑터와 타 영역 저장소로부터 날 것의 데이터 로드
        List<FriendJpaEntity> friends = friendRepository.findFriendsByUserIdAndStatus(userId, "FRIEND");
        //DB에서 친구 행을 몇 개나 긁어왔는지 확인
        log.info("[FriendQueryService] DB 친구 데이터 로드 완료 - 찾아낸 친구 수: {}개", friends.size());

        List<EnrollmentWithFMJpaEntity> myEnrollments = friendSideEnrollmentRepository.findByUserId_Id(userId);
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
            UserWithFMJpaEntity friendUser = friend.getFromUserId().getId().equals(userId) ? friend.getToUserId() : friend.getFromUserId();

            //로그인한 유저와 친구인 강사의 수강중인 강의명
            List<String> lectureTitleList = new ArrayList<>();

            if("ACTIVE".equals(friendUser.getStatus()) && "TEACHER".equals(friendUser.getRole())) {
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
    }
}
