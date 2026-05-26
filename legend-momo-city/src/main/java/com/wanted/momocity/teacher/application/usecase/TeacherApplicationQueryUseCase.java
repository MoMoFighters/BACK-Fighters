package com.wanted.momocity.teacher.application.usecase;

import com.wanted.momocity.teacher.domain.model.TeacherApplication;

import java.util.List;

/* comment.
    TeacherApplicationQueryUseCase 정리
    1. 해당 클래스가 하는 일 : Controller 가 강사 신청자 조회를 요청할 때 호출하는 진입점 약속
    2. 위치 : teacher/application/usecase
    3. *조회만* 책임 (CQRS) :
        - 변경(승인/반려) 은 별도 UseCase (TeacherApplicationCommandUseCase)
    4. 왜 인터페이스 + 구현체 분리? :
        - Controller 가 인터페이스 타입 으로 주입받음
        - 구현체 교체 가능 (테스트 시 Mock 주입 등)
        - 의존 역전 원칙(DIP)
 */

public interface TeacherApplicationQueryUseCase {

    TeacherApplicationListResult getApplicationList(int page, int size);

    TeacherApplication getApplicationDetail(Long userId);

    record TeacherApplicationListResult(
            List<TeacherApplication> applications,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }
}
