package com.wanted.momocity.admin.application.service;

import com.wanted.momocity.admin.application.usecase.AdminNoticeQueryUseCase;
import com.wanted.momocity.admin.domain.notice.AdminNotice;
import com.wanted.momocity.admin.domain.notice.AdminNoticeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
    public Page<AdminNotice> getNoticeList(Boolean isPinned, Pageable pageable) {
        // null 이라면 전체 조회, 값이 있다면 isPinned 필터 조회
        if (isPinned == null) {
            // 1. 고정 공지 조회 (없으면 Optional.empty())
            Optional<AdminNotice> pinnedNotice = adminNoticeRepository.findPinned();

            // 2. 고정 공지가 없으면 합칠 게 없으니 기존 방식 그대로
            if (pinnedNotice.isEmpty()) {
                return adminNoticeRepository.findAll(pageable);
            }

            // 3. 고정 공지 1자리를 미리 빼두고 일반 공지 페이지 크기 계산 (최소 1은 보장)
            int unpinnedSize = Math.max(pageable.getPageSize() - 1, 1);
            Pageable unpinnedPageable = PageRequest.of(pageable.getPageNumber(), unpinnedSize);
            Page<AdminNotice> unpinnedPage = adminNoticeRepository.findUnpinned(unpinnedPageable);

            // 4. 고정 공지를 맨 앞에 붙여서 매 페이지마다 항상 포함되게 함
            List<AdminNotice> merged = new ArrayList<>();
            merged.add(pinnedNotice.get());
            merged.addAll(unpinnedPage.getContent());


            // size=1처럼 극단적인 경우, 고정 공지 때문에 요청한 size를 넘을 수 있어 잘라냄
            if (merged.size() > pageable.getPageSize()) {
                merged = merged.subList(0, pageable.getPageSize());
            }

            // 5. totalPages 계산 기준을 "일반 공지 개수 ÷ unpinnedSize"로 정확히 맞추기 위해
            //    Page 자체의 pageable도 unpinnedPageable(size 하나 뺀 것)로 구성함
            /* comment.
                독립적인 COUNT 쿼리라 몇 페이지를 요청하든 항상 같은 값이 나오게 된다.
             */
            return new FixedTotalPage<>(merged, unpinnedPageable, adminNoticeRepository.countUnpinned());
        }
        // 값이 있는 경우
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
