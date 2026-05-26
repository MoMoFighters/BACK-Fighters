package com.wanted.momocity.teacher.application.service;

import com.wanted.momocity.teacher.application.port.UserQueryPort;
import com.wanted.momocity.teacher.application.usecase.TeacherApplicationQueryUseCase;
import com.wanted.momocity.teacher.domain.model.TeacherApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* comment.
    TeacherApplicationQueryService 정리
    해당 클래스가 하는 역할 : TeacherApplicationQueryUseCase 인터페이스의 실제 구현
    2. 위치 : teacher/application/service
    3. 의존 흐름 (이게 클린 아키텍처의 정수) :
        TeacherApplicationController (표현 계층)
        → TeacherApplicationQueryUseCase (인터페이스 약속)
        → TeacherApplicationQueryService (이 클래스, 구현)
        → UserQueryPort (외향 포트 약속)
        → MemberUserAdapter (Port 구현) → 회원 영역
    4. *두 인터페이스 사이의 다리* :
        - 위쪽 : UseCase 인터페이스 구현 (Controller 가 호출)
        - 아래쪽 : Port 인터페이스 의존 (다른 영역 호출)
        - 둘 다 *추상* 에 의존. 클래스 자체는 *조립 책임*
    5. 비즈니스 로직 위치 :
        - 도메인 검증 = 회원 영역의 Member 가 책임
        - 영역 조립 = 이 서비스가 책임 (Port 부르고 결과 조립)
    6. m03 미구현 (해야할 일)
        - getApplicationList : Port 호출 + Result 조립 (m03 구현 우선순위 1)
        - getApplicationDetail : Port 호출 + 없으면 404 예외 (우선순위 1)
 */

@Service
@Transactional(readOnly = true)
public class TeacherApplicationQueryService implements TeacherApplicationQueryUseCase {

    private final UserQueryPort userQueryPort;

    public TeacherApplicationQueryService(UserQueryPort userQueryPort) {
        this.userQueryPort = userQueryPort;
    }

    @Override
    public TeacherApplicationListResult getApplicationList(int page, int size) {
        // TODO m03: userQueryPort.findApplicationList + countApplications -> totalPages 계산 -> 결과 조립
        throw new UnsupportedOperationException("TODO: m03 우선순위 1 - 강사 신청자 목록 조회 (MS-3)");
    }

    @Override
    public TeacherApplication getApplicationDetail(Long userId) {
        // TODO m03: userQueryPort.findApplicationById -> Optional 처리 (없으면 404 예외)
        throw new UnsupportedOperationException("TODO: m03 우선순위 1 - 강사 신청자 상세 조회 (MS-4)");
    }
}
