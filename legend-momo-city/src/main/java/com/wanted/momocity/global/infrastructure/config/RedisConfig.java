package com.wanted.momocity.global.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/*
 * comment
 *  RedisConfig
 *  -
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

@EnableCaching
@Configuration
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                // 기본 TTL 1시간
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                // null 캐싱 방지
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

}
