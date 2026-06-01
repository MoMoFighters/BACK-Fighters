package com.wanted.momocity.auth.application.port;

import com.wanted.momocity.auth.domain.model.User;

import java.util.Optional;

public interface LoadUserPort {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);  

}
