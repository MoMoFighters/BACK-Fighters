package com.wanted.momocity.viewing.infrastructure.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.infrastructure.persistence.LectureJpaEntity;
import com.wanted.momocity.lecture.infrastructure.persistence.SpringDataLectureRepository;
import com.wanted.momocity.viewing.application.port.LecturePort;
import com.wanted.momocity.viewing.domain.model.Lecture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/*
* comment.
*  LecturePort 인터페이스 구현체
*  catalog 컨텍스트 소유의 Lecture 를 READ 전용으로 조회
*  -
*  StringRedisTemplate 직접 사용
*  -> @Cacheable + GenericJackson2JsonRedisSerializer 조합은 activateDefaultTyping 설정과 충돌 발생
*  (직렬화 실패 또는 역직렬화 시 LinkedHashMap 캐스팅 실패)
*  -> StringRedisTemplate 으로 raw JSON 문자열 직접 저장 / 조회
*  -> plainObjectMapper 로 순수 JSON 직렬화 / 역직렬화
*  -
*  저장형태
*  - key : "lecture::1"
*  - value : {"id":1,"teacherId":6,"title":"홈트레이닝 기초",...}
*  - TTL : 1시간
*  -
*  -> getLectureMeta(), getMyLectures() 등 매번 DB 조회
*  -> Redis 캐싱으로 DB 부하 감소
*  -> 강의 정보는 자주 바뀌지 않아 캐싱 효과 극대화
* */

@Component
@RequiredArgsConstructor
@Slf4j
public class LectureCatalogAdapter implements LecturePort {

    // SpringDataLectureRepository 주입
    private final SpringDataLectureRepository springDataLectureRepository;

    // LoadUserPort 주입
    // → teacherId 로 강사 이름 조회할 때 사용
    private final LoadUserPort loadUserPort;

    // String 타입 전용 RedisTemplate
    // 저장 / 조회 시 raw JSON 문자열 그대로 처리
    // GenericJackson2JsonRedisSerializer 의 @class 타입 정보 충돌 문제 없음
    private final StringRedisTemplate stringRedisTemplate;

    // activateDefaultTyping 비활성화 상태의 순수 ObjectMapper
    // @class 타입 정보 없이 순수 JSON 으로 직렬화 / 역직렬화
    // JavaTimeModule 등록으로 LocalDateTime 처리 가능
    // HTTP 응답 직렬화에는 영향 없음 (Spring 기본 ObjectMapper 와 별개)
    private final ObjectMapper plainObjectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());


    /*
     * comment.
     *  강의 단건 조회
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
    public Lecture findById(Long lectureId) {

        String cacheKey = "lecture::" + lectureId;

        // 1. Redis 캐시 조회
        try {
            String json = stringRedisTemplate.opsForValue().get(cacheKey);
            if (json != null) {
                Lecture lecture = plainObjectMapper.readValue(json, Lecture.class);
                log.debug("[Viewing] lecture 캐시 히트 | lectureId={}", lectureId);
                return lecture;
            }
        } catch (Exception e) {
            log.warn("[Viewing] lecture 캐시 조회 실패, DB 조회로 fallback | lectureId={} | 예외={}",
                    lectureId, e.getMessage());
        }

        // 2. DB 조회
        LectureJpaEntity entity = springDataLectureRepository.findById(lectureId)
                .orElseThrow(() -> new DomainRuleViolationException("강의를 찾을 수 없습니다."));

        String instructorName = loadUserPort.findById(entity.getTeacherId())
                .map(user -> user.getName())
                .orElse("강사");

        Lecture lecture = Lecture.reconstitute(
                entity.getId(),
                entity.getTeacherId(),
                entity.getTitle(),
                entity.getThumbnailUrl(),
                entity.getCategory().name(),
                instructorName,
                entity.getStatus().name()
        );

        // 3. 캐시 저장
        // plainObjectMapper 로 순수 JSON 직렬화 후 StringRedisTemplate 으로 저장
        // TTL 1시간 → 강의 정보는 자주 바뀌지 않아 1시간 캐싱 적절
        try {
            String json = plainObjectMapper.writeValueAsString(lecture);
            stringRedisTemplate.opsForValue().set(cacheKey, json, Duration.ofHours(1));
            log.debug("[Viewing] lecture 캐시 저장 | lectureId={}", lectureId);
        } catch (Exception e) {
            log.warn("[Viewing] lecture 캐시 저장 실패 | lectureId={}", lectureId);
        }

        return lecture;
    }

    /*
    * comment.
    *  카테고리별 강의 목록 조회
    *  -> CategoryProgressPort 에서 카테고리별 진척도 계산 시 사용
    *  -> ACTIVE 상태 강의만 조회
    *  -> String category -> LectureCategory Enum 변환 후 조회
    *  -> Pageable.unpaged() 로 페이지네이션 없이 조회
    *  -
    *  캐시 미적용
    *  -> 카테고리별 목록은 호출 빈도 낮음
    *  -> 캐시 적용 시 강의 추가 / 수정 시 무효화 복잡도 증가
    * */

    @Override
    public List<Lecture> findAllByCategory(String category) {
        return springDataLectureRepository
                .findAllByCategoryAndStatus(
                        LectureCategory.valueOf(category),
                        LectureStatus.ACTIVE,
                        Pageable.unpaged()
                )
                .getContent()
                .stream()
                .map(entity -> {
                    // instructorName : LectureJpaEntity 에 instructorName 없음
                    // LoadUserPort 로 teacherId 기준 강사 이름 조회 -> 없으면 강사 기본값 처리
                    String instructorName = loadUserPort.findById(entity.getTeacherId())
                            .map(user -> user.getName())
                            .orElse("강사");
                    return Lecture.reconstitute(
                            entity.getId(),
                            entity.getTeacherId(),
                            entity.getTitle(),
                            entity.getThumbnailUrl(),
                            entity.getCategory().name(),
                            instructorName,
                            entity.getStatus().name()
                    );
                })
                .toList();
    }
}
