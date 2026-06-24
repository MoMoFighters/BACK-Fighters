package com.wanted.momocity.global.infrastructure.cloudfront;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "cloud.aws.cloudfront")
public class CloudFrontProperties {

    private final String domain;

    public CloudFrontProperties(String domain) {
        this.domain = domain;
    }

}
