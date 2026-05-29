package com.wanted.momocity.user.presentation.api.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/* comment.
    TeacherApplicationDetailResponse 정리
    1. 해당 클래스가 하는 일 : 강사 신청자 상세 조회의 HTTP 응답 본문 매핑 객체
    2. ListResponse.Item 과의 차이 :
        - Item (6개) : userId, nickname, name, email, category, appliedAt
        - Detail (9개) : 위 6개 + birth, profileImageUrl, proof
        - 상세 조회 = *모든 정보* 노출, 목록 = *간략 정보* 노출
    3.도메인 모델 TeacherApplication 과의 관계 :
        - TeacherApplication 의 9개 필드 = Detail 의 9개 필드 (완전 일치)
        - 변환이 *거의 자동* (Controller 가 직접 매핑)
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
