package com.wanted.momocity.lecture.domain.exception;

/* comment
 * 이미 동영상이 등록된 챕터에 다시 동영상을 등록하려고 할 때 사용하는 예외
 * 하나의 챕터에는 하나의 동영상만 연결할 수 있다는 정책을 표현
 */
public class ChapterVideoAlreadyExistsException extends RuntimeException {

    public ChapterVideoAlreadyExistsException(String message) {
        super(message);
    }
}