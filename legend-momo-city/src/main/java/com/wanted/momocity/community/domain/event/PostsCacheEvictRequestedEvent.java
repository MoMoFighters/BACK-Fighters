package com.wanted.momocity.community.domain.event;

/*
 * comment.
 *  posts 목록 캐시 무효화 요청 이벤트
 *  - @CacheEvict를 트랜잭션 커밋 전에 즉시 실행하면, 아직 커밋 안 된 상태에서
 *    다른 요청이 목록을 재조회해 옛날 값을 다시 캐싱해버리는 문제가 생김
 *  - 그래서 캐시 무효화도 AFTER_COMMIT 시점으로 미룸
 * */

public record PostsCacheEvictRequestedEvent() {
}
