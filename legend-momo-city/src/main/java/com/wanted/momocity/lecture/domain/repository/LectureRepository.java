package com.wanted.momocity.lecture.domain.repository;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LecturePage;
import com.wanted.momocity.lecture.domain.model.LectureStatus;

import java.util.List;
import java.util.Optional;

/* comment
 * LectureRepository는 강의 도메인이 필요로 하는 저장소
 * domain/application 계층은 DB가 JPA인지 MyBatis인지 알 필요가 없고,
 * "강의를 저장한다", "강의를 조회한다" 같은 기능 계약만 알면 된다.
 */
public interface LectureRepository {

    // 강의를 저장
    LectureAggregate save(LectureAggregate lecture);

    // 강의 ID로 강의를 조회
    Optional<LectureAggregate> findById(Long lectureId);

    // 학생용 강의 목록을 조회
    LecturePage findLectures(
            LectureCategory category,
            String keyword,
            Boolean enrolled,
            List<Long> enrolledLectureIds,
            int page,
            int size
    );

    // 강사가 등록한 강의 목록을 조회
    LecturePage findTeacherLectures(
            Long teacherId,
            LectureCategory category,
            String keyword,
            int page,
            int size
    );

    // 관리자가 강의 목록을 조회
    // status 목록을 받아 WAITING, ACTIVE 같은 여러 상태를 한 번에 조회
    LecturePage findAdminLectures(
            List<LectureStatus> statuses,
            LectureCategory category,
            String keyword,
            int page,
            int size
    );
}