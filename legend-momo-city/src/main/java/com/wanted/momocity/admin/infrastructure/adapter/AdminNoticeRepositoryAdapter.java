package com.wanted.momocity.admin.infrastructure.adapter;

/* comment.
    도메인이 선언한 AdminNoticeRepository 게약을 실제로 구현하는 어댑터.
    SpringDataAdminNoticeRepository 에 위임해서 도메인과 JPA 사이클 연결한다.
 */

import com.wanted.momocity.admin.domain.notice.AdminNotice;
import com.wanted.momocity.admin.domain.notice.AdminNoticeRepository;
import com.wanted.momocity.admin.infrastructure.persistence.AdminNoticeJpaEntity;
import com.wanted.momocity.admin.infrastructure.persistence.SpringDataAdminNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdminNoticeRepositoryAdapter implements AdminNoticeRepository {

    // 실제 DB 작업은 SpringData 에 위임
    private final SpringDataAdminNoticeRepository springDataRepo;

    // 도메인 객체 : JPA 엔티티 변경 후 저장, 저장된 엔티티를 다시 도메인으로 변환해 반환
    @Override
    public AdminNotice save(AdminNotice notice) {
        return springDataRepo.save(AdminNoticeJpaEntity.fromDomain(notice)).toDomain();
    }

    // 고정 공지 상단 우선 정렬(isPinned DESC → createdAt DESC) 후 도메인으로 변환해 반환
    @Override
    public Page<AdminNotice> findAll(Pageable pageable) {
        return springDataRepo.findAllOrderByIsPinnedFirst(pageable).map(AdminNoticeJpaEntity::toDomain);
    }

    // isPinned 필터 조회 후 도메인으로 변환해 반환
    @Override
    public Page<AdminNotice> findByIsPinned(boolean isPinned, Pageable pageable) {
        return springDataRepo.findByIsPinned(isPinned, pageable).map(AdminNoticeJpaEntity::toDomain);
    }

    // 고정 공지 뺀 일반 공지만 최신순 페이지네이션 조회 후 도메인으로 변환
    @Override
    public Page<AdminNotice> findUnpinned(Pageable pageable) {
        return springDataRepo.findByIsPinnedFalseOrderByCreatedAtDesc(pageable)
                .map(AdminNoticeJpaEntity::toDomain);
    }

    // isPinned=false 인 공지 개수를 독립적으로 세어 반환
    @Override
    public long countUnpinned() {
        return springDataRepo.countByIsPinnedFalse();
    }

    // id 로 단건 조회 후 도메인으로 변환, 없으면 Optional.empty() 반환
    @Override
    public Optional<AdminNotice> findById(Long id) {
        return springDataRepo.findById(id).map(AdminNoticeJpaEntity::toDomain);
    }

    // id 목록으로 선택 삭제 : 없는 id 는 Spring Data 가 자동으로 무시
    @Override
    public void deleteAllByIds(List<Long> ids) {
        springDataRepo.deleteAllByIdIn(ids);
    }

    // 단건 삭제
    @Override
    public void delete(Long id) {
        springDataRepo.deleteById(id);
    }

    // 현재 고정된 공지를 SpringData 에서 조회 후 도메인으로 변환, 없으면 Optional.empty() 반환
    @Override
    public Optional<AdminNotice> findPinned() {
        return springDataRepo.findByIsPinnedTrue().map(AdminNoticeJpaEntity::toDomain);
    }

}
