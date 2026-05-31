package com.wanted.momocity.auth.infrastructure.jwt;

import com.wanted.momocity.auth.application.port.BlacklistPort;
import com.wanted.momocity.auth.application.service.RefreshService;
import com.wanted.momocity.auth.infrastructure.exception.ExpiredJwtCustomException;
import com.wanted.momocity.auth.infrastructure.exception.InvalidJwtCustomException;
import com.wanted.momocity.auth.infrastructure.exception.InvalidRefreshTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // OncePerRequestFilter를 상속 받아서
    // 하나의 요청에 딱 한 번만 자동으로 실행

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshService refreshService;
    private final BlacklistPort blacklistPort;


    // 로그인 후 프론트는 우리가 준 토큰값을 가지고 Authorization: Bearer eyJhbGci... 헤더를 붙여서 요청을 보냄
    // 그럼 이 흐름이 실행 됨
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = jwtTokenProvider.resolveToken(request);

        // access 토큰이 블랙 리스트면 예외
        if (accessToken != null && blacklistPort.isBlacklisted(accessToken)) {
            SecurityContextHolder.clearContext();
            sendUnauthorized(response, "다시 로그인해 주세요.");
            return;
        }

        try {
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtCustomException expiredAccessTokenException) {
            try {
                String refreshTokenValue = jwtTokenProvider.resolveRefreshToken(request);
                if (refreshTokenValue != null) {
                    String newAccessToken = refreshService.refreshAccessToken(refreshTokenValue);

                    Authentication newAuthentication = jwtTokenProvider.getAuthentication(newAccessToken);
                    SecurityContextHolder.getContext().setAuthentication(newAuthentication);
                    response.setHeader("New-Access-Token", newAccessToken);
                } else {
                    SecurityContextHolder.clearContext();
                    sendUnauthorized(response, "다시 로그인해 주세요.");
                    return;
                }
            } catch (InvalidRefreshTokenException invalidRefreshTokenEx) {
                SecurityContextHolder.clearContext();
                sendUnauthorized(response, "다시 로그인해 주세요.");
                return;
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                sendUnauthorized(response, "다시 로그인해 주세요.");
                return;
            }
        } catch (InvalidJwtCustomException invalidAccessTokenException) {
            SecurityContextHolder.clearContext();
            sendUnauthorized(response, "다시 로그인해 주세요.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"timestamp\": \"" + java.time.LocalDateTime.now() + "\", \"status\": 401, \"code\": \"UNAUTHORIZED\", \"message\": \"" + message + "\"}"
        );
    }
}