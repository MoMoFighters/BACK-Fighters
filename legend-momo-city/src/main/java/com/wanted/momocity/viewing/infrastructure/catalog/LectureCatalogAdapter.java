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

//        LectureJpaEntity lectureJpaEntity =
//                lectureJpaRepository.findById(lectureId)
//                .orElseThrow(() ->
//                        new DomainRuleViolationException("강의를 찾을 수 없습니다."));
//
//        String instructorName =
//                userJpaRepository.findById(lectureEntity.getTeacherId())
//                        .map(UserJpaEntity::getName)
//                        .orElseThrow(() -> new DomainRuleViolationException("강사를 찾을 수 없습니다."));
//
//        return Lecture.reconstitute(
//                lectureEntity.getId(),
//                lectureEntity.getTeacherId(),
//                lectureEntity.getTitle(),
//                lectureEntity.getThumbnailUrl(),
//                lectureEntity.getCategory(),
//                instructorName
//        );

        throw new UnsupportedOperationException("구현 예정");
    }
}
