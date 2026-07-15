package com.wanted.momocity.review.infrastructure.adapter;

import com.wanted.momocity.chatbot.application.port.ReviewInfoPort;
import com.wanted.momocity.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

// chatbot BC의 ReviewInfoPort를 review BC에서 구현 (LectureReviewQueryAdapter와 동일한 컨벤션)
@Component
@RequiredArgsConstructor
public class ReviewInfoAdapter implements ReviewInfoPort {

    private final ReviewRepository reviewRepository;

    @Override
    public List<String> getReviewContents(Long lectureId) {
        return reviewRepository.findContentsByLectureId(lectureId);
    }
}
