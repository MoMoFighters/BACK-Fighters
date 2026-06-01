package com.wanted.momocity.member.application.service;

import com.wanted.momocity.member.application.command.ChangeMemberStatusCommand;
import com.wanted.momocity.member.application.usecase.MemberCommandUseCase;
import com.wanted.momocity.member.domain.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* comment.
    MemberCommandService 정리
    1. 이 클래스의 역할 : MemberCommandUseCase 인터페이스의 실제 구현체. 회원 상태 변경 비즈니스 흐름 조정
    2. 위치 : member/application/service (응용 계층 - 구현)
    3. 왜 MemberQueryService 와 분리하는가 (CQRS) : 조회와 변경 책임 분리 목적
    4. 왜 @Transactional 에 readOnly = true 안 붙이나 : command 라 쓰는 작업이다. 따라서 readOnly X
    5. 왜 implements MemberCommandUseCase 인가 : 컨트롤러가 구현체 대신 인터페이스에 의존하게 된다.
    6. 왜 생성자 주입인가 (필드 주입 아님) : final 가능하며, 테스트 mock을 사용할 수 있기 때문이다.
 */
@Service
@Transactional
public class MemberCommandService implements MemberCommandUseCase {

    private final MemberRepository memberRepository;

    public MemberCommandService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /* comment.
        구현은 m03 우선순위 3 에서 채울 예정. 현재는 시그니처만 잡아둠.
        실제 흐름 (구현 시) :
        1. memberRepository.findById(command.userId()) 로 회원 조회
        2. member.changeStatusByAdmin(command.newStatus()) 로 도메인 행위 호출
        3. memberRepository.save(member) 로 저장
        4. MemberStatusChangeResult 만들어 반환
     */
    @Override
    public MemberStatusChangeResult changeStatus(ChangeMemberStatusCommand command) {
        throw new UnsupportedOperationException("TODO: m03 우선순위 3 - 회원 상태 변경 구현");
    }
}