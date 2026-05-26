package com.wanted.momocity.viewing.application.usecase;

import com.wanted.momocity.viewing.presentation.api.response.StreamingUrlResponse;

/*
* comment.
*  Controller 는 해당 인터페이스만 알고, 실제 구현체인 ViewingService 를 모름
*  -> 테스트 시 Mock 으로 교체 가능
* */

public interface GetStreamingUrlUseCase {

    StreamingUrlResponse getStreamingUrl (Long userId, Long lectureId, Long chapterId);

}
