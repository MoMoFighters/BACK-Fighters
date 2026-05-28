package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.enrollment.application.port.StudentAccountPort;
import com.wanted.momocity.lecture.application.port.LectureEnrollmentQueryPort;
import com.wanted.momocity.lecture.application.query.GetLecturesQuery;
import com.wanted.momocity.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import com.wanted.momocity.lecture.presentation.api.response.LectureListItemResponse;
import com.wanted.momocity.lecture.presentation.api.response.LecturePageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * LectureQueryService는 강의 목록 조회 로직을 처리하는 서비스
 * enrolled=true / false 조건에 따라
 * 로그인 사용자의 수강 신청 여부를 기준으로 강의를 필터링함.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureQueryService implements LectureQueryUseCase {

    // 강의 데이터를 조회하는 저장소
    private final LectureRepository lectureRepository;

    // 로그인 사용자 email로 userId를 찾기 위한 포트
    private final StudentAccountPort studentAccountPort;

    // 사용자의 수강 신청 정보를 조회하기 위한 포트
    private final LectureEnrollmentQueryPort lectureEnrollmentQueryPort;

    // 강의 목록을 조회
    @Override
    public LecturePageResponse getLectures(GetLecturesQuery query) {

        // Authorization 토큰에서 꺼낸 email로 userId를 조회
        Long userId = studentAccountPort.getStudentId(query.userId());

        // userId 기준으로 수강 신청한 강의 ID 목록을 조회
        List<Long> enrolledLectureIds = lectureEnrollmentQueryPort.findLectureIdsByUserId(userId);

        // 강의 목록을 조회
        // category와 enrolled 조건은 repository에서 처리함.
        var lecturePage = lectureRepository.findLectures(
                query.category(),
                query.enrolled(),
                enrolledLectureIds,
                query.page(),
                query.size()
        );

        // 조회된 강의들을 응답 DTO로 변환합니다.
        List<LectureListItemResponse> content = lecturePage.content().stream()
                .map(lecture -> toResponse(
                        lecture,
                        enrolledLectureIds.contains(lecture.getId()),
                        userId
                ))
                .toList();

        return new LecturePageResponse(
                content,
                query.page(),
                query.size(),
                lecturePage.totalElements(),
                lecturePage.totalPages()
        );
    }

    /**
     * Lecture 도메인 객체를 목록 응답 DTO로 변환
     */
    private LectureListItemResponse toResponse(
            LectureAggregate lecture,
            boolean enrolled,
            Long userId
    ) {
        // 수강 신청 ID
        Long enrollmentId = null;

        // 기본값은 0
        int totalProgress = 0;
        int completedCount = 0;

        // 수강 신청한 강의라면 enrollment에서 진도 정보를 조회
        if (enrolled) {
            var enrollmentInfo = lectureEnrollmentQueryPort.findByUserIdAndLectureId(
                    userId,
                    lecture.getId()
            );

            if (enrollmentInfo.isPresent()) {
                enrollmentId = enrollmentInfo.get().enrollmentId();
                totalProgress = enrollmentInfo.get().totalProgress();
                completedCount = enrollmentInfo.get().completedCount();
            }
        }

        return new LectureListItemResponse(
                enrollmentId,
                lecture.getId(),
                lecture.getTitle(),
                lecture.getThumbnailUrl(),
                lecture.getCategory().name(),
                lecture.getStatus().name(),
                enrolled,
                totalProgress,
                completedCount
        );
    }
}