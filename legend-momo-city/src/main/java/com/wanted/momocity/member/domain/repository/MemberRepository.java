package com.wanted.momocity.member.domain.repository;

import com.wanted.momocity.member.domain.model.Member;
import com.wanted.momocity.member.domain.model.MemberRole;
import com.wanted.momocity.member.domain.model.MemberStatus;

import java.util.List;
import java.util.Optional;

/* comment.
    MemberRepository 정리
    1. 해당 인터페이스가 하는 일 : 회원을 저장/조회할 수 있어야 한다. 하지만 어떻게 저장하는지는 모른다.
    2. 위치 : member/domain/repository
    -
    a) 인터페이스가 도메인 계층에 있는 이유
        우리는 기존에 인프라 쪽에 뒀었지만, 클린 아키텍처 방식을 사용하게 되면서 도메인 계층에 인터페이스를
       인프라에 구현하게 되었다.
    b) 메서드 4개의 이유 : CRUD 다 두면 안되는 것인가?
        Spring Data 처럼 CRUD 다 자동화 시켰지만, 우리는 도메인한테서 진짜 필요한 데이터들만 뽑아서
        진행을 하게 된다.
    c) 왜 Optional<Member> 로 반환하는가? Member 직접반환을 하지 않는 이유
        호출자가 null 체크 누락 시 NullPointException 을 띄울 수 있기 때문이다.
 */


public interface MemberRepository {

    Optional<Member> findById(Long memberId);

    List<Member> findByRoleAndStatus(MemberRole role, MemberStatus status, int page, int size);

    long countByRoleAndStatus(MemberRole role, MemberStatus status);

    // 왜 save() 는 void 가 아니라 Member 반환인가?
    // updatedAt 도 저장 시점에서 자동으로 갱신한다. 반환값으로 확인할 수 있다.
    Member save(Member member);
}
