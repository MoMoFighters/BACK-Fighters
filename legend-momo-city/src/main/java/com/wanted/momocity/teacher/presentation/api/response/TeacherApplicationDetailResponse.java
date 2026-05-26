package com.wanted.momocity.teacher.presentation.api.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * MS-4: 강사 신청자 상세 응답.
 *
 * 노션 명세의 9개 필드(userId, nickname, name, email, birth, profileImageUrl, category, proof, appliedAt).
 */
public record TeacherApplicationDetailResponse(
        Long userId,
        String nickname,
        String name,
        String email,
        LocalDate birth,
        String profileImageUrl,
        String category,
        String proof,
        LocalDateTime appliedAt
) {
}
