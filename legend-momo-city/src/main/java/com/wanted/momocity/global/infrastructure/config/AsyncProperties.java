package com.wanted.momocity.global.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * AsyncConfig 에서 사용하는 ThreadPool 설정값을 외부화한다.
 * application.yaml 에 async.* 로 오버라이드할 수 있다.
 * 미설정 시 아래 기본값이 적용된다.
 */

@ConfigurationProperties(prefix = "app.async")
public record AsyncProperties(

         int corePoolSize,
         int maxPoolSize,
         int queueCapacity,
         String threadNamePrefix

) {

}
