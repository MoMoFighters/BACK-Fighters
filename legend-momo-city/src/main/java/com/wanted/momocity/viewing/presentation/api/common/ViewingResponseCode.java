package com.wanted.momocity.viewing.presentation.api.common;

/*
* comment.
*  viewing 컨텍스트 전용 API 응답 코드 상수 모음
*  클라이언트가 업무 시나리오 기준으로 응답 해석 가능
*  VIEWING-*
* */

public class ViewingResponseCode {

    private ViewingResponseCode() {}

    // 강의 시청
    public static final String STREAMING_URL_ISSUED = "VIEWING-STREAMING-URL-ISSUED";
    public static final String LECTURE_META_FOUND    = "VIEWING-LECTURE-META-FOUND";
    public static final String CHAPTER_RESUME_FOUND  = "VIEWING-CHAPTER-RESUME-FOUND";

    // 진척도
    public static final String PROGRESS_SAVED        = "VIEWING-PROGRESS-SAVED";
    public static final String TOTAL_PROGRESS_FOUND  = "VIEWING-TOTAL-PROGRESS-FOUND";
    public static final String CHAPTER_PROGRESS_FOUND = "VIEWING-CHAPTER-PROGRESS-FOUND";

    // 내 강의
    public static final String MY_LECTURES_FOUND     = "VIEWING-MY-LECTURES-FOUND";

}
