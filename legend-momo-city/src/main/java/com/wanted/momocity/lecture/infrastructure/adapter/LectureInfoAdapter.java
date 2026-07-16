package com.wanted.momocity.lecture.infrastructure.adapter;

import com.wanted.momocity.chatbot.application.port.LectureInfoPort;
import com.wanted.momocity.chatbot.application.port.LectureInfoPort.LectureSummary;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

// chatbot의 포트를 Lecture 영역에서 구현하는 어뎁터
@Component
@RequiredArgsConstructor
public class LectureInfoAdapter implements LectureInfoPort {

    private final LectureRepository lectureRepository;

    // LectureInfoPort에 정의된 강의 요약 조회 기능 구현
    @Override
    public Optional<LectureSummary> findLectureSummary(Long lectureId) {
        // LectureRepository에서 전달받은 강의 ID로 강의를 조회
        return lectureRepository.findById(lectureId)
                // 강의가 존재할 때만 LectureSummary로 변환
                .map(lecture -> new LectureSummary(
                        lecture.getId(),
                        lecture.getTitle(),
                        lecture.getDescription()
                ));
    }
}
