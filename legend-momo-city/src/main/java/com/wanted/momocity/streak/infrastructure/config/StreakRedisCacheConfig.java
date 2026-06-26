package com.wanted.momocity.streak.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wanted.momocity.streak.presentation.api.response.StreakMonthlyResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*
* comment.
*  Streak 도메인 전용 Redis 캐시 설정
*  -
*  StreakMonthlyResponse 가 record 타입 -> GenericJackson2JsonSerializer 와 충돌
*  → Jackson2JsonRedisSerializer<StreakMonthlyResponse> 로 타입 명시
* */

@Configuration
public class StreakRedisCacheConfig {

    @Bean(name = "streakCacheConfiguration")
    public RedisCacheConfiguration streakCacheConfiguration() {

        // Redis 직렬화 전용 ObjectMapper
        // Spring 기본 ObjectMapper 와 별개로 관리
        // -> HTTP 응답 직렬화에 영향 없음
        ObjectMapper objectMapper = new ObjectMapper();

        // LocalDate (streakDate) 직렬화 지원
        objectMapper.registerModule(new JavaTimeModule());

        // 날짜를 타임스탬프 숫자 배열이 아닌 문자열로 저장
        // -> "2026-06-26" 형태로 저장
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 모르는 필드 무시 -> 향후 필드 추가 시 캐시 호환성 유지
        objectMapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
        );

        // StreakMonthlyResponse 타입 명시
        // -> List<StreakResponse> 포함한 중첩 구조도 정확히 역직렬화
        Jackson2JsonRedisSerializer<StreakMonthlyResponse> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, StreakMonthlyResponse.class);

        // TTL = 오늘 자정까지 남은 시간
        // -> 예: 현재 오후 9시 -> TTL = 3시간
        // -> 예: 현재 오전 1시 -> TTL = 23시간
        LocalDateTime midnight = LocalDate.now().plusDays(1).atStartOfDay();
        Duration ttl = Duration.between(LocalDateTime.now(), midnight);

        return RedisCacheConfiguration
                .defaultCacheConfig()
                // 자정까지 남은 시간으로 TTL 설정
                .entryTtl(ttl)
                // 캐시 키는 문자열로 저장
                // -> "streak::2:2026:6" 형태로 Redis 에서 확인 가능
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                // 캐시 값은 JSON 으로 저장
                // -> StreakMonthlyResponse 타입 명시로 역직렬화 안전
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(serializer)
                )
                // null 캐싱 방지
                // -> 잔디 없는 날짜 조회해도 null 저장 안 됨
                .disableCachingNullValues();
    }

    }
