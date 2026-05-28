package com.wanted.momocity.viewing.infrastructure.catalog;

import com.wanted.momocity.viewing.application.port.EnrollmentPort;
import com.wanted.momocity.viewing.application.port.EnrollmentPort.EnrollmentInfo;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/*
* comment.
*  Enrollment 인터페이스 구현체
*  enrollment 컨텍스트 소유의 수강 정보를 READ 전용으로 조회
* */

@Component
public class EnrollmentCatalogAdapter implements EnrollmentPort{

    @Override
    public Optional<EnrollmentInfo> findByUserIdAndLectureId(
            Long userId, Long lectureId
    ) {
        return Optional.of(new EnrollmentInfo(1L, userId, lectureId));
    }

    @Override
    public List<EnrollmentInfo> findAllByUserId(Long userId) {
        return List.of(
                new EnrollmentInfo(1L, userId, 1L),
                new EnrollmentInfo(2L, userId, 2L)
        );
    }

}
