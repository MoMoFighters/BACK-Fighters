package com.wanted.momocity.admin.application.port;

import java.util.List;

// 대시보드 월별 운영 추이 그래프 — 게시글 수 집계 포트 (커뮤니티 BC 어댑터에서 구현)
public interface PostStatsPort {

    List<MonthlyCount> countPostByMonth(int year);
}
