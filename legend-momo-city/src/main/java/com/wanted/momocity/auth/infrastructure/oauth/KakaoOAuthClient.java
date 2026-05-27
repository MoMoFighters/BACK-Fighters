package com.wanted.momocity.auth.infrastructure.oauth;

import com.wanted.momocity.auth.application.command.OAuthUserInfoCommand;
import com.wanted.momocity.auth.application.port.OAuthClientPort;
import com.wanted.momocity.auth.domain.exception.OAuthInvalidCodeException;
import com.wanted.momocity.auth.domain.exception.OAuthTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClientPort {

    // 엑세스 토큰 발급 및 유저 정보 가져오기

    private final WebClient webClient;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Override
    public OAuthUserInfoCommand getUserInfo(String code) {
        // 인가코드로 카카오 액세스토큰 요청
        String accessToken = getAccessToken(code);

        // 액세스토큰으로 유저정보 요청
        return getUserInfoFromKakao(accessToken);
    }

    private String getAccessToken(String code) {
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("code", code);

            Map<String, Object> response = webClient.post()
                    .uri("https://kauth.kakao.com/oauth/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue(params)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return (String) response.get("access_token");
        }catch (WebClientResponseException e){
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new OAuthInvalidCodeException("로그인에 실패하였습니다. 다시 시도해주세요.");
            }
            throw new OAuthTokenException("로그인에 실패하였습니다. 다시 시도해주세요.");
        }

    }

    private OAuthUserInfoCommand getUserInfoFromKakao(String accessToken) {
        try {

            Map<String, Object> response = webClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String providerId = String.valueOf(response.get("id"));

            Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
            Map<String, Object> properties = (Map<String, Object>) response.get("properties");

            String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
            String name = properties != null ? (String) properties.get("nickname") : "카카오유저";

            return new OAuthUserInfoCommand(providerId, email, name);
        }catch (WebClientResponseException e){
            throw new OAuthTokenException("유저 정보 조회에 실패했습니다.");
        }
    }
}
