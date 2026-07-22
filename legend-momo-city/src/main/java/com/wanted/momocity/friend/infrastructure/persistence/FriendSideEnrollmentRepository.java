package com.wanted.momocity.friend.infrastructure.persistence;


import com.wanted.momocity.friend.enrollment.EnrollmentWithFMJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

//충돌 피하기 위해 친구 기능에서 필요한 자동 쿼리 처리 공간(수강 테이블)
public interface FriendSideEnrollmentRepository extends JpaRepository<EnrollmentWithFMJpaEntity, Long> {

    // [최적화] 기존 메서드명 그대로 유지하고, 수강 신청을 긁어올 때 연관된 강의(lectureId)까지 한방에 땡겨오기!
    @Query("select e from FMEnrollment e " +
            "join fetch e.lectureId " +
            "where e.userId.id = :userId")
    List<EnrollmentWithFMJpaEntity> findByUserId_Id(@Param("userId") Long userId);
}
