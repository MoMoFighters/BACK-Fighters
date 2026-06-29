package com.wanted.momocity.user.infrastructure.persistence;

import com.wanted.momocity.user.domain.model.Role;

public interface UserNameProjection {
    // 프로젝션 인터페이스가 JPA 기술에 종속된 개념으로 인프라 계층

    /*comment
    *  인터페이스만 정의하면 JPA가 구현체를 자동으로 만들어준다 !
    *  1. 스프링이 부팅될 때 SpringDataUserRepository를 스캔하면서 List<UserNameProjection> 반환 타입을 보고 이게 프로젝션이라는 걸 인식
    *  2. JPQL을 실행하면서 각 행의 결과를 AS 별칭 기준으로 매핑
    *  3. 내부적으로 JDK 동적 프록시를 써서 UserNameProjection 구현체를 런타임에 생성*/
    Long getId();    // u.id AS id 랑 매핑
    String getName(); // u.name AS name 과 매핑
    Role getRole();// u.role AS role 과 매핑
}
