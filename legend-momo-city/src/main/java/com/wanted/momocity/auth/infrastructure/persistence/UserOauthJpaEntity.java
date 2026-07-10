package com.wanted.momocity.auth.infrastructure.persistence;

import com.wanted.momocity.auth.domain.model.Provider;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_oauth")
public class UserOauthJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected UserOauthJpaEntity() {}

    public UserOauthJpaEntity(UserJpaEntity user, Provider provider, String providerId, LocalDateTime createdAt) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public UserJpaEntity getUser() { return user; }
    public Provider getProvider() { return provider; }
    public String getProviderId() { return providerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
