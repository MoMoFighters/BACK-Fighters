package com.wanted.momocity.member.infrastructure.persistence;


/* comment.
    어댑터가 Member 도메인 모델을 import 하는 이유
    - 의존 방향은 항상 도메인을 향함. 인프라(어댑터) -> 도메인(Member) 굳!
    - 반대 방향이면 안된다. member -> memberRepositoryAdapter 로 가게 되면 안된다!
    - 도메인 코드 어디에도 import 없는지 검증 가능
    - 인프라는 도메인 만족시키는 중이며, 도메인은 인프라가 누군지 모른다.
 */
import com.wanted.momocity.member.domain.model.Member;
import com.wanted.momocity.member.domain.model.MemberRole;
import com.wanted.momocity.member.domain.model.MemberStatus;
import com.wanted.momocity.member.domain.repository.MemberRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/* comment.
    MemberRepositoryAdapter 정리
    1. 해당 클래스가 하는 역할 : 도메인 MemberRepository 인터페이스를 JPA 로 구현하는 어댑터이다.
    2. 위치 : member/infrastructure/persistence (인프라 계층)
    3. 핵심 책임 :
        - 도메인 Member 모델과 JPA MemberJpaEntity 간 변환한다.
        - SpringDataMemberRepository 의 실제 DB 호출을 감싸서 도메인이 안 보이게 격리한다.
    5. 의존 방향 (클린 아키텍처의 정점) :
        - implements MemberRepository (도메인 인터페이스) ← 인프라가 도메인 약속을 *지킴*
        - SpringDataMemberRepository 주입 (인프라 → 인프라) ← 같은 계층 호출
        - Member 사용 (인프라 → 도메인) ← 인프라는 도메인을 알아도 OK
        - 도메인은 *이 클래스의 존재를 모름* (인터페이스만 봄)
 */

// Spring 이 이클래스를 빈으로 등록한다. 다른 곳에서 주입받을 수 있게 진행
@Repository
// 모든 메소드를 트랜젝션으로 감쌈
// 데이터 베이스 작업의 안전성 보장. 중간에 실패하면 롤백 진행을 한다.
@Transactional
public class MemberRepositoryAdapter implements MemberRepository {

    // final : 한 번 주입되면 불변한 상태로 의존성 주입
    // autowired 필드 주입 -> 생성자가 없어도 되지만, final을 사용할 수 없음. 테스트가 어려움.
    private final SpringDataMemberRepository repository;

    public MemberRepositoryAdapter(SpringDataMemberRepository repository) {
        this.repository = repository;
    }

    @Override
    // 메서드 단위에서 transactional(readOnly = true 같이 덮어쓸 수 있음
    // 성능 최적화를 명시적으로 readOnly 의도를 명시
    // 기본적으로 안전한 쪽으로 : @Transactional 어노테이션을 깜빡해도 트랜젝션은 켜져있음.
    @Transactional(readOnly = true)
    public Optional<Member> findById(Long memberId) {
        throw new UnsupportedOperationException("TODO: m03 구현 - Member 단건 조회");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Member> findByRoleAndStatus(MemberRole role, MemberStatus status, int page, int size) {
        throw new UnsupportedOperationException("TODO: m03 구현 - role+status 페이징 조회");
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRoleAndStatus(MemberRole role, MemberStatus status) {
        throw new UnsupportedOperationException("TODO: m03 구현 - role+status 개수 조회");
    }

    @Override
    public Member save(Member member) {
        throw new UnsupportedOperationException("TODO: m03 구현 - Member 저장");
    }
}
