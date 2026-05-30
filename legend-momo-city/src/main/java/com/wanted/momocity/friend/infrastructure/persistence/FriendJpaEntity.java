package com.wanted.momocity.friend.infrastructure.persistence;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "friend")
@NoArgsConstructor
@Getter
public class FriendJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private UserWithFMJpaEntity fromUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id")
    private UserWithFMJpaEntity toUserId;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //친구 요청
    public static FriendJpaEntity createRequest(UserWithFMJpaEntity loginUser, UserWithFMJpaEntity targetUser) {
        FriendJpaEntity request = new FriendJpaEntity();
        request.fromUserId = loginUser;
        request.toUserId = targetUser;
        request.status = "SENT"; //친구 요청 기본값
        request.createdAt = LocalDateTime.now();
        request.updatedAt = LocalDateTime.now();
        return request;
    }

    //수강신청 완료 시 강사-친구 자동 행 추가
    public static FriendJpaEntity createTeacherStudentRelation(UserWithFMJpaEntity studentProxy, UserWithFMJpaEntity teacherProxy, String status) {
        FriendJpaEntity request = new FriendJpaEntity();
        request.fromUserId = studentProxy;
        request.toUserId = teacherProxy;
        request.status = status;
        request.createdAt = LocalDateTime.now();
        request.updatedAt = LocalDateTime.now();
        return request;
    }

    //친구 요청 수락 시 상태 변경(SENT -> FRIEND)
    public void changeStatus(String newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    //친구 차단 로직 변경(로그인 유저를 from으로)
    public void swapDirectionAndBlock() {
        //from과 to 바꾸기
        UserWithFMJpaEntity temp = this.fromUserId;
        this.fromUserId = this.toUserId;
        this.toUserId = temp;

        //상태를 BLOCK으로 변경
        this.status = "BLOCK";
    }
}
