package com.wanted.momocity.global.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/*
 * comment
 *  [역할]
 *  Spring Cache 와 Redis 를 연결하는 설정
 *  -> @Cacheable, @CacheEvict 등 캐시 어노테이션 활성화
 *  -
 *  [캐시 전략]
 *  chapter  : TTL 1시간 → 챕터 정보는 자주 바뀌지 않음
 *  chapters : TTL 1시간 → 강의 전체 챕터 목록
 *  lecture  : TTL 1시간 → 강의 정보는 자주 바뀌지 않음
 *  -
 *  [왜 캐싱이 필요한가]
 *  saveProgress() 가 5~10초 주기로 호출될 때마다
 *  -> ChapterPort.findById() → DB 조회
 *  -> ChapterPort.findAllByLectureId() → DB 조회
 *  -> LecturePort.findById() → DB 조회
 *  -> Redis 캐싱으로 DB 부하 감소
 *  -
 *  [직렬화 설정]
 *  Key   : StringRedisSerializer → 사람이 읽을 수 있는 문자열
 *  Value : GenericJackson2JsonRedisSerializer → JSON 형태로 저장
 */

// Spring 에서 캐싱 기능 활성화시키는 어노테이션
@EnableCaching
@Configuration
public class RedisConfig {

    /*
    * comment.
    *  RedisCacheManager : @Cacheable, @CacheEvict 등 어노테이션이 실제로 Redis 에 저장/조회/삭제하도록 연결해주는 관리자
    *  → Spring 이 캐시 작업 시 이 Bean 을 찾아서 실행
    * */

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        /*
        * comment.
        *  serializer : Java 객체 ↔ JSON 변환 담당
        *  -> Redis 는 바이트 데이터만 저장 가능
        *  -> Java 객체를 JSON 으로 변환해서 저장, 꺼낼 때 JSON 을 Java 객체로 복원
        *  -
        *  redisObjectMapper() : activateDefaultTyping 이 설정된 ObjectMapper 사용
        *  -> 타입 정보(@class) 포함해서 저장
        *  ->d 역직렬화 시 정확한 타입으로 복원 가능
        * */

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                // 기본 TTL 1시간 -> 1시간 후 자동 삭제
                // 챕터 정보 바뀌어도 최대 1시간 후 갱신
                .entryTtl(Duration.ofHours(1))
                // key 를 문자열로 저장
                // "chapter::1", "lecture::2" 형태로 Redis 에 저장
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                // value 를 JSON 으로 변환해서 저장
                // Chapter 객체 -> JSON 변환 후 저장
                .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer)
                )
                // null 캐싱 방지
                // 없는 챕터를 조회해도 Redis 에 저장 안 됨
                .disableCachingNullValues();


        // 캐시별 개별 TTL 설정
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // chapter 단건 조회 캐시
        cacheConfigs.put("chapter", defaultConfig.entryTtl(Duration.ofHours(1)));

        // 강의별 전체 챕터 목록 캐시
        cacheConfigs.put("chapters", defaultConfig.entryTtl(Duration.ofHours(1)));

        // lecture 단건 조회 캐시
        cacheConfigs.put("lecture", defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();

    }

    /*
    * comment.
    *  RedisTemplate : Redis 에 직접 데이터 저장/조회할 때 사용
    *  ->  @Cacheable 대신 코드로 직접 캐시 제어할 때 필요
    *  -
    *  @Cacheable 은 List<Chapter> 역직렬화 실패
    *  -> RedisTemplate 으로 직접 저장/조회 시 TypeReference 로 정확한 타입 지정 가능
    * */

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // setKeySerializer : key -> 문자열
        template.setKeySerializer(new StringRedisSerializer());
        // setValueSerializer : value -> JSON
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper()));
        return template;
    }

    /*
    * comment.
    *  redisObjectMapper()
    *  - private : @Bean 으로 등록하면 Spring 이 HTTP 응답 직렬화에도 사용
    *  -> 응답 JSON 에 "@class" 타입 정보가 노출되는 버그 발생
    *  -> private 으로 Redis 내부에서만 사용하도록 격리
    * */

    private ObjectMapper redisObjectMapper () {
        ObjectMapper objectMapper = new ObjectMapper();
        // JavaTimeModule : LocalDateTime 등 Java 8 날짜/시간 타입 처리
        // 없으면 enrolledAt 같은 날짜 직렬화 실패
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                // allowIfBaseType(Object.class) : 모든 Object 하위 타입 허용
                // -> Chapter, Lecture 등 커스텀 클래스 포함
                .allowIfBaseType(Object.class)
                .build();
        // activateDefaultTyping :  직렬화 시 각 객체에 타입 정보(@class) 포함
        // 예: {"@class":"...Chapter", "id":1, ...}
        // -> 역직렬화 시 @class 보고 정확한 타입으로 복원
        objectMapper.activateDefaultTyping(
                typeValidator,
                // DefaultTyping.NON_FINAL : final 이 아닌 모든 클래스에 타입 정보 포함
                // -> Chapter, Lecture 등 도메인 객체에 적용
                ObjectMapper.DefaultTyping.NON_FINAL,
                //  JsonTypeInfo.As.PROPERTY : 타입 정보를 JSON 프로퍼티로 포함
                // {"@class":"...Chapter", ...} 형태
                JsonTypeInfo.As.PROPERTY
        );

        return objectMapper;

    }


}
