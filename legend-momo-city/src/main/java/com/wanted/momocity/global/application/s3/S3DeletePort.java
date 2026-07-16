package com.wanted.momocity.global.application.s3;

// S3 구현체를 직접 알지 않고 파일 삭제를 요청하기 위한 계약
public interface S3DeletePort {

    // 전달받은 S3 객체 key에 해당하는 파일 하나를 삭제 (하나만 삭제할때)
    void delete(String key);

    // 전달받은 prefix로 시작하는 모든 S3 객체를 삭제 (강의 전체)
    void deleteByPrefix(String prefix);
}
