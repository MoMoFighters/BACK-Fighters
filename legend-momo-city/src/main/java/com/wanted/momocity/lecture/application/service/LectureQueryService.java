package com.wanted.momocity.lecture.application.service;

import com.wanted.momocity.enrollment.application.port.StudentAccountPort;
import com.wanted.momocity.lecture.application.port.LectureEnrollmentQueryPort;
import com.wanted.momocity.lecture.application.port.TeacherAccountPort;
import com.wanted.momocity.lecture.application.query.GetLecturesQuery;
import com.wanted.momocity.lecture.application.query.GetTeacherLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.GetTeacherLecturesQuery;
import com.wanted.momocity.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.momocity.lecture.domain.exception.LectureNotFoundException;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.domain.repository.ChapterRepository;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import com.wanted.momocity.lecture.presentation.api.response.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
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

    private final TeacherAccountPort teacherAccountPort;

    // 챕터 목록 조회
    private final ChapterRepository chapterRepository;

    // 강의 목록을 조회
    @Override
    public StudentLecturePageResponse getLectures(GetLecturesQuery query) {

        // Authorization 토큰에서 꺼낸 email로 userId를 조회
        Long userId = studentAccountPort.getStudentId(query.userId());

        // userId 기준으로 수강 신청한 강의 ID 목록을 조회
        List<Long> enrolledLectureIds = lectureEnrollmentQueryPort.findLectureIdsByUserId(userId);

        // 강의 목록을 조회
        // category와 enrolled 조건은 repository에서 처리함.
        var lecturePage = lectureRepository.findLectures(
                query.category(),
                query.keyword(),
                query.enrolled(),
                enrolledLectureIds,
                query.page(),
                query.size()
        );

        // 조회된 강의들을 응답 DTO로 변환합니다.
        List<StudentLectureListItemResponse> content = lecturePage.content().stream()
                .map(lecture -> toResponse(
                        lecture,
                        enrolledLectureIds.contains(lecture.getId()),
                        userId
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

    /*
     * 강사가 본인이 등록한 강의 목록을 조회합니다.
     */
    @Override
    public TeacherLecturePageResponse getTeacherLectures(GetTeacherLecturesQuery query) {
        /*
         * Authorization 토큰에서 가져온 email로 강사 ID를 조회합니다.
         * 강사 권한이 아니거나 사용자를 찾을 수 없으면 TeacherAccountPort 쪽에서 예외가 발생합니다.
         */
        Long teacherId = teacherAccountPort.getTeacherId(query.teacherId());

        // 강의 목록을 조회합니다.
        // category, keyword, enrolled 조건은 repository에서 처리합니다.
        var lecturePage = lectureRepository.findTeacherLectures(
                teacherId,
                query.category(),
                query.keyword(),
                query.page(),
                query.size()
        );

        /*
         * 도메인 모델을 강사용 목록 응답 DTO로 변환합니다.
         */
        List<TeacherLectureListItemResponse> content = lecturePage.content().stream()
                .map(TeacherLectureListItemResponse::from)
                .toList();

        /*
         * 페이지 정보와 목록 응답을 함께 반환합니다.
         */
        return new TeacherLecturePageResponse(
                content,
                query.page(),
                query.size(),
                lecturePage.totalElements(),
                lecturePage.totalPages()
        );
    }

    // 강사가 본인이 등록한 강의 상세 정보와 챕터 목록을 조회합니다.
    @Override
    public TeacherLectureDetailResponse getTeacherLectureDetail(GetTeacherLectureDetailQuery query) {

        // 토큰에서 꺼낸 teacherId가 실제 강사 계정인지 확인합니다.
        Long teacherId = teacherAccountPort.getTeacherId(query.teacherId());

        // lectureId로 강의를 조회합니다.
        LectureAggregate lecture = lectureRepository.findById(query.lectureId())
                .orElseThrow(() -> new LectureNotFoundException("강의를 찾을 수 없습니다."));

        // 본인이 등록한 강의가 아니면 조회할 수 없습니다.
        if (!lecture.isOwnedBy(teacherId)) {
            throw new AccessDeniedException("본인이 등록한 강의만 조회할 수 있습니다.");
        }

        // 해당 강의의 챕터 목록을 orderNo 오름차순으로 조회합니다.
        List<LectureChapter> chapters =
                chapterRepository.findAllByLectureIdOrderByOrderNoAsc(query.lectureId());

        // 강의 정보와 챕터 목록을 응답 DTO로 변환합니다.
        return TeacherLectureDetailResponse.from(lecture, chapters);
    }

    /**
     * Lecture 도메인 객체를 학생 강의 목록 응답 DTO로 변환합니다.
     */
    private StudentLectureListItemResponse toResponse(
            LectureAggregate lecture,
            boolean enrolled,
            Long userId
    ) {
        /*
         * TODO:
         * 지금은 강사 이름 조회 기능을 아직 연결하지 않았기 때문에 null로 둡니다.
         * 이후 teacherId로 user.name을 조회하는 포트를 연결하면 됩니다.
         */
        String teacherName = null;

        /*
         * TODO:
         * 지금은 리뷰 집계 기능을 아직 연결하지 않았기 때문에 기본값으로 둡니다.
         * 이후 review 테이블에서 평균 평점과 리뷰 개수를 조회하면 됩니다.
         */
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