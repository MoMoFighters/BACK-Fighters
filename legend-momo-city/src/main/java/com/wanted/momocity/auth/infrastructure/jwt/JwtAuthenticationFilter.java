package com.wanted.momocity.auth.infrastructure.jwt;

import com.wanted.momocity.auth.application.usecase.RefreshTokenUseCase;
import com.wanted.momocity.auth.infrastructure.exception.ExpiredJwtCustomException;
import com.wanted.momocity.auth.infrastructure.exception.InvalidJwtCustomException;
import com.wanted.momocity.auth.infrastructure.exception.InvalidRefreshTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // OncePerRequestFilter를 상속 받아서
    // 하나의 요청에 딱 한 번만 자동으로 실행

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, RefreshTokenUseCase refreshTokenUseCase) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    // 로그인 후 프론트는 우리가 준 토큰값을 가지고 Authorization: Bearer eyJhbGci... 헤더를 붙여서 요청을 보냄
    // 그럼 이 흐름이 실행 됨
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = jwtTokenProvider.resolveToken(request);

        try {
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtCustomException expiredAccessTokenException) {
            try {
                String refreshTokenValue = jwtTokenProvider.resolveRefreshToken(request);
                if (refreshTokenValue != null) {
                    String newAccessToken = refreshTokenUseCase.refreshAccessToken(refreshTokenValue); // 여기만 변경

                    Authentication newAuthentication = jwtTokenProvider.getAuthentication(newAccessToken);
                    SecurityContextHolder.getContext().setAuthentication(newAuthentication);
                    response.setHeader("New-Access-Token", newAccessToken);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } catch (InvalidRefreshTokenException invalidRefreshTokenEx) {
                SecurityContextHolder.clearContext();
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        } catch (InvalidJwtCustomException invalidAccessTokenException) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}