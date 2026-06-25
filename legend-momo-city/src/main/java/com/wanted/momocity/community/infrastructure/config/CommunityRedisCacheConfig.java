package com.wanted.momocity.community.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wanted.momocity.community.presentation.api.response.PostListResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/*
 * comment.
 *  Community 도메인 전용 Redis 캐시 설정
 *  -
 *  [왜 별도 설정인가]
 *  PostListResponse 가 record 타입
 *  -> GenericJackson2JsonRedisSerializer + activateDefaultTyping 조합 시
 *    직렬화 실패 또는 역직렬화 시 LinkedHashMap 캐스팅 실패 발생
 *  -> Jackson2JsonRedisSerializer<PostListResponse> 로 타입 명시
 *  -> 역직렬화 시 정확한 타입으로 복원 가능
 *  -
 *  [ObjectMapper 설정]
 *  JavaTimeModule : LocalDateTime 처리
 *  FAIL_ON_UNKNOWN_PROPERTIES = false : 알 수 없는 필드 무시
 *  WRITE_DATES_AS_TIMESTAMPS = false : 날짜를 문자열로 직렬화
 */
@Configuration
public class CommunityRedisCacheConfig {

    @Bean(name = "postsCacheConfiguration")
    public RedisCacheConfiguration postsCacheConfiguration() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
        );

        /*
         * Jackson2JsonRedisSerializer<PostListResponse>
         * -> 타입 명시로 역직렬화 시 정확한 타입 복원
         * -> GenericJackson2JsonRedisSerializer 와 달리 @class 타입 정보 불필요
         * -> record 타입 직렬화/역직렬화 가능
         */
        Jackson2JsonRedisSerializer<PostListResponse> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, PostListResponse.class);

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