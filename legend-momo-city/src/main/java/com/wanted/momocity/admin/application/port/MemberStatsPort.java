package com.wanted.momocity.admin.application.port;

import java.time.LocalDate;

/* comment.
    MemberStatsPort 정리
    대시보드에서 회원 관련 통계 수치를 받아오기 위해 정의하는 포트
    */
public interface MemberStatsPort {


    // 전체 회원 수 (상태 구분 없이 탈퇴 제외 전체)
    long countAll();

    // 현재 활성(ACTIVE) 회원 수
    long countActive();

    // 특정 날짜 이전 시점의 활성 회원 수 (증감률 계산용)
    // EX) 전월 말 시점의 회원 수
    long countActiveBefore(LocalDate date);

    // 승인 대기 중인 강사 수 (수영님 담당 어댑터에서 구현)
    long countPending();

}