package com.wanted.momocity.viewing.infrastructure.adapter;

import com.wanted.momocity.viewing.application.port.CategoryProgressInfo;
import com.wanted.momocity.viewing.application.port.CategoryProgressPort;
import com.wanted.momocity.viewing.application.service.ViewingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/*
* comment.
*  CategoryProgressPort 구현체
*  - ViewingQueryService 가 CategoryProgressPort 를 직접 implements 하면
*  순환 참조 발생 -> 별도 Adapter 로 분리해서 순환 참조 방지
*  -
*  - 흐름 : 팀원 도메인 -> CategoryProgressPort.getCategoryProgress() 호출
*  -> CategoryProgressAdapter -> ViewingQueryService.getCategoryProgress() 위임
* */

@Component
@RequiredArgsConstructor
public class ChapterProgressAdapter implements CategoryProgressPort {

    // 실제 카테고리별 진척도 계산 로직 보유
    // Adapter 는 Port 계약만 이행하고 실제 로직은 Service 에 위임
    private final ViewingQueryService viewingQueryService;

    @Override
    public CategoryProgressInfo getCategoryProgress(Long userId, String category) {
        return viewingQueryService.getCategoryProgress(userId, category);
    }
}
