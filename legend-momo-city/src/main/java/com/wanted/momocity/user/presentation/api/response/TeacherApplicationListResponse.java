package com.wanted.momocity.user.presentation.api.response;

import java.time.LocalDateTime;
import java.util.List;

/* comment.
    TeacherApplicationListResponse 정리
    1. 해당 클래스가 하는 일 : 강사 신청자 목록 조회의 HTTP 응답 본문 매핑 객체
    2. 응답 구조 :
        ApiResponse<TeacherApplicationListResponse> {
        timestamp, status, code, message,
        data: {
        applications: [Item, Item, ...]
        page, size, totalElements, totalPages
                }
            }
    3. 5개 외부 필드 :
        - applications : 신청자 목록 (Item record 의 List)
        - page : 현재 페이지 (1-base)
        - size : 페이지 크기
        - totalElements : 총 신청자 수
        - totalPages : 총 페이지 수
    4. nested Item record (6개 필드) :
        - userId, nickname, name, email, category, appliedAt
        - 노션 MS-3 의 applications 배열 항목 구조
    5. *상세(Detail) 와 다른 점* :
        - 목록 = 6개 필드 (간략 정보)
        - 상세 = 9개 필드 (birth, profileImageUrl, proof 추가)
    6. UseCase 의 ListResult 와 차이 :
        - ListResult : 응용 계층 반환 (도메인 모델 TeacherApplication 사용)
        - ListResponse : 표현 계층 반환 (Item record 로 변환)
        - Controller 가 ListResult → ListResponse 변환
 */

public record TeacherApplicationListResponse(
        List<Item> applications,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record Item(
            Long userId,
            String nickname,
            String name,
            String email,
            String category,
            LocalDateTime appliedAt
    ) {
    }
}
