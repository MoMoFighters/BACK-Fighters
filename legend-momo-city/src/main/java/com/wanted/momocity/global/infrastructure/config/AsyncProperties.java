package com.wanted.momocity.global.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * AsyncConfig 에서 사용하는 ThreadPool 설정값을 외부화한다.
 * application.yaml 의 app.async.* 키로 주입된다.
 * 디폴트값은 yaml 에 위치한다 (yaml-first).
 */

@ConfigurationProperties(prefix = "app.async")
public record AsyncProperties(

         int corePoolSize,
         int maxPoolSize,
         int queueCapacity,
         String threadNamePrefix

) {

}
