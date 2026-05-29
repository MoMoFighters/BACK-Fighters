package com.wanted.momocity.friend.application.service;

import com.wanted.momocity.friend.application.policy.FriendEligibilityPolicy;
import com.wanted.momocity.friend.domain.model.Friend;
import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.catalog.CatalogFriendAdapter;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.infrastructure.persistence.FriendSideLectureRepository;
import com.wanted.momocity.friend.infrastructure.persistence.FriendSideUserRepository;
import com.wanted.momocity.friend.lecture.LectureWithFMJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FriendHandlerService {
    private final FriendSideUserRepository friendSideUserRepository;
    private final CatalogFriendAdapter catalogFriendAdapter;
    private final FriendSideLectureRepository friendSideLectureRepository;

    //친구 쪽으로 발행된 이벤트를 핸들링하는 서비스
    public void createAndSaveTeacherFriendRelation(Long studentId, Long lectureId) {
        log.info("[FriendHandlerService] 강사-학생 자동 친구 추가 로직 시작");

        //강의 테이블에서 강의 있는지 확인
        LectureWithFMJpaEntity lecture = friendSideLectureRepository.findById(lectureId)
                .orElseThrow(() -> new DomainRuleViolationException("존재하지 않는 강의입니다."));

        //강의 테이블에서 강사 아이디 꺼내기
        UserWithFMJpaEntity teacher = lecture.getTeacherId();
        Long teacherId = teacher.getId();
        log.info("[FriendHandlerService] 강의 테이블 분석 완료 - 담당 강사ID: {}", teacherId);

        //중복 검증(이미 친구인지 확인)
        Optional<FriendJpaEntity> existingRelation = catalogFriendAdapter.findRelationBetween(studentId, teacherId);

        if (existingRelation.isPresent()) {
            FriendJpaEntity relation = existingRelation.get();
            //만약 친구(FRIEND)라면 조용히 성공 취급
            if ("FRIEND".equals(relation.getStatus())) {
                log.info("[FriendHandlerService] 이미 두 사람은 친구 관계입니다. 행 추가를 생략합니다.");
                return;
            }

//            //만약 다른 상태라면 정책 확인
//            friendEligibilityPolicy.ensureEligible(existingRelation);
        }

        UserWithFMJpaEntity student = friendSideUserRepository.findById(studentId)
                .orElseThrow(() -> new DomainRuleViolationException("해당 학생 유저를 찾을 수 없습니다. ID: " + studentId));

        //순수 도메인 모델을 먼저 탄생시킴
        Friend domainFriend = Friend.createTeacherStudentRelation(student.getId(), teacher.getId());

        //영속성 엔티티 생성
        FriendJpaEntity newFriendRelation = FriendJpaEntity.createTeacherStudentRelation(
                student,
                teacher,
                domainFriend.getStatus().name() //FRIEND
        );

        catalogFriendAdapter.save(newFriendRelation);
        log.info("[FriendHandlerService] 중복 없음 확인 완료 - 어댑터를 통해 friend 테이블에 강사-학생 자동 친구 행 추가 완료");
    }

}
