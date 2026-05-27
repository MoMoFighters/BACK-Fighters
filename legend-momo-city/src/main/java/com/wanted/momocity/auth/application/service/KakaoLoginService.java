package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.OAuthUserInfoCommand;
import com.wanted.momocity.auth.application.command.SocialLoginCommand;
import com.wanted.momocity.auth.application.port.OAuthClientPort;
import com.wanted.momocity.auth.application.port.TokenProviderPort;
import com.wanted.momocity.auth.application.usecase.KakaoLoginUsecase;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.domain.model.UserOauth;
import com.wanted.momocity.auth.domain.repository.UserOauthRepository;
import com.wanted.momocity.auth.domain.repository.UserRepository;
import com.wanted.momocity.auth.presentation.api.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class KakaoLoginService implements KakaoLoginUsecase {
    /*
    * 1. code → 카카오에 액세스토큰 요청 → 액세스토큰 받음
    * 2. 액세스토큰 → 카카오에 유저정보 요청 → { id, email, name } 받음
    * 3. 받은 값으로 OAuthUserInfoCommand 생성해서 반환
    * */

    // 카카오 로그인(소셜 로그인)은 새로운 유저를 db에 삽입(회원가입) + 토큰 생성(로그인)을 한 번에 진행

    private final OAuthClientPort kakaoOAuthClientPort;
    private final UserRepository userRepository;
    private final UserOauthRepository userOauthRepository;
    private final TokenProviderPort tokenProviderPort;

    @Override
    public LoginResponse socialLogin(SocialLoginCommand command) {
        // 카카오 API 호출 2번 해서 유저 정보 담음
        // KakaoOAuthClient 에 메서드 있음
        OAuthUserInfoCommand oAuthUserInfo = kakaoOAuthClientPort.getUserInfo(command.code());

        // 이미 카카오 로그인 한 사람인지 체크
        User user = userOauthRepository.findByProviderAndProviderId("KAKAO", oAuthUserInfo.providerId())
                .map(UserOauth::getUser)
                .orElseGet(() -> registerNewUser(oAuthUserInfo));

        // 인증 성공하면 JWT 토큰 발급
        String accessToken = tokenProviderPort.createAccessToken(
                String.valueOf(user.getId()),
                user.getRole().name()
        );
        String refreshToken = tokenProviderPort.createRefreshToken(
                String.valueOf(user.getId())
        );

        // 응답
        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getStatus(),
                tokenProviderPort.getAccessTokenValidityMilliseconds()
        );
    }

    // 새로운 유저 db에 등록
    private User registerNewUser(OAuthUserInfoCommand oAuthUserInfoCommand) {
        User newUser = userRepository.register(
                User.oAuthRegister(oAuthUserInfoCommand.email(), oAuthUserInfoCommand.name())
        );
        userOauthRepository.save(
                UserOauth.create(newUser, "KAKAO", oAuthUserInfoCommand.providerId())
        );
        return newUser;
    }
}