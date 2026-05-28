package com.wanted.momocity.viewing.infrastructure.catalog;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.viewing.application.port.LecturePort;
import com.wanted.momocity.viewing.domain.model.Lecture;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/*
* comment.
*  [역할]
*  LecturePort 인터페이스 구현체
*  catalog 컨텍스트 소유의 Lecture 를 READ 전용으로 조회
*  -
*  [Redis 캐싱 전략]
 * @Cacheable("lecture") → lectureId 기준 단건 캐싱
 * -
 * 왜 캐싱이 필요한가:
 * -> getLectureMeta(), getMyLectures() 등 매번 DB 조회
 * -> Redis 캐싱으로 DB 부하 감소
 * -> 강의 정보는 자주 바뀌지 않아 캐싱 효과 극대화
 * -
 * TODO: 팀원 LectureJpaRepository 완성 후
 *       실제 DB 조회 코드로 교체
* */

@Component
public class LectureCatalogAdapter implements LecturePort {

    // TODO : LectureJpaRepository 완성 후 주입
    // private final LectureJpaRepository lectureJpaRepository;

    /*
     * comment.
     *  @Cacheable("lecture")
     *  -> 처음 호출 시 DB 조회 후 Redis 에 저장
     *  -> 이후 호출 시 Redis 에서 반환 (DB 조회 없음)
     *  -> key = "lecture::1", "lecture::2" 형태로 저장
     */

    @Override
    @Cacheable(value = "lecture", key = "#lectureId")
    public Lecture findById(Long lectureId) {

        // TODO: 팀원 머지 후 실제 DB 조회로 교체
        // return lectureJpaRepository.findById(lectureId)
        //         .map(entity -> {
        //             String instructorName = userJpaRepository
        //                     .findById(entity.getTeacherId())
        //                     .map(UserJpaEntity::getName)
        //                     .orElseThrow(() -> new DomainRuleViolationException("강사를 찾을 수 없습니다."));
        //             return Lecture.reconstitute(
        //                     entity.getId(), entity.getTeacherId(),
        //                     entity.getTitle(), entity.getThumbnailUrl(),
        //                     entity.getCategory(), instructorName
        //             );
        //         })
        //         .orElseThrow(() -> new DomainRuleViolationException("강의를 찾을 수 없습니다."));

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
