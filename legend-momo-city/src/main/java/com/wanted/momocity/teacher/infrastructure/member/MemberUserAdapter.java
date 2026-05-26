package com.wanted.momocity.teacher.infrastructure.member;

import com.wanted.momocity.member.application.service.MemberQueryService;
import com.wanted.momocity.member.domain.repository.MemberRepository;
import com.wanted.momocity.teacher.application.port.UserQueryPort;
import com.wanted.momocity.teacher.application.port.UserStatusUpdatePort;
import com.wanted.momocity.teacher.domain.model.TeacherApplication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/*
 * REF: module00-clean-architecture enrollment/infrastructure/catalog/CatalogCourseAdapter.java
 *
 * MemberUserAdapter 는 강사 영역의 두 포트(UserQueryPort, UserStatusUpdatePort)를
 * 회원 영역(member BC)의 공개 컴포넌트(MemberQueryService, MemberRepository)로 구현하는 어댑터다.
 *
 * 이 어댑터가 있으므로 강사 영역의 응용 서비스는 회원 영역의 구체 클래스(UserJpaEntity, Member 등)를
 * 전혀 모르고도 동작한다.
 *
 * 주의:
 *  - 강사 신청자 필터(role='TEACHER' AND status='PENDING')는 이 어댑터에서 적용한다.
 *  - Member.approveAsTeacher() / Member.rejectAsTeacher() 호출도 이 어댑터 내부에서 수행한다.
 */
@Component
@Transactional
public class MemberUserAdapter implements UserQueryPort, UserStatusUpdatePort {

    private final MemberQueryService memberQueryService;
    private final MemberRepository memberRepository;

    public MemberUserAdapter(MemberQueryService memberQueryService, MemberRepository memberRepository) {
        this.memberQueryService = memberQueryService;
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeacherApplication> findApplicationById(Long userId) {
        // TODO m03: memberQueryService.findById -> role=TEACHER && status=PENDING 검증 -> TeacherApplication 변환
        throw new UnsupportedOperationException("TODO: m03 우선순위 1 - 강사 신청자 상세 조회 어댑터 (MS-4)");
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherApplication> findApplicationList(int page, int size) {
        // TODO m03: memberQueryService.findByRoleAndStatus(TEACHER, PENDING, page, size) -> TeacherApplication 변환
        throw new UnsupportedOperationException("TODO: m03 우선순위 1 - 강사 신청자 목록 어댑터 (MS-3)");
    }

    @Override
    @Transactional(readOnly = true)
    public long countApplications() {
        // TODO m03: memberQueryService.countByRoleAndStatus(TEACHER, PENDING)
        throw new UnsupportedOperationException("TODO: m03 우선순위 1 - 강사 신청자 개수 어댑터");
    }

    @Override
    public void approveTeacher(Long userId) {
        // TODO m03: memberQueryService.findById -> Member.approveAsTeacher() -> memberRepository.save()
        throw new UnsupportedOperationException("TODO: m03 우선순위 1 - 강사 승인 어댑터 (MS-5)");
    }

    @Override
    public void rejectTeacher(Long userId, String reason) {
        // TODO m03: memberQueryService.findById -> Member.rejectAsTeacher() -> memberRepository.save()
        throw new UnsupportedOperationException("TODO: m03 우선순위 1 - 강사 반려 어댑터 (MS-5)");
    }
}
