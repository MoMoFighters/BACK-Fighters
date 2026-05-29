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

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        ObjectMapper objectMapper = new ObjectMapper();

        // [필수 추가] 날짜/시간 모듈 등록
        // LocalDateTime 등을 처리하기 위해 꼭 필요합니다.
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        BasicPolymorphicTypeValidator typeValidator =
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class).build();

        objectMapper.activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                // 기본 TTL 1시간 -> 1시간 후 자동 삭제
                // 챕터 정보 바뀌어도 최대 1시간 후 갱신
                .entryTtl(Duration.ofHours(1))
                // key 를 문자열로 저장
                // "chapter::1" 형태로 Redis 에 저장
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                // value 를 JSON 으로 변환해서 저장
                .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer)
                )
                // null 캐싱 방지
                // 없는 챕터를 조회해도 Redis 에 저장 안 됨
                .disableCachingNullValues();


        // 캐시별 개별 TTL 설정
//        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // chapter 단건 조회 캐시
//        cacheConfigs.put("chapter", defaultConfig.entryTtl(Duration.ofHours(1)));

        // 강의별 전체 챕터 목록 캐시
//        cacheConfigs.put("chapters", defaultConfig.entryTtl(Duration.ofHours(1)));

        // lecture 단건 조회 캐시
//        cacheConfigs.put("lecture", defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
//                .withInitialCacheConfigurations(cacheConfigs)
                .build();

    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper()));
        return template;
    }

    private ObjectMapper redisObjectMapper () {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        objectMapper.activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return objectMapper;

    }


}
