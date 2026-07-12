package com.wanted.momocity.payment.infrastructure.portone;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "portone")
@Getter
public class PortOneProperties {
    private String storeId;
    private String apiSecret;
    private String baseUrl;

    public PortOneProperties(String storeId, String apiSecret, String baseUrl) {
        this.storeId = storeId;
        this.apiSecret = apiSecret;
        this.baseUrl = baseUrl;
    }
}
