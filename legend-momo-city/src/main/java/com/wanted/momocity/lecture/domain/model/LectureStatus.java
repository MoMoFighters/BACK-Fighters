package com.wanted.momocity.lecture.domain.model;

// 강의 상태를 표현하는 도메인 enum
// 신청을 하게 되면 바로 Active가 아닌 Waiting 상태
public enum LectureStatus {
    WAITING,
    ACTIVE,
    HOLD,
    DELETED
}
