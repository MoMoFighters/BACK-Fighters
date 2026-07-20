package com.wanted.momocity.viewing.application.port;

/*
* comment.
*  CategoryProgressPort 의 반환 DTO
*  팀원 빌딩 / 마이페이지에서 사용할 진척도 + 이어보기 정보
*  -> category == null 이면 전체 진척도 + 전체 최근 이어보기
*  -> category != null 이면 카테고리별 진척도 + 카테고리별 최근 이어보기
* */

public record CategoryProgressInfo (
        // 전체 또는 카테고리별 진척도
        int myTotalProgress,
        // 최근 본 강의 ID
        Long lectureId,
        // 최근 본 강의 제목
        String lectureTitle,
        // 최근 본 챕터 ID
        Long chapterId,
        // 최근 본 챕터 제목
        String chapterTitle,
        // 최근 본 챕터 썸네일
        String chapterThumbnailUrl,
        // 최근 본 챕터 진척도
        int chapterProgress
){}
