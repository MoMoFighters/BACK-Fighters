package com.wanted.momocity.viewing.infrastructure.catalog;

import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.infrastructure.persistence.LectureJpaEntity;
import com.wanted.momocity.lecture.infrastructure.persistence.SpringDataLectureRepository;
import com.wanted.momocity.viewing.application.port.LecturePort;
import com.wanted.momocity.viewing.domain.model.Lecture;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

/*
* comment.
*  [역할]
*  LecturePort 인터페이스 구현체
*  catalog 컨텍스트 소유의 Lecture 를 READ 전용으로 조회
*  -
*  [Redis 캐싱 전략]
 * @Cacheable("lecture") → lectureId 기준 단건 캐싱
 * -
 * 왜 캐싱이 필요한가:
 * -> getLectureMeta(), getMyLectures() 등 매번 DB 조회
 * -> Redis 캐싱으로 DB 부하 감소
 * -> 강의 정보는 자주 바뀌지 않아 캐싱 효과 극대화
* */

@Component
@RequiredArgsConstructor
public class LectureCatalogAdapter implements LecturePort {

    // SpringDataLectureRepository 주입
    private final SpringDataLectureRepository springDataLectureRepository;
    // LoadUserPort 주입
    // → teacherId 로 강사 이름 조회할 때 사용
    private final LoadUserPort loadUserPort;

    /*
     * comment.
     *  강의 단건 조회
     *  @Cacheable("lecture")
     *  -> 처음 호출 시 DB 조회 후 Redis 에 저장
     *  -> 이후 호출 시 Redis 에서 반환 (DB 조회 없음)
     *  -> key = "lecture::1", "lecture::2" 형태로 저장
     */

    @Override
    @Cacheable(value = "lecture", key = "#lectureId")
    public Lecture findById(Long lectureId) {

        // SpringDataLectureRepository 의 findById() 로 강의 DB 조회
        LectureJpaEntity entity = springDataLectureRepository.findById(lectureId)
                .orElseThrow(() -> new DomainRuleViolationException("강의를 찾을 수 없습니다."));

        // LoadUserPort 로 강사 이름 조회
        // → LectureJpaEntity 에 instructorName 없어서 LoadUserPort 통해 teacherId 로 user 이름 조회
        // → 없으면 "강사" 로 기본값 처리
        String instructorName = loadUserPort.findById(entity.getTeacherId())
                .map(user -> user.getName())
                .orElse("강사");

        // LectureJpaEntity → Lecture 도메인으로 변환
        // category 는 Enum → String 변환
        return Lecture.reconstitute(
                entity.getId(),
                entity.getTeacherId(),
                entity.getTitle(),
                entity.getThumbnailUrl(),
                entity.getCategory().name(),
                instructorName,
                entity.getStatus().name()
        );
    }

    /*
    * comment.
    *  카테고리별 강의 목록 조회
    *  -> CategoryProgressPort 에서 카테고리별 진척도 계산 시 사용
    *  -> ACTIVE 상태 강의만 조회
    *  -> String category -> LectureCategory Enum 변환 후 조회
    *  -> Pageable.unpaged() 로 페이지네이션 없이 조회
    * */

    @Override
    public List<Lecture> findAllByCategory(String category) {
        return springDataLectureRepository
                .findAllByCategoryAndStatus(
                        LectureCategory.valueOf(category),
                        LectureStatus.ACTIVE,
                        Pageable.unpaged()
                )
                .getContent()
                .stream()
                .map(entity -> {
                    // instructorName : LectureJpaEntity 에 instructorName 없음
                    // LoadUserPort 로 teacherId 기준 강사 이름 조회 -> 없으면 강사 기본값 처리
                    String instructorName = loadUserPort.findById(entity.getTeacherId())
                            .map(user -> user.getName())
                            .orElse("강사");
                    return Lecture.reconstitute(
                            entity.getId(),
                            entity.getTeacherId(),
                            entity.getTitle(),
                            entity.getThumbnailUrl(),
                            entity.getCategory().name(),
                            instructorName,
                            entity.getStatus().name()
                    );
                })
                .toList();
    }
}
