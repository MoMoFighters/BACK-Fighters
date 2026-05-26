package com.wanted.momocity.lecture.domain.repository;

import com.wanted.momocity.lecture.domain.model.Lecture;

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

    /*
     * 강의를 저장
     * 신규 강의 생성, 강의 수정, 강의 삭제 상태 반영 모두 save를 통해 처리
     */
    Lecture save(Lecture lecture);
}