package com.wanted.momocity.user.domain.repository;

import com.wanted.momocity.user.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long userId);
}
