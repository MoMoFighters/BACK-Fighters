package com.wanted.momocity.study.infrastructure.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;


@Configuration
public class StudyRedisScriptConfig {

    /*
     * comment.
     *  GroupRoomMemberCountAdapter.tryIncrement()에서 사용하는 Lua 스크립트 빈 등록
     *  GET + INCR을 하나의 원자적 명령으로 묶어, "초과했다가 되돌리는" 순간이 존재하지 않도록 함
     * */
    @Bean
    public RedisScript<Long> tryIncrementScript() {
        return RedisScript.of(
                new ClassPathResource("scripts/try_increment.lua"),
                Long.class
        );
    }

}
