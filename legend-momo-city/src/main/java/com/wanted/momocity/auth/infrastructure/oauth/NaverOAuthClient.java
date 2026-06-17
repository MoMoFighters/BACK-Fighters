package com.wanted.momocity.auth.infrastructure.oauth;

import com.wanted.momocity.auth.application.command.OAuthUserInfoCommand;
import com.wanted.momocity.auth.application.port.OAuthClientPort;
import com.wanted.momocity.auth.domain.exception.OAuthInvalidCodeException;
import com.wanted.momocity.auth.domain.exception.OAuthTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class NaverOAuthClient implements OAuthClientPort {

    private final WebClient webClient;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;


    @Override
    public OAuthUserInfoCommand getUserInfo(String code) {
        // 인가코드로 액세스토큰 요청
        String accessToken = getAccessToken(code);

        // 액세스토큰으로 유저정보 요청
        return getUserInfoFromNaver(accessToken);
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
                    .uri("https://nid.naver.com/oauth2.0/token")
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


    private OAuthUserInfoCommand getUserInfoFromNaver(String accessToken) {
        try {
            Map<String, Object> body = webClient.get()
                    .uri("https://openapi.naver.com/v1/nid/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Map<String, Object> response = (Map<String, Object>) body.get("response");

            String providerId = String.valueOf(response.get("id"));
            String email = (String) response.get("email");
            String name = response.get("name") != null ? (String) response.get("name") : "네이버유저";

            return new OAuthUserInfoCommand(providerId, email, name);

        } catch (WebClientResponseException e) {
            log.error("[naver] 유저정보 조회 실패 | status={} | body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new OAuthTokenException("유저 정보 조회에 실패했습니다.");
        }
    }


}
