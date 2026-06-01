package com.wanted.momocity.auth.infrastructure.jwt;

import com.wanted.momocity.auth.application.port.BlacklistPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class BlacklistAdapter implements BlacklistPort {

    /*comment
    *  블랙리스트 처리가 뭐냐
    *   로그아웃 → refresh 토큰을 삭제
    *   그러나 access 토큰은 살아있을 수도 있음
    *   그리고 그 액세스 토큰을 누가 훔쳐가면 그 토큰값을 멋대로 사용할 수 있음
    *  ---
    *  그래서 로그아웃 해서 리프레스 토큰이 사라지면 그에 맞는 access 토큰을 블랙리스트에 등록함
    *  이후에 요청이 들어오면 필터가 블랙리스트라면 차단해버림 !!
    * */

    private final StringRedisTemplate redisTemplate;

    // 로그아웃할 때 AccessToken을 블랙리스트에 등록
    @Override
    public void addBlacklist(String accessToken, long ttlMillis) {
        redisTemplate.opsForValue()
                .set(accessToken + "blacklist", "true", ttlMillis, TimeUnit.MILLISECONDS);
    }


    // 요청이 들어올 때 이 AccessToken이 블랙리스트에 있는지 확인
    @Override
    public boolean isBlacklisted(String accessToken) {
        return redisTemplate.hasKey(accessToken + "blacklist");
    }
}
