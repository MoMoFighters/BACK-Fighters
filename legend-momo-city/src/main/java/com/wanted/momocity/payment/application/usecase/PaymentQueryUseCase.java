package com.wanted.momocity.payment.application.usecase;

import com.wanted.momocity.payment.domain.model.MonthlySalesResult;

import java.util.List;

public interface PaymentQueryUseCase {

    // 총 매출 조회
    long getTotalSales();

    // 월별 매출 조회
    List<MonthlySalesResult> getMonthlySales(int year);
}
