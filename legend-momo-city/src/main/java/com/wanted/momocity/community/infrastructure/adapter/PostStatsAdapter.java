package com.wanted.momocity.community.infrastructure.adapter;

import com.wanted.momocity.community.infrastructure.persistence.PostJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * comment.
 *  월별 게시글 통계 어댑터
 *  -> 대시보드 월별 운영 추이 그래프용
 *  -> MonthlyCount 는 팀원 구현 대기 중 (완료 시 주석 해제)
 * */

@Component
@RequiredArgsConstructor
public class PostStatsAdapter /* implements PostStatsPort */ {

    private final PostJpaRepository postJpaRepository;

    // 해당 연도 월별 게시글 수 반환 (1월~12월)
    // 데이터 없는 달은 0으로 채움
    // public List<MonthlyCount> countPostByMonth(int year) {
    //     List<Object[]> rows = postJpaRepository.countPostByMonth(year);
    //
    //     // 1~12월 기본값 0으로 초기화
    //     Map<Integer, Long> monthMap = new HashMap<>();
    //     for (int i = 1; i <= 12; i++) monthMap.put(i, 0L);
    //
    //     // 쿼리 결과로 덮어쓰기
    //     for (Object[] row : rows) {
    //         int month = ((Number) row[0]).intValue();
    //         long count = ((Number) row[1]).longValue();
    //         monthMap.put(month, count);
    //     }
    //
    //     // MonthlyCount 리스트로 변환
    //     return IntStream.rangeClosed(1, 12)
    //             .mapToObj(m -> new MonthlyCount(m, monthMap.get(m)))
    //             .collect(Collectors.toList());
    // }
}