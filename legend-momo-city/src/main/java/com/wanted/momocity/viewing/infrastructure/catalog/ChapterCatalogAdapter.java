package com.wanted.momocity.viewing.infrastructure.catalog;

import com.wanted.momocity.viewing.application.port.ChapterPort;
import com.wanted.momocity.viewing.domain.model.Chapter;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

/*
* comment.
*   [역할]
*  catalog 컨텍스트 소유의 Chapter 를 READ 전용으로 조회
*  ChapterPort 인터페이스 구현체
*  -
*  [Redis 캐싱 전략]
 * @Cacheable("chapter")  → chapterId 기준 단건 캐싱
 * @Cacheable("chapters") → lectureId 기준 전체 챕터 목록 캐싱
 * -
 * 왜 캐싱이 필요한가:
 * -> saveProgress() 5~10초 주기 호출 시
 *   매번 DB 조회 → Redis 캐싱으로 DB 부하 감소
 * -
 * @CacheEvict:
 * -> 챕터 정보 변경 시 캐시 무효화 (팀원 머지 후 적용)
 * -> 현재는 Mock 데이터라 미사용
 * -
*  TODO : ChapterJpaRepository, ChapterJpaEntity 완성 후 주입 예정
* */

@Component
@RequiredArgsConstructor
public class ChapterCatalogAdapter implements ChapterPort {

    // TODO : ChapterJpaRepository 완성 후 주입 예정
    // private final ChapterJpaRepository chapterJpaRepository;

    /*
     * comment.
     *  @Cacheable("chapter")
     *  -> 처음 호출 시 DB 조회 후 Redis 에 저장
     *  -> 이후 호출 시 Redis 에서 반환 (DB 조회 없음)
     *  -> key = "chapter::1", "chapter::2" 형태로 저장
     */

    @Override
    @Cacheable(value = "chapter", key = "#chapterId")
    public Chapter findById(Long chapterId) {

        // TODO: 팀원 머지 후 실제 DB 조회로 교체
        // return chapterJpaRepository.findById(chapterId)
        //         .map(ChapterJpaEntity::toDomain)
        //         .orElseThrow(() -> new DomainRuleViolationException("챕터를 찾을 수 없습니다."));

        return Chapter.reconstitute(
                chapterId,
                1L,
                "임시 챕터 " + chapterId,
                1,
                "video/lecture1/chapter" + chapterId + ".mp4",
                600,
                Chapter.VideoStatus.READY
        );

    }

    /*
     * comment.
     *  @Cacheable("chapters")
     *  -> 처음 호출 시 강의 전체 챕터 목록 DB 조회 후 Redis 에 저장
     *  -> 이후 호출 시 Redis 에서 반환
     *  -> key = "chapters::1", "chapters::2" 형태로 저장
     */
    @Override
    @Cacheable(value = "chapters", key = "#lectureId")
    public List<Chapter> findAllByLectureId(Long lectureId) {

        // TODO: 팀원 머지 후 실제 DB 조회로 교체
        // return chapterJpaRepository.findAllByLectureId(lectureId)
        //         .stream()
        //         .map(ChapterJpaEntity::toDomain)
        //         .toList();

        return List.of(
                Chapter.reconstitute(1L, lectureId, "1강 임시 챕터", 1,
                        "video/lecture1/chapter1.mp4", 600, Chapter.VideoStatus.READY),
                Chapter.reconstitute(2L, lectureId, "2강 임시 챕터", 2,
                        "video/lecture1/chapter2.mp4", 600, Chapter.VideoStatus.READY),
                Chapter.reconstitute(3L, lectureId, "3강 임시 챕터", 3,
                        "video/lecture1/chapter3.mp4", 600, Chapter.VideoStatus.READY)
        );
    }

}