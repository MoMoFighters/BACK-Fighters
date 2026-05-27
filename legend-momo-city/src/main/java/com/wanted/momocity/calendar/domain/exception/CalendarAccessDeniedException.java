package com.wanted.momocity.calendar.domain.exception;

public class CalendarAccessDeniedException extends RuntimeException {
    public CalendarAccessDeniedException(String message) {
        super(message);
    }
}
