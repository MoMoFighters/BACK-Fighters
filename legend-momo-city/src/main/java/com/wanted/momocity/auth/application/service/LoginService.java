package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.LoginCommand;
import com.wanted.momocity.auth.application.port.EmailCodePort;
import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.application.port.RedisRefreshTokenPort;
import com.wanted.momocity.auth.application.port.TokenProviderPort;
import com.wanted.momocity.auth.application.usecase.LoginUsecase;
import com.wanted.momocity.auth.domain.exception.TempPasswordExpiredException;
import com.wanted.momocity.auth.domain.model.Status;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.domain.exception.InactiveUserException;
import com.wanted.momocity.auth.domain.exception.InvalidCredentialsException;
import com.wanted.momocity.auth.presentation.api.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginService implements LoginUsecase {

    private final AuthenticationManager authenticationManager;
    private final TokenProviderPort tokenProviderPort;
    private final LoadUserPort loadUserPort;
    private final RedisRefreshTokenPort redisRefreshTokenPort;
    private final EmailCodePort emailCodePort;


    @Override
    public LoginResponse login(LoginCommand command) {

        // email로 유저 먼저 조회해서 id 꺼내기
        User user = loadUserPort.findByEmail(command.email())
                .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 이메일/비밀번호로 사용자 인증
        Authentication authentication;
        try{
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(String.valueOf(user.getId()), command.password())
        );

        }catch (BadCredentialsException e){
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (user.getStatus() != Status.ACTIVE) {
            throw new InactiveUserException("로그인이 제한된 계정입니다.");
        }

        if (user.getIsTempPwd() && !emailCodePort.isTempPasswordVerified(command.email())) {
            throw new TempPasswordExpiredException("임시 비밀번호가 만료되었습니다. 다시 발급해주세요.");
        }

        // 인증 성공 후 액세스 토큰 발급
        String accessToken = user.getIsTempPwd()
                ? tokenProviderPort.createTempAccessToken(authentication)
                : tokenProviderPort.createAccessToken(authentication);

        // 리프레시 토큰 발급
        String refreshToken = tokenProviderPort.createRefreshToken(String.valueOf(user.getId()));

        // 기존 리프레시 토큰 삭제 후 새로 저장
        redisRefreshTokenPort.save(
                String.valueOf(user.getId()),
                refreshToken,
                Instant.now().plusMillis(tokenProviderPort.getRefreshTokenValidityMilliseconds())
        );

        // 컨트롤러로 보내서 프론트에게 전달할 수 있도록 리턴
        long accessTokenExpiry = user.getIsTempPwd()
                ? 3 * 60  // 3분
                : tokenProviderPort.getAccessTokenValidityMilliseconds() / 1000;

        return new LoginResponse(accessToken, refreshToken, user.getStatus(), accessTokenExpiry);
    }
}
