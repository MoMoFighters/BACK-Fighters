package com.wanted.momocity.report.domain.exception;

// ReportRepository 인터페이스에서 예외가 발생할 수 있기 떄문에, 예외 클래스 생성

public class ReportNotFoundException extends RuntimeException{

    public ReportNotFoundException(Long id) {
        super("신고를 찾을 수 없습니다!! id=" + id );
    }
}
