package com.wanted.momocity.admin.domain.notice;

/* comment.
    공지 한 건의 도메인 모델.
    create() 로 생성, restore() 로 DB 복원한다.
 */


import java.time.LocalDateTime;

public class AdminNotice {

    private final Long id;
    private String title;
    private String content;
    private boolean isPinned;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    private AdminNotice(Long id, String title, String content,
                        boolean isPinned, LocalDateTime createdAt, LocalDateTime updatedAt) {

        // 같은 이름이고, this 가 없다면, Java 가 어느 title 인지 헷갈린다.
        this.updatedAt = updatedAt;
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.isPinned = isPinned;
    }

    // MS - 11 공지 생성 시 호출
    public static AdminNotice create(String title, String content, boolean isPinned) {
        return new AdminNotice(null, title, content, isPinned, LocalDateTime.now(), null);
    }

    // DB 에서 꺼낸 데이터를 도메인 객체로 복원
    public static AdminNotice restore(Long id, String title, String content, boolean isPinned
                                      , LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new AdminNotice(id, title, content, isPinned, createdAt, updatedAt);
    }

    // MS-17 공지 수정 : title, content 만 변경 가능하다. isPinned 불가능
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    // GETTER
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
