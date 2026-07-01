package com.wanted.momocity.admin.infrastructure.adapter;

import com.wanted.momocity.admin.domain.access.AccessLog;
import com.wanted.momocity.admin.domain.access.AccessLogAction;
import com.wanted.momocity.admin.domain.access.AccessLogRepository;
import com.wanted.momocity.admin.infrastructure.persistence.AccessLogJpaEntity;
import com.wanted.momocity.admin.infrastructure.persistence.SpringDataAccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/* comment.
    AccessLogRepository(domain) 구현체.
    SpringDataAccessLogRepository 에 위임하고 결과를 도메인 객체로 변환한다.
 */
@Repository
@RequiredArgsConstructor
public class AccessLogRepositoryAdapter implements AccessLogRepository {

    private final SpringDataAccessLogRepository springDataAccessLogRepository;

    // action 필터 없이 user_id 전체 조회.
    @Override
    public Page<AccessLog> findAll(Pageable pageable) {
        return springDataAccessLogRepository
                .findAll(pageable)
                .map(AccessLogJpaEntity::toDomain);
    }

    // 위의 Override 와 동일하다. 다만, action 조건이 추가된다.
    // action.name() 으로 enum -> String 변환해서 JPA 에 넘긴다.
    @Override
    public Page<AccessLog> findByAction(AccessLogAction action, Pageable pageable) {
        return springDataAccessLogRepository
                .findByAction(action.name(), pageable)
                .map(AccessLogJpaEntity::toDomain);
    }

    @Override
    public List<AccessLog> findRecent(int limit) {
        return springDataAccessLogRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(AccessLogJpaEntity::toDomain)
                .toList();
    }

    // 도메인 객체를 엔티티로 변환 -> 저장 -> 저장된 결과를
    // 다시 도메인 객체로 변환해서 반환

    @Override
    public AccessLog save(AccessLog accessLog) {
        AccessLogJpaEntity entity = AccessLogJpaEntity.toEntity(accessLog);
        AccessLogJpaEntity saved = springDataAccessLogRepository.save(entity);
        return saved.toDomain();
    }

}