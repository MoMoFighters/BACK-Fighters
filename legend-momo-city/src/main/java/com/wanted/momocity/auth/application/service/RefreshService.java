package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.port.BlacklistPort;
import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.application.port.RedisRefreshTokenPort;
import com.wanted.momocity.auth.application.port.TokenProviderPort;
import com.wanted.momocity.auth.application.usecase.NewTokenUsecase;
import com.wanted.momocity.auth.domain.model.Status;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.infrastructure.exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RefreshService implements NewTokenUsecase {
    // 리프레시 토큰으로 새로운 액세스 토큰 만들기

    private final TokenProviderPort tokenProviderPort;
    private final RedisRefreshTokenPort redisRefreshTokenPort;
    private final LoadUserPort loadUserPort;
    private final BlacklistPort blacklistPort;

    // 필터용 (기존 액세스 토큰 블랙리스트 처리 없음 - 이미 만료된 상태라 불필요)
    public String refreshAccessToken(String refreshToken) {
        return refreshAccessToken(refreshToken, null);
    }

    @Override
    public String refreshAccessToken(String refreshToken, String oldAccessToken) {
        // 토큰 유효성 검사
        tokenProviderPort.validateToken(refreshToken);

        // redis에서 존재하는 토큰인지 확인
        redisRefreshTokenPort.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("로그인이 만료되었습니다. 다시 로그인해주세요."));

        // 토큰에서 id 꺼내기
        String userId = tokenProviderPort.getIdFromToken(refreshToken);

        // 유저 조회
        User user = loadUserPort.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UsernameNotFoundException("조회된 유저가 없습니다."));

        // 상태 체크 추가
        if (user.getStatus() == Status.PENDING) {
            log.warn("[refresh] 토큰 연장 불가 | userId={} | 사유=미승인 계정", userId);
            throw new InvalidRefreshTokenException("아직 승인되지 않은 계정입니다.");
        }

        // 임시비번 로그인 유저는 토큰 연장 불가
        if (user.getIsTempPwd()) {
            log.warn("[refresh] 토큰 연장 불가 | userId={} | 사유=임시비밀번호 미변경", userId);
            throw new InvalidRefreshTokenException("임시 비밀번호를 변경 후 이용해주세요.");
        }

        // 기존 액세스 토큰 블랙리스트 등록
        if (oldAccessToken != null) {
            long remainingMillis = tokenProviderPort.getRemainingMillis(oldAccessToken);
            if (remainingMillis > 0) {
                blacklistPort.addBlacklist(oldAccessToken, remainingMillis);
            }
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                String.valueOf(user.getId()),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        log.info("[refresh] 액세스 토큰 재발급 | userId={}", userId);
        // 새 액세스 토큰 발급
        return tokenProviderPort.createAccessToken(authentication);
    }
}
