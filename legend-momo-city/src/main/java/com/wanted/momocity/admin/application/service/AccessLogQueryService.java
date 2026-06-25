package com.wanted.momocity.admin.application.service;

import com.wanted.momocity.admin.application.usecase.AccessLogQueryUseCase;
import com.wanted.momocity.admin.domain.access.AccessLog;
import com.wanted.momocity.admin.domain.access.AccessLogAction;
import com.wanted.momocity.admin.domain.access.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* comment.
    AccessLogQueryUseCase 구현체.
    page + limit -> Pageable 로 변환해서 Repository 에 위임한다.
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// implements 를 해야만 UseCase 의 계약을 이행하는 클래스가 된다.
public class AccessLogQueryService implements AccessLogQueryUseCase {

    private final AccessLogRepository accessLogRepository;

    // 비로그인 제외 + 전체 접근 로그를 최신순으로 페이지 조회
    @Override
    public Page<AccessLog> getAll(int page, int limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        return accessLogRepository.findAllByUserIdIsNotNull(pageable);
    }

    // 비로그인 제외 + action 으로 필터링해서 최신순으로 페이지 조회
    @Override
    public Page<AccessLog> getByAction(AccessLogAction action, int page, int limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        return accessLogRepository.findByActionAndUserIdIsNotNull(action, pageable);
    }

}
