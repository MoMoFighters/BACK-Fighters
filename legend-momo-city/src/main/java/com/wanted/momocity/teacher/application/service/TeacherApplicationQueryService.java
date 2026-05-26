package com.wanted.momocity.teacher.application.service;

import com.wanted.momocity.teacher.application.port.UserQueryPort;
import com.wanted.momocity.teacher.application.usecase.TeacherApplicationQueryUseCase;
import com.wanted.momocity.teacher.domain.model.TeacherApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * 강사 신청자 조회 유스케이스 구현.
 *
 * 회원 영역(member BC)의 user 테이블에 직접 접근하지 않고
 * UserQueryPort 를 통해서만 강사 신청자 정보를 가져온다.
 *
 * REF: module00-clean-architecture catalog/application/service/CourseQueryService.java
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
