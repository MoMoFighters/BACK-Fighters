package com.wanted.momocity.member.application.service;

import com.wanted.momocity.member.domain.model.Member;
import com.wanted.momocity.member.domain.model.MemberRole;
import com.wanted.momocity.member.domain.model.MemberStatus;
import com.wanted.momocity.member.domain.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/* comment.
    MemberQueryService 정리
    1. 해당 클래스가 하는 역할 : 다른 영역(teacher/report/admin) 이 회원 정보를 가져갈 때 호출하는 공개적인 입구
    2. 위치 : member/application/service
    3. 호출 체인 :
        → MemberQueryService          ← *이 클래스*
        → MemberRepository (도메인 인터페이스)
        → MemberRepositoryAdapter (실제 구현)
        → SpringDataMemberRepository
        → 데이터베이스
    4. 왜 공개 서비스가 필요할까?
        - 강사 영역이 MemberRepository 를 직접 호출하면, 회원 영역의 세부 약속까지 전부 알게 된다.
        - 공개 서비스를 한 겹 두면 -> 강사 영역은 회원 정보 가져오는 몇 가지 방법 만 알면 된다.
        - 미래에 회원 영역 내부가 바뀌어도, 공개 서비스 시그니처만 바뀌지 않는다면 외부에 영향은 없다.
    5. Query 만 있는 이유 (Command 없음) :
        - 회원 *조회* 는 다른 영역도 자주 함 → 공개
        - 회원 *변경* (save 등)은 회원 영역 자체에서만 → MemberCommandService 가 5/27 추가 예정
        - CQRS(Command Query Responsibility Segregation) 비스무리한 분리
 */

// @Service : 해당 클래스가 서비스의 역할을 함을 명시
@Service
@Transactional(readOnly = true)
public class MemberQueryService {

    // 의존성을 주입하고 불변성을 위해 final 선언
    private final MemberRepository memberRepository;

    public MemberQueryService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /* comment.
        아래의 3개 메소드가 단순 위임이다.
            - 비즈니스 로직 없음. Repository 호출 결과를 그대로 반환한다.
            - 그렇다면 왜 Repository 에서 직접 부르지 않고, 호출하는 것일까?
                a) 다른 영역은 Repository 약속을 직접 안 봐도 된다.
                b) 나중에 캐싱, 로깅, 권한 검증 같은 것들을 여기에 추가할 수 있다.
                c) @Repository 빈을 다른 영역이 직접 주입하기보다는, 응용 계층 서비스를 거치는 것이 책임이 명확하다.
        Module03 구현부 채울 때 추가될 것 ( 현재는 위임만 진행)
            - 캐싱 (조회 빈도 가 높다면)
            - 권한 검증 (특정 회원 정보는 ADMIN 만 조회 가능할 수 있게)
            - 로깅 (감사 추적 용도)
     */

    public Optional<Member> findById(Long memberId) {
        return memberRepository.findById(memberId);
    }

    public List<Member> findByRoleAndStatus(MemberRole role, MemberStatus status, int page, int size) {
        return memberRepository.findByRoleAndStatus(role, status, page, size);
    }

    public long countByRoleAndStatus(MemberRole role, MemberStatus status) {
        return memberRepository.countByRoleAndStatus(role, status);
    }
}
