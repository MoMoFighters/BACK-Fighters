package com.wanted.momocity.admin.application.service;

import com.wanted.momocity.admin.application.command.CreateNoticeCommand;
import com.wanted.momocity.admin.application.usecase.AdminNoticeCommandUseCase;
import com.wanted.momocity.admin.domain.notice.AdminNotice;
import com.wanted.momocity.admin.domain.notice.AdminNoticeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/* comment.
    UseCase 계약을 실제로 구현하는 클래스이다.
    도메인 객체를 생성, 수정, 삭제하고 repo 를 통해 DB 반영
 */

@Service
@RequiredArgsConstructor
@Transactional
public class AdminNoticeCommandService implements AdminNoticeCommandUseCase {

    private final AdminNoticeRepository adminNoticeRepository;

    // CreateNoticeCommand 에서 title, content isPinned 를 꺼내서 AdminNotice.create로 도메인 객체를 만든다.
    @Override
    public void createNotice(CreateNoticeCommand command) {
        AdminNotice notice = AdminNotice.create(command.title(), command.content(), command.isPinned());
        adminNoticeRepository.save(notice);
    }

    // id 로 공지를 DB 에서 꺼낸다.
    // 없다면 .orElseThrow() 가 즉시 예외를 던져 이후 코드를 싱행하지 않는다.
    @Override
    public void updateNotice(Long id, String title, String content) {
        AdminNotice notice = adminNoticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 공지입니다."));
        notice.update(title, content);
        adminNoticeRepository.save(notice);
    }

    // 삭제 전에 findById 로 먼저 존재 여부를 확인한다.
    // 없으면 404 exception 처리
    @Override
    public void deleteNotice(Long id) {
        adminNoticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 공지입니다."));
        adminNoticeRepository.delete(id);
    }

    // id 목록을 그대로 repo 에 넘긴다.
    // MS-19 명세서 조건이 없는 id 는 무시하고 나머지 정상 처리라서 존재
    // 여부 확인 없이 바로 넘긴다.
    @Override
    public void deleteNotices(List<Long> ids) {
        adminNoticeRepository.deleteAllByIds(ids);
    }

    // MS-21 공지 고정 : target 존재 확인 먼저 → 없는 id 요청 시 기존 고정 공지가 의도치 않게 해제되는 버그 방지
    @Override
    public void pinNotice(Long id) {
        AdminNotice target = adminNoticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 공지입니다."));
        adminNoticeRepository.findPinned()
                .ifPresent(prev -> {
                    prev.unpin();
                    adminNoticeRepository.save(prev);
                });
        target.pin();
        adminNoticeRepository.save(target);
    }

    // MS-22 공지 고정 해제
    @Override
    public void unpinNotice(Long id) {
        AdminNotice target = adminNoticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 공지입니다."));
        target.unpin();
        adminNoticeRepository.save(target);
    }

}