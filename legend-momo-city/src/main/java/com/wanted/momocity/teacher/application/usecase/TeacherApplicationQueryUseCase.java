package com.wanted.momocity.teacher.application.usecase;

import com.wanted.momocity.teacher.domain.model.TeacherApplication;

import java.util.List;

/*
 * 강사 신청자 조회 유스케이스.
 *
 * 구현체: TeacherApplicationQueryService
 * 호출자: TeacherApplicationController (MS-3, MS-4)
 *
 * REF: module00-clean-architecture catalog/application/usecase/CourseQueryUseCase.java
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
