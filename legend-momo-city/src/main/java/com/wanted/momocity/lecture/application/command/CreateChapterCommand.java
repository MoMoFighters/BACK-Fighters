package com.wanted.momocity.lecture.application.command;

// CreateChapterCommand는 챕터 등록 유스케이스에 필요한 command
public record CreateChapterCommand(
        // 로그인한 강사의 email
        // Authorization 토큰에서 꺼낸 값
        Long teacherId,

        // 챕터를 등록할 강의 ID
        Long lectureId,

        // 챕터 제목
        String title,

        // 강의 안에서 챕터가 노출될 순서
        int orderNo
) {
}