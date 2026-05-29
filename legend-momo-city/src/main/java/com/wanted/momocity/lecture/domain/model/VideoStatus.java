package com.wanted.momocity.lecture.domain.model;

// 챕터 동영상 처리 상태
public enum VideoStatus {
    // 챕터 생성 직후 또는 동영상 업로드 전 기본 상태
    UPLOADING,

    // 동영상 인코딩 중인 상태
    ENCODING,

    // 동영상 재생이 가능한 상태
    READY,

    // 업로드 또는 인코딩에 실패한 상태
    FAILED
}