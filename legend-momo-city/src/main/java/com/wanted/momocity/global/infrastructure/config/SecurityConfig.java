package com.wanted.momocity.global.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/*
 * SecurityConfig의 역할 — 한 줄 요약
 * "Spring Security 필터 체인을 정의하고, 비밀번호 해싱·CORS·세션 정책 등 인증 기반 설정을 모은다."
 *
 * 현재 상태:
 * - JWT 인증 필터(JwtAuthenticationFilter)는 아직 없다.
 * - 컨텍스트 담당자들이 개발을 시작할 수 있도록 모든 요청을 일단 permitAll 로 열어둔다.
 * - 인증 담당자가 JwtAuthenticationFilter를 만들면
 *   아래 TODO 위치에 .addFilterBefore(...) 로 끼워 넣고, 보호 경로 정책을 강화한다.
 *
 * 메모:
 * - 세션은 STATELESS. 토큰 기반이므로 서버 세션을 사용하지 않는다.
 * - CSRF 는 비활성화. 쿠키-세션 기반이 아니므로 필요 없다.
 * - CORS 는 WebMvcConfig의 CorsConfigurationSource Bean 을 그대로 사용한다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // ===== 항상 공개 =====
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ===== 인증 담당이 채울 슬롯 =====
                        // .requestMatchers("/api/v1/auth/**").permitAll()
                        // .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // ===== 임시 정책: 컨텍스트 개발 시작을 위해 전부 허용 =====
                        // TODO 인증 담당: JwtAuthenticationFilter 추가 후 .authenticated() 로 강화
                        .anyRequest().permitAll()
                );

        // TODO 인증 담당:
        // http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
