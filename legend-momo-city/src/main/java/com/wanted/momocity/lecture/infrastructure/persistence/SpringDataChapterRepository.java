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

    // 강의에 videoUrl이 비어있는 챕터가  있는지 확인
    boolean existsByLectureIdAndVideoUrlIsNull(Long lectureId);

    // 특정 강의에 속한 챕터 목록을 orderNo 오름차순으로 조회
    List<ChapterJpaEntity> findAllByLectureIdOrderByOrderNoAsc(Long lectureId);

    // 특정 강의의 챕터 중 지정한 영상 상태가 아닌 챕터가 있는지 확인
    boolean existsByLectureIdAndVideoStatusNot(Long lectureId, VideoStatus videoStatus);
}