package com.wanted.momocity.friend.domain.event;

import java.time.LocalDateTime;

public record TeacherStudentAutoFriendPublishedEvent(
        Long fromUserId, //학생(userId: 알림 받는 사람(원래 상대방이어야 하는데 강사는 친구 기능이 없어서 알림 받을 필요 없음)
        Long toUserId, //강사(refId: 학생 쪽에서 넘어가게 할 상대방 아이디)
        String teacherName, //강사 닉네임이 없을 수도 있을 상황 대비
        String teacherNickname //notification 테이블에 추가할 강사 닉네임
) {

}
