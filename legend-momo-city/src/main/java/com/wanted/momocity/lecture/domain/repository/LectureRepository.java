package com.wanted.momocity.lecture.domain.repository;

import com.wanted.momocity.lecture.domain.model.Lecture;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LecturePage;

import java.util.List;
import java.util.Optional;

/*
 * LectureRepository는 강의 도메인이 필요로 하는 저장소
 * 이 인터페이스는 domain 계층에 위치한다.
 * domain/application 계층은 DB가 JPA인지 MyBatis인지 알 필요가 없고,
 * "강의를 저장한다", "강의를 조회한다" 같은 기능 계약만 알면 된다.
 * 실제 구현은 infrastructure/persistence의 LectureRepositoryAdapter가 담당
 */
public interface LectureRepository {

    // 강의를 저장
    Lecture save(Lecture lecture);

    // 강의 ID로 강의를 조회
    Optional<Lecture> findById(Long lectureId);

    /*
     * 학생용 강의 목록을 조회
     * 이 조회는 기본적으로 ACTIVE 상태의 강의만 대상으로 한다.
     * category가 있으면 해당 카테고리의 강의만 조회
     * enrolled가 true이면 enrolledLectureIds에 포함된 강의만 조회
     * enrolled가 false이면 enrolledLectureIds에 포함되지 않은 강의만 조회
     * enrolled가 null이면 수강 여부와 상관없이 조회
     */
    LecturePage findLectures(
            LectureCategory category,
            Boolean enrolled,
            List<Long> enrolledLectureIds,
            int page,
            int size
    );
}