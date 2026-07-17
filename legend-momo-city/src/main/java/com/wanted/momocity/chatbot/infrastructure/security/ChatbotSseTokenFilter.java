package com.wanted.momocity.chatbot.infrastructure.security;

import com.wanted.momocity.auth.infrastructure.exception.ExpiredJwtCustomException;
import com.wanted.momocity.auth.infrastructure.exception.InvalidJwtCustomException;
import com.wanted.momocity.auth.infrastructure.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/* comment.
    SSE 는 브라우저 표준상 Authorization 헤더를 보내지 못한다.
    그래서 챗봇의 SSE 경로에서만 쿼리파라미터 token 을 대신 읽어서 인증 처리하는 채봇 전용 필터이다.
    auth 모듈은 BC 침해를 하지 않기 위해서 절대 건드리지 않고, 이미 있는 검증 메서드에서 가져다 쓴다.
 */
@RequiredArgsConstructor
public class ChatbotSseTokenFilter extends OncePerRequestFilter {


    private static final String CHATBOT_SSE_PATH = "/api/v1/chatbot/questions/stream";

    private final JwtTokenProvider jwtTokenProvider;

    // 챗봇 SSE 경로가 아니면 이 필터는 완전히 무시되게 된다.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 이 경로가 아니면 아무것도 안 하고 통과 - 다른 요청엔 영향 없음
        return !CHATBOT_SSE_PATH.equals(request.getRequestURI());
    }

    // 쿼리파라미터 token 을 읽어서 검증이 된다면 SecurityContext 에 인증 정보 세팅
    // 실패하면 그냥 흘려보냄(뒤에서 401 처리 해버리기)
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = request.getParameter("token");

        if (token != null) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ExpiredJwtCustomException | InvalidJwtCustomException e) {
                // 여기서 막지 않고 통과시킴 -> 뒤에서 anyRequest().authenticated() 정책에 걸려서
                // CustomAuthenticationEntryPoint 가 401 처리해줌
            }
        }

        filterChain.doFilter(request, response);
    }
}
