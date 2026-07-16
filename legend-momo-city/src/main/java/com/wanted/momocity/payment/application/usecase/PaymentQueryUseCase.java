package com.wanted.momocity.payment.application.usecase;

import com.wanted.momocity.payment.domain.model.*;

import java.util.Arrays;
import java.util.List;

public interface PaymentQueryUseCase {

    // 총 매출 조회
    long getTotalSales();

    // 월별 매출 조회
    List<MonthlySalesResult> getMonthlySales();

    // 개인의 결제 내역 조회
    PersonalPaymentListResult getPersonalPaymentList(Long userId, Status status, int page, int size);

    // 관리자의 서비스 결제 내역 조회
    AdminPaymentListResult getAdminPaymentList(Status status, int page, int size);

    // 월별 + 플랜별 분포
    List<MonthlyPlanDistributionResult> getMonthlyPlanDistribution();
}
