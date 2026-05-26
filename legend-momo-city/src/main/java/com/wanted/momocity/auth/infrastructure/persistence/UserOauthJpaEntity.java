package com.wanted.momocity.auth.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_oauth")
public class UserOauthJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @Column(nullable = false)
    private String provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserOauthJpaEntity() {}

    public UserOauthJpaEntity(UserJpaEntity user, String provider, String providerId, Instant createdAt) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public UserJpaEntity getUser() { return user; }
    public String getProvider() { return provider; }
    public String getProviderId() { return providerId; }
    public Instant getCreatedAt() { return createdAt; }
}
