package com.wanted.momocity.chatbot.infrastructure.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "momo-ai")
@Getter
public class PolicySearchProperties {

    private String baseUrl;

    public PolicySearchProperties(String baseUrl) {
        this.baseUrl = baseUrl;
    }

}
