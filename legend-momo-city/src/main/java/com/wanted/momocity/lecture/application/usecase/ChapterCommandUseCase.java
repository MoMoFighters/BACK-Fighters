package com.wanted.momocity.lecture.application.usecase;

import com.wanted.momocity.lecture.application.command.ChangeChapterVideoStatusCommand;
import com.wanted.momocity.lecture.application.command.CreateChapterCommand;
import com.wanted.momocity.lecture.application.command.RegisterChapterVideoCommand;
import com.wanted.momocity.lecture.domain.model.LectureChapter;

// ChapterCommandUseCase는 챕터 상태를 변경하는 기능
public interface ChapterCommandUseCase {

    // 챕터를 등록
    LectureChapter createChapter(CreateChapterCommand command);

    // 동영상 등록
    LectureChapter registerChapterVideo(RegisterChapterVideoCommand command);

    // 챕터 동영상 처리 상태를 변경
    LectureChapter changeChapterVideoStatus(ChangeChapterVideoStatusCommand command);
}