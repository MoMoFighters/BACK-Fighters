package com.wanted.momocity.lecture.application.command;

import org.springframework.web.multipart.MultipartFile;

// 챕터 동영상 등록은 유스케이스에 필요한 값을 담은 command
public record RegisterChapterVideoCommand(
        // Authorization 토큰에서 꺼낸 로그인 강사 Email
        Long teacherId,
        // 강의 Id
        Long lectureId,
        // 챕터 Id
        Long chapterId,
        // form-data로 받은 실제 동영상 파일
        MultipartFile video,
        // 동영상 재생 시간
        Integer durationSec

) {
}
