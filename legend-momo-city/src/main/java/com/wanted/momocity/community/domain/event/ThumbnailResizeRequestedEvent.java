package com.wanted.momocity.community.domain.event;

/*
 * comment.
 *  ThumbnailResizeRequestedEvent
 *  - 게시글 콘텐츠 저장(트랜잭션) 완료 후 썸네일 리사이징을 요청하는 이벤트
 *  - @TransactionalEventListener(AFTER_COMMIT) 에서 수신하여
 *    메인 트랜잭션이 실제로 커밋된 뒤에만 비동기 리사이징이 시작되도록 보장
 */

public record ThumbnailResizeRequestedEvent(
        Long postId,
        String originalThumbnailUrl
) {
}
