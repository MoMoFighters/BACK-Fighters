package com.wanted.momocity.global.infrastructure.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/* comment.
    클라이언트 실제 IP를 조회하는 공용 유틸.
    리버스 프록시(Nginx 등) 뒤에 있으면 request.getRemoteAddr()가
    프록시 자신의 루프백 주소(0:0:0:0:0:0:0:1)를 반환하는 문제가 있어서,
    X-Forwarded-For 헤더를 우선 확인하고 없으면 getRemoteAddr()로 폴백한다.
 */
@Slf4j
public class ClientIpResolver {

    private ClientIpResolver() {}

    public static String resolve(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
               log.info("[ClientIpResolver] X-Forwarded-For 원본 값 = {} | getRemoteAddr() = {}", forwarded, request.getRemoteAddr());

        if (forwarded != null && !forwarded.isBlank()) {
            // 콤마로 여러 IP가 나열될 수 있음 (클라이언트IP, 프록시1IP, ...) - 맨 앞이 진짜 클라이언트
                       String resolved = forwarded.split(",")[0].trim();
                       log.info("[ClientIpResolver] X-Forwarded-For 사용 -> 최종 IP = {}", resolved);
                       return resolved;
        }
               log.info("[ClientIpResolver] X-Forwarded-For 없음 -> getRemoteAddr() 사용 = {}", request.getRemoteAddr());
        return request.getRemoteAddr();
    }
}