package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.OAuthUserInfoCommand;
import com.wanted.momocity.auth.application.command.SocialLoginCommand;
import com.wanted.momocity.auth.application.port.OAuthClientPort;
import com.wanted.momocity.auth.application.port.TokenProviderPort;
import com.wanted.momocity.auth.application.usecase.GoogleLoginUsecase;
import com.wanted.momocity.auth.application.usecase.KakaoLoginUsecase;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.domain.model.UserOauth;
import com.wanted.momocity.auth.domain.repository.UserOauthRepository;
import com.wanted.momocity.auth.domain.repository.UserRepository;
import com.wanted.momocity.auth.presentation.api.response.LoginResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class SocialLoginService implements KakaoLoginUsecase, GoogleLoginUsecase {
    /*소셜 로그인 기본 flow
    * 1. 사용자 → 소셜 로그인 버튼 클릭
    * 2. 카카오/구글 인증 페이지에서 동의 → 인가코드 발급
    * 3. [서버] 인가코드로 액세스토큰 요청 ← API 호출 1
    * 4. [서버] 액세스토큰으로 유저정보 요청 ← API 호출 2
    * 5. 유저정보로 자체 로그인/회원가입 처리
    * */

    private final Map<String, OAuthClientPort> oAuthClientPorts;
    private final UserRepository userRepository;
    private final UserOauthRepository userOauthRepository;
    private final TokenProviderPort tokenProviderPort;

    public SocialLoginService(
            @Qualifier("kakaoOAuthClient")
            OAuthClientPort kakaoOAuthClientPort,
            @Qualifier("googleOAuthClient")
            OAuthClientPort googleOAuthClientPort,
            UserRepository userRepository,
            UserOauthRepository userOauthRepository,
            TokenProviderPort tokenProviderPort
    ) {
        this.oAuthClientPorts = Map.of(
                "KAKAO", kakaoOAuthClientPort,
                "GOOGLE", googleOAuthClientPort
        );
        this.userRepository = userRepository;
        this.userOauthRepository = userOauthRepository;
        this.tokenProviderPort = tokenProviderPort;
    }

    @Override
    public LoginResponse socialLogin(SocialLoginCommand command) {
        // provider가 카카오면 kakaoOAuthClientPort
        // provider가 구글이면 googleOAuthClientPort
        OAuthClientPort oAuthClientPort = oAuthClientPorts.get(command.provider());
        // 거기서 api에 요청 두번 보내서 access 토큰 요청 + 유저 정보 요청하고 사용자 정도 담음
        OAuthUserInfoCommand oAuthUserInfo = oAuthClientPort.getUserInfo(command.code());

        // 이미 소셜 로그인 한 사람인지 확인
        User user = userOauthRepository.findByProviderAndProviderId(command.provider(), oAuthUserInfo.providerId())
                .map(UserOauth::getUser)
                .orElseGet(() -> registerNewUser(command.provider(), oAuthUserInfo));


        // 인증 성공하면 JWT 토큰 발급
        String accessToken = tokenProviderPort.createAccessToken(
                String.valueOf(user.getId()),
                user.getRole().name()
        );
        String refreshToken = tokenProviderPort.createRefreshToken(
                String.valueOf(user.getId())
        );


        // 응답
        return new LoginResponse(accessToken, refreshToken, user.getStatus(),
                tokenProviderPort.getAccessTokenValidityMilliseconds());
    }

    // 새로운 유저 db에 등록
    private User registerNewUser(String provider, OAuthUserInfoCommand oAuthUserInfoCommand) {
        User newUser = userRepository.register(
                User.oAuthRegister(oAuthUserInfoCommand.email(), oAuthUserInfoCommand.name())
        );
        userOauthRepository.save(
                UserOauth.create(newUser, provider, oAuthUserInfoCommand.providerId())
        );
        return newUser;
    }
}