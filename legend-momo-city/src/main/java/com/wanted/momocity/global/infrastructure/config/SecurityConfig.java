package com.wanted.momocity.global.infrastructure.config;

import com.wanted.momocity.auth.application.port.BlacklistPort;
import com.wanted.momocity.auth.application.service.RefreshService;
import com.wanted.momocity.auth.infrastructure.handler.CustomAccessDeniedHandler;
import com.wanted.momocity.auth.infrastructure.handler.CustomAuthenticationEntryPoint;
import com.wanted.momocity.auth.infrastructure.jwt.JwtAuthenticationFilter;
import com.wanted.momocity.auth.infrastructure.jwt.JwtTokenProvider;
import com.wanted.momocity.chatbot.infrastructure.security.ChatbotSseTokenFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshService refreshService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final BlacklistPort blacklistPort;

    //====================CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000", // React, Vue 등의 개발서버
                "http://localhost:8081", // 다른 로컬 개발 환경
                "https://momocity-six.vercel.app", // 배포하게 될 경우
                "https://momocity.kro.kr",
                "http://localhost:4444"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-Refresh-Token", // 리프레시 토큰을 위한 커스텀 헤더
                "Cookie",
                "webhook-id",
                "webhook-timestamp",
                "webhook-signature"
        ));

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "New-Access-Token", // 새 액세스 토큰 전달용 커스텀 헤더
                "Set-Cookie"
        ));

        configuration.setAllowCredentials(true);
        //Authorization 헤더(JWT 토큰)를 요청에 포함해도 된다는 허가
        // 이게 false면 브라우저가 인증 관련 헤더를 아예 안 보냄. JWT 인증이 통째로 안 됩니다.

        configuration.setMaxAge(3600L); // 1시간

        // 모든 경로("/**")에 대해 위에서 정의한 CORS 설정을 등록
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    //==================== 필드 선언
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
        // AuthenticationManager는 "아이디/비밀번호 맞는지 확인하는 심사관"입니다.
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // "비밀번호 암호화 기계 만들기"
    }



    //==================== securityFilterChain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // Stateless 환경에선 CSRF 불필요
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 생성 X
//                =================================================================
                // URL별 입장 규칙
                .authorizeHttpRequests(auth -> auth
                        // [챗봇 SSE 스트림 끊김 버그 수정] emitter.complete() 시 서블릿 컨테이너가 내부적으로
                        // ASYNC 재디스패치를 한 번 더 태우는데, 이때 우리 커스텀 인증 필터들은 재실행되지 않아
                        // SecurityContext가 비어있는 상태로 anyRequest().authenticated()에 걸려
                        // AuthorizationDeniedException이 터지고 이미 커밋된 SSE 응답이 조용히 끊기는 문제가 있었음.
                        // 최초 REQUEST 단계에서 이미 인증 검증이 끝났으므로, 내부 마무리용 ASYNC 재디스패치는 재검사 없이 통과시킨다.
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        // 여기서 1차 인가 관련 방호벽
                        // /api/user/ 하위에 endpoint 중에 admin / user 권한 별로 접근하기 위해서는
                        // 메서드 레벨에서 2차 방호벽 구축
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // 프로메테우스를 사용하기 위한 actuator 사용을 위한 코드 줄 추가
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/lectures").permitAll()
                        // 비로그인 강의 상세 조회 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/lectures/*").permitAll()
                        // 수강평 목록 조회는 비로그인 사용자도 볼 수 있게 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/lectures/*/reviews").permitAll()
                        // 게시글 목록 조회
                        .requestMatchers(HttpMethod.GET, "/api/v2/posts").permitAll()
                        // 게시글 목록 검색
                        .requestMatchers(HttpMethod.GET, "/api/v2/posts/search").permitAll()
                        // 게시글 상세 조회
                        .requestMatchers(HttpMethod.GET, "/api/v2/posts/{postId}").permitAll()
                        // 게시글 댓글 조회
                        .requestMatchers(HttpMethod.GET, "/api/v2/posts/{postId}/comments/**").permitAll()
                        // 상대방 게시글 목록 조회
                        .requestMatchers(HttpMethod.GET, "/api/v2/posts/users/{targetUserId}").permitAll()
                        // 상대방 대시보드 조회
                        .requestMatchers(HttpMethod.GET, "/api/v2/posts/users/{targetUserId}/dashboard").permitAll()
                        // /api/v1/reports : URL 역할 규칙 제거 → 메서드 레벨 @PreAuthorize 로 인가 (GET=ADMIN 조회 / POST=인증 회원 신고)
                        .requestMatchers("/api/v1/error-logs").hasAnyAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/teacher/**").hasAnyAuthority("ROLE_TEACHER")
                        .requestMatchers("/api/v1/auth/login/completed").authenticated()
                        .requestMatchers("/api/v1/auth/newtoken").authenticated()
                        .requestMatchers("/api/v1/auth/logout").authenticated()
                        // 임시 비밀번호 발급도 인증 토큰 필요
                        .requestMatchers("/api/v1/auth/**").permitAll() // 인증 없이 허용
                        .requestMatchers("/api/v1/user/onboarding").permitAll() // 인증 없이 허용
                        .requestMatchers("/ws-chat/**").permitAll()
                        .requestMatchers("/api/v3/payment/webhook").permitAll()
                        .anyRequest().authenticated()) // 나머지는 인증 필요
//                ========================================================================

                //.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 챗봇팀 수정: JwtAuthenticationFilter를 기준으로 걸려면 얘가 먼저 등록돼있어야 해서 순서를 바꿈
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, refreshService,blacklistPort),
                        UsernamePasswordAuthenticationFilter.class
                )
                // 챗봇 SSE 경로 전용 - 쿼리파라미터 token 인증, 기존 JwtAuthenticationFilter보다 먼저 실행
                .addFilterBefore(
                        new ChatbotSseTokenFilter(jwtTokenProvider),
                        JwtAuthenticationFilter.class
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // 인증되지 않은 사용자가 보호된 리소스 접근시 처리 방식 정의
                        .accessDeniedHandler(customAccessDeniedHandler)) // 인증은 되었지만 인가가 허용되지 않는 사용자 처리 방식 정의
                .build();
    }
}