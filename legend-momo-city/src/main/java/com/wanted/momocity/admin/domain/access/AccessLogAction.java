package com.wanted.momocity.admin.domain.access;

/* comment.
    access_log.action 컬럼 값.
    DB 에 String 으로 저장된다.
 */

public enum AccessLogAction {
    LOGIN, LOGOUT, FORBIDDEN
}
