package com.wanted.momocity.viewing.application.port;

import java.util.List;

/*
* comment.
*  특정 강의의 챕터별 진척도 조회 포트
* */

public interface LectureChapterProgressPort {
    List<ChapterProgressInfo> getLectureChapterProgress(Long userId, Long lectureId);
}
