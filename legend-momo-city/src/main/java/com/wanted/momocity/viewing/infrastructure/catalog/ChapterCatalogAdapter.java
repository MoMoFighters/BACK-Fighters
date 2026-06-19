package com.wanted.momocity.viewing.infrastructure.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.infrastructure.persistence.ChapterJpaEntity;
import com.wanted.momocity.lecture.infrastructure.persistence.SpringDataChapterRepository;
import com.wanted.momocity.viewing.application.port.ChapterPort;
import com.wanted.momocity.viewing.domain.model.Chapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/*
* comment.
*   [역할]
*  catalog 컨텍스트 소유의 Chapter 를 READ 전용으로 조회
*  ChapterPort 인터페이스 구현체
*  -
*  [Redis 캐싱 전략]
 * @Cacheable("chapter") -> chapterId 기준 단건 캐싱
 * findAllByLectureId() -> StringRedisTemplate 직접 사용
 * -
 * 왜 캐싱이 필요한가:
 * -> saveProgress() 5~10초 주기 호출 시 매번 DB 조회 → Redis 캐싱으로 DB 부하 감소
 * -
 * @CacheEvict:
 * -> 챕터 정보 변경 시 캐시 무효화 (팀원 머지 후 적용)
 * -> 현재는 Mock 데이터라 미사용
* */


/*
*
* [변환이 필요한 이유]
* - ChapterJpaEntity.toDomain() → LectureChapter 반환
* - Viewing 은 Chapter 도메인 사용
* -> 두 도메인이 다르므로 직접 변환 필요 (toChapter()로 변환)
*
* [VideoStatus 변환]
* 팀원: com.wanted.momocity.lecture.domain.model.VideoStatus
* -> Chapter.VideoStatus (내부 enum)
* -> toVideoStatus() 로 변환
* */

@Slf4j
@Component
public class ChapterCatalogAdapter implements ChapterPort {

    // SpringDataChapterRepository 주입
    // -> JpaRepository 상속받아 findById, findAllByLectureIdOrderByOrderNoAsc 등 제공
    private final SpringDataChapterRepository springDataChapterRepository;
    /*
     * StringRedisTemplate
     * -> String 타입 전용 RedisTemplate
     * -> 저장/조회 시 raw JSON 문자열 그대로 처리
     * -> GenericJackson2JsonRedisSerializer 를 거치지 않아 @class 타입 정보 충돌 문제 없음
     */
    private final StringRedisTemplate stringRedisTemplate;
    /*
     * plainObjectMapper
     * -> @class 타입 정보 없이 순수 JSON 으로 직렬화/역직렬화
     * -> activateDefaultTyping 비활성화 상태
     * -> Chapter.java 의 @JsonTypeInfo 제거와 함께 사용
     * -> HTTP 응답 직렬화에도 영향 없음 (Spring 기본 ObjectMapper 와 별개)
     */
    private final ObjectMapper plainObjectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    public ChapterCatalogAdapter(
            SpringDataChapterRepository springDataChapterRepository,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.springDataChapterRepository = springDataChapterRepository;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /*
     * comment.
     *  챕터 단건 조회
     *  @Cacheable("chapter")
     *  -> 처음 호출 시 DB 조회 후 Redis 에 저장
     *  -> 이후 호출 시 Redis 에서 반환 (DB 조회 없음)
     *  -> key = "chapter::1", "chapter::2" 형태로 저장
     *  -> 단건은 역직렬화 문제 없음
     */

    @Override
    @Cacheable(value = "chapter", key = "#chapterId")
    public Chapter findById(Long chapterId) {

        // SpringDataChapterRepository 의 findById() 로 DB 조회
        // 없으면 DomainRuleViolationException 발생
        ChapterJpaEntity entity = springDataChapterRepository.
                findById(chapterId)
                .orElseThrow(() -> new DomainRuleViolationException("챕터를 찾을 수 없습니다."));
        // ChapterJpaEntity -> chapter 도메인으로 변환
        return toChapter(entity);
    }

    /*
     * comment.
     *  강의 전체 챕터 목록 조회
     *  StringRedisTemplate 직접 사용
     *  -> @Cacheable 은 List<Chapter> 역직렬화 시 GenericJackson2JsonRedisSerializer 의 @class 타입 정보와 충돌 발생
     *  -> StringRedisTemplate 으로 raw JSON 문자열 직접 저장/조회
     *  -> plainObjectMapper 로 순수 JSON 직렬화/역직렬화
     *  -
     *  - 저장 형태
     *  -> key   : "chapters::6"
     *  -> value : [{"id":6,"lectureId":6,"title":"변수와 타입",...}, ...]
     *  -> TTL   : 1시간
     */
    @Override
    public List<Chapter> findAllByLectureId(Long lectureId) {

        String cacheKey = "chapters::" + lectureId;

        try {
            // StringRedisTemplate 으로 raw JSON 문자열 조회
            String json = stringRedisTemplate.opsForValue().get(cacheKey);
            if (json != null) {
                // plainObjectMapper 로 List<Chapter> 역직렬화
                List<Chapter> chapters = plainObjectMapper.readValue(
                        json,
                        new TypeReference<List<Chapter>>() {}
                );
                log.debug("[Viewing] chapters 캐시 히트 | lectureId={}", lectureId);
                return chapters;
            }
        } catch (Exception e) {
            log.warn("[Viewing] chapters 캐시 조회 실패, DB 조회로 fallback | lectureId={} | 예외={}",
                    lectureId, e.getMessage());
        }

        // findAllByLectureIdOrderByOrderNoAsc: lectureId 기준 챕터 목록 orderNo 오름차순 조회
        List<Chapter>chapters = springDataChapterRepository
                .findAllByLectureIdOrderByOrderNoAsc(lectureId)
                .stream()
                // ChapterJpaEntity -> Chapter 도메인으로 변환
                .map(this::toChapter)
                .toList();

        // plainObjectMapper 로 순수 JSON 직렬화 후 StringRedisTemplate 으로 저장
        try {
            String json = plainObjectMapper.writeValueAsString(chapters);
            stringRedisTemplate.opsForValue().set(cacheKey, json, Duration.ofHours(1));
            log.debug("[Viewing] chapters 캐시 저장 | lectureId={}", lectureId);
        } catch (Exception e) {
            log.warn("[Viewing] chapters 캐시 저장 실패 | lectureId={}", lectureId);
        }

        return chapters;

    }

    @Override
    public Optional<Chapter> findByLectureIdAndOrderNo(Long lectureId, int orderNo) {
        return springDataChapterRepository
                .findAllByLectureIdOrderByOrderNoAsc(lectureId)
                .stream()
                .filter(entity -> entity.getOrderNo() == orderNo)
                .findFirst()
                .map(this::toChapter);
    }

    /*
     * toChapter
     * ChapterJpaEntity -> Chapter 도메인 변환
     * durationSec null 가능 → 0 처리
     */
    private Chapter toChapter(ChapterJpaEntity entity) {
        return Chapter.reconstitute(
                entity.getId(),
                entity.getLectureId(),
                entity.getTitle(),
                entity.getOrderNo(),
                entity.getVideoUrl(),
                // durationSec null 가능 → 0 처리
                entity.getDurationSec() != null ? entity.getDurationSec() : 0
        );
    }

}