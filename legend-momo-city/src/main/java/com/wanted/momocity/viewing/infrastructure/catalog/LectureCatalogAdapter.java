package com.wanted.momocity.viewing.infrastructure.catalog;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.viewing.application.port.LecturePort;
import com.wanted.momocity.viewing.domain.model.Lecture;
import org.springframework.stereotype.Component;

/*
* comment.
*  LecturePort 인터페이스 구현체
*  catalog 컨텍스트 소유의 Lecture 를 READ 전용으로 조회
* */

@Component
public class LectureCatalogAdapter implements LecturePort {

    // LectureJpaRepository 완성 후 주입
    // private final LectureJpaRepository lectureJpaRepository;

    @Override
    public Lecture findById(Long lectureId) {
        return Lecture.reconstitute(
                lectureId,
                1L,
                "임시 강의 " + lectureId,
                "https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png",
                "HEALTH",
                "임시 강사"
        );
    }
}
