package com.wanted.momocity.lecture.application.usecase;

import com.wanted.momocity.lecture.application.command.LectureCommand.DeleteChapterVideoCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.DeleteLectureCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.DeleteChapterCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.ChangeLectureStatusCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.CreateLectureCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.CreateChapterCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.RegisterChapterVideoCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.AdminChangeLectureStatusCommand;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.presentation.api.response.AdminLectureResponse.AdminChangeLectureStatusResponse;

// LectureCommandUseCase는 강의 상태를 변경하는 인터페이스
public final class LectureCommandUseCases {

    private LectureCommandUseCases() {}

    public interface LectureCommandUseCase {
        // 강의를 등록
        LectureAggregate createLecture(CreateLectureCommand command);

        // 강사가 본인 강의를 WAITING 상태로 변경
        LectureAggregate changeLectureStatus(ChangeLectureStatusCommand command);

        // 강사 또는 관리자가 강의를 DELETED 상태로 변경
        LectureAggregate deleteLecture(DeleteLectureCommand command);


    }

    // ChapterCommandUseCase는 챕터 상태를 변경하는 기능
    public interface ChapterCommandUseCase {

        // 챕터를 등록
        LectureChapter createChapter(CreateChapterCommand command);

        // 동영상 등록
        LectureChapter registerChapterVideo(RegisterChapterVideoCommand command);

        void deleteChapter(DeleteChapterCommand command);

        // 강사가 본인 강의의 챕터 동영상을 삭제합니다.
// 영상은 챕터의 필수 요소이므로, 실제 구현에서는 영상이 속한 챕터까지 함께 삭제합니다.
        void deleteChapterVideo(DeleteChapterVideoCommand command);
    }

    // 관리자 강의 명령 기능을 정의하는 UseCase
    public interface AdminLectureCommandUseCase {

        // 관리자가 강의 상태를 변경
        AdminChangeLectureStatusResponse changeLectureStatus(AdminChangeLectureStatusCommand command);
    }



}
