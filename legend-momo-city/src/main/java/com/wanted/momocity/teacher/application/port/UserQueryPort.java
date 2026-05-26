package com.wanted.momocity.teacher.application.port;

import com.wanted.momocity.teacher.domain.model.TeacherApplication;

import java.util.List;
import java.util.Optional;

/* comment.
    UserQueryPort 정리
    1. 해당 클래스가 어떤 일을 하는가?
     강사 영역이 다른 영역에서 강사 신청자 정보를 가져오기 위한 약속
    2. 위치 : teacher/application/port (응용 계층 - 포트 폴더)
    3. Repository 와의 핵심 차이 :
        - MemberRepository (회원 영역): *내 영역의 데이터* 저장/조회 약속. 같은 영역의 영속화 책임
        - UserQueryPort (강사 영역): *다른 영역의 데이터* 가져오는 약속. 영역 간 다리
    4. 호출 흐름 :
        강사 영역 Application Service
        → UserQueryPort (이 인터페이스)
        → MemberUserAdapter (강사 영역 인프라)
        → MemberQueryService (회원 영역 공개 입구)
        → MemberRepository → ... → 데이터베이스
    5. 왜 *Repository* 라는 이름 안 썼나? :
        - Repository = *저장소 패턴* (CRUD 약속). 같은 영역 데이터 책임
        - Port = *영역 경계 다리*. 다른 영역 데이터 가져오기 명시
        - 이름으로 *의도 구분*
 */

public interface UserQueryPort {

    Optional<TeacherApplication> findApplicationById(Long userId);

    List<TeacherApplication> findApplicationList(int page, int size);

    long countApplications();
}
