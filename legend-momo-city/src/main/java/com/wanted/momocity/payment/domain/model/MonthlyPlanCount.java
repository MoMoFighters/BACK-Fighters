package com.wanted.momocity.payment.domain.model;

public record MonthlyPlanCount(
        int month,
        Plan plan,
        long count
        /*comment
        *  month=1, plan=PLUS, count=3
        *  month=1, plan=PRO, count=1
        *  month=2, plan=BASIC, count=2
        *  이런 식으로 월,플랜,수 를 db에서 꺼내오자마자 일단 담아두고 그걸 월별로 묶는 게 MonthlyPlanDistributionResult*/
) {
}
