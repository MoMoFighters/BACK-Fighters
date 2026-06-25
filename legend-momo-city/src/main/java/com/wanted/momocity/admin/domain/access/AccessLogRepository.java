package com.wanted.momocity.admin.domain.access;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/* comment.
    접근 로그 조회 계약. 구현은 AccessLogRepositoryAdapter 가 담당한다.
 */
public interface AccessLogRepository {

    // user_id IS NOT NULL 조건 포함 — 비로그인 외부 접근 제외
    Page<AccessLog> findAllByUserIdIsNotNull(Pageable pageable);

    // action 필터 + user_id IS NOT NULL
    Page<AccessLog> findByActionAndUserIdIsNotNull(AccessLogAction action, Pageable pageable);
}