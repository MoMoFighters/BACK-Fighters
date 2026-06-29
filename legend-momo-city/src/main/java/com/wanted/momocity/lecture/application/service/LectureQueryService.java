package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.auth.application.port.LoadUserPort;
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
import com.wanted.momocity.viewing.application.port.ChapterProgressInfo;
import com.wanted.momocity.viewing.application.port.LectureChapterProgressPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

    // 학생 상세 조회에서 챕터별 진척도 조회
    private final LectureChapterProgressPort lectureChapterProgressPort;

    // 학생/비로그인 기준 강의 목록 조회.
    @Override
    public StudentLecturePageResponse getLectures(GetLecturesQuery query) {
        long startTime = System.currentTimeMillis(); // 학생/비회원 강의 목록 조회 시작 시간을 기록

        log.info("강의 목록 조회 시작 - userId={}, category={}, keyword={}, page={}, size={}",
                query.userId(),
                query.category(),
                query.keyword(),
                query.page(),
                query.size()
        );

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

        // 학생/비회원 강의 목록 응답 객체 생성
        StudentLecturePageResponse response = new StudentLecturePageResponse(
                content, // 강의 목록 응답 데이터 설정
                query.page(), // 현재 페이지 번호 설정
                query.size(), // 페이지 크기 설정
                lecturePage.totalElements(), // 전체 강의 수 설정
                lecturePage.totalPages() // 전체 페이지 수 설정
        );

        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("강의 목록 조회 완료 - userId={}, contentCount={}, totalElements={}, totalPages={}, elapsedTime={}ms", // 조회 결과와 처리 시간을 기록
                query.userId(),
                content.size(),
                lecturePage.totalElements(),
                lecturePage.totalPages(),
                elapsedTime
        );

        return response;
    }

    // 학생 기준 강의 상세 조회.
    @Override
    public StudentLectureDetailResponse getStudentLectureDetail(GetStudentLectureDetailQuery query) {
        long startTime = System.currentTimeMillis();

        log.info("학생 강의 상세 조회 시작 - userId={}, lectureId={}",
                query.userId(),
                query.lectureId()
        );
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

        Boolean isCompleted = null; // 미수강이면 null로 내려가도록 기본값 설정

        if (enrollmentProgress.isPresent()) { // 수강 신청 정보가 있는 경우
            isCompleted = !chapters.isEmpty() // 챕터가 1개 이상 있는지 확인
                    && enrollmentProgress.get().completedCount() >= chapters.size(); // 완료 챕터 수가 전체 챕터 수 이상인지 확인
        }

        // 수강 중인 강의인지 확인
        Map<Long, ChapterProgressInfo> chapterProgressMap = isEnrolled
                // 수강 중이면 챕터별 진척도 목록 조회
                ? lectureChapterProgressPort.getLectureChapterProgress(studentId, query.lectureId())
                // chapterId 기준으로 Map으로 변환
                  .stream().collect(java.util.stream.Collectors.toMap(
                          // Map key는 chapterId
                          ChapterProgressInfo::chapterId,
                        // Map value는 챕터 진척도 정보 전체
                        progressInfo -> progressInfo
                        // 미수강이면  빈 Map 사용
                )) : Map.of();

        LectureReviewQueryPort.ReviewStats reviewStats = lectureReviewQueryPort.getReviewStats(lecture.getId());

        StudentLectureDetailResponse response = StudentLectureDetailResponse.from(
                lecture,
                chapters,
                reviewStats.averageRating(),
                reviewStats.reviewCount(),
                isEnrolled,
                lectureProgress,
                isCompleted,
                chapterProgressMap
        );

        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("학생 강의 상세 조회 완료 - studentId={}, lectureId={}, isEnrolled={}, chapterCount={}, lectureProgress={}, elapsedTime={}ms",
                studentId,
                lecture.getId(),
                isEnrolled,
                chapters.size(),
                lectureProgress,
                elapsedTime
        );

        return response;
    }

    // 강사 기준 본인 강의 목록 조회.
    @Override
    public TeacherLecturePageResponse getTeacherLectures(GetTeacherLecturesQuery query) {
        long startTime = System.currentTimeMillis();

        log.info("강사 강의 목록 조회 시작 - requestTeacherId={}, category={}, keyword={}, page={}, size={}",
                query.teacherId(),
                query.category(),
                query.keyword(),
                query.page(),
                query.size()
        );

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


        TeacherLecturePageResponse response = new TeacherLecturePageResponse(
                content,
                query.page(),
                query.size(),
                lecturePage.totalElements(),
                lecturePage.totalPages()
        );

        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("강사 강의 목록 조회 완료 - teacherId={}, contentCount={}, totalElements={}, totalPages={}, elapsedTime={}ms",
                teacherId,
                content.size(),
                lecturePage.totalElements(),
                lecturePage.totalPages(),
                elapsedTime
        );

        return response;
    }

    // 강사 기준 본인 강의 상세 조회.
    @Override
    public TeacherLectureDetailResponse getTeacherLectureDetail(GetTeacherLectureDetailQuery query) {

        long startTime = System.currentTimeMillis();

        log.info("선생 강의 상세 조회 시작 - teacherId={}, lectureId={}",
                query.teacherId(),
                query.lectureId()
        );

        Long teacherId = teacherAccountPort.getTeacherId(query.teacherId());

        LectureAggregate lecture = lectureRepository.findById(query.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        if (!lecture.isOwnedBy(teacherId)) {
            throw new AccessDeniedException("본인이 등록한 강의만 조회할 수 있습니다.");
        }

        List<LectureChapter> chapters =
                chapterRepository.findAllByLectureIdOrderByOrderNoAsc(query.lectureId());

        LectureReviewQueryPort.ReviewStats reviewStats = lectureReviewQueryPort.getReviewStats(lecture.getId());

        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("선생 강의 상세 조회 완료 - teacherId={}, lectureId={}, chapterCount={}, elapsedTime={}ms",
                teacherId,
                lecture.getId(),
                chapters.size(),
                elapsedTime
        );

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
        long startTime = System.currentTimeMillis();

        log.info("관리자 강의 목록 조회 시작 - status={}, category={}, keyword={}, page={}, size={}",
                query.status(),
                query.category(),
                query.keyword(),
                query.page(),
                query.size()
        );
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

                    int chapterCount = chapterRepository.countByLectureId(lecture.getId());

                    return  AdminLectureListItemResponse.from(
                            lecture,
                            reviewStats.averageRating(),
                            reviewStats.reviewCount(),
                            chapterCount
                    );
                })
                .toList();

        AdminLecturePageResponse response = new AdminLecturePageResponse(
                content,
                query.page(),
                query.size(),
                lecturePage.totalElements(),
                lecturePage.totalPages()
        );

        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("관리자 강의 목록 조회 완료 - statuses={}, contentCount={}, totalElements={}, totalPages={}, elapsedTime={}ms",
                statuses,
                content.size(),
                lecturePage.totalElements(),
                lecturePage.totalPages(),
                elapsedTime
        );

        return response;
    }

    // 관리자 기준 강의 상세 조회.
    @Override
    public AdminLectureDetailResponse getAdminLectureDetail(GetAdminLectureDetailQuery query) {
        long startTime = System.currentTimeMillis();

        log.info("관리자 강의 상세 조회 시작 - lectureId={}",
                query.lectureId()
        );
        LectureAggregate lecture = lectureRepository.findById(query.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        if (lecture.getStatus() != LectureStatus.WAITING
                && lecture.getStatus() != LectureStatus.ACTIVE) {
            throw new DomainRuleViolationException("관리자는 승인 대기 또는 진행 중 강의만 조회할 수 있습니다.");
        }

        List<LectureChapter> chapters =
                chapterRepository.findAllByLectureIdOrderByOrderNoAsc(query.lectureId());

        LectureReviewQueryPort.ReviewStats reviewStats = lectureReviewQueryPort.getReviewStats(lecture.getId());

        AdminLectureDetailResponse response = AdminLectureDetailResponse.from(
                lecture,
                chapters,
                reviewStats.averageRating(),
                reviewStats.reviewCount()
        );

        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("관리자 강의 상세 조회 완료 - lectureId={}, lectureStatus={}, chapterCount={}, reviewCount={}, elapsedTime={}ms",
                lecture.getId(),
                lecture.getStatus(),
                chapters.size(),
                reviewStats.reviewCount(),
                elapsedTime
        );

        return response;
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