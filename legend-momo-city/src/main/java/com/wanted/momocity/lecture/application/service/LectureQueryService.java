package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.enrollment.application.port.StudentAccountPort;
import com.wanted.momocity.lecture.application.port.LectureEnrollmentQueryPort;
import com.wanted.momocity.lecture.application.port.TeacherAccountPort;
import com.wanted.momocity.lecture.application.query.GetLecturesQuery;
import com.wanted.momocity.lecture.application.query.GetStudentLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.GetTeacherLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.GetTeacherLecturesQuery;
import com.wanted.momocity.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.momocity.lecture.domain.exception.LectureNotFoundException;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.domain.repository.ChapterRepository;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import com.wanted.momocity.lecture.presentation.api.response.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

/* comment
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

    private final TeacherAccountPort teacherAccountPort;

    // 챕터 목록 조회
    private final ChapterRepository chapterRepository;

    // 강사 이름과 프로필 이미지를 조회하기 위한 auth 포트
    private final LoadUserPort loadUserPort;

    @Override
    @Transactional(readOnly = true)
    public StudentLecturePageResponse getLectures(GetLecturesQuery query) {

        /*
         * Authorization 토큰에서 꺼낸 로그인 사용자 ID로 학생 ID를 조회한다.
         * 컨트롤러에서는 인증된 사용자 ID만 넘기고,
         * 실제 학생 식별자는 studentAccountPort를 통해 가져온다.
         */
        Long studentId = studentAccountPort.getStudentId(query.userId());

        /*
         * 현재 학생이 이미 수강 신청한 강의 ID 목록을 조회한다.
         * 이 목록은 isEnrolled 값을 만들고,
         * enrolled=true/false 필터링에도 사용된다.
         */
        List<Long> enrolledLectureIds =
                lectureEnrollmentQueryPort.findLectureIdsByUserId(studentId);

        /*
         * 학생에게 보여줄 강의 목록을 조회한다.
         * category, keyword, enrolled 조건과 페이지네이션은 repository에서 처리한다.
         */
        var lecturePage = lectureRepository.findLectures(
                query.category(),
                query.keyword(),
                query.enrolled(),
                enrolledLectureIds,
                query.page(),
                query.size()
        );

        /*
         * 조회된 강의들을 목록 응답 item으로 변환한다.
         * 강의가 1개여도 content 배열 안에 들어가야 하므로 List로 변환한다.
         */
        List<StudentLectureListItemResponse> content = lecturePage.content().stream()
                .map(lecture -> toResponse(
                        lecture,
                        enrolledLectureIds.contains(lecture.getId()),
                        studentId
                ))
                .toList();

        /*
         * 최종 data 영역을 만든다.
         * Controller에서는 이 객체를 ApiResponse.data에 넣어 응답한다.
         */
        return new StudentLecturePageResponse(
                content,
                query.page(),
                query.size(),
                lecturePage.totalElements(),
                lecturePage.totalPages()
        );
    }

    // 학생 강의 상세 조회
    @Override
    public StudentLectureDetailResponse getStudentLectureDetail(GetStudentLectureDetailQuery query) {

        // Authorization 토큰에서 가져온 userId가 실제 학생 계정인지 확인합니다.
        Long userId = studentAccountPort.getStudentId(query.userId());

        // lectureId로 강의를 조회합니다.
        LectureAggregate lecture = lectureRepository.findById(query.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        // 학생은 ACTIVE 상태의 강의만 상세 조회할 수 있음
        if (lecture.getStatus() != LectureStatus.ACTIVE) {
            throw new AccessDeniedException("진행 중인 강의만 조회할 수 있습니다.");
        }

        // 해당 강의의 챕터 목록을 orderNo 오름차순으로 조회
        List<LectureChapter> chapters =
                chapterRepository.findAllByLectureIdOrderByOrderNoAsc(query.lectureId());

        // 로그인한 학생이 이 강의를 수강신청했는지 확인
        boolean isEnrolled = lectureEnrollmentQueryPort
                .findByUserIdAndLectureId(userId, query.lectureId())
                .isPresent();

        // lecture.teacherId로 강사 정보를 조회
        User teacher = loadUserPort.findById(lecture.getTeacherId())
                .orElseThrow(() -> new LectureNotFoundException("강사 정보를 찾을 수 없습니다."));

        // 강사 이름
        String teacherName = teacher.getName();

        // 강사 프로필 이미지 URL
        String teacherProfileImageUrl = teacher.getProfileImageUrl();

        // 리뷰 집계는 다음 단계에서 review 테이블 조회로 연결
        double averageRating = 0.0;
        int reviewCount = 0;

        // 조회한 강의/챕터/부가정보를 학생 상세 응답 DTO로 변환
        return StudentLectureDetailResponse.from(
                lecture,
                chapters,
                teacherName,
                teacherProfileImageUrl,
                averageRating,
                reviewCount,
                isEnrolled
        );
    }

    // 강사가 본인이 등록한 강의 목록을 조회
    @Override
    public TeacherLecturePageResponse getTeacherLectures(GetTeacherLecturesQuery query) {

        // Authorization 토큰에서 가져온 email로 강사 ID를 조회
        Long teacherId = teacherAccountPort.getTeacherId(query.teacherId());

        // 강의 목록을 조회
        // category, keyword, enrolled 조건은 repository에서 처리
        var lecturePage = lectureRepository.findTeacherLectures(
                teacherId,
                query.category(),
                query.keyword(),
                query.page(),
                query.size()
        );

        // 도메인 모델을 강사용 목록 응답 DTO로 변환
        List<TeacherLectureListItemResponse> content = lecturePage.content().stream()
                .map(lecture -> TeacherLectureListItemResponse.from(
                        lecture,
                        0.0,
                        0
                ))
                .toList();

        // 페이지 정보와 목록 응답을 함께 반환
        return new TeacherLecturePageResponse(
                content,
                query.page(),
                query.size(),
                lecturePage.totalElements(),
                lecturePage.totalPages()
        );
    }

    // 강사가 본인이 등록한 강의 상세 정보와 챕터 목록을 조회
    @Override
    public TeacherLectureDetailResponse getTeacherLectureDetail(GetTeacherLectureDetailQuery query) {

        // 토큰에서 꺼낸 teacherId가 실제 강사 계정인지 확인
        Long teacherId = teacherAccountPort.getTeacherId(query.teacherId());

        // lectureId로 강의를 조회
        LectureAggregate lecture = lectureRepository.findById(query.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        // 본인이 등록한 강의가 아니면 조회할 수 없음
        if (!lecture.isOwnedBy(teacherId)) {
            throw new AccessDeniedException("본인이 등록한 강의만 조회할 수 있습니다.");
        }

        // 해당 강의의 챕터 목록을 orderNo 오름차순으로 조회
        List<LectureChapter> chapters =
                chapterRepository.findAllByLectureIdOrderByOrderNoAsc(query.lectureId());

        // 강의 정보와 챕터 목록을 응답 DTO로 변환
        return TeacherLectureDetailResponse.from(
                lecture,
                chapters,
                0.0,
                0
        );
    }

    // Lecture 도메인 객체를 학생 강의 목록 응답 DTO로 변환
    private StudentLectureListItemResponse toResponse(
            LectureAggregate lecture,
            boolean enrolled,
            Long userId
    ) {
        // 강사 이름 조회는 Null -> 나중에 검색 기능 확장 가능
        String teacherName = null;

        // 강의평은 module 4 때 구현 할 예정 -> 현재는 0으로 둠
        double averageRating = 0.0;
        int reviewCount = 0;

        return new StudentLectureListItemResponse(
                lecture.getId(),                  // lectureId: 강의 ID
                lecture.getTeacherId(),           // teacherId: 강사 ID
                teacherName,                      // teacherName: 강사 이름
                lecture.getTitle(),               // title: 강의 제목
                lecture.getDescription(),         // description: 강의 설명
                lecture.getThumbnailUrl(),        // thumbnailUrl: 썸네일 URL
                lecture.getCategory().name(),     // category: 강의 카테고리
                lecture.getStatus().name(),       // lectureStatus: 강의 상태
                lecture.getCompletedUserCount(),  // completedUserCount: 완료한 사용자 수
                averageRating,                    // averageRating: 평균 평점
                reviewCount,                      // reviewCount: 리뷰 개수
                enrolled,                         // isEnrolled: 수강신청 여부
                lecture.getCreatedAt()            // createdAt: 강의 등록일
        );
    }


}