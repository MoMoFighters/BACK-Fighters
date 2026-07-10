package com.wanted.momocity.chatbot.infrastructure.adapter;

import com.wanted.momocity.chatbot.application.port.ReviewInfoPort;
import com.wanted.momocity.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// review 도메인에 lectureId 기준 목록 조회 메서드가 아직 없음 (모모님 응답 대기) → 응답 오기 전까지 임시 stub
@Component
@RequiredArgsConstructor
public class ReviewInfoAdapter implements ReviewInfoPort {

    private final ReviewRepository reviewRepository;
}
