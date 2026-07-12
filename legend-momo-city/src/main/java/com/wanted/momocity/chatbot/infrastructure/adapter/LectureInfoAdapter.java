package com.wanted.momocity.chatbot.infrastructure.adapter;

import com.wanted.momocity.chatbot.application.port.LectureInfoPort;
import com.wanted.momocity.lecture.domain.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// LectureRepository.findById()가 이미 있어서 즉시 구현 가능 (모모님 응답 대기 불필요)
@Component
@RequiredArgsConstructor
public class LectureInfoAdapter implements LectureInfoPort {

    private final LectureRepository lectureRepository;
}
