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

/* comment.
    MemberUserAdapter 정리
    1. 이 클래스가 하는 일 : 강사 영역의 두 포트 (UserQueryPort + UserStatusUpdatePort) 를 회원 영역 호출로 구현하는 어댑터
    2. 위치 : teacher/infrastructure/member
    3. 두 Port 동시 구현 (implements UserQueryPort, UserStatusUpdatePort) :
        - 같은 대상(회원 영역)을 향하는 어댑터라 통합
        - 어댑터 분리도 가능하지만 책임이 단순해서 한 클래스로
    4. 회원 영역 의존 :
        - MemberQueryService : 조회 (강사 신청자 필터)
        - MemberRepository : 저장 (승인/반려 후 변경 사항 저장)
    5. 진짜 변환 책임 :
        - Member (회원 영역 도메인) → TeacherApplication (강사 영역 도메인)
        - 강사 신청자 필터 (role='TEACHER', status='PENDING') 도 여기서 적용
        - Member.approveAsTeacher() / rejectAsTeacher() 호출도 여기서
    6. @Component (@Repository 아님) :
        - 우리 영역의 데이터 저장소 아님
        - 다른 영역으로 가는 어댑터라 @Component 가 적합
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
