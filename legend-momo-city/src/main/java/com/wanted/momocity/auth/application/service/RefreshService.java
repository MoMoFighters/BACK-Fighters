package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.application.port.RefreshTokenRepositoryPort;
import com.wanted.momocity.auth.application.port.TokenProviderPort;
import com.wanted.momocity.auth.domain.model.Status;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.infrastructure.exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshService {

    private final TokenProviderPort tokenProviderPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final LoadUserPort loadUserPort;

    public String refreshAccessToken(String refreshToken) {
        // 토큰 유효성 검사
        tokenProviderPort.validateToken(refreshToken);

        // DB에서 존재하는 토큰인지 확인
        refreshTokenRepositoryPort.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("토큰이 없습니다."));

        // 토큰에서 id 꺼내기
        String userId = tokenProviderPort.getIdFromToken(refreshToken);

        // 유저 조회
        User user = loadUserPort.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UsernameNotFoundException("조회된 유저가 없습니다."));

        // 상태 체크 추가
        if (user.getStatus() == Status.PENDING) {
            throw new InvalidRefreshTokenException("아직 승인되지 않은 계정입니다.");
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                String.valueOf(user.getId()),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        // 새 액세스 토큰 발급
        return tokenProviderPort.createAccessToken(authentication);
    }
}
