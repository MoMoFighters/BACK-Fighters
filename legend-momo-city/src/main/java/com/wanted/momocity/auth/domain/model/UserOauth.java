package com.wanted.momocity.auth.domain.model;

import java.time.Instant;

public class UserOauth {

    private final Long id;
    private final User user;
    private final String provider;
    private final String providerId;
    private final Instant createdAt;

    private UserOauth(Long id, User user, String provider, String providerId, Instant createdAt) {
        this.id = id;
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
        this.createdAt = createdAt;
    }

    // 신규 생성
    public static UserOauth create(User user, String provider, String providerId) {
        return new UserOauth(null, user, provider, providerId, Instant.now());
    }

    // DB에서 꺼내 쓸 때
    public static UserOauth restore(Long id, User user, String provider, String providerId, Instant createdAt) {
        return new UserOauth(id, user, provider, providerId, createdAt);
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getProvider() { return provider; }
    public String getProviderId() { return providerId; }
    public Instant getCreatedAt() { return createdAt; }

}