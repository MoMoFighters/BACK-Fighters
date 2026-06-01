package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.query.GetAdminLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.GetAdminLecturesQuery;
import com.wanted.momocity.lecture.application.usecase.AdminLectureQueryUseCase;
import com.wanted.momocity.lecture.domain.exception.LectureNotFoundException;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.domain.repository.ChapterRepository;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import com.wanted.momocity.lecture.presentation.api.response.AdminLectureDetailResponse;
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
    private final ChapterRepository chapterRepository;

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
                .map(lecture -> AdminLectureListItemResponse.from(
                        lecture,
                        0.0,
                        0
                )).toList();
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

    @Override
    public AdminLectureDetailResponse getAdminLectureDetail(GetAdminLectureDetailQuery query) {
        /*
         * 관리자 상세 조회 대상 강의를 조회한다.
         * 강의가 없으면 404로 변환될 수 있는 예외를 던진다.
         */
        LectureAggregate lecture = lectureRepository.findById(query.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        /*
         * 관리자 화면에서는 승인 대기(WAITING) 또는 진행 중(ACTIVE) 강의만 상세 조회한다.
         * HOLD, DELETED까지 보여줄 정책이면 이 조건은 나중에 확장하면 된다.
         */
        if (lecture.getStatus() != LectureStatus.WAITING
                && lecture.getStatus() != LectureStatus.ACTIVE) {
            throw new DomainRuleViolationException("관리자는 승인 대기 또는 진행 중 강의만 조회할 수 있습니다.");
        }

        /*
         * 강의에 연결된 챕터 목록을 노출 순서대로 조회한다.
         * 관리자 상세 화면에서 챕터와 영상 상태를 함께 확인하기 위해 필요하다.
         */
        List<LectureChapter> chapters =
                chapterRepository.findAllByLectureIdOrderByOrderNoAsc(query.lectureId());

        // 강의 정보와 챕터 목록을 관리자 상세 응답 DTO로 변환
        return AdminLectureDetailResponse.from(
                lecture,
                chapters,
                0.0,
                0
        );
    }
}