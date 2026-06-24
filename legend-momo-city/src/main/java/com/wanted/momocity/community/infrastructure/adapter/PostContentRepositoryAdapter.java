package com.wanted.momocity.community.infrastructure.adapter;

import com.wanted.momocity.community.domain.model.PostContent;
import com.wanted.momocity.community.domain.repository.PostContentRepository;
import com.wanted.momocity.community.infrastructure.persistence.PostContentJpaEntity;
import com.wanted.momocity.community.infrastructure.persistence.PostContentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/*
* comment.
*  PostContentRepository 인터페이스 구현체
*  -> domain.repository 인터페이스 <- 구현 -> JpaRepository 연결
* */

@Component
@RequiredArgsConstructor
public class PostContentRepositoryAdapter implements PostContentRepository {

    private final PostContentJpaRepository postContentJpaRepository;

    @Override
    public PostContent save(PostContent postContent) {
        return postContentJpaRepository.save(PostContentJpaEntity.from(postContent)).toDomain();
    }

    @Override
    public List<PostContent> saveAll(List<PostContent> postContents) {
        return postContents.stream()
                .map(PostContentJpaEntity::from)
                .map(postContentJpaRepository::save)
                .map(PostContentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<PostContent> findAllByPostId(Long postId) {
        return postContentJpaRepository
                .findAllByPostIdOrderByOrderNoAsc(postId)
                .stream()
                .map(PostContentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteAllByPostId(Long postId) {
        postContentJpaRepository.deleteAllByPostId(postId);
    }
}
