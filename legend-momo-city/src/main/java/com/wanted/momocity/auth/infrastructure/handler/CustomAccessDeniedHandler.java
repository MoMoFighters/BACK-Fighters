package com.wanted.momocity.auth.infrastructure.handler;

// [MS-4 접근로그] admin BC 접근로그 저장을 위해 import (auth BC 담당자 승인, 예외적 BC 간 참조)
import com.wanted.momocity.admin.domain.access.AccessLog;
import com.wanted.momocity.admin.domain.access.AccessLogAction;
import com.wanted.momocity.admin.domain.access.AccessLogRepository;
import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    // [MS-4 접근로그] admin BC 접근로그 저장을 위해 추가 (auth BC 담당자 승인, 예외적 BC 간 참조)
    private final AccessLogRepository accessLogRepository;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"timestamp\": \"" + java.time.LocalDateTime.now() + "\", \"status\": 403, \"code\": \"FORBIDDEN\", \"message\": \"접근 권한이 없습니다.\"}"
        );

        // [MS-4 접근로그] 403 발생 시 FORBIDDEN 액션 기록 (auth BC 담당자 승인)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails)
                ? userDetails.getUserId()
                : null;
        accessLogRepository.save(AccessLog.create(userId, request.getRemoteAddr(), AccessLogAction.FORBIDDEN));
    }

}
