package com.wanted.momocity.lecture.application.command;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
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

    // 강사 또는 관리자가 강의 삭제할 때 사용하는 Command
    public record DeleteLectureCommand(
            Long userId,
            String role,
            Long lectureId
    ) {
        // Command 생성 시 필수 값
        public DeleteLectureCommand {
            // 만약 사용자 정보가 없다면
            if (userId == null) {
                throw new DomainRuleViolationException("사용자 정보가 없습니다.");
            }

            // 만약 사용자의 권한이 없다면
            if (role == null || role.isBlank()) {
                throw new DomainRuleViolationException("사용자 권한 정보가 없습니다.");
            }

            // 만약 강의 ID가 없다면
            if (lectureId == null) {
                throw new DomainRuleViolationException("강의 Id는 필수입니다.");
            }
        }
    }

    // 강사가 강의에 챕터를 추가할 때 사용하는 Command.
    public record CreateChapterCommand(
            Long teacherId,
            Long lectureId,
            String title,
            int orderNo,
            MultipartFile thumbnail
    ) {
        public CreateChapterCommand { // Compact Constructor로 Command 값 검증
            if (teacherId == null) { // 강사 ID가 없는지 확인
                throw new DomainRuleViolationException("강사 ID는 필수입니다."); // 없으면 예외 발생
            }

            if (lectureId == null) { // 강의 ID가 없는지 확인
                throw new DomainRuleViolationException("강의 ID는 필수입니다."); // 없으면 예외 발생
            }

            if (title == null || title.isBlank()) { // 챕터 제목이 없는지 확인
                throw new DomainRuleViolationException("챕터명은 필수입니다."); // 없으면 예외 발생
            }

            if (orderNo < 1) { // 챕터 순서가 1보다 작은지 확인
                throw new DomainRuleViolationException("챕터 순서는 1 이상이어야 합니다."); // 1 미만이면 예외 발생
            }

            if (thumbnail == null || thumbnail.isEmpty()) { // 챕터 썸네일 URL이 없는지 확인
                throw new DomainRuleViolationException("챕터 썸네일 URL은 필수입니다."); // 없으면 예외 발생
            }
        }
    }

    // 강사가 본인 강의의 챕터를 삭제할 때 사용하는 Command
    public record DeleteChapterCommand(
            Long teacherId,
            Long lectureId,
            Long chapterId
    ) {
        public DeleteChapterCommand {
            if (teacherId == null) {
                throw new DomainRuleViolationException("강사 ID는 필수입니다.");
            }

            if (lectureId == null) {
                throw new DomainRuleViolationException("강의 ID는 필수입니다.");
            }

            if (chapterId == null) {
                throw new DomainRuleViolationException("챕터 ID는 필수입니다.");
            }
        }
    }

    // 챕터 수정
    public record UpdateChapterCommand(
            Long teacherId,
            Long lectureId,
            Long chapterId,
            String title,
            int orderNo
    ) {
        public UpdateChapterCommand {
            if (teacherId == null) {
                throw new DomainRuleViolationException("강사 정보는 필수입니다.");
            }

            if (lectureId == null) {
                throw new DomainRuleViolationException("강의 ID는 필수입니다.");
            }

            if (chapterId == null) {
                throw new DomainRuleViolationException("챕터 ID는 필수입니다.");
            }


            if (title == null || title.isBlank()) {
                throw new DomainRuleViolationException("챕터 제목은 필수입니다.");
            }

            // 챕터 순서는 1 이상이어야 하므로 1보다 작으면 예외를 발생시킵니다.
            if (orderNo < 1) {
                // 공통 도메인 규칙 위반 예외로 잘못된 요청임을 표현합니다.
                throw new DomainRuleViolationException("챕터 순서는 1 이상이어야 합니다.");
            }
        }
    }

    // 영상 삭제
    public record DeleteChapterVideoCommand(
            Long teacherId,
            Long lectureId,
            Long chapterId
    ) {
        public DeleteChapterVideoCommand {
            if (teacherId == null) {
                throw new DomainRuleViolationException("강사 ID는 필수입니다.");
            }

            if (lectureId == null) {
                throw new DomainRuleViolationException("강의 ID는 필수입니다.");
            }

            if (chapterId == null) {
                throw new DomainRuleViolationException("챕터 ID는 필수입니다.");
            }
        }
    }

    /* comment
     * 강사가 강의를 등록할 때 사용하는 Command.
     * thumbnailUrl은 컨트롤러에서 S3 업로드 후 만들어진 URL을 넘긴다.
     */
    public record CreateLectureCommand(
            Long teacherId,
            String title,
            String description,
            MultipartFile thumbnail,
            LectureCategory category
    ) {
        public CreateLectureCommand { // Compact Constructor로 Command 생성 시점에 값 검증

            if (teacherId == null) { // 강사 ID가 없는지 확인

                throw new DomainRuleViolationException("강사 ID는 필수입니다."); // 강사 ID가 없으면 예외 발생

            } // if 종료

            if (title == null || title.isBlank()) { // 강의 제목이 비어 있는지 확인

                throw new DomainRuleViolationException("강의 제목은 필수입니다."); // 제목이 없으면 예외 발생

            } // if 종료

            if (description == null || description.isBlank()) { // 강의 설명이 비어 있는지 확인

                throw new DomainRuleViolationException("강의 설명은 필수입니다."); // 설명이 없으면 예외 발생

            } // if 종료

            if (thumbnail == null || thumbnail.isEmpty()) { // 썸네일 파일이 없는지 확인

                throw new DomainRuleViolationException("썸네일 이미지는 필수입니다."); // 썸네일이 없으면 예외 발생

            } // if 종료

            if (category == null) { // 강의 카테고리가 없는지 확인

                throw new DomainRuleViolationException("강의 카테고리는 필수입니다."); // 카테고리가 없으면 예외 발생

            } // if 종료

        } // Compact Constructor 종료

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