package com.wanted.momocity.lecture.infrastructure.persistence;

import com.wanted.momocity.lecture.domain.model.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// SpringDataChapterRepository는 Spring Data JPA 전용 Repository
public interface SpringDataChapterRepository extends JpaRepository<ChapterJpaEntity, Long> {

    // 특정 강의에 등록된 챕터 개수를 조회
    int countByLectureId(Long lectureId);

    // 같은 강의 안에서 동일한 orderNo가 이미 있는지 확인
    boolean existsByLectureIdAndOrderNo(Long lectureId, int orderNo);

    // 현재 챕터를 제외하고 같은 강의에 동일한 순서가 존재하는지 조회합
    boolean existsByLectureIdAndOrderNoAndIdNot(
            Long lectureId,
            int orderNo,
            Long chapterId
    );

    // 강의에 videoUrl이 비어있는 챕터가  있는지 확인
    boolean existsByLectureIdAndVideoUrlIsNull(Long lectureId);

    // 특정 강의에 속한 챕터 목록을 orderNo 오름차순으로 조회
    List<ChapterJpaEntity> findAllByLectureIdOrderByOrderNoAsc(Long lectureId);

    // lectureId 기준으로 해당 강의에 속한 모든 챕터 row를 실제 삭제
    void deleteAllByLectureId(Long lectureId);
}