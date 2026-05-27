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
public class GoogleOAuthClient implements OAuthClientPort {

    private final WebClient webClient;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Override
    public OAuthUserInfoCommand getUserInfo(String code) {
        // 인가코드로 액세스토큰 요청
        String accessToken = getAccessToken(code);

        // 액세스토큰으로 유저정보 요청
        return getUserInfoFromGoogle(accessToken);
    }

    private String getAccessToken(String code) {
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("code", code);

            // 구글 서버에 정보 담아서 POST 요청 보내기 : 액세스 토큰 만들어주세요
            Map<String, Object> response = webClient.post()
                    .uri("https://oauth2.googleapis.com/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue(params)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // 구글이 json으로 응답 줌 - Map으로 받으면0 WebClient가 JSON을 자동으로 변환해줌
            return (String) response.get("access_token");

        }catch (WebClientResponseException e){
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new OAuthInvalidCodeException("로그인에 실패하였습니다. 다시 시도해주세요.");
            }
            throw new OAuthTokenException("로그인에 실패하였습니다. 다시 시도해주세요.");
        }
    }

    private OAuthUserInfoCommand getUserInfoFromGoogle(String accessToken) {
        try {
            // 사용자 정보도 json으로 넘겨줌
            Map<String, Object> response = webClient.get()
                    .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String providerId = String.valueOf(response.get("id"));
            String email = (String) response.get("email");
            String name = response.get("name") != null ? (String) response.get("name") : "구글유저";

            return new OAuthUserInfoCommand(providerId, email, name);

        }catch (WebClientResponseException e){
            throw new OAuthTokenException("유저 정보 조회에 실패했습니다.");
        }
    }
}