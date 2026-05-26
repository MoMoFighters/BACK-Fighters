package com.wanted.momocity.auth.domain.repository;

import com.wanted.momocity.auth.domain.model.UserOauth;

import java.util.Optional;

public interface UserOauthRepository {


    UserOauth save(UserOauth userOauth);

    Optional<UserOauth> findByProviderAndProviderId(String provider, String providerId);
}