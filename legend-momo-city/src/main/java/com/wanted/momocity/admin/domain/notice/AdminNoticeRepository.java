package com.wanted.momocity.admin.domain.notice;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
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

    // MA-02(M5) : 고정 공지 분리 후 일반 공지만 최신순 페이지네이션 조회
    Page<AdminNotice> findUnpinned(Pageable pageable);


    // MA-02(M5) : isPinned=false 인 공지 개수를 페이지 위치와 무관하게 세는 계약
    long countUnpinned();

    // MS-16, 17, 18 : id 로 공지 단건 조회
    // findById 는 해당 id 의 공지가 DB 에 없을 수도 있어서 Optional 사용
    Optional<AdminNotice> findById(Long id);

    // MS-18 : 공지 단건 삭제
    void delete(Long id);

    // MS-19 : 공지 선택 삭제
    void deleteAllByIds(List<Long> ids);

    // 공지가 0 개 일수도 있기 때문에 null 체크 없이 .ifPresent( ) 로 안전하게 처리할 수 있다.
    // MS-21, 22 : 현재 고정된 공지 단건 조회 (없으면 Optional.empty())
    Optional<AdminNotice> findPinned();

}
