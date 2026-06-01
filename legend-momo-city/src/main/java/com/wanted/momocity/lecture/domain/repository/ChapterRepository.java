package com.wanted.momocity.lecture.domain.repository;


import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.domain.model.VideoStatus;

import java.util.List;
import java.util.Optional;


// ChapterRepository는 챕터 도메인이 필요로 하는 저장소
// 실제 JPA 구현은 infrastructure 계층에서 담당
public interface ChapterRepository {

    // 챕터를 저장
    LectureChapter save(LectureChapter chapter);

    // 특정 강의에 등록된 챕터 개수를 조회
    int countByLectureId(Long lectureId);

    // 같은 강의 안에서 동일한 orderNo가 이미 사용 중인지 확인
    boolean existsByLectureIdAndOrderNo(Long lectureId, int orderNo);

    // chapterId로 기존 챕터를 조회
    // 동영상 등록은 기존 챕터에 영상 정보를 채우는 작업이라 단건 조회가 필요
    Optional<LectureChapter> findById(Long chapterId);

    // 특정 강의에 동영상이 등록되지 않은 챕터가 있는지 확인
    boolean existsByLectureIdAndVideoUrlIsNull(Long lectureId);

    // 특정 강의에 등록된 챕터 목록을 orderNo 오름차순으로 조회
    List<LectureChapter> findAllByLectureIdOrderByOrderNoAsc(Long lectureId);

    // 특정 강의 안에 지정한 영상 상태가 아닌 챕터가 있는지 확인한
    boolean existsByLectureIdAndVideoStatusNot(Long lectureId, VideoStatus videoStatus);
}