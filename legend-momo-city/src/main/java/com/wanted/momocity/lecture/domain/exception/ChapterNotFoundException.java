package com.wanted.momocity.lecture.domain.exception;

/* comment
 * 요청한 챕터를 찾을 수 없을 때 사용하는 예외
 * 동영상 등록, 챕터 수정/삭제에서도 사용 가능
 */
public class ChapterNotFoundException extends RuntimeException {

    public ChapterNotFoundException(String message) {
        super(message);
    }
}