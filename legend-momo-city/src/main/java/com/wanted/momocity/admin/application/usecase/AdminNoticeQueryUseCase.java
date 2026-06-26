package com.wanted.momocity.admin.application.usecase;

import com.wanted.momocity.admin.domain.notice.AdminNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/* comment.
    AdminNoticeQueryUseCase
    Controller 가 공지 목록 상세 조회를 요청할 때 호출할 메서드를 정의한 읽기 전용 계약 인터페이스
 */

public interface AdminNoticeQueryUseCase {

    //MS-12 공지 목록 조회
    Page<AdminNotice> getNoticeList(boolean isPinned, Pageable pageable);

    // MS-16 공지 상세 조회
    AdminNotice getNoticeDetail(Long id);
}
