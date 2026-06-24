package com.wanted.momocity;

import com.wanted.momocity.global.infrastructure.cloudfront.CloudFrontProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(CloudFrontProperties.class)
public class LegendMomoCityApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegendMomoCityApplication.class, args);
    }

}
