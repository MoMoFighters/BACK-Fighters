package com.wanted.momocity.message.infrastructure.persistence;


import com.wanted.momocity.friend.enrollment.EnrollmentWithFMJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//충돌 피하기 위해 메시지 기능에서 필요한 자동 쿼리 처리 공간(수강 테이블)
public interface MessageSideEnrollmentRepository extends JpaRepository<EnrollmentWithFMJpaEntity, Long> {

    List<EnrollmentWithFMJpaEntity> findByUserId_Id(Long userId);
}
