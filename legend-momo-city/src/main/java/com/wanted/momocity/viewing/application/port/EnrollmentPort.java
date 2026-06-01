package com.wanted.momocity.viewing.application.port;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  enrollment 컨텍스트 소유의 수강 정보를 READ 전용으로 조회
 *  viewing 컨텍스트가 enrollment 를 직접 참조하지 않고 이 포트를 통해서만 접근
 *  실제 구현체 : infrastructure.catalog.EnrollmentCatalogAdapter
 * */

public interface EnrollmentPort {

    // 수강 여부 확인
    // 수강 신청된 강의인지 체크할 때 사용
   Optional<EnrollmentInfo> findByUserIdAndLectureId (Long userId, Long lectureId);

    // 내 수강 강의 전체 목록 조회
    // 마이페이지 수강 목록 조회할 때 사용
   List<EnrollmentInfo> findAllByUserId (Long userId);

    // EnrollmentPort 전용 응답 모델
    // 팀원 Enrollment 도메인 모델을 직접 참조하지 않기 위해
    // viewing 컨텍스트 전용으로 필요한 필드만 담음
   record EnrollmentInfo(
           Long enrollmentId,
           Long userId,
           Long lectureId,
           LocalDateTime enrolledAt
   ) {}

}
