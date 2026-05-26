package com.wanted.momocity.auth.infrastructure.email;

import com.wanted.momocity.auth.application.port.EmailCodePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class EmailCodeAdapter implements EmailCodePort {

    private final StringRedisTemplate redisTemplate;

    // 인증 코드 발송하고 redis에 키를 이메일, 값을 code로 저장
    @Override
    public void save(String email, String code, long ttlSeconds) {
        redisTemplate.opsForValue().set(email, code, ttlSeconds, TimeUnit.SECONDS);
    }

    // 사용자가 입력한 코드랑 서버가 보낸 거랑 같은 지 검증할 때 redis에서 값 가져오기
    @Override
    public String find(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    // 인증 완료하고서 쓴 코드 삭제
    @Override
    public void delete(String email) {
        redisTemplate.delete(email);
    }

    // 이메일 인증 하면 이메일 인증 했다는 상태를 저장 -> 회원가입 할 때 인증 안 했으면 예외
    @Override
    public void saveVerified(String email, long ttlSeconds) {
        redisTemplate.opsForValue()
                .set( email+"verified", "true", ttlSeconds, TimeUnit.SECONDS);
        // 키값을 그냥 email이라고 하면 인증코드를 없애버려서 구분용으로 +"verified" 추가
    }

    // 실제로 이메일 인증 했으면 true / 아니면 fasle
    @Override
    public boolean isVerified(String email) {
        return redisTemplate.hasKey(email+"verified");
    }

    // 인증 완료 상태 제거
    @Override
    public void deleteVerified(String email) {
        redisTemplate.delete(email+"verified");
    }

    @Override
    public void saveTempPassword(String email, long ttlSeconds) {
        redisTemplate.opsForValue()
                .set(email + "tempPwd", "true", ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean isTempPasswordVerified(String email) {
        return redisTemplate.hasKey(email + "tempPwd");
    }

    @Override
    public void deleteTempPassword(String email) {
        redisTemplate.delete(email + "tempPwd");
    }
}
