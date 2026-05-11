package com.aifinance.dashboard.dto;

import java.math.BigDecimal;

import com.aifinance.finance.dto.FinancialSnapshot;

public record TrendPointResponse(
        Long periodId,
        String periodLabel,
        BigDecimal revenue,
        BigDecimal cost,
        BigDecimal netProfit,
        BigDecimal operatingCashFlow,
        BigDecimal netMargin,
        String unit) {

    public static TrendPointResponse from(FinancialSnapshot snapshot) {
        return new TrendPointResponse(
                snapshot.periodId(),
                snapshot.periodLabel(),
                snapshot.revenue(),
                snapshot.cost(),
                snapshot.netProfit(),
                snapshot.operatingCashFlow(),
                snapshot.netMargin(),
                snapshot.unit());
    }
}
