package com.wanted.momocity.admin.application.service;

import com.wanted.momocity.admin.application.usecase.AdminNoticeQueryUseCase;
import com.wanted.momocity.admin.domain.notice.AdminNotice;
import com.wanted.momocity.admin.domain.notice.AdminNoticeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;



@Service
@RequiredArgsConstructor
// 일긱 전용으로
@Transactional(readOnly = true)

/* comment.
    조회 전용 서비스.
    목록,상세 조회 요청을 받아 repo 에서 데이터를 꺼내 반환한다.
 */

public class AdminNoticeQueryService implements AdminNoticeQueryUseCase {

    private final AdminNoticeRepository adminNoticeRepository;


    // isPinned 값과 페이지 정보를 받아서 repo 에 그대로 넘긴다.
    // 결과로 Page<AdminNotice> 를 반환하는데,
    // Page 는 공지 목록 + 전체 개수 + 현재페이지 번호 등 페이지 메타 정보를 담은 묶음
    @Override
    public Page<AdminNotice> getNoticeList(boolean isPinned, Pageable pageable) {
        return adminNoticeRepository.findByIsPinned(isPinned, pageable);
    }


    // id 로 공지 단건을 DB 에서 꺼낸다.
    // .orElseThrow() 는 Optional 이 비어 있을 때, 즉, 해당 id 공지가 없을 때 즉시 에외를 던진다.
    @Override
    public AdminNotice getNoticeDetail(Long id) {
        return adminNoticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 공지입니다."));
    }

}
