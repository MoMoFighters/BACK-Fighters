package com.wanted.momocity.global.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/*
 * WebMvcConfig의 역할 — 한 줄 요약
 * "Spring MVC 전역 설정(CORS, Interceptor, Formatter 등)을 모아두는 어댑터 설정 클래스."
 *
 * 현재는 CORS 정책만 정의한다.
 * - 로컬 개발 환경의 프론트 출처(Origin) 를 와일드카드로 허용
 * - 자격 증명 헤더(Authorization, Cookie) 동반 요청 허용
 * - 응답에서 Authorization 헤더를 클라이언트가 읽을 수 있게 노출
 *
 * 운영 도메인 추가는 인프라 회의 후 별도 이슈로 트래킹한다.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOriginPatterns(List.of(
//                "http://localhost:*"
//                // TODO 인프라 / 프론트 도메인 결정 후 운영 Origin 추가
//        ));
//        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
//        configuration.setAllowedHeaders(List.of("*"));
//        configuration.setExposedHeaders(List.of("Authorization"));
//        configuration.setAllowCredentials(true);
//        configuration.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
}
