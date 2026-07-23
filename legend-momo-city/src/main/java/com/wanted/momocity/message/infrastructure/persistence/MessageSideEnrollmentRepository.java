package com.wanted.momocity.message.infrastructure.persistence;


import com.wanted.momocity.friend.enrollment.EnrollmentWithFMJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

//충돌 피하기 위해 메시지 기능에서 필요한 자동 쿼리 처리 공간(수강 테이블)
public interface MessageSideEnrollmentRepository extends JpaRepository<EnrollmentWithFMJpaEntity, Long> {

    // [패치 조인 적용] 로그인 유저의 수강 이력을 긁어올 때,
    // 연관된 강의(lectureId)와 강의를 만든 강사 정보까지 한 번에 패치 조인으로 묶어서 가져옵니다.
    @Query("select e from FMEnrollment e " +
            "join fetch e.lectureId l " +
            "join fetch l.teacherId " +
            "where e.userId.id = :userId")
    List<EnrollmentWithFMJpaEntity> findByUserId_Id(@Param("userId") Long userId);

    //채팅방 목록 조회 개선 보강
    // MessageSideEnrollmentRepository
    @Query("select e from FMEnrollment e join fetch e.lectureId l join fetch l.teacherId where e.userId.id in :userIds")
    List<EnrollmentWithFMJpaEntity> findByUserId_IdIn(@Param("userIds") List<Long> userIds);
}
