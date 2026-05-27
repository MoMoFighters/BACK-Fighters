package com.wanted.momocity.enrollment.infrastructure.adapter;

import com.wanted.momocity.enrollment.application.port.EnrollmentLecturePort;
import com.wanted.momocity.enrollment.domain.exception.EnrollmentLectureNotFoundException;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.infrastructure.persistence.LectureJpaEntity;
import com.wanted.momocity.lecture.infrastructure.persistence.SpringDataLectureRepository;
import org.springframework.stereotype.Component;

// lecture 저장소를 통해 수강신청에 필요한 강의 정보를 조회
@Component
public class LectureEnrollmentAdapter implements EnrollmentLecturePort {

    private final SpringDataLectureRepository lectureRepository;

    public LectureEnrollmentAdapter(SpringDataLectureRepository lectureRepository) {
        this.lectureRepository = lectureRepository;
    }

    @Override
    public LectureStatus getLectureStatus(Long lectureId) {
        LectureJpaEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new EnrollmentLectureNotFoundException("강의를 찾을 수 없습니다."));

        return lecture.getStatus();
    }
}