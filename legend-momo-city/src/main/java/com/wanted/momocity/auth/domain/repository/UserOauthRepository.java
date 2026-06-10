package com.wanted.momocity.auth.domain.repository;

import com.wanted.momocity.auth.domain.model.Provider;
import com.wanted.momocity.auth.domain.model.UserOauth;

import java.util.Optional;

public interface UserOauthRepository {


    UserOauth save(UserOauth userOauth);

    Optional<UserOauth> findByProviderAndProviderId(Provider provider, String providerId);
}