package com.wanted.momocity.viewing.application.port;

/*
* comment.
*  카테고리별 진척도 + 최근 이어보기 조회 포트
*  팀원 빌딩 / 마이페이지 도메인에서 주입받아 사용
*  -> category = null 이면 전체 조회
*  -> category != null 이면 해당 카테고리 조회
*  -
*  viewing.application.service.ViewingQueryService
* */

public interface CategoryProgressPort {
    CategoryProgressInfo getCategoryProgress(Long userId, String category);
}
