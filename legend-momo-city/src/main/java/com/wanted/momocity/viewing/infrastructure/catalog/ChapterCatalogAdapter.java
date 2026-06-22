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
*  catalog 컨텍스트 소유의 Chapter 를 READ 전용으로 조회
*  ChapterPort 인터페이스 구현체
*  -
*  StringRedisTemplate 직접 사용
*  -> @Cacheable + GenericJackson2JsonRedisSerializer 조합은 activateDefaultTyping 설정과 충돌 발생
*  (직렬화 실패 또는 역직렬화 시 LinkedHashMap 캐스팅 실패)
*  -> StringRedisTemplate 으로 raw JSON 문자열 직접 저장 / 조회
*  -> plainObjectMapper 로 순수 JSON 직렬화 / 역직렬화
*  -
*  저장형태
*  - chapter : key = "chapter::1", value = {"id":1,"lectureId":1,...}
*  - chapters : key = "chapters::1", value = [{"id":1,...},{"id":2,...}]
*  - TTL : 1시간
*  -
*  saveProgress() 가 5 - 10초 주기로 반복 호출될 때마다
*  -> ChapterPort.findById() -> DB 조회 발생
*  -> ChapterPoser.findAllByLectureId() -> DB 조회 발생
*  -> Redis 캐싱으로 DB 부하 감소
*  -
*  ChapterJpaEntity.toDomain() -> LectureChapter 반환
*  Viewing 은 Chapter 도메인 사용
*  -> 두 도메인이 다르므로 직접 변환 필요 (toChapter() 로 반환)
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
     *  -
     *  캐시 조회 우선
     *  1. Redis 에서 "lecture::1" 키로 raw JSON 조회
     *  2. 캐시 히트 시 plainObjectMapper 로 Lecture 역직렬화 후 반환
     *  3. 캐시 미스 시 DB 조회 후 Redis 저장
     *  -
     *   예외처리
     *  - 캐시 조회 / 저장 실패 시 -> 로그만 남기고 DB 조회로 fallback
     *  -> 캐시 장애가 서비스 장애로 이어지지 않도록 방어
     */

    @Override
    public Chapter findById(Long chapterId) {

        String cacheKey = "chapter::" + chapterId;

        // 1. Redis 캐시 조회
        try {
            String json = stringRedisTemplate.opsForValue().get(cacheKey);
            if (json != null) {
                Chapter chapter = plainObjectMapper.readValue(json, Chapter.class);
                log.debug("[Viewing] chapter 캐시 히트 | chapterId={}", chapterId);
                return chapter;
            }
        } catch (Exception e) {
            log.warn("[Viewing] chapter 캐시 조회 실패, DB 조회로 fallback | chapterId={} | 예외={}",
                    chapterId, e.getMessage());
        }

        // 2. DB 조회
        ChapterJpaEntity entity = springDataChapterRepository
                .findById(chapterId)
                .orElseThrow(() -> new DomainRuleViolationException("챕터를 찾을 수 없습니다."));

        Chapter chapter = toChapter(entity);

        // 3. Redis 캐시 저장
        // TTL 1시간 -> 챕터 정보는 자주 바뀌지 않아 1시간 캐싱
        try {
            String json = plainObjectMapper.writeValueAsString(chapter);
            stringRedisTemplate.opsForValue().set(cacheKey, json, Duration.ofHours(1));
            log.debug("[Viewing] chapter 캐시 저장 | chapterId={}", chapterId);
        } catch (Exception e) {
            log.warn("[Viewing] chapter 캐시 저장 실패 | chapterId={} | 예외={}",
                    chapterId, e.getMessage());
        }

        return chapter;
    }

    /*
     * comment.
     *  강의 전체 챕터 목록 조회
     *  -
     *  캐시 조회 우선
     *  1. Redis 에서 "lecture::1" 키로 raw JSON 조회
     *  2. 캐시 히트 시 plainObjectMapper 로 Lecture 역직렬화 후 반환
     *  3. 캐시 미스 시 DB 조회 후 Redis 저장
     *  -
     *  List 타입 역직렬화
     *  TypeReference<List<Chapter>> 로 정확한 타입 지정
     *  -> @Cacheable 은 List 제네릭 타입 역직렬화 불가
     *  -> StringRedisTemplate + TypeReference 조합으로 해결
     */
    @Override
    public List<Chapter> findAllByLectureId(Long lectureId) {

        String cacheKey = "chapters::" + lectureId;

        // 1. Redis 캐시 조회
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

        // 2. DB 조회
        // findAllByLectureIdOrderByOrderNoAsc: lectureId 기준 챕터 목록 orderNo 오름차순 조회
        List<Chapter>chapters = springDataChapterRepository
                .findAllByLectureIdOrderByOrderNoAsc(lectureId)
                .stream()
                // ChapterJpaEntity -> Chapter 도메인으로 변환
                .map(this::toChapter)
                .toList();

        // 3. Redis 캐시 저장
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