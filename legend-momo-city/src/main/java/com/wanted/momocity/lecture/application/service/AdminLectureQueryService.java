package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.lecture.application.query.GetAdminLecturesQuery;
import com.wanted.momocity.lecture.application.usecase.AdminLectureQueryUseCase;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import com.wanted.momocity.lecture.presentation.api.response.AdminLectureListItemResponse;
import com.wanted.momocity.lecture.presentation.api.response.AdminLecturePageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 관리자 강의 조회 로직을 처리하는 Application Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLectureQueryService implements AdminLectureQueryUseCase {

    private final LectureRepository lectureRepository;

    @Override
    public AdminLecturePageResponse getAdminLectures(GetAdminLecturesQuery query) {
        // status가 없으면 관리자 전체 목록 기준인 WAITING + ACTIVE를 조회한다.
        List<LectureStatus> statuses = resolveStatuses(query.status());

        // 관리자 강의 목록을 Repository를 통해 조회한다.
        var lecturePage = lectureRepository.findAdminLectures(
                statuses,
                query.category(),
                query.keyword(),
                query.page(),
                query.size()
        );

        // 도메인 모델을 관리자 목록 응답 DTO로 변환한다.
        List<AdminLectureListItemResponse> content = lecturePage.content().stream()
                .map(AdminLectureListItemResponse::from)
                .toList();

        // 페이지 정보와 목록을 함께 반환한다.
        return new AdminLecturePageResponse(
                content,
                query.page(),
                query.size(),
                lecturePage.totalElements(),
                lecturePage.totalPages()
        );
    }

    // 관리자 목록 조회에서 사용할 강의 상태 목록을 정한다.
    private List<LectureStatus> resolveStatuses(LectureStatus status) {
        if (status == null) {
            return List.of(
                    LectureStatus.WAITING,
                    LectureStatus.ACTIVE
            );
        }

        return List.of(status);
    }
}