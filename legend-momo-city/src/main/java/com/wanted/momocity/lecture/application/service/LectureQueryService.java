package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.enrollment.application.port.StudentAccountPort;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.port.LectureEnrollmentQueryPort;
import com.wanted.momocity.lecture.application.port.LectureReviewQueryPort;
import com.wanted.momocity.lecture.application.port.TeacherAccountPort;
import com.wanted.momocity.lecture.application.query.LectureQuery.*;
import com.wanted.momocity.lecture.application.usecase.LectureQueryUseCases.AdminLectureQueryUseCase;
import com.wanted.momocity.lecture.application.usecase.LectureQueryUseCases.LectureQueryUseCase;
import com.wanted.momocity.lecture.domain.exception.LectureNotFoundException;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.domain.repository.ChapterRepository;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import com.wanted.momocity.lecture.presentation.api.response.AdminLectureResponse.AdminLectureDetailResponse;
import com.wanted.momocity.lecture.presentation.api.response.AdminLectureResponse.AdminLectureListItemResponse;
import com.wanted.momocity.lecture.presentation.api.response.AdminLectureResponse.AdminLecturePageResponse;
import com.wanted.momocity.lecture.presentation.api.response.StudentLectureResponse.StudentLectureDetailResponse;
import com.wanted.momocity.lecture.presentation.api.response.StudentLectureResponse.StudentLectureListItemResponse;
import com.wanted.momocity.lecture.presentation.api.response.StudentLectureResponse.StudentLecturePageResponse;
import com.wanted.momocity.lecture.presentation.api.response.TeacherLectureResponse.TeacherLectureDetailResponse;
import com.wanted.momocity.lecture.presentation.api.response.TeacherLectureResponse.TeacherLectureListItemResponse;
import com.wanted.momocity.lecture.presentation.api.response.TeacherLectureResponse.TeacherLecturePageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 강의 조회 기능을 처리하는 Application Service.
 * 기존 LectureQueryService와 AdminLectureQueryService를 하나로 합친 형태.
 * 학생, 강사, 관리자 강의 조회 기능을 모두 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureQueryService implements
        LectureQueryUseCase,
        AdminLectureQueryUseCase {

    // 강의 조회 저장소
    private final LectureRepository lectureRepository;

    // 챕터 조회 저장소
    private final ChapterRepository chapterRepository;

    // 로그인한 사용자의 학생 ID를 조회하는 포트
    private final StudentAccountPort studentAccountPort;

    // 로그인한 사용자의 강사 ID를 조회하는 포트
    private final TeacherAccountPort teacherAccountPort;

    // 수강 신청 정보 조회 포트
    private final LectureEnrollmentQueryPort lectureEnrollmentQueryPort;

    // 강의별 수강평 평균 평점과 수강평 개수 조회
    private final LectureReviewQueryPort lectureReviewQueryPort;

    // 강사 이름, 프로필 이미지 조회를 위한 auth 포트
    private final LoadUserPort loadUserPort;

    // 학생/비로그인 기준 강의 목록 조회.
    @Override
    public StudentLecturePageResponse getLectures(GetLecturesQuery query) {

        /*
         * comment
         * 비회원은 전체 ACTIVE 강의 목록을 조회한다.
         * 학생 회원은 본인이 수강 신청한 ACTIVE 강의 목록만 조회한다.
         * category, keyword, page, size 조건은 두 경우 모두 동일하게 적용된다.
         */
        boolean enrolledOnly = query.userId() != null;
        Long studentId = null;
        List<Long> enrolledLectureIds = List.of();

        if (enrolledOnly) {
            studentId = studentAccountPort.getStudentId(query.userId());

            enrolledLectureIds = lectureEnrollmentQueryPort.findLectureIdsByUserId(studentId);

            /*
             * comment
             * 로그인한 학생이 수강 신청한 강의가 없으면 빈 페이지를 반환한다.
             */
            if (enrolledLectureIds.isEmpty()) {
                return new StudentLecturePageResponse(
                        List.of(),
                        query.page(),
                        query.size(),
                        0,
                        0
                );
            }
        }

        var lecturePage = lectureRepository.findLectures(
                query.category(),
                query.keyword(),
                enrolledOnly,
                enrolledLectureIds,
                query.page(),
                query.size()
        );

        Long finalStudentId = studentId;

        // 현재 페이지 강의들의 리뷰 통계를 한 번에 조회
        Map<Long, LectureReviewQueryPort.ReviewStats> reviewStatsMap = lectureReviewQueryPort.getReviewStatsMap(
                lecturePage.content().stream()
                        // 강의 Id만 추출
                        .map(LectureAggregate::getId)
                        .toList()
        );

        List<StudentLectureListItemResponse> content = lecturePage.content().stream() // 현재 페이지 강의 목록을 스트림으로 변환
                .map(lecture -> toStudentListItemResponse(lecture, finalStudentId, reviewStatsMap)) // 리뷰 통계 Map을 함께 넘겨 응답 DTO 생성
                .toList();

        return new StudentLecturePageResponse(
                content,
                query.page(),
                query.size(),
                lecturePage.totalElements(),
                lecturePage.totalPages()
        );
    }

    // 학생 기준 강의 상세 조회.
    @Override
    public StudentLectureDetailResponse getStudentLectureDetail(GetStudentLectureDetailQuery query) {
        Long studentId = studentAccountPort.getStudentId(query.userId());

        LectureAggregate lecture = lectureRepository.findById(query.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        if (lecture.getStatus() != LectureStatus.ACTIVE) {
            throw new AccessDeniedException("진행 중인 강의만 조회할 수 있습니다.");
        }

        List<LectureChapter> chapters =
                chapterRepository.findAllByLectureIdOrderByOrderNoAsc(query.lectureId());

        // 수강 신청 정보 조회
        var enrollmentProgress = lectureEnrollmentQueryPort
                // 학생 Id와 강의ID로 수강 신청 정보 조회
                .findByUserIdAndLectureId(studentId, query.lectureId());

        // 수강 신청 정보가 있으면 true, 없으면 null
        boolean isEnrolled = enrollmentProgress.isPresent();

        // 조회된 수강 신청 정보 Optional 사용
        Integer lectureProgress = enrollmentProgress
                .map(LectureEnrollmentQueryPort.EnrollmentProgress::totalProgress).orElse(null);

        LectureReviewQueryPort.ReviewStats reviewStats = lectureReviewQueryPort.getReviewStats(lecture.getId());

        return StudentLectureDetailResponse.from(
                lecture,
                chapters,
                reviewStats.averageRating(),
                reviewStats.reviewCount(),
                isEnrolled,
                lectureProgress
        );
    }

    // 강사 기준 본인 강의 목록 조회.
    @Override
    public TeacherLecturePageResponse getTeacherLectures(GetTeacherLecturesQuery query) {

        Long teacherId = teacherAccountPort.getTeacherId(query.teacherId());

        var lecturePage = lectureRepository.findTeacherLectures(
                teacherId,
                query.category(),
                query.keyword(),
                query.page(),
                query.size()
        );

        Map<Long, LectureReviewQueryPort.ReviewStats> reviewStatsMap = lectureReviewQueryPort.getReviewStatsMap(
                lecturePage.content().stream()
                        .map(LectureAggregate::getId)
                        .toList()
        );

        // 현재 페이지 강의 목록 스트림
        List<TeacherLectureListItemResponse> content = lecturePage.content().stream()
                .map(lecture -> {
                    // 해당 강의의 평균 평점과 수강평 개수 조회
                    LectureReviewQueryPort.ReviewStats reviewStats = reviewStatsMap.getOrDefault(
                            lecture.getId(),
                            new LectureReviewQueryPort.ReviewStats(0.0, 0)
                    );

                    // 강사 강의 목록 아이템 응답 DTO
                    return TeacherLectureListItemResponse.from(
                            lecture,
                            reviewStats.averageRating(),
                            reviewStats.reviewCount()
                    );
                }).toList();


        return new TeacherLecturePageResponse(
                content,
                query.page(),
                query.size(),
                lecturePage.totalElements(),
                lecturePage.totalPages()
        );
    }

    // 강사 기준 본인 강의 상세 조회.
    @Override
    public TeacherLectureDetailResponse getTeacherLectureDetail(GetTeacherLectureDetailQuery query) {
        Long teacherId = teacherAccountPort.getTeacherId(query.teacherId());

        LectureAggregate lecture = lectureRepository.findById(query.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        if (!lecture.isOwnedBy(teacherId)) {
            throw new AccessDeniedException("본인이 등록한 강의만 조회할 수 있습니다.");
        }

        List<LectureChapter> chapters =
                chapterRepository.findAllByLectureIdOrderByOrderNoAsc(query.lectureId());

        LectureReviewQueryPort.ReviewStats reviewStats = lectureReviewQueryPort.getReviewStats(lecture.getId());

        return TeacherLectureDetailResponse.from(
                lecture,
                chapters,
                reviewStats.averageRating(),
                reviewStats.reviewCount()
        );
    }

    /* comment
     * 관리자 기준 강의 목록 조회.
     * status가 없으면 WAITING, ACTIVE 강의를 함께 조회한다.
     */
    @Override
    public AdminLecturePageResponse getAdminLectures(GetAdminLecturesQuery query) {
        List<LectureStatus> statuses = resolveAdminLectureStatuses(query.status());

        var lecturePage = lectureRepository.findAdminLectures(
                statuses,
                query.category(),
                query.keyword(),
                query.page(),
                query.size()
        );

        Map<Long, LectureReviewQueryPort.ReviewStats> reviewStatsMap = lectureReviewQueryPort.getReviewStatsMap(
                lecturePage.content().stream()
                        .map(LectureAggregate::getId)
                        .toList()
        );

        List<AdminLectureListItemResponse> content = lecturePage.content().stream()
                .map(lecture -> {
                    LectureReviewQueryPort.ReviewStats reviewStats = reviewStatsMap.getOrDefault(
                            lecture.getId(),
                            new LectureReviewQueryPort.ReviewStats(0.0, 0)
                    );

                    return  AdminLectureListItemResponse.from(
                            lecture,
                            reviewStats.averageRating(),
                            reviewStats.reviewCount()
                    );
                })
                .toList();

        return new AdminLecturePageResponse(
                content,
                query.page(),
                query.size(),
                lecturePage.totalElements(),
                lecturePage.totalPages()
        );
    }

    // 관리자 기준 강의 상세 조회.
    @Override
    public AdminLectureDetailResponse getAdminLectureDetail(GetAdminLectureDetailQuery query) {
        LectureAggregate lecture = lectureRepository.findById(query.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        if (lecture.getStatus() != LectureStatus.WAITING
                && lecture.getStatus() != LectureStatus.ACTIVE) {
            throw new DomainRuleViolationException("관리자는 승인 대기 또는 진행 중 강의만 조회할 수 있습니다.");
        }

        List<LectureChapter> chapters =
                chapterRepository.findAllByLectureIdOrderByOrderNoAsc(query.lectureId());

        LectureReviewQueryPort.ReviewStats reviewStats = lectureReviewQueryPort.getReviewStats(lecture.getId());

        return AdminLectureDetailResponse.from(
                lecture,
                chapters,
                reviewStats.averageRating(),
                reviewStats.reviewCount()
        );
    }

    // 관리자 목록 조회에서 사용할 강의 상태 목록을 결정
    private List<LectureStatus> resolveAdminLectureStatuses(LectureStatus status) {
        if (status == null) {
            return List.of(
                    LectureStatus.WAITING,
                    LectureStatus.ACTIVE
            );
        }

        return List.of(status);
    }

    // 학생 강의 목록용 응답 객체로 변환
    // 학생 강의 목록용 응답 객체로 변환
    private StudentLectureListItemResponse toStudentListItemResponse(
            LectureAggregate lecture,
            Long studentId,
            Map<Long, LectureReviewQueryPort.ReviewStats> reviewStatsMap
    ) {

        // 강의평 관련 (강의 평점 평균, 강의평 개수)
        // 현재 강의 ID에 대해 해당하는 리뷰 통계 조회
        // getOrDefault : 보통 값을 가지고 오지만 , 없다면 기본값을 사용
        LectureReviewQueryPort.ReviewStats reviewStats = reviewStatsMap.getOrDefault(
                lecture.getId(),
                // 리뷰가 없다면 기본값 사용
                new LectureReviewQueryPort.ReviewStats(0.0, 0)
        );
        double averageRating = reviewStats.averageRating();
        int reviewCount = reviewStats.reviewCount();

        // 현재 강의의 전체 챕터 수를 조회
        int chapterCount = chapterRepository.countByLectureId(lecture.getId());

        /*
         * comment
         * 비회원 또는 미수강 강의는 진행 정보가 없으므로 null로 둔다.
         * @JsonInclude(JsonInclude.Include.NON_NULL)에 의해 JSON 응답에서 제외된다.
         */
        Integer lectureProgress = null;
        Boolean isCompleted = null;

        var progress = studentId == null
                ? java.util.Optional.<LectureEnrollmentQueryPort.EnrollmentProgress>empty()
                : lectureEnrollmentQueryPort.findByUserIdAndLectureId(studentId, lecture.getId());

        if (progress.isPresent()) {
            lectureProgress = progress.get().totalProgress();

            // 완료 여부는 전체 챕터 수와 완료 챕터 수를 기준으로 판단
            isCompleted = chapterCount > 0 && progress.get().completedCount() >= chapterCount;
        }

        return StudentLectureListItemResponse.from(
                lecture,
                averageRating,
                reviewCount,
                lectureProgress,
                isCompleted,
                chapterCount
        );
    }

}