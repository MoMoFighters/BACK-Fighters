package com.wanted.momocity.lecture.application.command;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.domain.model.VideoStatus;
import org.springframework.web.multipart.MultipartFile;

// Lecture 도메인에서 사용하는 Command record 모음집
public final class LectureCommand {

    // 유틸/컨테이너 클래스라서 인스턴스 생성을 막는다.
    private LectureCommand() {
    }

    /* comment
     * 관리자가 강의 상태를 변경할 때 사용하는 Command.
     *
     * 예:
     * - 강의 승인: ACTIVE
     * - 강의 거절: HOLD
     */
    public record AdminChangeLectureStatusCommand(
            Long adminId,
            Long lectureId,
            LectureStatus lectureStatus
    ) {
        public AdminChangeLectureStatusCommand {
            validateAdminId(adminId);
            validateLectureId(lectureId);
            validateLectureStatus(lectureStatus);
        }

        private static void validateAdminId(Long adminId) {
            if (adminId == null) {
                throw new DomainRuleViolationException("관리자 정보는 필수입니다.");
            }
        }

        private static void validateLectureId(Long lectureId) {
            if (lectureId == null) {
                throw new DomainRuleViolationException("강의 ID는 필수입니다.");
            }
        }

        private static void validateLectureStatus(LectureStatus lectureStatus) {
            if (lectureStatus == null) {
                throw new DomainRuleViolationException("강의 상태는 필수입니다.");
            }

            if (lectureStatus != LectureStatus.ACTIVE
                    && lectureStatus != LectureStatus.HOLD) {
                throw new DomainRuleViolationException("관리자는 강의를 승인 또는 거절 상태로만 변경할 수 있습니다.");
            }
        }
    }

    // 강사가 챕터 동영상 처리 상태를 변경할 때 사용하는 Command.
    public record ChangeChapterVideoStatusCommand(
            Long teacherId,
            Long lectureId,
            Long chapterId,
            VideoStatus videoStatus
    ) {
    }

    /* comment
     * 강사가 본인 강의 상태를 변경할 때 사용하는 Command.
     * 보통 강의 등록 후 WAITING 상태로 변경할 때 사용된다.
     */
    public record ChangeLectureStatusCommand(
            Long teacherId,
            Long lectureId,
            LectureStatus lectureStatus
    ) {
    }

    // 강사가 강의에 챕터를 추가할 때 사용하는 Command.
    public record CreateChapterCommand(
            Long teacherId,
            Long lectureId,
            String title,
            int orderNo
    ) {
    }

    /* comment
     * 강사가 강의를 등록할 때 사용하는 Command.
     * thumbnailUrl은 컨트롤러에서 S3 업로드 후 만들어진 URL을 넘긴다.
     */
    public record CreateLectureCommand(
            Long teacherId,
            String title,
            String description,
            String thumbnailUrl,
            LectureCategory category
    ) {
    }

    // 강사가 챕터에 동영상 파일을 등록할 때 사용하는 Command.
    public record RegisterChapterVideoCommand(
            Long teacherId,
            Long lectureId,
            Long chapterId,
            MultipartFile video,
            Integer durationSec
    ) {
    }
}