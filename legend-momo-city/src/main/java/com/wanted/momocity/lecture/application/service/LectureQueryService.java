package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.enrollment.application.port.StudentAccountPort;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.port.LectureEnrollmentQueryPort;
import com.wanted.momocity.lecture.application.port.TeacherAccountPort;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetAdminLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetAdminLecturesQuery;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetLecturesQuery;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetStudentLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetTeacherLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetTeacherLecturesQuery;
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

/**
 * 강의 조회 기능을 처리하는 Application Service.
 *
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

    // 강사 이름, 프로필 이미지 조회를 위한 auth 포트
    private final LoadUserPort loadUserPort;

    // 학생 기준 강의 목록 조회.
    @Override
    public StudentLecturePageResponse getLectures(GetLecturesQuery query) {
        Long studentId = studentAccountPort.getStudentId(query.userId());

        List<Long> enrolledLectureIds =
                lectureEnrollmentQueryPort.findLectureIdsByUserId(studentId);

        var lecturePage = lectureRepository.findLectures(
                query.category(),
                query.keyword(),
                query.enrolled(),
                enrolledLectureIds,
                query.page(),
                query.size()
        );

        List<StudentLectureListItemResponse> content = lecturePage.content().stream()
                .map(lecture -> toStudentListItemResponse(
                        lecture,
                        enrolledLectureIds.contains(lecture.getId())
                ))
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

        boolean isEnrolled = lectureEnrollmentQueryPort
                .findByUserIdAndLectureId(studentId, query.lectureId())
                .isPresent();

        User teacher = loadUserPort.findById(lecture.getTeacherId())
                .orElseThrow(() -> new LectureNotFoundException("강사 정보를 찾을 수 없습니다."));

        return StudentLectureDetailResponse.from(
                lecture,
                chapters,
                teacher.getName(),
                teacher.getProfileImageUrl(),
                0.0,
                0,
                isEnrolled
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

        List<TeacherLectureListItemResponse> content = lecturePage.content().stream()
                .map(lecture -> TeacherLectureListItemResponse.from(
                        lecture,
                        0.0,
                        0
                ))
                .toList();

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

        return TeacherLectureDetailResponse.from(
                lecture,
                chapters,
                0.0,
                0
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

        List<AdminLectureListItemResponse> content = lecturePage.content().stream()
                .map(lecture -> AdminLectureListItemResponse.from(
                        lecture,
                        0.0,
                        0
                ))
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

        return AdminLectureDetailResponse.from(
                lecture,
                chapters,
                0.0,
                0
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
    private StudentLectureListItemResponse toStudentListItemResponse(
            LectureAggregate lecture,
            boolean enrolled
    ) {
        return new StudentLectureListItemResponse(
                lecture.getId(),
                lecture.getTeacherId(),
                null,
                lecture.getTitle(),
                lecture.getDescription(),
                lecture.getThumbnailUrl(),
                lecture.getCategory().name(),
                lecture.getStatus().name(),
                lecture.getCompletedUserCount(),
                0.0,
                0,
                enrolled,
                lecture.getCreatedAt()
        );
    }
}