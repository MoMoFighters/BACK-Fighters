package com.wanted.momocity.viewing.application.port;

/*
* comment.
*  Application 이 AWS S3 가 핗요하다고 선언하는 인터페이스
*  실제 구현체는 infrastructure.adapter.S3PresignedUrlAdapter 가 담당
*  -
*  videoUrl(S3 key) 를 받아서 Presigned URL 반환
*  -> ViewingService 가 해당 포트를 호출해서 URL 발급
* */

public interface S3Port {

    // Presigned URL 발급
    // videoUrl : chapter 테이블의 video_url (S3 key)
    // 반환값 : 사용자가 바로 재생할 수 있는 임시 URL
    String generatePresignedUrl (String videoUrl);

}
