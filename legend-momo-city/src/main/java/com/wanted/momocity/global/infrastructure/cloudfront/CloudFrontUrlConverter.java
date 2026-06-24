package com.wanted.momocity.global.infrastructure.cloudfront;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CloudFrontUrlConverter {

    private final CloudFrontProperties cloudFrontProperties;

    public String convert(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            return null;
        }
        return cloudFrontProperties.getDomain() + "/" + s3Key;
    }

}
