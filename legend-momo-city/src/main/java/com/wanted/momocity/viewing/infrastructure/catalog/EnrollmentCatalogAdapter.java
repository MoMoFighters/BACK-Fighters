package com.wanted.momocity.viewing.infrastructure.catalog;

import com.wanted.momocity.enrollment.infrastructure.persistence.EnrollmentJpaRepository;
import com.wanted.momocity.viewing.application.port.EnrollmentPort;
import com.wanted.momocity.viewing.application.port.EnrollmentPort.EnrollmentInfo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/*
* comment.
*  Enrollment 인터페이스 구현체
*  enrollment 컨텍스트 소유의 수강 정보를 READ 전용으로 조회
*  -
*  [역할]
 * enrollment 컨텍스트 소유의 수강 정보를 READ 전용으로 조회
 * -> EnrollmentJpaRepository 주입해서 실제 DB 조회
 * -> EnrollmentJpaEntity → EnrollmentInfo 로 변환
 * -
 * [왜 Port 로 분리했는가]
 * enrollment 테이블은 팀원 소유
 * -> 직접 enrollment 테이블 건드리면 안 됨
 * -> EnrollmentPort 인터페이스로 SELECT 만 허용
 * -> 수강 여부 확인, 수강 목록 조회만 가능
 * -
 * [enrolledAt]
 * EnrollmentJpaEntity 에 enrolledAt 있음
 * → 추후 EnrollmentInfo 에 추가 예정
* */

@Component
@RequiredArgsConstructor
public class EnrollmentCatalogAdapter implements EnrollmentPort{

    // 팀원 EnrollmentJpaRepository 주입
    private final EnrollmentJpaRepository enrollmentJpaRepository;

    /*
     * 특정 사용자의 특정 강의 수강 여부 조회
     * → EnrollmentAccessPolicy 에서 수강 여부 확인할 때 사용
     * → 수강 신청 안 했으면 Optional.empty() 반환
     * → ViewingAccessDeniedException 발생 → 403
     */

    @Override
    public Optional<EnrollmentInfo> findByUserIdAndLectureId(
            Long userId, Long lectureId
    ) {
        return enrollmentJpaRepository
                .findByUserIdAndLectureId(userId, lectureId)
                .map(entity -> new EnrollmentInfo(
                        entity.getId(),
                        entity.getUserId(),
                        entity.getLectureId(),
                        entity.getEnrolledAt()
                ));
    }

    /*
     * 특정 사용자의 전체 수강 강의 목록 조회
     * → getMyLectures() 에서 수강 목록 가져올 때 사용
     * → 수강 강의 없으면 빈 리스트 반환
     * → ViewingQueryService.getMyLectures() 에서
     *   lectureId 기준으로 강의 정보 추가 조회
     */

    @Override
    public List<EnrollmentInfo> findAllByUserId(Long userId) {
        return enrollmentJpaRepository
                .findAllByUserId(userId)
                .stream()
                .map(entity -> new EnrollmentInfo(
                        entity.getId(),
                        entity.getUserId(),
                        entity.getLectureId(),
                        entity.getEnrolledAt()
                ))
                .toList();
    }

}
