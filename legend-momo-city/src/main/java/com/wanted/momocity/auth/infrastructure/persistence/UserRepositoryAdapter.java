package com.wanted.momocity.auth.infrastructure.persistence;

import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.domain.repository.StudentSignupRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UserRepositoryAdapter implements StudentSignupRepository {

    // 순수한 자바 객체를 엔티티 객체로 만듦

    private final UserRepository userRepository;

    public UserRepositoryAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User register(User user) {
        return null;
    }
}
