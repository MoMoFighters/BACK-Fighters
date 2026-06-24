package com.wanted.momocity.report.domain.model;

/* comment.
    ReportTargetType 정리
    신고 대상 종류 분류 (PAGE 타입은 targetId null 허용)
 */
public enum ReportTargetType {
    POST,
    COMMENT,
    LECTURE,
    CHAPTER,
    REVIEW,
    CHAT,
    PAGE
}