package com.wanted.momocity.lecture.infrastructure.persistence;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LecturePage;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class LectureRepositoryAdapter implements LectureRepository {

    private final SpringDataLectureRepository repository;

    public LectureRepositoryAdapter(SpringDataLectureRepository repository) {
        this.repository = repository;
    }

    // 강의를 저장
    /* 강의 등록
    *    → id null
    *   → 새 row 저장
    * 강의 상태 변경
    *   → id 있음
    *   → 기존 row 조회 후 status만 변경
    */
    @Override
    public LectureAggregate save(LectureAggregate lecture) {
        if (lecture.getId() != null) {
            LectureJpaEntity entity = repository.findById(lecture.getId())
                    .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

            entity.changeStatus(lecture.getStatus());

            return toDomain(entity);
        }

        LectureJpaEntity entity = new LectureJpaEntity(
                lecture.getTeacherId(),
                lecture.getTitle(),
                lecture.getDescription(),
                lecture.getThumbnailUrl(),
                lecture.getCategory(),
                lecture.getStatus()
        );

        LectureJpaEntity saved = repository.save(entity);

        return toDomain(saved);
    }

    // 강의 ID로 강의를 조회
    @Override
    @Transactional(readOnly = true)
    public Optional<LectureAggregate> findById(Long lectureId) {
        return repository.findById(lectureId)
                .map(this::toDomain);
    }


    // 학생용 강의 목록을 조회
    // 학생용 목록은 기본적으로 ACTIVE 상태의 강의 보여준다.
    @Override
    @Transactional(readOnly = true)
    public LecturePage findLectures(
            LectureCategory category,
            String keyword,
            Boolean enrolled,
            List<Long> enrolledLectureIds,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);

        String normalizedKeyword = normalizeKeyword(keyword);

        Page<LectureJpaEntity> lecturePage = findLecturePage(
                category,
                normalizedKeyword,
                enrolled,
                enrolledLectureIds,
                pageable
        );

        List<LectureAggregate> content = lecturePage.getContent().stream()
                .map(this::toDomain)
                .toList();

        return new LecturePage(
                content,
                lecturePage.getTotalElements(),
                lecturePage.getTotalPages()
        );
    }

    // 강사용
    @Override
    @Transactional(readOnly = true)
    public LecturePage findTeacherLectures(
            Long teacherId,
            LectureCategory category,
            String keyword,
            int page,
            int size
    ) {
        // 프론트는 page를 1부터 보내고, Spring Data JPA는 page를 0부터 시작
        Pageable pageable = PageRequest.of(page - 1, size);

        // keyword가 null이거나 공백이면 검색 조건을 적용하지 않도록 null로 정리
        String normalizedKeyword = normalizeKeyword(keyword);

        Page<LectureJpaEntity> lecturePage = repository.findTeacherLectures(
                teacherId,
                category,
                normalizedKeyword,
                pageable
        );

        // jpa entity를 도메인 모델로 변환
        List<LectureAggregate> content = lecturePage.getContent().stream()
                .map(this::toDomain)
                .toList();

        return new LecturePage(
                content,
                lecturePage.getTotalElements(),
                lecturePage.getTotalPages()
        );
    }

    // 관리자가 강의 목록을 조회
    // statuses에는 기본적으로 WAITING, ACTIVE가 들어온다.
    @Override
    @Transactional(readOnly = true)
    public LecturePage findAdminLectures(
            List<LectureStatus> statuses,
            LectureCategory category,
            String keyword,
            int page,
            int size
    ) {
        // 프론트는 page를 1부터 보내고, Spring Data JPA는 page를 0부터 시작
        Pageable pageable = PageRequest.of(page - 1, size);

        // keyword가 null이거나 공백이면 검색 조건을 적용하지 않기 위해 null로 정리
        String normalizedKeyword = normalizeKeyword(keyword);

        // 관리자 강의 목록 조회 쿼리를 실행
        Page<LectureJpaEntity> lecturePage = repository.findAdminLectures(
                statuses,
                category,
                normalizedKeyword,
                pageable
        );

        // JPA Entity를 도메인 모델로 변환
        List<LectureAggregate> content = lecturePage.getContent().stream()
                .map(this::toDomain)
                .toList();

        // 도메인 페이지 객체로 감싸서 반환
        return new LecturePage(
                content,
                lecturePage.getTotalElements(),
                lecturePage.getTotalPages()
        );
    }

    // keyword가 비어 있으면 검색 조건을 적용하지 않기 위해 null로 변환
    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return keyword.trim();
    }

    // category, enrolled 조건에 맞춰 ACTIVE 강의만 조회
    private Page<LectureJpaEntity> findLecturePage(
            LectureCategory category,
            String keyword,
            Boolean enrolled,
            List<Long> enrolledLectureIds,
            Pageable pageable
    ) {
        // 학생용 목록에서는 항상 ACTIVE 상태의 강의만 조회
        LectureStatus status = LectureStatus.ACTIVE;

        // enrolled 조건이 없으면 수강 여부와 상관없이 ACTIVE 강의를 조회
        if (enrolled == null) {
            return repository.findStudentLectures(
                    status,
                    category,
                    keyword,
                    pageable
            );
        }

        // enrolled=true인데 내가 신청한 강의가 하나도 없다면, 조회 결과는 빈 페이지
        if (Boolean.TRUE.equals(enrolled) && enrolledLectureIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // enrolled=true이면 내가 신청한 ACTIVE 강의만 조회
        if (Boolean.TRUE.equals(enrolled)) {
            return repository.findStudentLecturesByEnrolled(
                    status,
                    enrolledLectureIds,
                    category,
                    keyword,
                    pageable
            );
        }

        // enrolled=false인데 내가 신청한 강의가 없다면,제외할 강의가 없으므로 ACTIVE 강의 전체를 조회
        if (enrolledLectureIds.isEmpty()) {
            return repository.findStudentLectures(
                    status,
                    category,
                    keyword,
                    pageable
            );
        }

        // enrolled=false이면 내가 신청하지 않은 ACTIVE 강의만 조회
        return repository.findStudentLecturesByNotEnrolled(
                status,
                enrolledLectureIds,
                category,
                keyword,
                pageable
        );

    }

    // JPA Entity를 도메인 모델로 변환
    private LectureAggregate toDomain(LectureJpaEntity entity) {
        return LectureAggregate.restore(
                entity.getId(),
                entity.getTeacherId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getThumbnailUrl(),
                entity.getCategory(),
                entity.getStatus(),
                entity.getCompletedUserCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

}
