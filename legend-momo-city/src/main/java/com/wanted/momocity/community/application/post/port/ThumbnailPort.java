package com.wanted.momocity.community.application.post.port;

/*
 * comment.
 *  썸네일 생성이 필요하다고 선언하는 포트
 *  - 원본 이미지 URL 을 받아서, 리사이징 후 업로드된 새 URL 반환
 *  - 실제 구현체는 infrastructure.adapter.ThumbnailAdapter 가 담당
 */

public interface ThumbnailPort {

    // 원본 이미지 URL -> 리사이징 -> S3 재업로드 -> 새 URL 반환
    String generateThumbnail(String originalImageUrl);

    // 더 이상 참조되지 않는(orphan) 썸네일 파일을 S3 에서 삭제
    void deleteThumbnail(String thumbnailUrl);

}
