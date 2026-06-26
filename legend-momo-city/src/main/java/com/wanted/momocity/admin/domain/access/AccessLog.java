package com.wanted.momocity.admin.domain.access;

import java.time.LocalDateTime;

/* comment.
    접근 로그 한 건의 도메인 모델.
    읽기 전용 - restore() 로만 복원
 */

public class AccessLog {

    // final 로 바뀌지 않게 선언
    private final Long id;
    private final Long userId;        // null 이면 비로그인 → 조회에서 제외됨
    private final String ip;
    private final AccessLogAction action;
    private final LocalDateTime createdAt;

    private AccessLog(Long id, Long userId, String ip,
                      AccessLogAction action, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.ip = ip;
        this.action = action;
        this.createdAt = createdAt;
    }

    // DB 에서 꺼낸 데이터를 도메인 객체로 복원할 때 사용
    public static AccessLog restore(Long id, Long userId, String ip,
                                    AccessLogAction action, LocalDateTime createdAt) {
        return new AccessLog(id, userId, ip, action, createdAt);
    }

    // setter 없이 getter 만
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getIp() { return ip; }
    public AccessLogAction getAction() { return action; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}