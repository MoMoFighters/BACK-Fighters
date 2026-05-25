package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.LoginCommand;
import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.application.port.RefreshTokenRepositoryPort;
import com.wanted.momocity.auth.application.port.TokenProviderPort;
import com.wanted.momocity.auth.application.result.LoginResult;
import com.wanted.momocity.auth.application.usecase.LoginUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginService implements LoginUsecase {

    private final AuthenticationManager authenticationManager;
    private final TokenProviderPort tokenProviderPort;
    private final PasswordEncoder passwordEncoder;
    private final LoadUserPort loadUserPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;


    @Override
    public LoginResult login(LoginCommand command) {
        // 이메일/비밀번호로 사용자 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(command.email(), command.password())
        );

        // 인증 성공 후 액세스 토큰 발급
        String accessToken = tokenProviderPort.createAccessToken(authentication);

        // 리프레시 토큰 발급
        String refreshToken = tokenProviderPort.createRefreshToken(command.email());

        // 기존 리프레시 토큰 삭제 후 새로 저장
        refreshTokenRepositoryPort.deleteByEmail(command.email());
        refreshTokenRepositoryPort.save(
                command.email(),
                refreshToken,
                Instant.now().plusMillis(tokenProviderPort.getRefreshTokenValidityMilliseconds())
        );

        // 컨트롤러로 보내서 프론트에게 전달할 수 있도록 리턴
        return new LoginResult(accessToken, refreshToken);
    }
}
