package com.wanted.momocity.friend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

//도메인 애그리거트
@AllArgsConstructor
@Getter
public class Friend {
    private final Long id;
    private final Long fromUserId;
    private final Long toUserId;
    private final FriendStatus status;

    //순수 도메인 모델 안에서 비즈니스 생성 규칙을 정의
    public static Friend createRequest(Long fromUserId, Long toUserId) {
        return new Friend(null, fromUserId, toUserId, FriendStatus.SENT);
    }

    //수강신청 자동 친구 맺기용 (상태: FRIEND)
    public static Friend createTeacherStudentRelation(Long studentId, Long teacherId) {
        return new Friend(null, studentId, teacherId, FriendStatus.FRIEND);
    }
}
