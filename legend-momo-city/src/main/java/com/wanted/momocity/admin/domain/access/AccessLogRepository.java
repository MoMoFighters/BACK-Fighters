package com.wanted.momocity.admin.domain.access;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/* comment.
    접근 로그 조회 계약. 구현은 AccessLogRepositoryAdapter 가 담당한다.
 */
public interface AccessLogRepository {

    // 비로그인 포함 전체 접근 로그 페이지 조회
    Page<AccessLog> findAll(Pageable pageable);

    // action 필터 + 비로그인 포함
    Page<AccessLog> findByAction(AccessLogAction action, Pageable pageable);

    // 최근 접근 로그 N 개 ( userId null 포함 - 비로그인 FORBIDDEN 접근도 포함 )
    List<AccessLog> findRecent(int limit);
}