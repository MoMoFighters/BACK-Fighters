package com.wanted.momocity.admin.domain.notice;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
/* comment.
    공지 영속화 계약.
    구현한 AdminNoticeRepositoryAdapter 가 담당한다.
 */

public interface AdminNoticeRepository {

    // MS-11 공지 저장
    AdminNotice save(AdminNotice notice);

    // MS-12 전체 목록 조회
    Page<AdminNotice> findAll(Pageable pageable);

    // MS - 12 isPinned 필터 조회
    Page<AdminNotice> findByIsPinned(boolean isPinned, Pageable pageable);

}
