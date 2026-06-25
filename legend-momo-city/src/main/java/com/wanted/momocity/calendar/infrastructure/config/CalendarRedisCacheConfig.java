package com.wanted.momocity.calendar.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wanted.momocity.calendar.presentation.api.response.MonthlyCalendarResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/*
 * comment.
 *  Calendar 도메인 전용 Redis 캐시 설정
 *  -
 *  [왜 별도 설정인가]
 *  MonthlyCalendarResponse 가 record 타입
 *  -> GenericJackson2JsonRedisSerializer 와 충돌
 *  -> Jackson2JsonRedisSerializer<MonthlyCalendarResponse> 로 타입 명시
 *  -
 *  [ObjectMapper 설정]
 *  JavaTimeModule : LocalDate 처리
 *  WRITE_DATES_AS_TIMESTAMPS = false : 날짜를 문자열로 직렬화
 */
@Configuration
public class CalendarRedisCacheConfig {

    @Bean(name = "calendarCacheConfiguration")
    public RedisCacheConfiguration calendarCacheConfiguration() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
        );

        Jackson2JsonRedisSerializer<MonthlyCalendarResponse> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, MonthlyCalendarResponse.class);

        return RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(serializer)
                )
                .disableCachingNullValues();
    }
}