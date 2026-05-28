package com.wanted.momocity.lecture.domain.repository;


import com.wanted.momocity.lecture.domain.model.LectureChapter;


// ChapterRepository는 챕터 도메인이 필요로 하는 저장소
// 실제 JPA 구현은 infrastructure 계층에서 담당
public interface ChapterRepository {

    // 챕터를 저장
    LectureChapter save(LectureChapter chapter);

    // 특정 강의에 등록된 챕터 개수를 조회
    int countByLectureId(Long lectureId);

    // 같은 강의 안에서 동일한 orderNo가 이미 사용 중인지 확인
    boolean existsByLectureIdAndOrderNo(Long lectureId, int orderNo);

}