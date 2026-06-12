package com.wanted.momocity.viewing.application.port;

import com.wanted.momocity.auth.domain.model.User;

import java.util.Optional;

public interface UserPort {
    Optional<User> findById(Long userId);
}
