package com.wanted.momocity.teacher.application.port;

import com.wanted.momocity.teacher.domain.model.TeacherApplication;

import java.util.List;
import java.util.Optional;

/*
 * 강사 영역(teacher BC)이 회원 영역(member BC)에 "강사 신청자 정보"를 묻기 위한 인터페이스.
 *
 * 구현체: teacher/infrastructure/member/MemberUserAdapter
 *
 * REF: module00-clean-architecture enrollment/application/port/CourseCatalogPort.java
 *
 * 왜 직접 회원 영역의 Repository / JpaEntity 를 부르지 않는가:
 *  - 두 영역의 변경 영향이 서로 번지지 않도록 의존 방향을 인터페이스로 한 번 끊는다.
 *  - 강사 영역의 응용 서비스는 user 테이블이 무엇인지 몰라도 된다.
 */
public interface UserQueryPort {

    Optional<TeacherApplication> findApplicationById(Long userId);

    List<TeacherApplication> findApplicationList(int page, int size);

    long countApplications();
}
