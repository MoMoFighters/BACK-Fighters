package com.wanted.momocity.admin.application.port;

// 월별 집계 공통 자료구조 — 대시보드 월별 운영 추이 그래프용
public record MonthlyCount(int month, long count) {}
