package com.wanted.momocity.lecture.application.command;


//  LectureThumbnailFile은 presentation 계층의 MultipartFile을
//  application 계층에서 사용할 수 있는 순수 데이터로 변환한 객체다.
public record LectureThumbnailFile(
        String originalFilename,
        String contentType,
        long size,
        byte[] content
) {
}
